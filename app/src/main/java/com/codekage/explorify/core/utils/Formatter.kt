package com.codekage.explorify.core.utils

/**
 * Created by abhisheksimac on 31/03/19.
 */
class Formatter{

    companion object {

        fun getTextForGlassesOfWaterInDialog(glasses: Int) : CharSequence {
            return if (glasses <= 1) "$glasses glass of water" else "$glasses glasses of water"
        }

    }

}