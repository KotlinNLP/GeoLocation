/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.geolocation.dictionary.LocationsDictionary
import com.kotlinnlp.utils.Timer
import java.io.File
import java.io.FileOutputStream

/**
 * Load a dictionary from a JSON line file and serialize it into an output file.
 *
 * Command line arguments:
 *  1. The path of the input file in JSON line format.
 *  2. The path of the output file in which to save the serialized dictionary.
 */
fun main(args: Array<String>) {

  require(args.size == 2) { "Required 2 arguments: <input_file_path> <output_file_path>." }

  saveDictionary(dictionary = loadDictionary(args[0]), path = args[1])

  println("Done.")
}

/**
 * Load a [LocationsDictionary] from a JSON line file.
 *
 * @param path the path of the input file
 *
 * @return a new locations dictionary
 */
private fun loadDictionary(path: String): LocationsDictionary {

  val timer = Timer()

  println("Loading locations from '$path'...")

  val dictionary = LocationsDictionary(filename = path)

  println("Elapsed time: %s".format(timer.formatElapsedTime()))

  return dictionary
}

/**
 * Serialize a given [dictionary] into the file with a given [path].
 *
 * @param dictionary a locations dictionary
 * @param path the path of the output file
 */
private fun saveDictionary(dictionary: LocationsDictionary, path: String) {

  val timer = Timer()

  println("Serializing dictionary into '$path'...")

  dictionary.dump(FileOutputStream(File(path)))

  println("Elapsed time: %s".format(timer.formatElapsedTime()))
}
