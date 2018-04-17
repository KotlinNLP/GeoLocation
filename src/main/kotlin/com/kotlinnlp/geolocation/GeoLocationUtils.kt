/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation

/**
 * Normalize the name of an entity.
 *
 * @param name the name of an entity
 *
 * @return a new string with the [name] normalized
 */
internal fun normalizeEntityName(name: String): String = name.trim().toLowerCase()
