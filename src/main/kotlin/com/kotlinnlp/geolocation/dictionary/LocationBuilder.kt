/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.dictionary

import com.beust.klaxon.Parser
import com.kotlinnlp.geolocation.structures.LanguageISOCode
import com.kotlinnlp.geolocation.structures.Location

/**
 * A helper to build a location encoded in JSON format.
 */
internal object LocationBuilder {

  /**
   * A set of location sub-types not valid to be built.
   */
  private val INVALID_SUB_TYPES = setOf("hamlet", "village")

  /**
   * A JSON parser.
   */
  private val jsonParser = Parser()

  /**
   * A set of valid language ISO codes (upper case).
   */
  private val validLanguageISOCodes: Set<String> = LanguageISOCode.values().map { it.toString().toUpperCase() }.toSet()

  /**
   * Whether a location is valid to be built: ensure that the sub-type is valid and the name is not null.
   *
   * @param properties the properties list of a location
   *
   * @return a boolean indicating if the location is valid to be built
   */
  fun isValidLocation(properties: List<*>): Boolean = properties[3] !in INVALID_SUB_TYPES && properties[4] != null

  /**
   * Build a location, given a string containing its properties as JSON list (as explained in the resources
   * `README.md` file).
   *
   * @param properties a string of encoded properties
   *
   * @return a new location
   */
  fun buildLocation(properties: String): Location = decodeProperties(properties).let { propList ->

    val iter: Iterator<*> = propList.iterator()

    @Suppress("UNCHECKED_CAST")
    Location(
      id = (iter.next() as String).toUpperCase(),
      unlocode = (iter.next() as? String)?.toUpperCase(),
      isoA2 = iter.next() as? String,
      subType = iter.next() as? String,
      name = iter.next() as String,
      translations = iter.next()?.let { buildTranslations(it as Map<String, String>) },
      otherNames = iter.next()?.toStringList(),
      demonym = iter.next() as? String,
      coords = buildCoordinates(lat = iter.next() as? Double, lon = iter.next() as? Double),
      borders = iter.next()?.toStringList(),
      isCapital = iter.next() as? Boolean,
      area = iter.next() as? Int,
      population = iter.next() as? Int,
      languages = iter.next()?.toStringList(),
      altDivisions = iter.next()?.let { buildAltDivisions(it as List<*>) }
    )
  }

  /**
   * Decode the location properties from a JSON string.
   *
   * @param jsonLocation the properties representing a location, encoded as JSON list
   *
   * @return a list of location properties
   */
  fun decodeProperties(jsonLocation: String): List<*> = jsonParser.parse(StringBuilder(jsonLocation)) as List<*>

  /**
   * Build the name translations of a location.
   *
   * @param translations a map of name translations associated by language ISO code
   *
   * @return a new translations map or null if no valid translation has been found
   */
  private fun buildTranslations(translations: Map<String, String>): Map<LanguageISOCode, String>? {

    val translationsUpper: Map<String, String> = translations.mapKeys { it.key.toUpperCase() }
    val validLangCodes: Set<String> = translationsUpper.keys.intersect(this.validLanguageISOCodes)

    val validTranslations: Map<LanguageISOCode, String> = validLangCodes.associate {
      LanguageISOCode.valueOf(it) to translationsUpper.getValue(it)
    }

    translationsUpper.keys.subtract(validLangCodes).forEach { println("[WARNING] Invalid language ISO code: $it") }

    return if (validTranslations.isNotEmpty()) validTranslations else null
  }


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
      Location.Coordinates(lat = it, lon = lon)
    }

  /**
   * Build the alternative divisions of a location given the list of its context objects.
   *
   * @param altDivisions the list of alternative division objects of a location
   *
   * @return a new list of alternative divisions
   */
  private fun buildAltDivisions(altDivisions: List<*>): List<Location.AlternativeDivision> =
    altDivisions.map { it as List<*>
      Location.AlternativeDivision(type = it[0] as String, name = it[1] as String, level = it[2] as Int)
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