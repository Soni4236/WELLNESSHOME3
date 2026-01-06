package com.simats.wellnesshome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class SleepActivity : AppCompatActivity() {

    private lateinit var batteryLiquid: View
    private lateinit var batteryContainer: FrameLayout
    private lateinit var touchLayer: View
    private lateinit var tvHours: TextView
    private lateinit var ivMoon: ImageView
    private lateinit var seekBarQuality: SeekBar
    private lateinit var tvQualityLabel: TextView
    private lateinit var btnSubmit: AppCompatButton

    private var sleepHours = 7.0f // Default
    private var sleepQuality = 1 // 0=Poor, 1=Fair, 2=Excellent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        // Init Views
        batteryLiquid = findViewById(R.id.batteryLiquid)
        batteryContainer = findViewById(R.id.batteryContainer)
        touchLayer = findViewById(R.id.touchLayer)
        tvHours = findViewById(R.id.tvHours)
        
        ivMoon = findViewById(R.id.ivMoon)
        seekBarQuality = findViewById(R.id.seekBarQuality)
        tvQualityLabel = findViewById(R.id.tvQualityLabel)
        btnSubmit = findViewById(R.id.btnSubmit)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // Setup Battery Interaction
        setupBatteryTouch()

        // Setup Moon Slider
        setupMoonSlider()

        btnSubmit.setOnClickListener {
            saveSleepData()
        }
        
        // Init Battery Visuals
        batteryContainer.post {
            updateBatteryVisuals(sleepHours)
        }
    }

    private fun setupBatteryTouch() {
        touchLayer.setOnTouchListener { view, event ->
            val height = batteryContainer.height
            
            // Y coordinate is relative to view top (0), so bottom is height. 
            // We want fill from bottom.
            // Touch Y goes 0 (top) -> height (bottom).
            // Percentage filled = (height - event.y) / height
            
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Clamp y
                    var y = event.y
                    if (y < 0) y = 0f
                    if (y > height) y = height.toFloat()
                    
                    val filledPercent = (height - y) / height
                    // Map 0.0-1.0 to 0-12 hours
                    sleepHours = filledPercent * 12f
                    
                    updateBatteryVisuals(sleepHours)
                    true
                }
                else -> false
            }
        }
    }

    private fun updateBatteryVisuals(hours: Float) {
        // Update Text
        tvHours.text = String.format("%.1f hrs", hours)

        // Update Liquid Height
        val params = batteryLiquid.layoutParams as FrameLayout.LayoutParams
        val containerHeight = batteryContainer.height
        // Avoid 0 height crash if layout not ready, but we use post {}
        if (containerHeight > 0) {
            params.height = ((hours / 12f) * containerHeight).toInt()
            batteryLiquid.layoutParams = params
        }

        // Update Color
        val colorRes = when {
            hours < 4 -> android.R.color.holo_red_light
            hours < 7 -> android.R.color.holo_orange_light
            else -> android.R.color.holo_green_light
        }
        // Use a better color set if available, but standard android colors work for prototype
        // Or specific hex to match theme
        val colorHex = when {
            hours < 5 -> "#EF5350" // Red
            hours < 7 -> "#FFCA28" // Amber
            else -> "#66BB6A" // Green
        }
        batteryLiquid.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorHex))
    }

    private fun setupMoonSlider() {
        seekBarQuality.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sleepQuality = progress
                updateMoonVisuals()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateMoonVisuals() {
        when (sleepQuality) {
            0 -> {
                ivMoon.setImageResource(R.drawable.ic_moon_crescent)
                tvQualityLabel.text = "Poor Sleep"
            }
            1 -> {
                ivMoon.setImageResource(R.drawable.ic_moon_half)
                tvQualityLabel.text = "Fair Sleep"
            }
            2 -> {
                ivMoon.setImageResource(R.drawable.ic_moon_full)
                tvQualityLabel.text = "Excellent Sleep"
            }
        }
    }

    private fun saveSleepData() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putFloat("LATEST_SLEEP_HOURS", sleepHours)
            putInt("LATEST_SLEEP_QUALITY", sleepQuality)
            putLong("LATEST_SLEEP_TIMESTAMP", System.currentTimeMillis())
            apply()
        }

        CheckinManager.markSleepDone(this)
        CheckinManager.incrementSleepGoal(this, sleepHours)
        CheckinManager.updateGenericStreak(this)
        
        val intent = Intent(this, MoodSuccessActivity::class.java)
        intent.putExtra("NEXT_SCREEN", "BREATHING")
        startActivity(intent)
        finish()
    }
}
