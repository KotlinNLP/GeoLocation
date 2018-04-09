/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

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
   * A candidate location to give as input.
   *
   * @property id an ID (unique within the input list of candidates)
   * @property name the name of the candidate entity
   * @property score the semantic score (as confidence that the candidate could be a location)
   */
  data class Candidate(val id: Int, val name: String, val score: Double)

  /**
   * Get the valid locations within a given list of candidates.
   *
   * @param candidates the entities found in a text, candidate as locations
   *
   * @return a list with the same length of the given [candidates], containing at each position the related location
   *         if one has been found, otherwise null
   */
  fun getLocations(candidates: List<Candidate>): List<Location?> {

    require(candidates.map { it.id }.toSet().size == candidates.size) {
      "Candidate locations must all have different IDs."
    }

    TODO()
  }
}