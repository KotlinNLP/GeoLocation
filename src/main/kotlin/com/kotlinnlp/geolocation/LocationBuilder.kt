/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

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
    Location(
      id = (it[0] as String).toUpperCase(),
      iso = it[1] as? String,
      subType = it[2] as? String,
      name = it[3] as String,
      translations = buildTranslations(it),
      otherNames = it[10]?.toStringList(),
      demonym = it[11] as? String,
      coords = buildCoordinates(it),
      borders = it[14]?.toStringList(),
      isCapital = it[15] as? Boolean,
      area = it[16] as? Int,
      population = it[17] as? Int,
      languages = it[18]?.toStringList(),
      contexts = it[19]?.let { buildContexts(it as List<*>) }
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
   * Build the name translations of a location given the list of its properties.
   *
   * @param properties the list of location properties
   *
   * @return a new translations object or null if no translations are defined in the given [properties]
   */
  private fun buildTranslations(properties: List<*>): Location.Translations? =
    if (properties.subList(4, 10).any { it != null })
      Location.Translations(
        en = properties[4] as? String,
        it = properties[5] as? String,
        de = properties[6] as? String,
        es = properties[7] as? String,
        fr = properties[8] as? String,
        ar = properties[9] as? String
      )
    else
      null

  /**
   * Build the coordinates of a location given the list of its properties.
   *
   * @param properties the list of location properties
   *
   * @return a new coordinates object or null if no coordinates are defined in the given [properties]
   */
  private fun buildCoordinates(properties: List<*>): Location.Coordinates? =
    properties[12]?.let {
      require(properties[13] != null) { "Invalid format: if 'lat' is not null also 'lon' must be not null." }
      Location.Coordinates(lat = it as Double, lon = properties[13] as Double)
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