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
class LocationsFinder(
  private val dictionary: LocationsDictionary,
  private val text: String,
  candidateEntities: Set<CandidateEntity>,
  coordinateEntitiesGroups: List<Set<String>>
) {

  /**
   * The candidate entities names mapped to best locations found (or null if no one has been found).
   */
  val bestLocations: Map<String, ExtendedLocation?>

  /**
   * The map of entities to the groups of coordinate entities in which they are involved.
   */
  private val coordinateEntitiesMap: Map<String, List<Set<String>>>

  /**
   * The map of current candidate locations associated by id.
   */
  private val candidateLocationsMap: Map<String, ExtendedLocation>

  /**
   * A set of adding candidates (taken from the parents labels) that are mentioned in the text.
   * All names are lower case.
   */
  private val addingEntities: Set<String>

  /**
   * Calculate scores and find best locations.
   */
  init {
    this.candidateLocationsMap = this.buildCandidateLocationsMap(candidateEntities)
    this.coordinateEntitiesMap = this.buildCoordinateEntitiesMap(coordinateEntitiesGroups)

    // TODO: add this.solveAmbiguities()

    this.addingEntities = this.buildAddingEntities()
    this.setScores()

    this.bestLocations = this.findBestLocations()
  }

  /**
   * Build the candidate locations map.
   *
   * @param candidateEntities a set of candidate entities
   *
   * @return a map of entities names to extended locations
   */
  private fun buildCandidateLocationsMap(candidateEntities: Set<CandidateEntity>): Map<String, ExtendedLocation> {

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

    return ExtendedLocation(location = location, parents = parents, entities = entities.toList(), initScore = score)
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

    // A set of parent location ids that are not in the candidateLocationsMap.
    val addingLocationIds: Set<String> =
      this.candidateLocationsMap.let { it.values.flatMap { it.location.parentsIds }.subtract(it.keys) }

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

    this.candidateLocationsMap.values.forEach {
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

      this.candidateLocationsMap[parent.id]?.let { location.boostByParent(it) } ?:
        location.boostByParentLabels(parent = parent, candidateNames = this.addingEntities, rateFactor = 0.333)
    }
  }

  /**
   * Set the score of a given [location] by its brothers (locations under the same hierarchic upper level).
   *
   * @param location a candidate location
   */
  private fun setScoreByBrothers(location: ExtendedLocation) {

    this.candidateLocationsMap.values
      .filter { it.isBrother(location) }
      .forEach { location.boostByBrother(brother = it, coordinateEntitiesMap = this.coordinateEntitiesMap) }
  }

  /**
   * Find the location that best represent each input candidate entity.
   *
   * @return a map that associates an extended location (or null if no one has been found) to each candidate
   */
  private fun findBestLocations(): Map<String, ExtendedLocation?> {
    TODO()
  }
}