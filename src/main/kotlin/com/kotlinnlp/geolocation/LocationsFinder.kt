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
 * @param dictionary a dictionary containing all the locations that can be recognized
 */
class LocationsFinder(private val dictionary: LocationsDictionary) {

  /**
   * The input text.
   */
  private lateinit var inputText: String

  /**
   * The map of entities to the groups of coordinate entities in which they are involved.
   */
  private lateinit var coordinateEntitiesMap: Map<String, List<Set<String>>>

  /**
   * The map of current candidate locations associated by id.
   */
  private lateinit var candidateLocationsMap: Map<String, ExtendedLocation>

  /**
   * A set of adding candidates (taken from the parents labels) that are mentioned in the text.
   * All names are lower case.
   */
  private lateinit var addingCandidates: Set<String>

  /**
   * Get the locations that best represent the candidate entities given in a set.
   *
   * @param text the input text
   * @param candidateEntities a set of entities found in a text, candidate as locations
   * @param coordinateEntitiesGroups a list of groups of entities that are coordinate in the text
   *
   * @return a map that associates a location (or null if no one has been found) to each candidate
   */
  fun getLocations(text: String,
                   candidateEntities: Set<CandidateEntity>,
                   coordinateEntitiesGroups: List<Set<String>>): Map<String, Location?> {

    this.inputText = text
    this.setCandidateLocations(candidateEntities)
    this.setCoordinateEntitiesMap(coordinateEntitiesGroups)

    // TODO: add this.solveAmbiguities()

    this.setAddingCandidates()
    this.setScores()

    TODO()
  }

  /**
   * Set the current candidate locations.
   *
   * @param candidateEntities a set of candidate entities
   */
  private fun setCandidateLocations(candidateEntities: Set<CandidateEntity>) {

    val entitiesNamesByLocId = mutableMapOf<String, MutableSet<CandidateEntity>>()

    val candidateLocations: List<Location> = candidateEntities.flatMap { entity ->

      val locations: List<Location>? = this.dictionary.getByLabel(entity.normName)

      locations?.forEach { entitiesNamesByLocId.getOrPut(it.id) { mutableSetOf() }.add(entity) }

      locations ?: listOf()
    }

    this.candidateLocationsMap = candidateLocations.distinctBy { it.id }.associate {
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
   * Set the coordinate entities map.
   *
   * @param coordinateEntitiesGroups a list of groups of entities that are coordinate in the text
   */
  private fun setCoordinateEntitiesMap(coordinateEntitiesGroups: List<Set<String>>) {

    val coordinateEntitiesMap = mutableMapOf<String, MutableList<Set<String>>>()

    coordinateEntitiesGroups.forEach { entitiesGroup ->
      entitiesGroup.forEach { entity ->
        coordinateEntitiesMap.getOrPut(entity) { mutableListOf() }.add(entitiesGroup)
      }
    }

    this.coordinateEntitiesMap = coordinateEntitiesMap
  }

  /**
   * Set the adding candidates found in the input text, starting from adding entities taken from the parents labels.
   */
  private fun setAddingCandidates() {

    // A set of parent location ids that are not in the candidateLocationsMap.
    val addingLocationIds: Set<String> =
      this.candidateLocationsMap.let { it.values.flatMap { it.location.parentsIds }.subtract(it.keys) }

    // A set of adding entities that could be mentioned in the text.
    val addingEntities: Set<String> = addingLocationIds.flatMap { id -> this.dictionary.getValue(id).labels }.toSet()

    this.addingCandidates = this.searchInText(entities = addingEntities) // TODO: differentiate between title and body?
  }

  /**
   * Search entities in the input text that match a set of given entities.
   *
   * @param entities a set of entities
   *
   * @return a set of candidates lower names found in the input text
   */
  private fun searchInText(entities: Set<String>): Set<String> = entities.filter {
    this.inputText.toLowerCase().findAnyOf(listOf(it)) != null
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
        location.boostByParentLabels(parent = parent, candidateNames = this.addingCandidates, rateFactor = 0.333)
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
}