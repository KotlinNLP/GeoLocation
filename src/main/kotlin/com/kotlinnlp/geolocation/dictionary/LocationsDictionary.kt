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
import com.kotlinnlp.utils.Serializer
import com.kotlinnlp.utils.forEachLine
import com.kotlinnlp.utils.getLinesCount
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

/**
 * A dictionary containing locations organized in a hierarchy per type, such as continents, countries, cities, etc..
 * It is loaded from a file in JSON line format (as explained in the resources `README.md` file).
 *
 * @param filename the name of the input file containing all the locations in JSON line format
 * @param verbose whether to print the loading progress (default = true)
 */
class LocationsDictionary(filename: String, verbose: Boolean = true) : Serializable {

  companion object {

    /**
     * Private val used to serialize the class (needed by Serializable).
     */
    @Suppress("unused")
    private const val serialVersionUID: Long = 1L

    /**
     * Read a [LocationsDictionary] (serialized) from an input stream and decode it.
     *
     * @param inputStream the [InputStream] from which to read the serialized [LocationsDictionary]
     *
     * @return the [LocationsDictionary] read from [inputStream] and decoded
     */
    fun load(inputStream: InputStream): LocationsDictionary = Serializer.deserialize(inputStream)
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
   * Serialize this [LocationsDictionary] and write it to an output stream.
   *
   * @param outputStream the [OutputStream] in which to write this serialized [LocationsDictionary]
   */
  fun dump(outputStream: OutputStream) = Serializer.serialize(this, outputStream)

  /**
   * Add an entry to the dictionary, given the JSON string containing the encoded properties of a location.
   *
   * @param jsonLocation the properties representing a location, encoded as JSON list
   */
  private fun addEntry(jsonLocation: String) {

    LocationBuilder.decodeProperties(jsonLocation).let { properties ->

      if (LocationBuilder.isValidLocation(properties)) {

        val location: Location = LocationBuilder.buildLocation(jsonLocation)

        this.encodedLocationsById[location.id] = location

        location.labels.forEach {
          this.encodedLocationsByLabel.getOrPut(it.toLowerCase()) { mutableSetOf() }.add(location)
        }
      }
    }
  }
}