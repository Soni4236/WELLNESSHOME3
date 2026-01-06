package com.simats.wellnesshome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class StressActivity : AppCompatActivity() {

    private lateinit var ivStressSphere: ImageView
    private lateinit var tvStressLabel: TextView
    private lateinit var layoutTrigger: View
    private lateinit var spinnerTrigger: Spinner
    private lateinit var btnSubmit: AppCompatButton
    
    private var stressLevel = 1
    private var selectedTrigger: String = "Select a trigger"
    private var isLocked = false
    private var startY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stress)

        ivStressSphere = findViewById(R.id.ivStressSphere)
        tvStressLabel = findViewById(R.id.tvStressLabel)
        layoutTrigger = findViewById(R.id.layoutTrigger)
        spinnerTrigger = findViewById(R.id.spinnerTrigger)
        btnSubmit = findViewById(R.id.btnSubmit)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        setupSpinner()
        
        btnSubmit.setOnClickListener {
            saveStressData()
        }

        // Detect touch on the root view (or specific container)
        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            if (isLocked) return@setOnTouchListener false

            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    val deltaY = startY - event.y // Dragging UP increases value
                    updateStressVisuals(deltaY)
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    lockSelection()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateStressVisuals(dragDistance: Float) {
        // Map drag distance (pixels) to Stress 1-10
        // Assume ~1000px is full range for now, adjust sensitivity
        val maxDrag = 1000f
        val clampedDrag = dragDistance.coerceIn(0f, maxDrag)
        
        // 1 to 10
        stressLevel = 1 + ((clampedDrag / maxDrag) * 9).toInt()
        
        // Scale 1.0 to 2.5
        val scale = 1.0f + (clampedDrag / maxDrag) * 1.5f
        ivStressSphere.scaleX = scale
        ivStressSphere.scaleY = scale

        // Color Interpolation
        // 1-3: Blue (#81D4FA) -> 4-7: Purple (#9575CD) -> 8-10: Red (#FF7043)
        val color = when {
            stressLevel <= 3 -> android.graphics.Color.parseColor("#81D4FA")
            stressLevel <= 7 -> android.graphics.Color.parseColor("#9575CD")
            else -> android.graphics.Color.parseColor("#FF7043")
        }
        ivStressSphere.setColorFilter(color)

        // Labels
        tvStressLabel.text = when {
            stressLevel <= 3 -> "Calm"
            stressLevel <= 6 -> "Tense"
            stressLevel <= 8 -> "Stressed"
            else -> "Overwhelmed"
        }

        // Shake/Vibration for high stress
        if (stressLevel >= 8) {
            val shake = (Math.random().toFloat() - 0.5f) * 10f * (stressLevel / 10f)
            ivStressSphere.translationX = shake
            ivStressSphere.translationY = shake
        } else {
            ivStressSphere.translationX = 0f
            ivStressSphere.translationY = 0f
        }
    }

    private fun lockSelection() {
        isLocked = true
        tvStressLabel.text = "Score: $stressLevel/10"
        
        // Reset translation
        ivStressSphere.translationX = 0f
        ivStressSphere.translationY = 0f

        // Show Trigger Selection
        layoutTrigger.visibility = View.VISIBLE
        layoutTrigger.alpha = 0f
        layoutTrigger.animate().alpha(1f).setDuration(500).start()
    }

    private fun setupSpinner() {
        val triggers = arrayOf("Select a trigger", "Work", "Family", "Money", "Health", "Education", "Social", "Time Management", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, triggers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTrigger.adapter = adapter

        spinnerTrigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTrigger = triggers[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun saveStressData() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putInt("LATEST_STRESS_LEVEL", stressLevel)
            putString("LATEST_STRESS_TRIGGER", selectedTrigger)
            putLong("LATEST_STRESS_TIMESTAMP", System.currentTimeMillis())
            apply()
        }

        CheckinManager.markStressDone(this)
        
        val intent = Intent(this, MoodSuccessActivity::class.java)
        intent.putExtra("NEXT_SCREEN", "DASHBOARD")
        startActivity(intent)
        finish()
    }
}
