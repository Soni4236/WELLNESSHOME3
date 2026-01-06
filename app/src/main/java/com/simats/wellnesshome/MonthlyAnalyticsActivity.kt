package com.simats.wellnesshome

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MonthlyAnalyticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_analytics)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }
}
