/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.geolocation.structures.Location
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 */
class LocationSpec : Spek({

  describe("a Location") {

    context("a city with a complete hierarchy") {

      val location = Location(id = "51180C026000A", name = "Washington")

      on("type property") {

        it("should have type City") {
          assertEquals(Location.Type.City, location.type)
        }
      }

      on("parents ids properties") {

        it("should have the expected adminArea1Id property") {
          assertEquals("51180C0260000", location.adminArea1Id)
        }

        it("should have the expected adminArea2Id property") {
          assertEquals("51180C0000000", location.adminArea2Id)
        }

        it("should have the expected countryId property") {
          assertEquals("5118000000000", location.countryId)
        }

        it("should have the expected regionId property") {
          assertEquals("0100000000000", location.regionId)
        }

        it("should have the expected continentId property") {
          assertEquals("5000000000000", location.continentId)
        }
      }

      on("boolean hierarchy properties") {

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
    }

    context("a city with an incomplete hierarchy") {

      val location = Location(id = "1308020000001", name = "Shoreditch")

      on("type property") {

        it("should have type City") {
          assertEquals(Location.Type.City, location.type)
        }
      }

      on("parents ids properties") {

        it("should have a null adminArea1Id property") {
          assertNull(location.adminArea1Id)
        }

        it("should have the expected adminArea2Id property") {
          assertEquals("1308020000000", location.adminArea2Id)
        }

        it("should have the expected countryId property") {
          assertEquals("1308000000000", location.countryId)
        }

        it("should have the expected regionId property") {
          assertEquals("0300000000000", location.regionId)
        }

        it("should have the expected continentId property") {
          assertEquals("1000000000000", location.continentId)
        }
      }

      on("boolean hierarchy properties") {

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

    context("an admin area 1") {

      val location = Location(id = "2222000010000", name = "São Tomé")

      on("type property") {

        it("should have type AdminArea1") {
          assertEquals(Location.Type.AdminArea1, location.type)
        }
      }

      on("parents ids properties") {

        it("should have a null adminArea1Id property") {
          assertNull(location.adminArea1Id)
        }

        it("should have a null adminArea2Id property") {
          assertNull(location.adminArea2Id)
        }

        it("should have the expected countryId property") {
          assertEquals("2222000000000", location.countryId)
        }

        it("should have the expected regionId property") {
          assertEquals("0200000000000", location.regionId)
        }

        it("should have the expected continentId property") {
          assertEquals("2000000000000", location.continentId)
        }
      }

      on("boolean hierarchy properties") {

        it("should have isInsideAdminArea1 false") {
          assertFalse { location.isInsideAdminArea1 }
        }

        it("should have isInsideAdminArea2 true") {
          assertFalse { location.isInsideAdminArea2 }
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

    context("an admin area 2") {

      val location = Location(id = "5118010000000", name = "West Virginia")

      on("type property") {

        it("should have type AdminArea2") {
          assertEquals(Location.Type.AdminArea2, location.type)
        }
      }

      on("parents ids properties") {

        it("should have a null adminArea1Id property") {
          assertNull(location.adminArea1Id)
        }

        it("should have a null adminArea2Id property") {
          assertNull(location.adminArea2Id)
        }

        it("should have the expected countryId property") {
          assertEquals("5118000000000", location.countryId)
        }

        it("should have the expected regionId property") {
          assertEquals("0100000000000", location.regionId)
        }

        it("should have the expected continentId property") {
          assertEquals("5000000000000", location.continentId)
        }
      }

      on("boolean hierarchy properties") {

        it("should have isInsideAdminArea1 false") {
          assertFalse { location.isInsideAdminArea1 }
        }

        it("should have isInsideAdminArea2 false") {
          assertFalse { location.isInsideAdminArea2 }
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

    context("a country") {

      val location = Location(id = "2201000000000", name = "Burkina Faso")

      on("type property") {

        it("should have type Country") {
          assertEquals(Location.Type.Country, location.type)
        }
      }

      on("parents ids properties") {

        it("should have a null adminArea1Id property") {
          assertNull(location.adminArea1Id)
        }

        it("should have a null adminArea2Id property") {
          assertNull(location.adminArea2Id)
        }

        it("should have a null countryId property") {
          assertNull(location.countryId)
        }

        it("should have the expected regionId property") {
          assertEquals("0200000000000", location.regionId)
        }

        it("should have the expected continentId property") {
          assertEquals("2000000000000", location.continentId)
        }
      }

      on("boolean hierarchy properties") {

        it("should have isInsideAdminArea1 false") {
          assertFalse { location.isInsideAdminArea1 }
        }

        it("should have isInsideAdminArea2 false") {
          assertFalse { location.isInsideAdminArea2 }
        }

        it("should have isInsideCountry false") {
          assertFalse { location.isInsideCountry }
        }

        it("should have isInsideContinent true") {
          assertTrue { location.isInsideContinent }
        }

        it("should have isInsideRegion true") {
          assertTrue { location.isInsideRegion }
        }
      }
    }

    context("a region") {

      val location = Location(id = "0100000000000", name = "Americas")

      on("type property") {

        it("should have type Region") {
          assertEquals(Location.Type.Region, location.type)
        }
      }

      on("parents ids properties") {

        it("should have a null adminArea1Id property") {
          assertNull(location.adminArea1Id)
        }

        it("should have a null adminArea2Id property") {
          assertNull(location.adminArea2Id)
        }

        it("should have a null countryId property") {
          assertNull(location.countryId)
        }

        it("should have a null regionId property") {
          assertNull(location.regionId)
        }

        it("should have a null continentId property") {
          assertNull(location.continentId)
        }
      }

      on("boolean hierarchy properties") {

        it("should have isInsideAdminArea1 false") {
          assertFalse { location.isInsideAdminArea1 }
        }

        it("should have isInsideAdminArea2 false") {
          assertFalse { location.isInsideAdminArea2 }
        }

        it("should have isInsideCountry false") {
          assertFalse { location.isInsideCountry }
        }

        it("should have isInsideContinent false") {
          assertFalse { location.isInsideContinent }
        }

        it("should have isInsideRegion false") {
          assertFalse { location.isInsideRegion }
        }
      }
    }

    context("a continent") {

      val location = Location(id = "1000000000000", name = "Europe")

      on("type property") {

        it("should have type Continent") {
          assertEquals(Location.Type.Continent, location.type)
        }
      }

      on("parents ids properties") {

        it("should have a null adminArea1Id property") {
          assertNull(location.adminArea1Id)
        }

        it("should have a null adminArea2Id property") {
          assertNull(location.adminArea2Id)
        }

        it("should have a null countryId property") {
          assertNull(location.countryId)
        }

        it("should have a null regionId property") {
          assertNull(location.regionId)
        }

        it("should have a null continentId property") {
          assertNull(location.continentId)
        }
      }

      on("boolean hierarchy properties") {

        it("should have isInsideAdminArea1 false") {
          assertFalse { location.isInsideAdminArea1 }
        }

        it("should have isInsideAdminArea2 false") {
          assertFalse { location.isInsideAdminArea2 }
        }

        it("should have isInsideCountry false") {
          assertFalse { location.isInsideCountry }
        }

        it("should have isInsideContinent false") {
          assertFalse { location.isInsideContinent }
        }

        it("should have isInsideRegion false") {
          assertFalse { location.isInsideRegion }
        }
      }
    }
  }
})
