/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.dictionary

import com.beust.klaxon.JsonArray
import com.beust.klaxon.Parser
import com.kotlinnlp.geolocation.structures.Location

/**
 * A helper to build a location encoded in JSON format.
 */
internal object LocationBuilder {

  /**
   * A JSON parser.
   */
  private val jsonParser = Parser()

  /**
   * Build a location, given a string containing its properties as JSON list (as explained in the resources
   * `README.md` file).
   *
   * @param properties a string of encoded properties
   *
   * @return a new location
   */
  fun buildLocation(properties: String): Location = decodeProperties(properties).let {

    val iter: Iterator<*> = it.iterator()

    Location(
      id = (iter.next() as String).toUpperCase(),
      iso = iter.next() as? String,
      subType = iter.next() as? String,
      name = iter.next() as String,
      translations = buildTranslations(translations = (0 until 6).map { iter.next() }),
      otherNames = iter.next()?.toStringList(),
      demonym = iter.next() as? String,
      coords = buildCoordinates(lat = iter.next() as? Double, lon = iter.next() as? Double),
      borders = iter.next()?.toStringList(),
      isCapital = iter.next() as? Boolean,
      area = iter.next() as? Int,
      population = iter.next() as? Int,
      languages = iter.next()?.toStringList(),
      contexts = iter.next()?.let { buildContexts(it as List<*>) }
    )
  }

  /**
   * Decode the location properties from a JSON string.
   *
   * @param jsonLocation the properties representing a location, encoded as JSON list
   *
   * @return a JSON array of location properties
   */
  fun decodeProperties(jsonLocation: String): JsonArray<*> =
    jsonParser.parse(StringBuilder(jsonLocation)) as JsonArray<*>

  /**
   * Build the name translations of a location.
   *
   * @param translations the list of translations
   *
   * @return a new translations object or null if no translation is defined
   */
  private fun buildTranslations(translations: List<*>): Location.Translations? =
    if (translations.any { it != null })
      Location.Translations(
        en = translations[0] as? String,
        it = translations[1] as? String,
        de = translations[2] as? String,
        es = translations[3] as? String,
        fr = translations[4] as? String,
        ar = translations[5] as? String
      )
    else
      null

  /**
   * Build the coordinates of a location.
   *
   * @param lat the latitude (can be null)
   * @param lon the longitude (can be null)
   *
   * @return a new coordinates object or null if coordinates are not defined
   */
  private fun buildCoordinates(lat: Double?, lon: Double?): Location.Coordinates? =
    lat?.let {
      require(lon != null) { "Invalid format: if 'lat' is not null also 'lon' must be not null." }
      Location.Coordinates(lat = it, lon = lon!!)
    }

  /**
   * Build the contexts of a location given the list of its context objects.
   *
   * @param contexts the list of context objects of a location
   *
   * @return a new contexts list
   */
  private fun buildContexts(contexts: List<*>): List<Location.Context> =
    contexts.map { it as List<*>
      Location.Context(type = it[0] as String, name = it[1] as String, level = it[2] as Int)
    }

  /**
   * Cast any object into a list of [String].
   *
   * @param lowerCase whether to convert each string to lower case (default false)
   *
   * @return a list of strings
   */
  private fun Any.toStringList(lowerCase: Boolean = false): List<String> = (this as List<*>).map {
    if (lowerCase) (it as String).toLowerCase() else (it as String)
  }
}