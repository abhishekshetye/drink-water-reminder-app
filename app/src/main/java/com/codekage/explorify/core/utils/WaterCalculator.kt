package com.codekage.explorify.core.utils

import kotlin.math.round

/**
 * Created by abhisheksimac on 31/03/19.
 */

class WaterCalculator {


    companion object {

        private const val ML_IN_ONE_GLASS_OF_WATER = 240

        fun calculateWaterInTermsOfGlasses(waterInMl: Int): Int {
            val waterGlasses = waterInMl.toFloat().div(ML_IN_ONE_GLASS_OF_WATER.toFloat())
            return round(waterGlasses).toInt()
        }


        fun getWaterInMultipleOfFive(progress: Int): Int {
            return (progress + 4) / 5 * 5
        }

    }

}