/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

import com.kotlinnlp.geolocation.dictionary.LocationsDictionary
import com.kotlinnlp.geolocation.structures.CandidateEntity
import com.kotlinnlp.geolocation.structures.ExtendedLocation

/**
 * Associate a location to each candidate entity in a set, that has been found in a text and already scored respect to
 * semantic properties.
 *
 * @param dictionary a dictionary containing all the locations that can be recognized
 * @param text the input text
 * @param candidateEntities a set of entities found in the [text], candidate as locations
 * @param coordinateEntitiesGroups a list of groups of entities that are coordinate in the text
 *
 * @return a list of the best extended locations found, sorted by descending importance
 */
fun findLocations(
  dictionary: LocationsDictionary,
  text: String,
  candidateEntities: Set<CandidateEntity>,
  coordinateEntitiesGroups: List<Set<String>>
): List<ExtendedLocation> =
  LocationsFinder(
    dictionary = dictionary,
    text = text,
    candidateEntities = candidateEntities,
    coordinateEntitiesGroups = coordinateEntitiesGroups
  ).bestLocations
