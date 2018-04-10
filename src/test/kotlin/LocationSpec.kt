/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.geolocation.Location
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class LocationSpec : Spek({

  describe("a Location") {

    context("a city with a complete hierarchy") {

      val location = Location(id = "51180C026000A", name = "Washington")

      it("should have isInsideAdminArea1 true") {
        assertTrue { location.isInsideAdminArea1 }
      }

      it("should have isInsideAdminArea2 true") {
        assertTrue { location.isInsideAdminArea2 }
      }

      it("should have isInsideCountry true") {
        assertTrue { location.isInsideCountry }
      }

      it("should have isInsideContinent true") {
        assertTrue { location.isInsideContinent }
      }

      it("should have isInsideRegion true") {
        assertTrue { location.isInsideRegion }
      }
    }

    context("a city with an incomplete hierarchy") {

      val location = Location(id = "1308020000001", name = "Shoreditch")

      it("should have isInsideAdminArea1 false") {
        assertFalse { location.isInsideAdminArea1 }
      }

      it("should have isInsideAdminArea2 true") {
        assertTrue { location.isInsideAdminArea2 }
      }

      it("should have isInsideCountry true") {
        assertTrue { location.isInsideCountry }
      }

      it("should have isInsideContinent true") {
        assertTrue { location.isInsideContinent }
      }

      it("should have isInsideRegion true") {
        assertTrue { location.isInsideRegion }
      }
    }
  }
})
