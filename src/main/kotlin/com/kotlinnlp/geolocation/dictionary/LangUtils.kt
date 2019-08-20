/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.dictionary

object LangUtils {

  /**
   * There is bulletproof regexp contains only cyrillic letters.
   */
  private val cyrillicLetters = Regex("^[аАбБвВгГдДеЕёЁжЖзЗиИйЙкКлЛмМнНоОпПрРсСтТуУфФхХцЦчЧшШщЩъЪыЫьЬэЭюЮяЯ]+$")

  /**
   * @return true if this token contains cyrillic letters only, otherwise false
   */
  private fun String.isCyrillic(): Boolean = cyrillicLetters.matches(this)

  /**
   * Launch the [callback] for each added declination.
   *
   * @param baseForm the base form
   * @param callback the callback launched for each new declination
   */
  fun forEachDeclination(baseForm: String, callback: (String) -> Unit) {

    if (baseForm.isCyrillic()) {

      if (baseForm.length > 1 && baseForm.last() == 'а') {

        val root = baseForm.dropLast(1)

        callback(root + "у")
        callback(root + "ы")
        callback(root + "е")
        callback(root + "ой")

      } else if (baseForm.length > 2 &&  baseForm.takeLast(2) == "ия") {

        val root = baseForm.dropLast(2)

        callback(root + "ию")
        callback(root + "ии")
        callback(root + "ией")

      } else if (baseForm.length > 1 && baseForm.last() == 'я') {

        val root = baseForm.dropLast(1)

        callback(root + "ю")
        callback(root + "и")
        callback(root + "е")
        callback(root + "ей")
        callback(root + "ени")
        callback(root + "енем")
        callback(root + "ени")

      } else if (baseForm.length > 1 && baseForm.last() == 'ь') {

        val root = baseForm.dropLast(1)

        callback(root + "я")
        callback(root + "ю")
        callback(root + "ем")
        callback(root + "е")
        callback(root + "и")
        callback(root + "ю")
        callback(root + "ью")
        callback(root + "ём")

      } else if (baseForm.length > 2 &&  baseForm.takeLast(2) == "ий") {

        val root = baseForm.dropLast(2)

        callback(root + "ия")
        callback(root + "ию")
        callback(root + "ем")
        callback(root + "ии")

      } else if (baseForm.length > 1 && baseForm.last() == 'й') {

        val root = baseForm.dropLast(1)

        callback(root + "я")
        callback(root + "ю")
        callback(root + "ем")
        callback(root + "е")

      } else if (baseForm.length > 2 &&  baseForm.takeLast(2) == "ин") {

        callback(baseForm + "ина")
        callback(baseForm + "ину")
        callback(baseForm + "иемом")
        callback(baseForm + "ине")

      } else if (baseForm.length > 1 && baseForm.last() == 'о') {

        val root = baseForm.dropLast(1)

        callback(root + "а")
        callback(root + "у")
        callback(root + "ом")
        callback(root + "е")

      } else if (baseForm.length > 1 && baseForm.last() == 'е') {

        val root = baseForm.dropLast(1)

        callback(root + "я")
        callback(root + "ю")
        callback(root + "ем")
        callback(root + "е")

      } else {

        callback(baseForm + "а")
        callback(baseForm + "у")
        callback(baseForm + "ом")
        callback(baseForm + "е")
      }
    }
  }
}