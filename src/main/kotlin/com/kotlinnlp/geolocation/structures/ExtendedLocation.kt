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

      var locationScoreBoost = 0.0
      var parentScoreBoost = 0.0
      val entitiesInters: Set<String> = this.entitiesNames.intersect(parent.entitiesNames)

      parent.entitiesNames.subtract(entitiesInters).forEach {

        var boost: Double = parent.getEntityScore(it)

        this.boost.parents[it] = boost

        // If the entity already boosted this location as child, mediates the boosts
        this.boost.children[it]?.let { childBoost -> boost = (boost + childBoost) / 2 }

        locationScoreBoost = max(locationScoreBoost, boost)
      }

      this.entitiesNames.subtract(entitiesInters).forEach {

        var boost: Double = this.getEntityScore(it)

        parent.boost.children[it] = boost

        // If the entity already boosted the parent as parent, mediates the boosts
        parent.boost.parents[it]?.let { parentBoost -> boost = (boost + parentBoost) / 2 }

        parentScoreBoost = max(parentScoreBoost, boost)
      }

      this.score += locationScoreBoost
      parent.score += parentScoreBoost / 2
    }
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
   * @param name the name of an entity that originated this location
   *
   * @return the score associated to the entity with the given [name]
   */
  private fun getEntityScore(name: String): Double = this.entitiesScoresMap.getValue(name)
}