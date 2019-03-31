package com.codekage.explorify.activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.codekage.explorify.R
import com.codekage.explorify.core.notification.NotificationHandler.Companion.setNotification
import com.codekage.explorify.core.notification.NotificationHandler.Companion.setReminderToDrinkWaterEveryCoupleHours
import com.sdsmdg.harjot.crollerTest.Croller
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private var outStandingWater: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        outStandingWater = intent?.extras?.getFloat("OUTSTANDING_WATER")!!

        updateUI()

        waterLimitOptionTab.setOnClickListener {
            var dialog = createWaterLimitInputDialog()
            dialog.show()
        }

        notificationOptionTab.setOnClickListener({
            setNotification(applicationContext, "$outStandingWater ml water outstanding for today's goal")
            Toast.makeText(this, "Notification set", Toast.LENGTH_SHORT).show()
        })

        reminderOptionTab.setOnClickListener({
            var featureEnabled = toggleReminderSetting()
            if (featureEnabled) {
                setReminderToDrinkWaterEveryCoupleHours(applicationContext)
                Toast.makeText(this, "Alarm set for every 2 hours", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Alarm disabled", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun createWaterLimitInputDialog(): Dialog {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.set_water_goal_dialog)
        val text = dialog.findViewById<TextView>(R.id.water_text)
        val waterInput = dialog.findViewById<Croller>(R.id.croller)
        waterInput.progress = 2000
        waterInput.label = "Daily Water Consumption Goal"
        waterInput.setOnProgressChangedListener { progress ->
            text.text = "${getRoundedProgress(progress)} ml"
        }
        waterInput.labelColor = Color.WHITE

        var saveButton = dialog.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener({
            Log.d("SAVING", "Setting ${waterInput.progress} ml of water as daily goal")
            var savedSuccessfully = saveWaterConsumptionGoalInSharedPrefs(getRoundedProgress(waterInput.progress))
            if (savedSuccessfully) {
                Toast.makeText(applicationContext, "Saved daily water consumption goal as ${getRoundedProgress(waterInput.progress)} ml", Toast.LENGTH_SHORT).show()
                updateUI()
                dialog.dismiss()
            }
        })
        return dialog
    }


    private fun getRoundedProgress(progress: Int): Int {
        return (progress + 4) / 5 * 5
    }

    private fun updateUI() {
        setWaterLimitSubText.text = "${getWaterDailyGoal()} ml"
    }

    private fun saveWaterConsumptionGoalInSharedPrefs(progress: Int): Boolean {
        if (progress <= 500) {
            Toast.makeText(applicationContext, "$progress ml is way too less as daily goal. Please refer information tab to know regarding your daily limit.", Toast.LENGTH_SHORT).show()
            return false
        }
        val sharedPrefs = getSharedPreferences(resources.getString(R.string.prefs_name), Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putInt(resources.getString(R.string.water_daily_goal), progress)
        editor.apply()
        return true
    }

    private fun getWaterDailyGoal(): Int {
        val sharedPrefs = getSharedPreferences(resources.getString(R.string.prefs_name), Context.MODE_PRIVATE)
        return sharedPrefs.getInt(resources.getString(R.string.water_daily_goal), 2000)
    }

    private fun toggleReminderSetting(): Boolean {
        val sharedPrefs = getSharedPreferences(resources.getString(R.string.prefs_name), Context.MODE_PRIVATE)
        var featureEnabled = sharedPrefs.getBoolean(resources.getString(R.string.reminderEnabled), false)
        val editor = sharedPrefs.edit()
        editor.putBoolean(resources.getString(R.string.reminderEnabled), !featureEnabled)
        editor.apply()
        return !featureEnabled
    }
}
