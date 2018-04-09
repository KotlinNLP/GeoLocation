/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

/**
 * A location.
 *
 * @property id the unique location id
 * @property iso
 * @property type
 * @property subType
 * @property name
 * @property translations
 * @property otherNames
 * @property labels
 * @property demonym
 * @property coords
 * @property borders
 * @property isCapital
 * @property area
 * @property population
 * @property languages
 * @property contexts
 */
data class Location(
  val id: String,
  val iso: String?,
  val type: String,
  val subType: String?,
  val name: String,
  val translations: Translations,
  val otherNames: List<String>?,
  val labels: List<String>,
  val demonym: String?,
  val coords: Coordinates?,
  val borders: List<String>?,
  val isCapital: Boolean?,
  val area: Int?,
  val population: Int?,
  val languages: List<String>?,
  val contexts: List<Context>?
) {

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
  )

  /**
   * The coordinates.
   *
   * @property lat the latitude
   * @property lon the longitude
   */
  data class Coordinates(val lat: Double, val lon: Double)

  /**
   * An adding context.
   *
   * @property type the type
   * @property name the name
   * @property level the hierarchic level (useful in case of more contexts)
   */
  data class Context(val type: String, val name: String, val level: Int)
}
