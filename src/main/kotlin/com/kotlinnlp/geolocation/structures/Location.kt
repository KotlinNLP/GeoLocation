/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.structures

import java.io.Serializable

/**
 * A location.
 *
 * @property id the unique location id
 * @property iso the ISO 3166-1 alpha-2 code of a country (can be null)
 * @property subType the sub-type (can be null)
 * @property name the name
 * @property translations a name translations object (can be null)
 * @property otherNames a list of other possible names (can be null)
 * @property demonym the demonym (can be null)
 * @property coords the coordinates (can be null)
 * @property borders a list of border countries (can be null)
 * @property isCapital a boolean indicating if a city is the capital of its country (can be null)
 * @property area the area in km^2 (can be null)
 * @property population the population (can be null)
 * @property languages a list of spoken languages (can be null)
 * @property contexts a list of contexts (can be null)
 */
data class Location(
  val id: String,
  val iso: String? = null,
  val subType: String? = null,
  val name: String,
  val translations: Translations? = null,
  val otherNames: List<String>? = null,
  val demonym: String? = null,
  val coords: Coordinates? = null,
  val borders: List<String>? = null,
  val isCapital: Boolean? = null,
  val area: Int? = null,
  val population: Int? = null,
  val languages: List<String>? = null,
  val contexts: List<Context>? = null
) : Serializable {

  companion object {

    /**
     * Private val used to serialize the class (needed by Serializable).
     */
    @Suppress("unused")
    private const val serialVersionUID: Long = 1L
  }

  /**
   * The location type.
   */
  enum class Type { City, AdminArea1, AdminArea2, Country, Region, Continent }

  /**
   * The [name] translations.
   *
   * @property en the English name
   * @property ar the Arabic name
   * @property it the Italian name
   * @property de the German name
   * @property es the Spanish name
   * @property fr the French name
   */
  data class Translations(
    val en: String?,
    val ar: String?,
    val it: String?,
    val de: String?,
    val es: String?,
    val fr: String?
  ) : Serializable {

    companion object {

      /**
       * Private val used to serialize the class (needed by Serializable).
       */
      @Suppress("unused")
      private const val serialVersionUID: Long = 1L
    }

    /**
     * The list of not null translations.
     */
    val list: List<String> by lazy { listOfNotNull(this.en, this.ar, this.it, this.de, this.es, this.fr) }
  }

  /**
   * The coordinates.
   *
   * @property lat the latitude
   * @property lon the longitude
   */
  data class Coordinates(val lat: Double, val lon: Double) : Serializable {

    companion object {

      /**
       * Private val used to serialize the class (needed by Serializable).
       */
      @Suppress("unused")
      private const val serialVersionUID: Long = 1L
    }
  }

  /**
   * An adding context.
   *
   * @property type the type
   * @property name the name
   * @property level the hierarchic level (useful in case of more contexts)
   */
  data class Context(val type: String, val name: String, val level: Int) : Serializable {

    companion object {

      /**
       * Private val used to serialize the class (needed by Serializable).
       */
      @Suppress("unused")
      private const val serialVersionUID: Long = 1L
    }
  }

  /**
   * The location type.
   */
  val type: Type by lazy { this.buildType() }

  /**
   * A set of all the labels (lower case) with which the location can be named.
   */
  val labels: Set<String> by lazy { this.buildLabels() }

  /**
   * Whether this location is a City.
   */
  val isCity: Boolean by lazy { this.type == Type.City }

  /**
   * Whether this location is a little City (sub-type != "city").
   */
  val isLittleCity: Boolean by lazy { this.type == Type.City && this.subType != "city" }

  /**
   * Whether this location is a big City (sub-type = "city").
   */
  val isBigCity: Boolean by lazy { this.type == Type.City && this.subType == "city" }

  /**
   * Whether this location is an Admin Area 1.
   */
  val isAdminArea1: Boolean by lazy { this.type == Type.AdminArea1 }

  /**
   * Whether this location is an Admin Area 2.
   */
  val isAdminArea2: Boolean by lazy { this.type == Type.AdminArea2 }

  /**
   * Whether this location is a Country.
   */
  val isCountry: Boolean by lazy { this.type == Type.Country }

  /**
   * Whether this location is a Region.
   */
  val isRegion: Boolean by lazy { this.type == Type.Region }

  /**
   * Whether this location is a Continent.
   */
  val isContinent: Boolean by lazy { this.type == Type.Continent }

  /**
   * The list of not null parent IDs, from the nearest in the hierarchy to the top (excluding the region).
   */
  val parentsIds: List<String> by lazy {
    listOfNotNull(this.adminArea1Id, this.adminArea2Id, this.countryId, this.continentId)
  }

  /**
   * The admin area 1 ID in which this location is (or null if it is not inside an admin area 1).
   */
  val adminArea1Id: String? by lazy {
    if (this.isInsideAdminArea1) this.id.replaceRange(9 until 13, "0".repeat(4)) else null
  }

  /**
   * The admin area 2 ID in which this location is (or null if it is not inside an admin area 2).
   */
  val adminArea2Id: String? by lazy {
    if (this.isInsideAdminArea2) this.id.replaceRange(6 until 13, "0".repeat(7)) else null
  }

  /**
   * The country ID in which this location is (or null if it is not inside a country).
   */
  val countryId: String? by lazy {
    if (this.isInsideCountry) this.id.replaceRange(4 until 13, "0".repeat(9)) else null
  }

  /**
   * The region ID in which this location is (or null if it is not inside a region).
   */
  val regionId: String? by lazy { if (this.isInsideRegion) "0" + this.id[1] + "0".repeat(11) else null }

  /**
   * The continent ID in which this location is (or null if it is not inside a continent).
   */
  val continentId: String? by lazy { if (this.isInsideContinent) this.id[0] + "0".repeat(12) else null }

  /**
   * Whether this location is inside a continent.
   */
  val isInsideContinent: Boolean by lazy { this.type !in setOf(Type.Continent, Type.Region) }

  /**
   * Whether this location is inside a region.
   */
  val isInsideRegion: Boolean by lazy { this.isInsideContinent }

  /**
   * Whether this location is inside a country.
   */
  val isInsideCountry: Boolean by lazy { this.isInsideContinent && this.type != Type.Country }

  /**
   * Whether this location is inside an admin area 2.
   */
  val isInsideAdminArea2: Boolean by lazy {
    this.type in setOf(Type.City, Type.AdminArea1) && !this.allIdZeros(4 until 6)
  }

  /**
   * Whether this location is inside an admin area 1.
   */
  val isInsideAdminArea1: Boolean by lazy { this.type == Type.City && !this.allIdZeros(6 until 9) }

  /**
   * Build the type of this location deducing it from the [id].
   *
   * @return the location type
   */
  private fun buildType(): Type = when {
    this.allIdZeros(1 until 13) -> Type.Continent
    this.allIdZeros(2 until 13) -> Type.Region
    this.allIdZeros(4 until 13) -> Type.Country
    this.allIdZeros(6 until 13) -> Type.AdminArea2
    this.allIdZeros(9 until 13) -> Type.AdminArea1
    else -> Type.City
  }

  /**
   * Build the set of labels of this location.
   *
   * @return a set of labels
   */
  private fun buildLabels(): Set<String> {

    val labels: MutableSet<String> = mutableSetOf(this.name.toLowerString())

    this.translations?.list?.forEach { labels.add(it.toLowerString()) }
    this.otherNames?.forEach { labels.add(it.toLowerString()) }

    return labels
  }

  /**
   * Cast any object into a [String] converting it to lower case.
   *
   * @return a string
   */
  private fun Any.toLowerString(): String = (this as String).toLowerCase()

  /**
   * Check if all the digits of the [id] in the given [range] are zeros.
   *
   * @param range a range of digits positions within the [id] length
   *
   * @return whether all the ID digits in the given range are zeros
   */
  private fun allIdZeros(range: IntRange): Boolean = range.all { i -> this.id[i] == '0' }
}
