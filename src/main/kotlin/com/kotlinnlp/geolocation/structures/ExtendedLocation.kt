/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.structures

/**
 * A structure that extends a location with adding properties.
 *
 * @property location a location
 * @property parents the list of the [location] parents, in the same order of its 'parentIds' property
 * @property entities the list of entities from which this location originated
 * @property initScore the initial score of the location
 * @property boost the score boosts structure
 */
data class ExtendedLocation(
  val location: Location,
  val parents: List<Location>,
  val entities: List<CandidateEntity>,
  val initScore: Double,
  val boost: Boost = Boost()
) {

  /**
   * A structure containing the boosts for the [score].
   *
   * @property children the score boost related to the [location] children
   * @property parents the score boost related to the [location] parents
   * @property brothers the score boost related to the [location] brothers
   */
  data class Boost(var children: Double = 0.0, var parents: Double = 0.0, var brothers: Double = 0.0)

  /**
   * The location score.
   */
  var score: Double = this.initScore
    private set

  /**
   * Boost the [score] by a given parent.
   *
   * @param parent a parent location of [location]
   */
  fun boostByParent(parent: Location) {

    require(parent.id in this.location.parentsIds) { "Invalid parent." }

    if (this.parentIsInfluential(parent)) {
      TODO()
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
}