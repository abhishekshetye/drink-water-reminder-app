package com.codekage.explorify.core.consumptions

import com.codekage.explorify.core.WaterConsumptionStatsGatherer
import com.codekage.explorify.core.database.DataHandler


/**
 * Created by abhisheksimac on 23/03/19.
 */

class WeekWaterConsumptionStatsGatherer (dataHandler: DataHandler) : WaterConsumptionStatsGatherer(dataHandler) {


    override fun getDaysOffSet(): Int {
        return 7
    }

}