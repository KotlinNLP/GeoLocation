/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.dictionary

import com.kotlinnlp.geolocation.structures.Location
import com.kotlinnlp.geolocation.exceptions.LocationNotFound
import com.kotlinnlp.progressindicator.ProgressIndicatorBar
import com.kotlinnlp.utils.forEachLine
import com.kotlinnlp.utils.getLinesCount

/**
 * A dictionary containing locations organized in a hierarchy per type, such as continents, countries, cities, etc..
 * It is loaded from a file in JSON line format (as explained in the resources `README.md` file).
 *
 * @param filename the name of the input file containing all the locations in JSON line format
 * @param verbose whether to print the loading progress (default = true)
 */
class LocationsDictionary(filename: String = defaultDictionaryPath, verbose: Boolean = true) {

  companion object {

    /**
     * The path of the default JSON line file containing all the locations.
     */
    private val defaultDictionaryPath =
      LocationsDictionary::class.java.classLoader.getResource("locations.jsonl").path

    /**
     * A set of location sub-types not valid to be inserted in the dictionary.
     */
    private val INVALID_SUB_TYPES = setOf("hamlet", "village")
  }

  /**
   * The locations associated by id.
   */
  private val encodedLocationsById = mutableMapOf<String, Location>()

  /**
   * The sets of locations associated by label.
   */
  private val encodedLocationsByLabel = mutableMapOf<String, MutableSet<Location>>()

  /**
   * Load the dictionary from file.
   */
  init {

    val progress: ProgressIndicatorBar? = if (verbose) ProgressIndicatorBar(total = getLinesCount(filename)) else null

    forEachLine(filename) {

      progress?.tick()

      this.addEntry(it)
    }
  }

  /**
   * Get a location by id.
   *
   * @param id the id of a location
   *
   * @return the location with the given [id] or null if no one has been found
   */
  operator fun get(id: String): Location? = this.encodedLocationsById[id.toUpperCase()]

  /**
   * Force to get a location by id.
   *
   * @param id the id of a location
   *
   * @throws LocationNotFound if no location has been found
   *
   * @return the location with the given [id]
   */
  fun getValue(id: String): Location = this[id] ?: throw LocationNotFound(id)

  /**
   * Get all the locations with the given [label].
   *
   * @param label a label of a location
   *
   * @return the locations with the given [label] or null if no one has been found
   */
  fun getByLabel(label: String): List<Location>? = this.encodedLocationsByLabel[label.toLowerCase()]?.toList()

  /**
   * Add an entry to the dictionary, given the JSON string containing the encoded properties of a location.
   *
   * @param jsonLocation the properties representing a location, encoded as JSON list
   */
  private fun addEntry(jsonLocation: String) {

    LocationBuilder.decodeProperties(jsonLocation).let { properties ->

      // ensure that the sub-type is valid and the name is not null
      if (properties[2] !in INVALID_SUB_TYPES && properties[3] != null) {

        val location: Location = LocationBuilder.buildLocation(jsonLocation)

        this.encodedLocationsById[location.id] = location

        location.labels.forEach {
          this.encodedLocationsByLabel.getOrPut(it.toLowerCase()) { mutableSetOf() }.add(location)
        }
      }
    }
  }
}