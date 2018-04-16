/* Copyright 2018-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.geolocation.structures

/**
 * Global statistics.
 *
 * @property score metrics about score
 * @property confidence metrics about confidence
 */
data class Statistics(val score: Metrics, val confidence: Metrics) {

  /**
   * Statistics metrics.
   *
   * @property avg an average of values
   * @property variance the variance of values
   * @property stdDev the standard deviation of values
   * @property stdDevPerc the standard deviation as percentage of the [avg]
   */
  data class Metrics(val avg: Double, val variance: Double, val stdDev: Double, val stdDevPerc: Double) {

    companion object {

      /**
       * Build [Metrics] by values.
       *
       * @param values a double array of values
       *
       * @return a new object of stats metrics about the given [values]
       */
      operator fun invoke(values: DoubleArray): Metrics {

        val avg: Double = values.average()
        val variance: Double = values.map { Math.pow(it - avg, 2.0) }.average()
        val stdDev: Double = Math.sqrt(variance)

        return Metrics(
          avg = avg,
          variance = variance,
          stdDev = stdDev,
          stdDevPerc = if (avg > 0) stdDev / avg else 0.0)
      }
    }

    override fun toString(): String = "avg %.2f, var %.2f, std dev %.2f (%.1f%%)".format(
      this.avg, this.variance, this.stdDev, this.stdDevPerc
    )
  }
}