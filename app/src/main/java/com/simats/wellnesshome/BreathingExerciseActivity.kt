package com.simats.wellnesshome

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class BreathingExerciseActivity : AppCompatActivity() {

    private lateinit var tvInstruction: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvCycleCount: TextView
    private lateinit var ivBreathingCircle: ImageView
    private lateinit var btnPause: AppCompatButton

    private var isRunning = false
    private var isPaused = false
    private var currentCycle = 0
    private val totalCycles = 5
    
    // State machine: 0=Inhale, 1=Hold, 2=Exhale, 3=Hold
    private var breathState = 0 
    private var secondsLeftInState = 4
    
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathing_exercise)

        tvInstruction = findViewById(R.id.tvInstruction)
        tvTimer = findViewById(R.id.tvTimer)
        tvCycleCount = findViewById(R.id.tvCycleCount)
        ivBreathingCircle = findViewById(R.id.ivBreathingCircle)
        btnPause = findViewById(R.id.btnPause)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            isRunning = false
            timerRunnable?.let { handler.removeCallbacks(it) }
            finish()
        }

        btnPause.setOnClickListener {
            togglePause()
        }

        startExercise()
    }

    private fun startExercise() {
        isRunning = true
        isPaused = false
        currentCycle = 1
        breathState = 0 // Inhale
        secondsLeftInState = 4
        updateUI()
        runGameLoop()
    }

    private fun togglePause() {
        isPaused = !isPaused
        btnPause.text = if (isPaused) "Resume" else "Pause"
        if (!isPaused) {
            runGameLoop()
        }
    }

    private fun runGameLoop() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (!isRunning) return
                if (isPaused) return

                if (secondsLeftInState > 0) {
                    tvTimer.text = secondsLeftInState.toString()
                    animateCircleForState(breathState, secondsLeftInState)
                    secondsLeftInState--
                } else {
                    // State finished, move to next
                    breathState++
                    if (breathState > 3) {
                        breathState = 0
                        currentCycle++
                        if (currentCycle > totalCycles) {
                            completeExercise()
                            return
                        }
                    }
                    
                    // Set Duration for next state
                    secondsLeftInState = when (breathState) {
                        0 -> 4 // Inhale
                        1 -> 4 // Hold
                        2 -> 4 // Exhale
                        3 -> 2 // Hold empty
                        else -> 4
                    }
                    updateUI()
                }
                
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun updateUI() {
        tvCycleCount.text = "Cycle $currentCycle of $totalCycles"
        val stateText = when (breathState) {
            0 -> "Inhale"
            1 -> "Hold"
            2 -> "Exhale"
            3 -> "Hold"
            else -> ""
        }
        tvInstruction.text = stateText
        tvTimer.text = secondsLeftInState.toString()
    }

    private fun animateCircleForState(state: Int, timeLeft: Int) {
        // Simple scale updates based on state
        // This is a rough approximation for smooth visual
        // Ideally we use ValueAnimator for smooth interpolation between seconds
        
        val scale = when (state) {
            0 -> 1.0f + (4 - timeLeft) * 0.125f // Grow 1.0 -> 1.5 over 4s
            1 -> 1.5f // Stay
            2 -> 1.5f - (4 - timeLeft) * 0.125f // Shrink 1.5 -> 1.0 over 4s
            3 -> 1.0f // Stay
            else -> 1.0f
        }
        
        ivBreathingCircle.animate().scaleX(scale).scaleY(scale).setDuration(1000).start()
    }

    private fun completeExercise() {
        isRunning = false
        tvInstruction.text = "Done!"
        tvTimer.text = "✓"
        
        // Award BIG Coins
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 125)
        val newCoins = currentCoins + 50 // Big Reward
        
        with(sharedPref.edit()) {
            putInt("COIN_BALANCE", newCoins)
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            putString("LAST_DAILY_ACTIVITY_DATE", todayDate)
            apply()
        }

        Toast.makeText(this, "Excellent! +50 Coins Awarded", Toast.LENGTH_LONG).show()

        handler.postDelayed({
            val intent = Intent(this, DailyCompletionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }, 1500)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
}
