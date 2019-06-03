package com.codekage.explorify

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.codekage.explorify.activities.AboutMeActivity
import com.codekage.explorify.activities.SettingsActivity
import com.codekage.explorify.core.WaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.MonthWaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.TodayWaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.WeekWaterConsumptionStatsGatherer
import com.codekage.explorify.core.database.DataHandler
import com.codekage.explorify.core.notification.NotificationHandler
import com.codekage.explorify.core.notification.NotificationHandler.Companion.setNotification
import com.codekage.explorify.core.utils.Formatter
import com.codekage.explorify.core.utils.Formatter.Companion.getFirstCharaterRed
import com.codekage.explorify.core.utils.Formatter.Companion.getFormattedInteger
import com.codekage.explorify.core.utils.Formatter.Companion.getProdSansTypeFace
import com.codekage.explorify.core.utils.WaterCalculator.Companion.calculateWaterInTermsOfGlasses
import com.codekage.explorify.core.utils.WaterCalculator.Companion.getWaterInMultipleOfFive
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.sdsmdg.harjot.crollerTest.Croller
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var lineChart: LineChart? = null
    private var drinkWaterButton: Button? = null
    private var todayStatsButton: Button? = null
    private var weekStatsButton: Button? = null
    private var monthStatsButton: Button? = null
    private var waterDrankText: TextView? = null
    private var outstandingWaterText: TextView? = null
    private var avgWaterConsumptionText: TextView? = null
    private var navigationButton: Button? = null
    private var settingsButton: Button? = null
    private var pendingGlassesOfWaterTextView: TextView? = null

    private var selectedStatsSelectableDrawable: Drawable? = null

    private var dataHandler: DataHandler? = null
    private var monthsWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer? = null
    private var weekWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer? = null
    private var todayWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer? = null
    private var currentWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        if (intent.extras != null && intent.extras.getBoolean("FROM_NOTIFICATION"))
            setNotification(applicationContext, "Reminder to drink water", 700)

        initUIComponents()
        initDataHandlerAndStatsGatherer()
        setUpChartAxes(lineChart)
        setTodayGathererAsCurrentGathererAndPopulate()
        setNotificationForMorning()
        setNavBarClickListeners()

        dataHandler?.getAllWaterConsumptionData()?.let { dataHandler?.printDataAsLogs(it) }
    }

    override fun onResume() {
        super.onResume()
        if (currentWaterConsumptionStatsGatherer != null)
            populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    private fun initUIComponents() {
        selectedStatsSelectableDrawable = ContextCompat.getDrawable(this, R.drawable.button_round_background)
        lineChart = this.findViewById(R.id.lineChart)
        drinkWaterButton = this.findViewById(R.id.drinkWaterButton)
        drinkWaterButton!!.setOnClickListener {
            val dialog = createInsertDialog()
            dialog.show()
        }
        todayStatsButton = this.findViewById(R.id.todayStatsButton)
        weekStatsButton = this.findViewById(R.id.weekStatsButton)
        monthStatsButton = this.findViewById(R.id.monthStatsButton)
        waterDrankText = this.findViewById(R.id.waterDrankText)
        avgWaterConsumptionText = this.findViewById(R.id.avgWaterConsumptionText)
        outstandingWaterText = this.findViewById(R.id.outStandingWaterText)
        settingsButton = this.findViewById(R.id.settings_button)
        navigationButton = this.findViewById(R.id.navigation_button)
        pendingGlassesOfWaterTextView = this.findViewById(R.id.header_center_text_lower)

        todayStatsButton!!.setOnClickListener {
            setTodayGathererAsCurrentGathererAndPopulate()
        }

        weekStatsButton!!.setOnClickListener {
            setWeekGathererAsCurrentGathererAndPopulate()
        }

        monthStatsButton!!.setOnClickListener {
            setMonthGathererAsCurrentGathererAndPopulate()
        }

        settingsButton!!.setOnClickListener {
            openSettingsActivity()
        }

        navigationButton!!.setOnClickListener {
            openNavigationDrawer()
        }
    }

    private fun openSettingsActivity() {
        var intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("OUTSTANDING_WATER", getOutStandingWater())
        startActivity(intent)
    }


    private fun getOutStandingWater(): Float {
        val totalWaterDrank = todayWaterConsumptionStatsGatherer?.getTotalWaterDrankInMl()
        val outStandingWater = getWaterDailyGoal().toFloat().minus(totalWaterDrank?.toFloat()!!)
        return if (outStandingWater <= 0) 0f else outStandingWater
    }

    private fun openNavigationDrawer() {
        drawer_layout.openDrawer(GravityCompat.START)
    }

    private fun createInsertDialog(): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.insert_dialog)
        val glassesOfWaterText = dialog.findViewById<TextView>(R.id.glassesOfWaterText)
        val text = dialog.findViewById<TextView>(R.id.water_text)
        val waterInput = dialog.findViewById<Croller>(R.id.croller)
        waterInput.progress = 240
        waterInput.setOnProgressChangedListener { progress ->
            text.text = "${getWaterInMultipleOfFive(progress)} ml"
            val totalWaterText = calculateWaterInTermsOfGlasses(getWaterInMultipleOfFive(progress))
            glassesOfWaterText.text = "$totalWaterText"
        }
        waterInput.labelColor = Color.WHITE

        var saveButton = dialog.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            Log.d("SAVING", "Saving ${waterInput.progress} ml of water in the DB")
            val status = dataHandler?.saveWaterConsumption(getCurrentDateInString(), getWaterInMultipleOfFive(waterInput.progress))
            Log.d("SAVING", "Done saving. Returned status is $status")
            Toast.makeText(applicationContext, "Saved entry", Toast.LENGTH_SHORT).show()
            populateUIWithWaterGathererStats(todayWaterConsumptionStatsGatherer)
            dialog.dismiss()
        }
        return dialog
    }

    private fun getCurrentDateInString(): String {
        val currentTime = Calendar.getInstance().time
        val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        return simpleDateFormatter.format(currentTime)
    }

    private fun initDataHandlerAndStatsGatherer() {
        dataHandler = DataHandler(this)
        monthsWaterConsumptionStatsGatherer = MonthWaterConsumptionStatsGatherer(dataHandler!!, monthStatsButton, pendingGlassesOfWaterTextView)
        weekWaterConsumptionStatsGatherer = WeekWaterConsumptionStatsGatherer(dataHandler!!, weekStatsButton, pendingGlassesOfWaterTextView)
        todayWaterConsumptionStatsGatherer = TodayWaterConsumptionStatsGatherer(dataHandler!!, todayStatsButton, pendingGlassesOfWaterTextView)
    }

    private fun getWaterDailyGoal(): Int {
        val sharedPrefs = getSharedPreferences(resources.getString(R.string.prefs_name), Context.MODE_PRIVATE)
        return sharedPrefs.getInt(resources.getString(R.string.water_daily_goal), 2000)
    }

    private fun populateChart(entries: List<Entry>) {
        var dataSet = createDataSet(entries)
        var lineData = createLineData(dataSet)
        displayDataOnChart(lineData, entries.size)
    }

    private fun displayDataOnChart(lineData: LineData, entriesSize : Int) {
        lineChart?.data = lineData
        lineChart?.setDrawBorders(false)
        lineChart?.moveViewToX(entriesSize.toFloat())
        lineChart?.invalidate()
        lineChart?.setVisibleXRangeMaximum(4f)
        lineChart?.animateY(500)
    }

    private fun createDataSet(entries: List<Entry>): LineDataSet {
        var dataSet = LineDataSet(entries, "Water")
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.setCircleColor(resources.getColor(R.color.darkThemeRed))
        dataSet.circleHoleColor = resources.getColor(R.color.darkThemeRed)
        dataSet.lineWidth = 0f
        dataSet.setDrawHighlightIndicators(false)
        dataSet.valueFormatter = DefaultValueFormatter(0)
        dataSet.fillDrawable = ContextCompat.getDrawable(this, R.drawable.button_focus)
        return dataSet
    }

    private fun createLineData(dataSet: LineDataSet): LineData {
        var lineData = LineData(dataSet)
        lineData.isHighlightEnabled = true
        return lineData
    }

    private fun setUpChartAxes(lineChart: LineChart?) {
        val xAxis = lineChart?.xAxis
        xAxis?.position = XAxis.XAxisPosition.BOTTOM
        xAxis?.textSize = 10f
        xAxis?.setDrawAxisLine(false)
        lineChart?.xAxis?.setDrawGridLines(false)
        var description = Description()
        description.text = ""
        lineChart?.description = description
        lineChart?.contentDescription = ""
        lineChart?.axisLeft?.axisLineColor = resources.getColor(R.color.darkThemeLightText1)
        lineChart?.axisLeft?.textColor = resources.getColor(R.color.darkThemeLightText1)
        lineChart?.xAxis?.axisLineColor = resources.getColor(R.color.darkThemeLightText1)
        lineChart?.xAxis?.textColor = resources.getColor(R.color.darkThemeLightText1)
        lineChart?.axisRight?.setDrawAxisLine(false)
        lineChart?.axisRight?.setDrawTopYLabelEntry(false)
        lineChart?.axisRight?.setDrawLabels(false)
        lineChart?.axisRight?.setLabelCount(2, true)
        lineChart?.axisLeft?.setDrawGridLines(false)
        lineChart?.axisLeft?.setDrawAxisLine(false)
        lineChart?.axisLeft?.labelCount = 3
        lineChart?.legend?.isEnabled = false
        lineChart?.isAutoScaleMinMaxEnabled = true;
        lineChart?.axisRight?.setDrawGridLines(false)
    }


    private fun openAboutMeActivity() {
        val aboutMeActivityIntent = Intent(this, AboutMeActivity::class.java)
        startActivity(aboutMeActivityIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationHandler.closeNotification(applicationContext)
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun setNotificationForMorning() {
        NotificationHandler.setReminderToDrinkWaterEveryMorning(this)
    }


    private fun setTodayGathererAsCurrentGathererAndPopulate() {
        removeHighlightedBorderOnButton()
        currentWaterConsumptionStatsGatherer = todayWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun removeHighlightedBorderOnButton() {
        todayStatsButton?.setBackgroundColor(Color.TRANSPARENT)
        weekStatsButton?.setBackgroundColor(Color.TRANSPARENT)
        monthStatsButton?.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setWeekGathererAsCurrentGathererAndPopulate() {
        removeHighlightedBorderOnButton()
        currentWaterConsumptionStatsGatherer = weekWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun setMonthGathererAsCurrentGathererAndPopulate() {
        removeHighlightedBorderOnButton()
        currentWaterConsumptionStatsGatherer = monthsWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer?) {
        currentWaterConsumptionStatsGatherer?.putHighlightedBorderOnButton()
        fetchDataFromStatsGathererAndPopulateUI(currentWaterConsumptionStatsGatherer)
        val totalWaterDrank = currentWaterConsumptionStatsGatherer?.getTotalWaterDrankInMl()
        val avgWaterConsumption = totalWaterDrank?.div((currentWaterConsumptionStatsGatherer.getDaysOffSet().plus(1)))
        var outStandingWater = getWaterDailyGoal().toFloat().minus(totalWaterDrank?.toFloat()!!)
        outStandingWater = if (outStandingWater <= 0) 0f else outStandingWater

        setCircularProgressBar(totalWaterDrank, currentWaterConsumptionStatsGatherer)
        populateTotalWaterDrank(totalWaterDrank)
        populateAverageWaterDrank(avgWaterConsumption, outStandingWater.toInt())

        currentWaterConsumptionStatsGatherer.populatePendingWaterTextView(outStandingWater.toInt())
    }

    private fun populateAverageWaterDrank(avgWaterConsumption: Int?, outStandingWater: Int?) {
        avgWaterConsumptionText?.text = "${getFormattedInteger(avgWaterConsumption)} ml"
        outStandingWaterText?.text = "${getFormattedInteger(outStandingWater)} ml"
    }

    private fun populateTotalWaterDrank(totalWaterDrank: Int?) {
        val waterDrankInStr = getFormattedInteger(totalWaterDrank).toString()
        waterDrankText?.text = "$waterDrankInStr ml"
    }

    private fun fetchDataFromStatsGathererAndPopulateUI(statsGatherer: WaterConsumptionStatsGatherer?) {
        var entries = statsGatherer?.fetchWaterEntries()
        Log.d("FETCH_DATA", "Data returned from SQLite ${entries?.size}")
        if (entries != null && entries.isNotEmpty())
            entries.let { populateChart(it) }
    }

    private fun setCircularProgressBar(totalWaterDrank: Int?, currentWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer?) {
        progressBar.progress = 0f
        progressBar.enableIndeterminateMode(true)
        object : CountDownTimer(2000, 2000) {
            override fun onFinish() {
                runOnUiThread {
                    progressBar.enableIndeterminateMode(false)
                    if (getWaterDailyGoal() == 0) {
                        Toast.makeText(applicationContext, "Daily water consumption goal cannot be zeroooo!", Toast.LENGTH_SHORT).show()
                    } else {
                        var days = currentWaterConsumptionStatsGatherer?.getDaysOffSet()!!.plus(1)
                        progressBar.progress = totalWaterDrank?.toFloat()!!.div(getWaterDailyGoal().toFloat().times(days)).times(100f)
                        Log.d("UI", "Water drank $totalWaterDrank and ${getWaterDailyGoal()} ml")
                    }
                }
            }

            override fun onTick(p0: Long) {}
        }.start()
    }


    private fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "You know human body is made up of 70% water and your body needs to maintain the water level every single day. " +
                        "\nCheckout WaterMeter to track daily water consumption - https://play.google.com/store/apps/details?id=com.codekage.explorify !")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun setNavBarClickListeners() {
        firstNotificationButton.setOnClickListener {
            openSettingsActivity()
        }

        secondNotificationButton.setOnClickListener{
            shareApp()
        }

        thirdNotificationButton.setOnClickListener{
            openAboutMeActivity()
        }
    }

}
