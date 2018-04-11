/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

import com.kotlinnlp.geolocation.structures.CandidateEntity
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
   * Get the valid locations within a given list of candidate entities.
   *
   * @param candidateEntities a set of entities found in a text, candidate as locations
   *
   * @return a list with the same length of the given [candidateEntities], containing at each position the related location
   *         if one has been found, otherwise null
   */
  fun getLocations(candidateEntities: Set<CandidateEntity>): List<Location?> {
    TODO()
  }
}