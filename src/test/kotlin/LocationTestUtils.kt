/* Copyright 2020-present Simone Cangialosi. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.geolocation.structures.Location

/**
 * @param id a location ID
 * @param name a location name
 *
 * @return a new location with the given id and name
 */
internal fun buildLocation(id: String, name: String) = Location(
  id = id,
  name = name,
  unlocode = null,
  isoA2 = null,
  subType = null,
  translations = mapOf(),
  otherNames = listOf(),
  demonym = null,
  coords = null,
  borders = listOf(),
  isCapital = false,
  area = null,
  population = null,
  languages = listOf(),
  altDivisions = listOf()
) 