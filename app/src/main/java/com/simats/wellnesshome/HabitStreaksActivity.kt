package com.simats.wellnesshome

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HabitStreaksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_streaks)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<androidx.cardview.widget.CardView>(R.id.cardMoodStreak).setOnClickListener {
            startActivity(android.content.Intent(this, WeeklyMoodTrendActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardActivityStreak).setOnClickListener {
             // For now, linking to Monthly Analytics as it's the catch-all "next" detailed view avail
             // In future, this could be a specific Daily Activity log
            startActivity(android.content.Intent(this, MonthlyAnalyticsActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardSleepStreak).setOnClickListener {
            startActivity(android.content.Intent(this, SleepMoodLinkActivity::class.java))
        }

        // New Stress Card Listener
        findViewById<androidx.cardview.widget.CardView>(R.id.cardStressStreak).setOnClickListener {
            startActivity(android.content.Intent(this, StressMoodLinkActivity::class.java))
        }

        setupHeatmap()
    }

    private fun setupHeatmap() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayoutHeatmap)
        
        // Populate 35 cells (5 weeks)
        // Simulate random activity levels
        val colors = listOf(
            "#E0E0E0", // Empty
            "#C8E6C9", // Low
            "#A5D6A7", // Madeium
            "#81C784", // High
            "#66BB6A"  // Very High
        )

        for (i in 0 until 35) {
            val cell = View(this)
            
            // Randomly pick intensity
            // More likely to be active
            val intensity = if (Math.random() > 0.3) (1..4).random() else 0
            
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 80 // px approx
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(8, 8, 8, 8)
            cell.layoutParams = params

            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadius = 12f
            shape.setColor(Color.parseColor(colors[intensity]))
            cell.background = shape

            gridLayout.addView(cell)
        }
    }
}
