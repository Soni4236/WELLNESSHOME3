package com.simats.wellnesshome

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class StressMoodLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stress_mood_link)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }
}
