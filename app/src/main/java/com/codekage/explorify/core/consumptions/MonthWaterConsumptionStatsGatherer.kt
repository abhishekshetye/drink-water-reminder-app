package com.codekage.explorify.core.consumptions

import android.widget.Button
import com.codekage.explorify.R
import com.codekage.explorify.core.WaterConsumptionStatsGatherer
import com.codekage.explorify.core.database.DataHandler



/**
 * Created by abhisheksimac on 23/03/19.
 */

class MonthWaterConsumptionStatsGatherer(dataHandler: DataHandler, statsButton: Button?) : WaterConsumptionStatsGatherer(dataHandler) {

    var statsButton = statsButton

    override fun getDaysOffSet(): Int {
        return 30
    }


    override fun getButtonForGatherer(): Button? {
        return statsButton
    }


}