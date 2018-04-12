/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.structures

import java.lang.Double.max

/**
 * A structure that extends a location with adding properties.
 *
 * @property location a location
 * @property parents the list of the [location] parents, in the same order of its 'parentIds' property
 * @property entities the list of entities from which this location originated
 * @property initScore the initial score of the location
 */
data class ExtendedLocation(
  val location: Location,
  val parents: List<Location>,
  val entities: List<CandidateEntity>,
  val initScore: Double
) {

  /**
   * A structure containing the boosts for the [score].
   *
   * @property children a map of score boosts associated by child entity
   * @property parents a map of score boosts associated by parent entity
   * @property brothers a map of score boosts associated by brother entity
   */
  private data class Boost(
    var children: MutableMap<String, Double> = mutableMapOf(),
    var parents: MutableMap<String, Double> = mutableMapOf(),
    var brothers: MutableMap<String, Double> = mutableMapOf()
  )

  /**
   * The location score.
   */
  var score: Double = this.initScore
    private set

  /**
   * The score boosts structure.
   */
  private val boost: Boost = Boost()

  /**
   * The list of [entities] names.
   */
  private val entitiesNames: List<String> by lazy { this.entities.map { it.normName } }

  /**
   * A map of entities scores associated by name
   */
  private val entitiesScoresMap: Map<String, Double> = this.entities.associate { it.name to it.score }

  /**
   * Boost the [score] of this location by a given [parent] and vice versa.
   *
   * @param parent the extended location of a parent of [location]
   */
  fun boostByParent(parent: ExtendedLocation) {

    require(parent.location.id in this.location.parentsIds) { "Invalid parent." }

    if (this.parentIsInfluential(parent.location)) {

      val entitiesInters: Set<String> = this.entitiesNames.intersect(parent.entitiesNames)

      this.boostScore(
        entitiesEntries = parent.getEntitiesEntries(exceptNames = entitiesInters),
        boostMap = this.boost.parents,
        boostMapRelative = this.boost.children)

      parent.boostScore(
        entitiesEntries = this.getEntitiesEntries(exceptNames = entitiesInters),
        boostMap = this.boost.parents,
        boostMapRelative = this.boost.children,
        rateFactor = 0.5)
    }
  }

  /**
   * Boost the [score] of this location for each label of a given parent that is present in a given set of candidates.
   *
   * @param parent a parent of this location
   * @param candidateNames a set of candidate names (must be lower case)
   * @param rateFactor a rate factor by which each boost is multiplied before it is applied
   */
  fun boostByParentLabels(parent: Location, candidateNames: Set<String>, rateFactor: Double) {

    parent.labels.filter { it in candidateNames }.forEach { this.score += rateFactor * this.initScore }
  }

  /**
   * @param parent a parent of this location
   *
   * @return whether the given [parent] is influential to score this location
   */
  private fun parentIsInfluential(parent: Location): Boolean =
    parent.type == Location.Type.AdminArea1 ||
      parent.type == Location.Type.AdminArea2 ||
      (parent.type == Location.Type.Country && !this.location.isInsideAdminArea2)

  /**
   * Get a list of <name, score> entities entries of this location.
   *
   * @param exceptNames a set of entities names to be excluded from the response
   *
   * @return a list of <name, score> map entries
   */
  private fun getEntitiesEntries(exceptNames: Set<String>): List<Map.Entry<String, Double>> {

    val entitiesNames: Set<String> = this.entitiesNames.subtract(exceptNames)

    return this.entitiesScoresMap.entries.filter { it.key in entitiesNames }
  }

  /**
   * Boost the score of this location through the scores of given entities.
   *
   * @param entitiesEntries the <name, score> entities entries
   * @param boostMap the boost map in which to save each entity boost
   * @param boostMapRelative the boost map of relatives in which to check if an entity already boosted this location
   * @param rateFactor a rate factor by which the boost is multiplied before it is applied (default = 1.0)
   */
  private fun boostScore(entitiesEntries: List<Map.Entry<String, Double>>,
                         boostMap: MutableMap<String, Double>,
                         boostMapRelative: MutableMap<String, Double>,
                         rateFactor: Double = 1.0) {

    var finalBoost = 0.0

    entitiesEntries.forEach { (name, score) ->

      // If the entity already boosted this location as another relative, mediates the boosts
      val boost: Double = boostMapRelative[name]?.let { (score + it) / 2 } ?: score

      boostMap[name] = boost

      finalBoost = max(finalBoost, boost)
    }

    this.score += rateFactor * finalBoost
  }
}