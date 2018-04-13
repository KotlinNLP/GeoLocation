# GeoLocation [![GitHub version](https://badge.fury.io/gh/KotlinNLP%2FGeoLocation.svg)](https://badge.fury.io/gh/KotlinNLP%2FGeoLocation) [![Build Status](https://travis-ci.org/KotlinNLP/GeoLocation.svg?branch=master)](https://travis-ci.org/KotlinNLP/GeoLocation)

GeoLocation is a Kotlin library designed to support the identification of geo-locations in a text.

GeoLocation is part of [KotlinNLP](http://kotlinnlp.com/ "KotlinNLP").


## Getting Started

### Import with Maven

```xml
<dependency>
    <groupId>com.kotlinnlp</groupId>
    <artifactId>geolocation</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Dictionary model

Download a serialized a model of the `LocationsDictionary` 
[here](https://drive.google.com/open?id=1ZX9xWmb4lADqcaDVmii4HN168Itf0Lmc).


## The locations JSON line file

A JSON line file, with a location for each line. 

### Locations hierarchy

Locations have one of the following types, that are deduced from their ID:
* `city` (ID: "XXXX?????XXXX")
* `admin_area_1` (ID: "XXXX??XXX0000")
* `admin_area_2` (ID: "XXXXXX0000000")
* `country` (ID: "XXXX000000000")
* `continent` (ID: "X000000000000")
* `region` (ID: "0X00000000000")

("X" stands for a hex digit from 1 to F, "?" stands for "0" or "X")

They are distributed following this hierarchy:
```
continent     region
    |___________|
          |
       country
          |______________
          |              |
          |           admin_area_2
          |______________|
          |       |
          |   admin_area_1
          |_______|
          |
         city
```
Note: as shown in the graph, the `admin_area_1` and the `admin_area_2` are optional in the hierarchy of a lower level 
location.

### Location properties

Each location is represented by a list with 20 elements, each representing a property:

0.  `id`: String. The ID.
1.  `iso-a2`: String, nullable. Defined only for type 'country'. The ISO 3166-1 alpha-2 code of the country.
2.  `sub-type`: String, nullable. Defined only for some locations of type 'country', 'admin_area_2', 'admin_area_1' and 
'city'. The sub-type (the type is deduced from the `id`).
3.  `name`: String, nullable. The main name.
4.  `name en`: String, nullable. Defined only for some locations of type 'country', 'admin_area_1' and 'city'. 
The English name.
5.  `name it`: String, nullable. Defined only for some locations of type 'country', 'admin_area_1' and 'city'. 
The Italian name.
6.  `name de`: String, nullable. Defined only for some locations of type 'country', 'admin_area_1' and 'city'. 
The German name.
7.  `name es`: String, nullable. Defined only for some locations of type 'country', 'admin_area_1' and 'city'. 
The Spanish name.
8.  `name fr`: String, nullable. Defined only for some locations of type 'country', 'admin_area_1' and 'city'. 
The French name.
9. `name ar`: String, nullable. Defined only for some locations of type 'country', 'admin_area_1' and 'city'. 
The Arabic name.
10. `other names`: nullable. Defined only for some locations of type 'country', 'admin_area_2' and 'admin_area_1'. 
A list of other names.
11. `demonym`: String, nullable. Defined only for some locations of type 'country', 'admin_area_1'. 
Defined only 'West Bank' and 'Gaza Strip'). The demonym.
12. `lat`: Int, null for type 'region'. The latitude coordinate.
13. `lon`: Int, null for type 'region'. The longitude coordinate.
14. `borders`: List of String, nullable. Defined only for some locations of type 'country'. 
It contains a list of the adjacent countries ids.
15. `is capital`: Boolean, nullable. Defined only for type 'city'. Whether a city is the capital of its country.
16. `area`: Int, nullable. Defined only for type 'country'. The area of the country in km^2.
17. `population`: Int, nullable. Defined only for some locations of type 'country', 'admin_area_1' and 'city'. 
The population.
18. `languages`: List of String, nullable. Defined only for some locations of type 'country' and 'admin_area_1'. 
The ISO 639-1 codes of the languages spoken in the location.
19. `contexts`: List of List, nullable. Defined only for some locations of type 'city'. 
It is a list of contexts, containing in turn `type` (String), `name` (String) and `level` (Int), in this order.

### Location ID

A string containing a number in hexadecimal format, composed by 13 digits divided in 6 groups, each representing a 
level of the locations hierarchy.

An example:
```
"52A30012F04DD"
 5   2   A3   00   12F  04DD
(1) (2)  (3)  (4)  (5)  (6)

```

Groups:
1. Continent (1 digit)
2. Region (1 digit)
3. Country (2 digits)
4. Admin Area 2 (2 digits)
5. Admin Area 1 (3 digits)
6. City (4 digits)

Note: in the example, the country related to that location has ID "52A3000000000" (it is enough to replace the sections 
of the lower levels with zeros).

### Location sub-types

Here is a list of the possible values of the `sub-type` property:
* "administrative county"
* "administrative state"
* "administrative zone"
* "automonous region"
* "autonomous city"
* "autonomous commune"
* "autonomous community"
* "autonomous monastic state"
* "autonomous province"
* "autonomous region"
* "autonomous republic"
* "autonomous sector"
* "autonomous territory"
* "canton"
* "capital"
* "capital city"
* "capital district"
* "capital metropolitan city"
* "capital region"
* "capital territory"
* "captial district"
* "centrally administered area"
* "circuit"
* "city"
* "city|municipality|thanh pho"
* "commissiary"
* "commune|municipality"
* "country"
* "county"
* "departamento"
* "department"
* "dependency"
* "district"
* "district|regencies"
* "division"
* "economic prefecture"
* "emirate"
* "federal dependency"
* "federal district"
* "federal republic"
* "federal state"
* "federal territory"
* "governorate"
* "hamlet"
* "highly urbanized city"
* "independent city"
* "independent component city"
* "independent municipality"
* "independent town"
* "indigenous territory"
* "intendancy"
* "metropolis"
* "metropolitan city"
* "municipality"
* "municipality|governarate"
* "municipality|prefecture"
* "national capital area"
* "national district"
* "national territory"
* "neutral city"
* "parish"
* "prefecture"
* "préfecture"
* "province"
* "provincial city"
* "quarter"
* "region"
* "regional council"
* "republic"
* "republican city"
* "sovereign state"
* "special city"
* "special district"
* "special municipality"
* "special region"
* "special self-governing city"
* "state"
* "statistical region"
* "suburb"
* "territorial unit"
* "territory"
* "town"
* "township"
* "union territory"
* "unitary authority"
* "urban county"
* "urban prefecture"
* "usa territory"
* "village"
* "voivodeship|province"


## License

This software is released under the terms of the 
[Mozilla Public License, v. 2.0](https://mozilla.org/MPL/2.0/ "Mozilla Public License, v. 2.0")


## Contributions

We greatly appreciate any bug reports and contributions, which can be made by filing an issue or making a pull 
request through the [github page](https://github.com/kotlinnlp/GeoLocation "GeoLocation on GitHub").
