/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

import com.kotlinnlp.geolocation.structures.ExtendedLocation
import com.kotlinnlp.geolocation.structures.Location
import java.lang.Math.pow

/**
 * The helper that calculates the confidence scores of the best locations.
 *
 * @param bestLocations the list of best locations
 */
internal class ConfidenceHelper(private val bestLocations: List<ExtendedLocation>) {

  /**
   * The set of best locations ids.
   */
  private val bestLocationsIds: Set<String> = this.bestLocations.map { it.location.id }.toSet()

  /**
   * A map of best locations counts associated by type.
   */
  private val bestLocationsCountPerType: Map<Location.Type, Int> =
    this.bestLocations.groupBy { it.location.type }.mapValues { it.value.size }

  /**
   * A map of sub-levels counts associated by location id.
   */
  private val subLevelsCountPerLocation: MutableMap<String, Int> =
    this.bestLocationsIds.associateTo(mutableMapOf()) { it to 0 }

  /**
   * A map of sub-levels types associated by location id.
   */
  private val subLevelsTypesPerLocation: MutableMap<String, MutableSet<Location.Type>> =
    this.bestLocationsIds.associateTo(mutableMapOf()) { it to mutableSetOf<Location.Type>() }

  /**
   * Init internal variables.
   */
  init {

    this.bestLocations.forEach {
      it.parents.forEach { parent ->
        this.subLevelsCountPerLocation.computeIfPresent(parent.id) { _, count -> count + 1 }
        this.subLevelsTypesPerLocation[parent.id]?.add(it.location.type)
      }
    }
  }

  /**
   * Set the confidence of the [bestLocations].
   */
  fun setConfidences() {

    this.bestLocations.forEach {

      this.boostByParents(it)
      this.boostBySubLevels(it)
      this.boostByBrothers(it)
      this.boostByOtherRelatives(it)

      if (it.location.isCountry) this.boostByBorders(it)

      this.normalize(it)
    }
  }

  /**
   * Boost the location confidence by its parents.
   *
   * @param location an extended location within the [bestLocations]
   */
  private fun boostByParents(location: ExtendedLocation) {

    val bestParents: List<Location> = location.parents.filter { it.id in this.bestLocationsIds }
    val bestParentsTypes: Set<Location.Type> = bestParents.map { it.type }.toSet()
    val sameTypeParentsCount: Int = this.bestLocationsCountPerType.filterKeys { it in bestParentsTypes }.values.sum()

    if (sameTypeParentsCount > 0) location.confidence += bestParents.size.toFloat() / sameTypeParentsCount
  }

  /**
   * Boost the location confidence by its sub-levels.
   *
   * @param location an extended location within the [bestLocations]
   */
  private fun boostBySubLevels(location: ExtendedLocation) {

    val locId: String = location.location.id

    val subLevelsTypes: Set<Location.Type> = this.subLevelsTypesPerLocation.getValue(locId)

    // The count of best locations with type equal to the sub-levels of the given location
    val sameSubLevelsTypesCount: Int = this.bestLocationsCountPerType.filterKeys { it in subLevelsTypes }.values.sum()

    if (sameSubLevelsTypesCount > 0) {

      val subLevelsCount: Int = this.subLevelsCountPerLocation.getValue(locId)

      location.confidence += subLevelsCount.toFloat() / sameSubLevelsTypesCount
    }
  }

  /**
   * Boost the location confidence by brother locations.
   *
   * @param location an extended location within the [bestLocations]
   */
  private fun boostByBrothers(location: ExtendedLocation) {

    this.bestLocationsCountPerType.getValue(location.location.type).let { sameTypeCount ->

      if (sameTypeCount > 1) {

        // The brothers of the given location that are best locations
        val bestBrothersCount: Int = this.bestLocations.count { it.isBrother(location) }

        // sameTypeCount - 1 = the count of best locations of the same type of the given location except itself
        location.confidence += bestBrothersCount.toFloat() / (sameTypeCount - 1)
      }
    }
  }

  /**
   * Boost the location confidence by other relatives.
   *
   * @param location an extended location within the [bestLocations]
   */
  private fun boostByOtherRelatives(location: ExtendedLocation) {

    val possibleRelativesCount: Int = this.bestLocations.count { it.location.isInsideCountry }

    if (possibleRelativesCount > 0) {

      val relativesCount: Int = this.bestLocations.count { it.isRelative(location) }

      location.confidence += relativesCount.toFloat() / possibleRelativesCount
    }
  }

  /**
   * Boost the confidence of a country by its borders.
   *
   * @param location an extended location within the [bestLocations]
   */
  private fun boostByBorders(location: ExtendedLocation) {

    this.bestLocationsCountPerType.getValue(Location.Type.Country).let { bestCountriesCount ->

      if (bestCountriesCount > 1) {

        // The border countries of the given location that are best locations
        val bestBordersCount: Int = location.location.borders?.count { it in this.bestLocationsIds } ?: 0

        // bestCountriesCount - 1 = the count of best countries except the given location itself
        location.confidence += bestBordersCount.toFloat() / (bestCountriesCount - 1)
      }
    }
  }

  /**
   * Normalize the confidence of a location.
   *
   * @param location an extended location within the [bestLocations]
   */
  private fun normalize(location: ExtendedLocation) {

    location.confidence /= 5 // average of all the contributions

    location.confidence = pow(location.confidence, 0.333)
  }
}
