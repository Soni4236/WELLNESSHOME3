package com.simats.wellnesshome

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class WeeklyMoodTrendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_mood_trend)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<android.view.View>(R.id.btnViewMonthly).setOnClickListener {
            startActivity(android.content.Intent(this, MonthlyAnalyticsActivity::class.java))
        }
    }
}
