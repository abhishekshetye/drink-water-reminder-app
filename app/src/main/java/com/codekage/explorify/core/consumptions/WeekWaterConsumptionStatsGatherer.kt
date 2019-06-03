package com.codekage.explorify.core.consumptions

import android.widget.Button
import android.widget.TextView
import com.codekage.explorify.core.WaterConsumptionStatsGatherer
import com.codekage.explorify.core.database.DataHandler


/**
 * Created by abhisheksimac on 23/03/19.
 */

class WeekWaterConsumptionStatsGatherer(dataHandler: DataHandler, statsButton: Button?, pendingGlassesOfWaterTextView: TextView?) : WaterConsumptionStatsGatherer(dataHandler) {

    private var statsButton = statsButton
    private var pendingGlassesOfWaterTextView = pendingGlassesOfWaterTextView

    override fun getDaysOffSet(): Int {
        return 7
    }

    override fun getButtonForGatherer(): Button? {
        return statsButton
    }

    override fun populatePendingWaterTextView(water: Int) {
        pendingGlassesOfWaterTextView?.text = ""
    }

}