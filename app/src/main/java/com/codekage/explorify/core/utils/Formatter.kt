package com.codekage.explorify.core.utils

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.widget.TextView
import java.text.DecimalFormat
import java.util.*

/**
 * Created by abhisheksimac on 31/03/19.
 */
class Formatter{

    companion object {

        private const val TYPEFACE_NAME = "prodsans.ttf"
        private const val TYPEFACE_PATH = "fonts/%s"


        fun getTextForGlassesOfWaterInDialog(glasses: Int) : CharSequence {
            return if (glasses <= 1) "$glasses Glass of Water" else "$glasses Glasses of Water"
        }

        fun getColoredTextForGlassesOfWaterInDialog(glasses: Int) : CharSequence {
            return if (glasses <= 1) "<font color='#ffffff'>~$glasses</font> Glass of Water" else "<font color='#ffffff'>~$glasses</font> Glasses of Water"
        }


        fun getTextForSipsOfWaterInDialog(sips: Int): CharSequence {
            return if (sips <= 1) "$sips Sip of Water" else "$sips Sips of Water"
        }

        fun getColoredTextForSipsOfWaterInDialog(sips: Int): CharSequence {
            return if (sips <= 1) "<font color='#ffffff'>~$sips</font> Sip of Water" else "<font color='#ffffff'>~$sips</font> Sips of Water"
        }

        fun getFormattedInteger(number: Int?): CharSequence? {
            val formatter = DecimalFormat("#,###,###")
            return formatter.format(number)
        }

        fun getFirstCharaterRed(text: String) : String {
            val stringBuffer = StringBuffer()
            var first = true
            stringBuffer.append("<font color='#F44336'>")
            for(alphabet in text.toCharArray()){
                stringBuffer.append(alphabet)
                if(first){
                    stringBuffer.append("</font>")
                    first = false
                }
            }
            return stringBuffer.toString()
        }

        //it's not allowed to use Product Sans font
        fun setTypeFaceToProductSans(context: Context, textViews: List<TextView?> ){
            val typeFace = getProdSansTypeFace(context)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//                textViews.stream().forEach { tv -> tv?.typeface = typeFace }
//            else
//                for (tv in textViews)
//                    tv?.typeface = typeFace

        }

        fun getProdSansTypeFace(context: Context): Typeface? {
            val assetManager = context.applicationContext.assets
            return Typeface.createFromAsset(assetManager, String.format(Locale.US, TYPEFACE_PATH, TYPEFACE_NAME))
        }

    }

}