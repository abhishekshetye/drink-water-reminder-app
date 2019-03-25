package com.codekage.explorify

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.codekage.explorify.activities.SettingsActivity
import com.codekage.explorify.core.WaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.MonthWaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.TodayWaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.WeekWaterConsumptionStatsGatherer
import com.codekage.explorify.core.database.DataHandler
import com.codekage.explorify.core.notification.NotificationHandler
import com.codekage.explorify.core.notification.NotificationHandler.Companion.setNotification
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sdsmdg.harjot.crollerTest.Croller
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {



    private var lineChart : LineChart? = null
    private var drinkWaterButton : Button ?= null
    private var todayStatsButton: Button ?= null
    private var weekStatsButton: Button ?= null
    private var monthStatsButton: Button ?= null
    private var waterDrankText: TextView ?= null
    private var outstandingWaterText: TextView ?= null
    private var avgWaterConsumptionText: TextView ?= null
    private var navigationButton: Button ?= null
    private var settingsButton: Button ?= null

    private var selectedStatsSelectableDrawable: Drawable?= null

    private var dataHandler: DataHandler ?= null
    private var monthsWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer ?= null
    private var weekWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer ?= null
    private var todayWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer ?= null
    private var currentWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer ?= null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        if(intent.extras!=null && intent.extras.getBoolean("FROM_NOTIFICATION"))
            setNotification(applicationContext, "Reminder to drink water", 700)

        initUIComponents()
        initDataHandlerAndStatsGatherer()
        setUpChartAxes(lineChart)
        setTodayGathererAsCurrentGathererAndPopulate()

        dataHandler?.getAllWaterConsumptionData()?.let { dataHandler?.printDataAsLogs(it) }
    }


    private fun setTodayGathererAsCurrentGathererAndPopulate(){
        removeHighlightedBorderOnButton()
        currentWaterConsumptionStatsGatherer = todayWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun removeHighlightedBorderOnButton() {
        todayStatsButton?.setBackgroundColor(Color.TRANSPARENT)
        weekStatsButton?.setBackgroundColor(Color.TRANSPARENT)
        monthStatsButton?.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setWeekGathererAsCurrentGathererAndPopulate(){
        removeHighlightedBorderOnButton()
        currentWaterConsumptionStatsGatherer = weekWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun setMonthGathererAsCurrentGathererAndPopulate(){
        removeHighlightedBorderOnButton()
        currentWaterConsumptionStatsGatherer = monthsWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer?){
        currentWaterConsumptionStatsGatherer?.putHighlightedBorderOnButton()
        fetchDataFromStatsGathererAndPopulateUI(currentWaterConsumptionStatsGatherer)
        var totalWaterDrank = currentWaterConsumptionStatsGatherer?.getTotalWaterDrankInMl()
        setCircularProgressBar(totalWaterDrank)
        waterDrankText?.text = "$totalWaterDrank ml"
        avgWaterConsumptionText?.text = "${totalWaterDrank?.div((currentWaterConsumptionStatsGatherer?.getDaysOffSet()?.plus(1)!!))} ml"
        var outStandingWater = getWaterDailyGoal().toFloat().minus(totalWaterDrank?.toFloat()!!)
        outStandingWater = if (outStandingWater <= 0) 0f else outStandingWater
        outStandingWaterText?.text = "$outStandingWater ml"
    }

    private fun fetchDataFromStatsGathererAndPopulateUI(statsGatherer: WaterConsumptionStatsGatherer?){
        var entries = statsGatherer?.fetchWaterEntries()
        Log.d("FETCH_DATA", "Data returned from SQLite ${entries?.size}")
        if(entries!=null && entries.isNotEmpty())
            entries.let { populateChart(it) }
    }

    private fun setCircularProgressBar(totalWaterDrank : Int?) {
        progressBar.progress = 0f
        progressBar.enableIndeterminateMode(true)
        object: CountDownTimer(2000, 2000){
            override fun onFinish() {
                runOnUiThread {
                    progressBar.enableIndeterminateMode(false)
                    if(getWaterDailyGoal() == 0)
                    {
                        Toast.makeText(applicationContext, "Daily water consumption goal cannot be zeroooo!", Toast.LENGTH_SHORT).show()
                    }else {
                        progressBar.progress = totalWaterDrank?.toFloat()!!.div(getWaterDailyGoal().toFloat()).times(100f)
                        Log.d("UI", "Water drank $totalWaterDrank and ${getWaterDailyGoal()} ml")
                    }
                }
            }
            override fun onTick(p0: Long) {}
        }.start()
    }

    override fun onResume() {
        super.onResume()
        if(currentWaterConsumptionStatsGatherer!=null)
            populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }


    private fun initUIComponents() {
        selectedStatsSelectableDrawable = ContextCompat.getDrawable(this, R.drawable.button_round_background)
        lineChart = this.findViewById(R.id.lineChart)
        drinkWaterButton = this.findViewById(R.id.drinkWaterButton)
        drinkWaterButton!!.setOnClickListener({
            var dialog = createInsertDialog()
            dialog.show()
        })
        todayStatsButton = this.findViewById(R.id.todayStatsButton)
        weekStatsButton = this.findViewById(R.id.weekStatsButton)
        monthStatsButton = this.findViewById(R.id.monthStatsButton)
        waterDrankText = this.findViewById(R.id.waterDrankText)
        avgWaterConsumptionText = this.findViewById(R.id.avgWaterConsumptionText)
        outstandingWaterText = this.findViewById(R.id.outStandingWaterText)
        settingsButton = this.findViewById(R.id.settings_button)
        navigationButton = this.findViewById(R.id.navigation_button)

        todayStatsButton!!.setOnClickListener({
            setTodayGathererAsCurrentGathererAndPopulate()
        })

        weekStatsButton!!.setOnClickListener({
            setWeekGathererAsCurrentGathererAndPopulate()
        })

        monthStatsButton!!.setOnClickListener({
            setMonthGathererAsCurrentGathererAndPopulate()
        })

        settingsButton!!.setOnClickListener({
            openSettingsActivity()
        })

        navigationButton!!.setOnClickListener({
            openNavigationDrawer()
        })
    }

    private fun openSettingsActivity() {
        var intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("OUTSTANDING_WATER", getOutStandingWater())
        startActivity(intent)
    }


    private fun getOutStandingWater() : Float {
        var totalWaterDrank = todayWaterConsumptionStatsGatherer?.getTotalWaterDrankInMl()
        var outStandingWater = getWaterDailyGoal().toFloat().minus(totalWaterDrank?.toFloat()!!)
        return if (outStandingWater <= 0) 0f else outStandingWater
    }

    private fun openNavigationDrawer() {
        drawer_layout.openDrawer(GravityCompat.START)
    }

    private fun createInsertDialog(): Dialog {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.insert_dialog)
        var text = dialog.findViewById<TextView>(R.id.water_text)
        var waterInput = dialog.findViewById<Croller>(R.id.croller)
        waterInput.progress = 200
        waterInput.setOnProgressChangedListener { progress ->
            text.text = "${getRoundedProgress(progress)} ml"
        }
        waterInput.labelColor = Color.WHITE

        var saveButton = dialog.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener({
            Log.d("SAVING", "Saving ${waterInput.progress} ml of water in the DB")
            val status = dataHandler?.saveWaterConsumption(getCurrentDateInString(), getRoundedProgress(waterInput.progress))
            Log.d("SAVING", "Done saving. Returned status is $status")
            Toast.makeText(applicationContext, "Saved entry", Toast.LENGTH_SHORT).show()
            populateUIWithWaterGathererStats(todayWaterConsumptionStatsGatherer)
            dialog.dismiss()
        })
        return dialog
    }

    private fun getRoundedProgress(progress: Int) : Int{
        return (progress + 4) / 5 * 5
    }

    private fun getCurrentDateInString() : String {
        var currentTime = Calendar.getInstance().time
        var simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        return simpleDateFormatter.format(currentTime)
    }

    private fun initDataHandlerAndStatsGatherer() {
        dataHandler = DataHandler(this)
        monthsWaterConsumptionStatsGatherer = MonthWaterConsumptionStatsGatherer(dataHandler!!, monthStatsButton)
        weekWaterConsumptionStatsGatherer = WeekWaterConsumptionStatsGatherer(dataHandler!!, weekStatsButton)
        todayWaterConsumptionStatsGatherer = TodayWaterConsumptionStatsGatherer(dataHandler!!, todayStatsButton)
    }

    private fun getWaterDailyGoal() : Int{
        val sharedPrefs = getSharedPreferences(resources.getString(R.string.prefs_name), Context.MODE_PRIVATE)
        return sharedPrefs.getInt(resources.getString(R.string.water_daily_goal), 2000)
    }

    private fun populateChart(entries : List<Entry> ){
        var dataSet = createDataSet(entries)
        var lineData = createLineData(dataSet)
        displayDataOnChart(lineData)
    }

    private fun displayDataOnChart(lineData: LineData) {
        lineChart?.data = lineData
        lineChart?.invalidate()
        lineChart?.animateY(500)
    }

    private fun createDataSet(entries: List<Entry>) : LineDataSet{
        var dataSet = LineDataSet(entries, "Water")
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.fillDrawable = ContextCompat.getDrawable(this, R.drawable.side_nav_bar)
        return dataSet
    }

    private fun createLineData(dataSet: LineDataSet) : LineData{
        var lineData = LineData(dataSet)
        lineData.isHighlightEnabled = true
        return lineData
    }

    private fun setUpChartAxes(lineChart: LineChart?) {

        val xAxis = lineChart?.xAxis
        xAxis?.position = XAxis.XAxisPosition.BOTTOM
        xAxis?.textSize = 10f

        lineChart?.axisLeft?.setDrawGridLines(false);
        lineChart?.xAxis?.setDrawGridLines(false);
        lineChart?.axisRight?.setDrawGridLines(false);

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationHandler.closeNotification(applicationContext)
    }
}
