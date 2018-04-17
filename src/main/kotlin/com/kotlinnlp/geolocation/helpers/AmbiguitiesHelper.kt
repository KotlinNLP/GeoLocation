/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.helpers

import com.kotlinnlp.geolocation.structures.ExtendedLocation

/**
 * The helper that solves ambiguities of entities.
 *
 * @param candidateLocationsById the map of candidate locations associated by id
 */
internal class AmbiguitiesHelper(private val candidateLocationsById: MutableMap<String, ExtendedLocation>) {

  /**
   * The map of candidate locations associated by entity norm name.
   */
  private var candidateLocationsByEntity: Map<String, Set<ExtendedLocation>> = this.buildCandidateLocationsByEntity()

  /**
   * Solve the ambiguities of entities composed by more words.
   *
   * Each ambiguity group is a list of strings, each representing a way to form a candidate location entity with part
   * of the same words involved.
   *
   * For each ambiguity group one entity (that is associated to a location) is chosen and the others are discarded and
   * removed from the locations they generated.
   *
   * If a location remains without candidate entities it is deleted from candidates too.
   *
   * @param ambiguityGroups a list of ambiguity groups
   */
  fun solveAmbiguities(ambiguityGroups: List<List<String>>) {

    val entitiesToDelete: Set<String> = this.getEntitiesToDelete(ambiguityGroups)

    // Decrease the score of locations with deleting entities
    entitiesToDelete.forEach { entityName ->
      this.candidateLocationsByEntity.getValue(entityName).forEach { it.updateInitScore(0.9 * it.initScore) }
    }

    // Remove entities from the candidate locations
    this.candidateLocationsById.values.forEach { it.candidateEntities.removeAll { it.normName in entitiesToDelete } }

    // Remove all candidate locations without entities
    this.candidateLocationsById.values.filter { it.candidateEntities.isEmpty() }.forEach {
      this.candidateLocationsById.remove(it.location.id)
    }
  }

  /**
   * @return a map of candidate locations sets associated by entity norm name
   */
  private fun buildCandidateLocationsByEntity(): Map<String, Set<ExtendedLocation>> {

    val locationsByEntity = mutableMapOf<String, MutableSet<ExtendedLocation>>()

    this.candidateLocationsById.values.forEach {
      it.candidateEntities.forEach { entity ->
        locationsByEntity.getOrPut(entity.normName) { mutableSetOf() }.add(it)
      }
    }

    return locationsByEntity
  }

  /**
   * Get the entities that must be deleted because they was ambiguous compared to others and they have been discarded.
   *
   * @param ambiguityGroups a list of ambiguity groups
   *
   * @return a set of entities that must be deleted
   */
  private fun getEntitiesToDelete(ambiguityGroups: List<List<String>>): Set<String> {

    val entitiesToKeep = mutableSetOf<String>()
    val entitiesToDelete = mutableSetOf<String>()

    ambiguityGroups.forEach { group ->

      var ambiguitySolved = false

      group.filter { it in this.candidateLocationsByEntity }.forEach { entityName ->

        if (ambiguitySolved) {

          if (entityName !in entitiesToKeep) entitiesToDelete.add(entityName)

        } else {

          ambiguitySolved = true

          entitiesToKeep.add(entityName)
          entitiesToDelete.remove(entityName)
        }
      }
    }

    return entitiesToDelete
  }
}