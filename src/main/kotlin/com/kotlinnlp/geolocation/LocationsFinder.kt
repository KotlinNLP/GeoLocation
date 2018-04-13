/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

import com.kotlinnlp.geolocation.dictionary.LocationsDictionary
import com.kotlinnlp.geolocation.structures.CandidateEntity
import com.kotlinnlp.geolocation.structures.ExtendedLocation
import com.kotlinnlp.geolocation.structures.Location

/**
 * The LocationsFinder searches for all the valid locations among a set of candidate entities found in a text, already
 * scored respect to semantic properties.
 *
 * A LocationsFinder is a standalone class, that must be instantiated for each input text and it makes available the
 * [bestLocations] property containing the best locations found.
 *
 * @param dictionary a dictionary containing all the locations that can be recognized
 * @param text the input text
 * @param candidateEntities a set of entities found in the [text], candidate as locations
 * @param coordinateEntitiesGroups a list of groups of entities that are coordinate in the text
 */
internal class LocationsFinder(
  private val dictionary: LocationsDictionary,
  private val text: String,
  candidateEntities: Set<CandidateEntity>,
  coordinateEntitiesGroups: List<Set<String>>
) {

  /**
   * The list of the best extended locations found, sorted by descending importance.
   */
  val bestLocations: List<ExtendedLocation>

  /**
   * The map of entities to the groups of coordinate entities in which they are involved.
   */
  private val coordinateEntitiesMap: Map<String, List<Set<String>>>

  /**
   * The map of current candidate locations associated by id.
   */
  private val candidateLocationsById: Map<String, ExtendedLocation>

  /**
   * A set of adding candidates (taken from the parents labels) that are mentioned in the text.
   * All names are lower case.
   */
  private val addingEntities: Set<String>

  /**
   * Calculate scores and find best locations.
   */
  init {

    // Attention: the order of the operations is very important!

    this.candidateLocationsById = this.buildCandidateLocationsById(candidateEntities)
    this.coordinateEntitiesMap = this.buildCoordinateEntitiesMap(coordinateEntitiesGroups)
    // TODO: add this.solveAmbiguities()
    this.addingEntities = this.buildAddingEntities()

    this.setScores()

    this.bestLocations = this.buildBestLocations()
  }

  /**
   * Build the candidate locations associated by ID.
   *
   * @param candidateEntities a set of candidate entities
   *
   * @return a map of extended locations associated by id
   */
  private fun buildCandidateLocationsById(candidateEntities: Set<CandidateEntity>): Map<String, ExtendedLocation> {

    val entitiesNamesByLocId = mutableMapOf<String, MutableSet<CandidateEntity>>()

    val candidateLocations: List<Location> = candidateEntities.flatMap { entity ->

      val locations: List<Location>? = this.dictionary.getByLabel(entity.normName)

      locations?.forEach { entitiesNamesByLocId.getOrPut(it.id) { mutableSetOf() }.add(entity) }

      locations ?: listOf()
    }

    return candidateLocations.distinctBy { it.id }.associate {
      it.id to this.buildExtendedLocation(location = it, entities = entitiesNamesByLocId.getValue(it.id))
    }
  }

  /**
   * Build an [ExtendedLocation] from a [Location].
   *
   * @param location a location
   * @param entities the set of entities from which the [location] has been originated
   *
   * @return a new extended location
   */
  private fun buildExtendedLocation(location: Location, entities: Set<CandidateEntity>): ExtendedLocation {

    val score: Double = entities.sumByDouble { it.score } / entities.size
    val parents: List<Location> = location.parentsIds.map { this.dictionary.getValue(it) }

    return ExtendedLocation(
      location = location,
      parents = parents,
      candidateEntities = entities.toList(),
      initScore = score
    )
  }

  /**
   * Build the coordinate entities map.
   *
   * @param coordinateEntitiesGroups a list of groups of entities that are coordinate in the text
   *
   * @return a map of entities names to groups of coordinate entities in which they are involved
   */
  private fun buildCoordinateEntitiesMap(coordinateEntitiesGroups: List<Set<String>>): Map<String, List<Set<String>>> {

    val coordinateEntitiesMap = mutableMapOf<String, MutableList<Set<String>>>()

    coordinateEntitiesGroups.forEach { entitiesGroup ->
      entitiesGroup.forEach { entity ->
        coordinateEntitiesMap.getOrPut(entity) { mutableListOf() }.add(entitiesGroup)
      }
    }

    return coordinateEntitiesMap
  }

  /**
   * Build the adding candidates found in the input text, starting from adding entities taken from the parents labels.
   *
   * @return a set of adding candidate entities
   */
  private fun buildAddingEntities(): Set<String> {

    // A set of parent location ids that are not in the candidateLocationsById.
    val addingLocationIds: Set<String> =
      this.candidateLocationsById.let { it.values.flatMap { it.location.parentsIds }.subtract(it.keys) }

    // A set of adding entities that could be mentioned in the text.
    val addingEntities: Set<String> = addingLocationIds.flatMap { id -> this.dictionary.getValue(id).labels }.toSet()

    return this.searchInText(entities = addingEntities) // TODO: differentiate between title and body?
  }

  /**
   * Search entities in the input text that match a set of given entities.
   *
   * @param entities a set of entities
   *
   * @return a set of entities lower names found in the input text
   */
  private fun searchInText(entities: Set<String>): Set<String> = entities.filter {
    this.text.toLowerCase().findAnyOf(listOf(it)) != null
  }.toSet()

  /**
   * Set the current candidates scores.
   */
  private fun setScores() {

    this.candidateLocationsById.values.forEach {
      this.setScoreByParents(it)
      this.setScoreByBrothers(it)
    }
  }

  /**
   * Set the score of a given [location] by its parents.
   *
   * @param location a candidate location
   */
  private fun setScoreByParents(location: ExtendedLocation) {

    location.parents.forEach { parent ->

      this.candidateLocationsById[parent.id]?.let { location.boostByParent(it) } ?:
        location.boostByParentLabels(parent = parent, candidateNames = this.addingEntities, rateFactor = 0.333)
    }
  }

  /**
   * Set the score of a given [location] by its brothers (locations under the same hierarchic upper level).
   *
   * @param location a candidate location
   */
  private fun setScoreByBrothers(location: ExtendedLocation) {

    this.candidateLocationsById.values
      .filter { it.isBrother(location) }
      .forEach { location.boostByBrother(brother = it, coordinateEntitiesMap = this.coordinateEntitiesMap) }
  }

  /**
   * Build the best locations.
   *
   * @return a list of the best extended locations found, sorted by descending importance
   */
  private fun buildBestLocations(): List<ExtendedLocation> {

    val bestLocations: List<ExtendedLocation> = this.findBestLocations()

    ConfidenceHelper(bestLocations).setConfidences()

    this.normalizeScores(bestLocations)

    return bestLocations.sortedWith(
      Comparator({ locA, locB -> if (locA.isMoreProbableThan(locB)) -1 else 1 }) // descending order
    )
  }

  /**
   * Find the locations that best represent each input candidate entity.
   *
   * @return a list of the best extended locations found
   */
  private fun findBestLocations(): List<ExtendedLocation> {

    val bestLocationsMap: MutableMap<String, ExtendedLocation> = mutableMapOf()

    this.candidateLocationsById.values.forEach { location ->
      location.candidateEntities.forEach { entity ->
        bestLocationsMap[entity.name].let { bestLoc ->
          if (bestLoc == null || location.isMoreProbableThan(bestLoc)) bestLocationsMap[entity.name] = location
        }
      }
    }

    this.setLocationsEntities(bestLocationsMap)

    return bestLocationsMap.values.toList()
  }

  /**
   * Set the 'entities' property of each best location.
   *
   * @param bestLocationsMap the map of best extended locations associated by candidate
   */
  private fun setLocationsEntities(bestLocationsMap: Map<String, ExtendedLocation>) {

    val candidatesByLocationId: Map<String, List<String>> =
      bestLocationsMap.keys.groupBy { bestLocationsMap.getValue(it).location.id }

    bestLocationsMap.values.forEach { it.entities = candidatesByLocationId.getValue(it.location.id) }
  }

  /**
   * Normalize the scores of the given locations.
   *
   * @param locations a list of locations
   */
  private fun normalizeScores(locations: List<ExtendedLocation>) {

    val scoresSum: Double = locations.sumByDouble { it.score }

    locations.forEach { it.score.div(scoresSum) }
  }
}