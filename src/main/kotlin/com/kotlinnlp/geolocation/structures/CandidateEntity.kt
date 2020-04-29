/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.structures

import com.kotlinnlp.geolocation.normalizeEntityName

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
  val normName: String = normalizeEntityName(this.name)

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