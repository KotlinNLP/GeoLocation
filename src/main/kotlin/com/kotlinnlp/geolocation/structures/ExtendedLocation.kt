/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.structures

/**
 * A structure that extends a location with adding properties, such as [score], [confidence] and [entities].
 *
 * @property location a location
 * @param parents the list of the [location] parents, in the same order of its 'parentIds' property
 * @param candidateEntities the list of candidate entities from which this location originated
 * @param initScore the initial score of the location
 */
data class ExtendedLocation(
  val location: Location,
  internal val parents: List<Location>,
  internal val candidateEntities: List<CandidateEntity>,
  internal val initScore: Double
) {

  /**
   * A structure containing the boosts for the [location] score.
   *
   * @property children a map of score boosts associated by child entity
   * @property parents a map of score boosts associated by parent entity
   * @property brothers a map of score boosts associated by brother entity
   */
  internal data class Boost(
    var children: MutableMap<String, Double> = mutableMapOf(),
    var parents: MutableMap<String, Double> = mutableMapOf(),
    var brothers: MutableMap<String, Double> = mutableMapOf()
  )

  /**
   * The score that indicates how much the location is relevant respect to the others in the text.
   * It is a percentage distributed among all the locations found in a text.
   */
  var score: Double = this.initScore
    internal set

  /**
   * The score of confidence that the location is the correct one associated to its candidates.
   * This score is calculated looking to the relations between this location and the others found in the input text.
   */
  var confidence: Double = 0.0
    internal set

  /**
   * The deviation of the [score] respect to the scores average of all the best locations found in the input text.
   */
  var scoreDeviation: Double = 0.0
    internal set

  /**
   * The deviation of the [confidence] respect to the scores average of all the best locations found in the input text.
   */
  var confidenceDeviation: Double = 0.0
    internal set

  /**
   * The list of entities names to which this locations has been associated.
   */
  lateinit var entities: List<String>
    internal set

  /**
   * The score boosts structure.
   */
  internal val boost: Boost = Boost()

  /**
   * The set of [candidateEntities] names.
   */
  internal val entitiesNames: Set<String> by lazy { this.candidateEntities.map { it.normName }.toSet() }

  /**
   * A map of entities scores associated by name
   */
  internal val entitiesScoresMap: Map<String, Double> by lazy {
    this.candidateEntities.associate { it.normName to it.score }
  }

  /**
   * @return a string representation of this class
   */
  override fun toString(): String = "[%.2f, %.2f] %s".format(this.score, this.confidence, this.location)

  /**
   * @return the hash code of this extended location
   */
  override fun hashCode(): Int = this.location.id.hashCode()

  /**
   * @param other another object
   *
   * @return whether this extended location is equal to [other]
   */
  override fun equals(other: Any?): Boolean {

    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ExtendedLocation

    if (location.id != other.location.id) return false

    return true
  }

  /**
   * Whether this location is a brother of another (they have the same hierarchic upper level or they are analogous
   * cities of the same country).
   *
   * @param otherLocation an extended location
   *
   * @return whether this location is a brother of the given [otherLocation]
   */
  internal fun isBrother(otherLocation: ExtendedLocation): Boolean {

    val thisLoc: Location = this.location
    val otherLoc: Location = otherLocation.location

    if (otherLoc.id == thisLoc.id || otherLoc.type != thisLoc.type) return false

    if (otherLoc.parentsIds.first() != thisLoc.parentsIds.first()) {

      val analogousCities: Boolean = otherLoc.isCity && otherLoc.subType == thisLoc.subType

      if (!analogousCities || otherLoc.countryId != thisLoc.countryId) return false
    }

    return true
  }

  /**
   * Whether this location is a relative of another (they are in the same country and share a common parent).
   *
   * @param otherLocation an extended location
   *
   * @return whether this location is a relative of the given [otherLocation]
   */
  internal fun isRelative(otherLocation: ExtendedLocation): Boolean {

    val thisLoc: Location = this.location
    val otherLoc: Location = otherLocation.location

    if (thisLoc.id == otherLoc.id) return false
    if (!thisLoc.isInsideCountry || !otherLoc.isInsideCountry) return false



    return true
  }

  /**
   * Whether this location is more probable than another, regarding their score, type, sub-type and population.
   *
   * @param otherLocation another extended location
   *
   * @return whether this location is more probable then another
   */
  internal fun isMoreProbableThan(otherLocation: ExtendedLocation): Boolean {

    val thisLoc: Location = this.location
    val otherLoc: Location = otherLocation.location

    if (this.score > otherLocation.score) return true

    if (this.score == otherLocation.score) {

      // City >> Admin Area 1
      if (thisLoc.isBigCity && otherLoc.isAdminArea1) return true

      // Country, Admin Area 1, Big City >> Little City
      if (thisLoc.let { it.isCountry || it.isAdminArea1 || it.isBigCity } && otherLoc.isLittleCity) return true

      // Country >> Admin Area 1, Continent
      if (thisLoc.isCountry && otherLoc.let { it.isAdminArea1 || it.isCountry }) return true

      // (Big City | Country) & (Big City | Country)
      if (thisLoc.let { it.isCountry || it.isBigCity } && otherLoc.let { it.isCountry || it.isBigCity }) {

        // Compare populations
        if (this.comparePopulation(otherLocation) > 0) return true
      }
    }

    return false
  }

  /**
   * Compare the population of this location with another.
   *
   * @param otherLocation another extended location
   *
   * @return 0 if the two population properties are equal
   *         -1 if this location population is null or less then the other one
   *         +1 if the other location population is null or less then this one
   */
  private fun comparePopulation(otherLocation: ExtendedLocation): Int {

    val thisPop: Int? = this.location.population
    val otherPop: Int? = otherLocation.location.population

    if (thisPop == otherPop) return 0

    return when {
      thisPop == null -> -1
      otherPop == null -> 1
      else -> thisPop.compareTo(otherPop)
    }
  }
}