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
 * The com.kotlinnlp.geolocation.LocationsFinder searches for all the valid locations among a set of candidate entities found in a text, already
 * scored respect to semantic properties.
 *
 * @param dictionary a dictionary containing all the locations that can be recognized
 */
class LocationsFinder(
  private val dictionary: LocationsDictionary = LocationsDictionary.load(defaultDictionaryPath)
) {

  companion object {

    /**
     * The path of the default locations dictionary file.
     */
    private val defaultDictionaryPath =
      LocationsFinder::class.java.classLoader.getResource("locations.jsonl").path
  }

  /**
   * The current candidate locations.
   */
  private lateinit var currentCandidates: List<ExtendedLocation>

  /**
   * The set of location ids of the current candidates.
   */
  private lateinit var locationIds: List<String>

  /**
   * A set of adding entities (taken from the parents labels) that could be mentioned in the text.
   */
  private lateinit var addingEntities: Set<String>

  /**
   * Get the valid locations within a given list of candidate entities.
   *
   * @param candidateEntities a set of entities found in a text, candidate as locations
   *
   * @return a list with the same length of the given [candidateEntities], containing at each position the related location
   *         if one has been found, otherwise null
   */
  fun getLocations(candidateEntities: Set<CandidateEntity>): List<Location?> {

    this.setCandidateLocations(candidateEntities)
    // TODO: add this.solveAmbiguities()
    this.setAddingEntities()
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

    this.currentCandidates = candidateLocations.distinctBy { it.id }.map {
      this.buildExtendedLocation(location = it, entities = entitiesNamesByLocId.getValue(it.id))
    }

    this.locationIds = this.currentCandidates.map { it.location.id }
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
   * Set the adding candidates that could be found in the text.
   */
  private fun setAddingEntities() {

    val addingLocationIds: Set<String> =
      this.currentCandidates.flatMap { it.location.parentsIds }.subtract(this.locationIds)

    this.addingEntities = addingLocationIds.flatMap { id -> this.dictionary.getValue(id).labels }.toSet()
  }

  /**
   * Set the current candidates scores.
   */
  private fun setScores() {

    this.currentCandidates.forEach {
      this.setScoreByParents(it)
    }
  }

  /**
   * Set the score of a given [location] by its parents.
   *
   * @param location a candidate location
   */
  private fun setScoreByParents(location: ExtendedLocation) {

    location.parents.forEach { parent ->

      if (parent.id in this.locationIds) {

        location.boostByParent(parent)

      } else {

        // location.scoreByAddingEntities()
      }
    }
  }
}