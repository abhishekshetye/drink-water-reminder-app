package com.codekage.explorify.core.utils

import com.codekage.explorify.core.utils.Formatter.Companion.getColoredTextForGlassesOfWaterInDialog
import com.codekage.explorify.core.utils.Formatter.Companion.getColoredTextForSipsOfWaterInDialog
import com.codekage.explorify.core.utils.Formatter.Companion.getTextForGlassesOfWaterInDialog
import com.codekage.explorify.core.utils.Formatter.Companion.getTextForSipsOfWaterInDialog
import kotlin.math.round

/**
 * Created by abhisheksimac on 31/03/19.
 */

class WaterCalculator {


    companion object {

        private const val ML_IN_ONE_GLASS_OF_WATER = 240
        private const val ML_IN_ONE_SIP_OF_WATER = 80

        fun calculateWaterInTermsOfGlasses(waterInMl: Int, withColoredText: Boolean = false): CharSequence {

            if(waterInMl == 0)
                return "Out of Water :<("

            if(waterInMl < 240){
                var sips = waterInMl.div(ML_IN_ONE_SIP_OF_WATER)
                sips = Math.max(sips, 1)
                return if (!withColoredText) getTextForSipsOfWaterInDialog(sips) else getColoredTextForSipsOfWaterInDialog(sips)
            }

            val waterGlasses = waterInMl.toFloat().div(ML_IN_ONE_GLASS_OF_WATER.toFloat())
            return if (!withColoredText) getTextForGlassesOfWaterInDialog(round(waterGlasses).toInt()) else getColoredTextForGlassesOfWaterInDialog(round(waterGlasses).toInt())
        }



        fun getWaterInMultipleOfFive(progress: Int): Int {
            return (progress + 4) / 5 * 5
        }

    }

}