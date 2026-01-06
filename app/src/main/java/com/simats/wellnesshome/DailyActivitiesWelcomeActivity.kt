package com.simats.wellnesshome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class DailyActivitiesWelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_welcome)

        // Delay for 1.5 seconds then move to Stats
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, GameStatisticsActivity::class.java))
            finish()
        }, 1500)
    }
}
