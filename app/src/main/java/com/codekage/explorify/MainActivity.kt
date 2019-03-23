package com.codekage.explorify

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.codekage.explorify.core.WaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.MonthWaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.TodayWaterConsumptionStatsGatherer
import com.codekage.explorify.core.consumptions.WeekWaterConsumptionStatsGatherer
import com.codekage.explorify.core.database.DataHandler
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sdsmdg.harjot.crollerTest.Croller
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
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

        initDataHandlerAndStatsGatherer()
        initUIComponents()
        setUpChartAxes(lineChart)
        setTodayGathererAsCurrentGathererAndPopulate()

        dataHandler?.getAllWaterConsumptionData()?.let { dataHandler?.printDataAsLogs(it) }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }


    private fun setTodayGathererAsCurrentGathererAndPopulate(){
        removeSelectedResource()
        todayStatsButton?.setBackgroundResource(R.drawable.button_round_background)
        currentWaterConsumptionStatsGatherer = todayWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun removeSelectedResource() {
        todayStatsButton?.setBackgroundColor(Color.TRANSPARENT)
        weekStatsButton?.setBackgroundColor(Color.TRANSPARENT)
        monthStatsButton?.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setWeekGathererAsCurrentGathererAndPopulate(){
        removeSelectedResource()
        weekStatsButton?.setBackgroundResource(R.drawable.button_round_background)
        currentWaterConsumptionStatsGatherer = weekWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun setMonthGathererAsCurrentGathererAndPopulate(){
        removeSelectedResource()
        monthStatsButton?.setBackgroundResource(R.drawable.button_round_background)
        currentWaterConsumptionStatsGatherer = monthsWaterConsumptionStatsGatherer
        populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer)
    }

    private fun populateUIWithWaterGathererStats(currentWaterConsumptionStatsGatherer: WaterConsumptionStatsGatherer?){
        fetchDataFromStatsGathererAndPopulateUI(currentWaterConsumptionStatsGatherer)
        var totalWaterDrank = currentWaterConsumptionStatsGatherer?.getTotalWaterDrankInMl()
        waterDrankText?.text = "$totalWaterDrank ml"
        avgWaterConsumptionText?.text = "${totalWaterDrank?.div((currentWaterConsumptionStatsGatherer?.getDaysOffSet()?.plus(1)!!))} ml"
    }

    private fun fetchDataFromStatsGathererAndPopulateUI(statsGatherer: WaterConsumptionStatsGatherer?){
        var entries = statsGatherer?.fetchWaterEntries()
        Log.d("FETCH_DATA", "Data returned from SQLite ${entries?.size}")
        if(entries!=null && entries.isNotEmpty())
            entries?.let { populateChart(it) }
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

        todayStatsButton!!.setOnClickListener(View.OnClickListener {
            setTodayGathererAsCurrentGathererAndPopulate()
        })

        weekStatsButton!!.setOnClickListener(View.OnClickListener {
            setWeekGathererAsCurrentGathererAndPopulate()
        })

        monthStatsButton!!.setOnClickListener(View.OnClickListener {
            setMonthGathererAsCurrentGathererAndPopulate()
        })
    }

    private fun createInsertDialog(): Dialog {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.insert_dialog)
        var text = dialog.findViewById<TextView>(R.id.water_text)
        var waterInput = dialog.findViewById<Croller>(R.id.croller)
        waterInput.progress = 200
        waterInput.setOnProgressChangedListener { progress ->
            text.text = "$progress ml"
        }
        waterInput.labelColor = Color.WHITE

        var saveButton = dialog.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener({
            Log.d("SAVING", "Saving ${waterInput.progress} ml of water in the DB")
            val status = dataHandler?.saveWaterConsumption(getCurrentDateInString(), waterInput.progress)
            Log.d("SAVING", "Done saving. Returned status is $status")
            Toast.makeText(applicationContext, "Saved entry", Toast.LENGTH_SHORT).show()
            fetchDataFromStatsGathererAndPopulateUI(todayWaterConsumptionStatsGatherer)
            dialog.dismiss()
        })
        return dialog
    }

    private fun getCurrentDateInString() : String {
        var currentTime = Calendar.getInstance().time
        var simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        return simpleDateFormatter.format(currentTime)
    }

    private fun initDataHandlerAndStatsGatherer() {
        dataHandler = DataHandler(this)
        monthsWaterConsumptionStatsGatherer = MonthWaterConsumptionStatsGatherer(dataHandler!!)
        weekWaterConsumptionStatsGatherer = WeekWaterConsumptionStatsGatherer(dataHandler!!)
        todayWaterConsumptionStatsGatherer = TodayWaterConsumptionStatsGatherer(dataHandler!!)
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
        lineData?.isHighlightEnabled = true
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
}
