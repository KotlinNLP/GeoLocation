/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

import com.kotlinnlp.geolocation.structures.ExtendedLocation
import com.kotlinnlp.geolocation.structures.Location

/**
 * Boost the score of this location by a given [parent] and vice versa.
 *
 * @param parent the extended location parent of this location
 */
internal fun ExtendedLocation.boostByParent(parent: ExtendedLocation) {

  require(parent.location.id in this.location.parentsIds) { "Invalid parent." }

  if (this.location.parentIsInfluential(parent.location)) {

    val entitiesInters: Set<String> = this.entitiesNames.intersect(parent.entitiesNames)

    this.score += calcBoost(
      entitiesEntries = parent.getEntitiesEntries(exceptNames = entitiesInters),
      boostMap = this.boost.parents,
      relativesBoostMaps = listOf(this.boost.children))

    parent.score += 0.5 * calcBoost(
      entitiesEntries = this.getEntitiesEntries(exceptNames = entitiesInters),
      boostMap = this.boost.children,
      relativesBoostMaps = listOf(this.boost.parents))
  }
}

/**
 * Boost the score of this location for each label of a given [parent] that is present in a given set of
 * candidates.
 *
 * @param parent a parent of this location
 * @param candidateNames a set of candidate names (must be lower case)
 * @param rateFactor a rate factor by which each boost is multiplied before it is applied
 */
internal fun ExtendedLocation.boostByParentLabels(parent: Location, candidateNames: Set<String>, rateFactor: Double) {

  parent.labels.filter { it in candidateNames }.forEach { this.score += rateFactor * this.initScore }
}

/**
 * Boost the score of this location by a given [brother] of it.
 *
 * @param brother the extended location brother of this location
 * @param coordinateEntitiesMap the map of entities to the groups of coordinate entities in which they are involved
 */
internal fun ExtendedLocation.boostByBrother(brother: ExtendedLocation,
                                             coordinateEntitiesMap: Map<String, List<Set<String>>>) {

  val entitiesInters: Set<String> = this.entitiesNames.intersect(brother.entitiesNames)
  val coordinatesEntities: List<String> = brother.entitiesNames.filter { name ->
    coordinateEntitiesMap[name]?.any { group -> group.any { it != name && it in this.entitiesNames } } ?: false
  }
  val notCoordinatesEntities: Set<String> = brother.entitiesNames.subtract(coordinatesEntities)

  // Boost by brothers that are not coordinate
  this.score += 0.5 * calcBoost(
    entitiesEntries = brother.getEntitiesEntries(exceptNames = entitiesInters.union(coordinatesEntities)),
    boostMap = this.boost.brothers,
    relativesBoostMaps = listOf(this.boost.children, this.boost.parents))

  // Boost by brothers that are coordinate
  this.score += calcBoost(
    entitiesEntries = brother.getEntitiesEntries(exceptNames = entitiesInters.union(notCoordinatesEntities)),
    boostMap = this.boost.brothers,
    relativesBoostMaps = listOf(this.boost.children, this.boost.parents))
}

/**
 * Calculate the score boost of a location through the scores of given entities.
 *
 * @param entitiesEntries the <name, score> entities entries
 * @param boostMap the boost map in which to save each entity boost
 * @param relativesBoostMaps the boost maps of relatives in which to check if an entity already boosted this location
 *
 * @return the score boost
 */
private fun calcBoost(entitiesEntries: List<Map.Entry<String, Double>>,
                      boostMap: MutableMap<String, Double>,
                      relativesBoostMaps: List<MutableMap<String, Double>>): Double {

  var finalBoost = 0.0

  entitiesEntries.forEach { (name, score) ->

    val validBoostMaps = relativesBoostMaps.filter { name in it }

    // If the entity already boosted this location as another relative, mediates the boosts
    val boost: Double = (score + validBoostMaps.sumByDouble { it.getValue(name) }) / (validBoostMaps.size + 1)

    boostMap[name] = boost

    finalBoost = java.lang.Double.max(finalBoost, boost)
  }

  return finalBoost
}

/**
 * @param parent a parent of this location
 *
 * @return whether the given [parent] is influential to score this location
 */
private fun Location.parentIsInfluential(parent: Location): Boolean =
  parent.isAdminArea1 || parent.isAdminArea2 || (parent.isCountry && !this.isInsideAdminArea2)

/**
 * Get a list of <name, score> entities entries of this location.
 *
 * @param exceptNames a set of entities names to be excluded from the response
 *
 * @return a list of <name, score> map entries
 */
private fun ExtendedLocation.getEntitiesEntries(exceptNames: Set<String>): List<Map.Entry<String, Double>> {

  val entitiesNames: Set<String> = this.entitiesNames.subtract(exceptNames)

  return this.entitiesScoresMap.entries.filter { it.key in entitiesNames }
}