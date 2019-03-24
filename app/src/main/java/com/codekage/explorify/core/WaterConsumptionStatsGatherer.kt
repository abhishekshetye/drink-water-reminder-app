package com.codekage.explorify.core

import android.os.Build
import android.util.Log
import android.widget.Button
import com.codekage.explorify.R
import com.codekage.explorify.core.database.DataHandler
import com.codekage.explorify.core.entities.WaterConsumed
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

/**
 * Created by abhisheksimac on 23/03/19.
 */

abstract class WaterConsumptionStatsGatherer (dataHandler: DataHandler) {


    private val dataHandler = dataHandler


    internal abstract fun getDaysOffSet(): Int

    internal abstract fun getButtonForGatherer() : Button?

    fun putHighlightedBorderOnButton() {
        Log.d("UI", "Putting highlighter on button ${getButtonForGatherer()}")
        getButtonForGatherer()?.setBackgroundResource(R.drawable.button_round_background)
    }


    fun fetchWaterEntries(): List<Entry> {
        var waterConsumptionList =  if (getStartDate() == getEndDate())  this.dataHandler.getAllWaterConsumptionData()
                                    else this.dataHandler.getWaterConsumptionByDate(getStartDate(), getEndDate())
        return convertListToEntries(waterConsumptionList)
    }


    fun getTotalWaterDrankInMl() : Int{
        var waterConsumptionList =  if (getStartDate() == getEndDate())  this.dataHandler.getAllWaterConsumptionData()
                                    else this.dataHandler.getWaterConsumptionByDate(getStartDate(), getEndDate())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return waterConsumptionList.stream().mapToInt { wc -> wc.water }.sum()
        }

        var sum=0
        for(wc in waterConsumptionList)
            sum += wc.water
        return sum
    }


    private fun convertListToEntries(waterConsumptionList: List<WaterConsumed>): List<Entry> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return waterConsumptionList.stream()
                    .map { wc -> Entry(parseDateFromString(wc.date), wc.water.toFloat()) }
                    .collect(Collectors.toList())
        }

        val entries = ArrayList<Entry>()
        for(wc in waterConsumptionList){
            entries.add(Entry(wc.id.toFloat(), wc.water.toFloat()))
        }
        return entries
    }


    private fun formatDate(date: Date): String {
        val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormatter.format(date)
    }

    private fun parseDateFromString(dateStr: String): Float {
        val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd")
        Log.d("UI", "Parsed date ${simpleDateFormatter.parse(dateStr).time.toFloat()}")
        return simpleDateFormatter.parse(dateStr).time.toFloat()
    }

    private fun getStartDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1 * getDaysOffSet())
        val oldDate = cal.time
        return formatDate(oldDate)
    }

    private fun getEndDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 1)
        val oldDate = cal.time
        return formatDate(oldDate)
    }

}