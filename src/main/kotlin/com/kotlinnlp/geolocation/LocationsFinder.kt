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
   * @property name the name of the entity
   * @property score the semantic score (as confidence that the candidate could be a location)
   */
  data class CandidateEntity(val name: String, val score: Double) {

    /**
     * The normalized [name].
     */
    val normName: String = this.name.trim().toLowerCase()

    /**
     * @return the hash code for this class, based on the [normName]
     */
    override fun hashCode(): Int = this.normName.hashCode()

    /**
     * Compare this object to another by [normName].
     *
     * @param other any object
     *
     * @return whether this object is equal to another
     */
    override fun equals(other: Any?): Boolean {

      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      if (normName != (other as CandidateEntity).normName) return false

      return true
    }
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