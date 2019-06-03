package com.codekage.explorify.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.codekage.explorify.R
import com.codekage.explorify.core.utils.Formatter
import kotlinx.android.synthetic.main.activity_settings.*
import android.content.Intent
import android.net.Uri



class AboutMeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_me)
        val titleOfActivity = findViewById<TextView>(R.id.title)
        Formatter.setTypeFaceToProductSans(this, mutableListOf( titleOfActivity, notificationMainText, notificationSubText,
                setWaterLimitMainText, setWaterLimitSubText, informationMainText, informationSubText))

        waterLimitOptionTab.setOnClickListener {
            openPlayStoreAccount()
        }

        informationOptionTab.setOnClickListener {
            sendEmail()
        }

    }


    private fun openPlayStoreAccount(){
        val appPackageName = packageName // getPackageName() from Context or Activity object
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }

    }

    private fun sendEmail(){
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "abhi15kk@gmail.com", null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for WaterMeter App")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Here you go!")
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}
