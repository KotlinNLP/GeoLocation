/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.geolocation.LocationsFinder
import com.kotlinnlp.geolocation.dictionary.LocationsDictionary
import com.kotlinnlp.geolocation.structures.CandidateEntity
import com.kotlinnlp.geolocation.structures.ExtendedLocation
import com.kotlinnlp.geolocation.structures.Statistics
import com.kotlinnlp.neuraltokenizer.NeuralTokenizer
import com.kotlinnlp.neuraltokenizer.NeuralTokenizerModel
import com.kotlinnlp.utils.Timer
import java.io.File
import java.io.FileInputStream

/**
 * Find the locations mentioned in a given text, with candidates already scored.
 *
 * Command line arguments:
 *  1. The path of the serialized locations dictionary.
 *  2. The serialized model of the neural tokenizer.
 */
fun main(args: Array<String>) {

  require(args.size == 2) { "Required 2 arguments: <locations_dictionary_path> <tokenizer_model_filename>." }

  val dictionary: LocationsDictionary = loadDictionary(path = args[0])
  val tokenizer: NeuralTokenizer = loadTokenizer(modelFilename = args[1])
  val finder: LocationsFinder = findLocations(dictionary = dictionary, tokenizer = tokenizer)

  println("\nLOCATIONS")
  printLocations(finder.bestLocations)

  if (finder.bestLocations.isNotEmpty()) {
    println("\nSTATS")
    printStats(finder.stats)
  }
}

/**
 * Load a serialized [LocationsDictionary] from file.
 *
 * @param path the path of the serialized locations dictionary
 *
 * @return a locations dictionary
 */
private fun loadDictionary(path: String): LocationsDictionary {

  val timer = Timer()

  println("Loading locations dictionary from '$path'...")

  val dictionary = LocationsDictionary.load(FileInputStream(File(path)))

  println("Elapsed time: %s".format(timer.formatElapsedTime()))

  return dictionary
}

/**
 * Build a [NeuralTokenizer] loading its serialized model from file.
 *
 * @param modelFilename the filename of the neural tokenizer model
 *
 * @return a neural tokenizer
 */
private fun loadTokenizer(modelFilename: String): NeuralTokenizer {

  println("Loading tokenizer model from '$modelFilename'...")

  return NeuralTokenizer(model = NeuralTokenizerModel.load(FileInputStream(File(modelFilename))))
}

/**
 * Find locations in an example text.
 *
 * @param dictionary the locations dictionary
 * @param tokenizer the neural tokenizer
 *
 * @return a locations finder instantiated for the example text
 */
private fun findLocations(dictionary: LocationsDictionary, tokenizer: NeuralTokenizer): LocationsFinder {

  val timer = Timer()
  val text = """The crime rate is very high in the following cities of the United States of America: Los Angeles,
    ||New York and Philadelphia.""".trimMargin()

  println("\nSearching for locations...")

  val finder = LocationsFinder(
    dictionary = dictionary,
    textTokens = tokenizer.tokenize(text).flatMap { sentence -> sentence.tokens.map { it.form } },
    candidateEntities = setOf(
      CandidateEntity(name = "Los Angeles", score = 0.4),
      CandidateEntity(name = "New York", score = 0.6),
      CandidateEntity(name = "York", score = 0.6),
      CandidateEntity(name = "Philadelphia", score = 0.1),
      CandidateEntity(name = "rate", score = 0.2),
      CandidateEntity(name = "United States of America", score = 0.3),
      CandidateEntity(name = "United States", score = 0.3),
      CandidateEntity(name = "America", score = 0.3)
    ),
    coordinateEntitiesGroups = listOf(
      setOf("Los Angeles", "New York", "Philadelphia")
    ),
    ambiguityGroups = listOf(
      listOf("United States of America", "United States", "America"),
      listOf("New York", "York")
    )
  )

  println("Elapsed time: %s".format(timer.formatElapsedTime()))

  return finder
}

/**
 * Print the best locations found.
 *
 * @param locations the locations found
 */
private fun printLocations(locations: List<ExtendedLocation>) {

  if (locations.isNotEmpty()) {

    val maxEntityLen: Int = locations.map { entitiesToString(it).length }.max()!!

    locations.forEach { println("%-${maxEntityLen}s -> %s".format(entitiesToString(it), it)) }

  } else {
    println("No locations found.")
  }
}

/**
 * Print the global stats about the locations found.
 *
 * @param stats the statistics of the locations finder
 */
private fun printStats(stats: Statistics) {

  println("Score      : " + stats.score)
  println("Confidence : " + stats.confidence)
}

/**
 * @param location an extended location
 *
 * @return the string representation of the [location] entities
 */
private fun entitiesToString(location: ExtendedLocation): String =
  "'" + location.entities.joinToString(separator = "', '") + "'"
