package com.simats.wellnesshome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView

    private lateinit var tvCoinBalance: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvUserName = findViewById(R.id.tvUserName)
        tvCoinBalance = findViewById(R.id.tvCoinBalance)

        // Retrieve user name from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("FULL_NAME", "User")
        tvUserName.text = fullName

        setupNavigation()
        setupDailyActivity()
        updateCoinDisplay()
    }

    override fun onResume() {
        super.onResume()
        updateCoinDisplay()
    }

    private fun updateCoinDisplay() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 125)
        tvCoinBalance.text = "$currentCoins Coins"
    }

    private fun setupDailyActivity() {
        findViewById<android.widget.LinearLayout>(R.id.cardDailyActivity).setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val lastDate = sharedPref.getString("LAST_DAILY_ACTIVITY_DATE", "")
            
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            
            // For testing purposes, we can allow re-entry or keep the check.
            // User flow: Dashboard -> Intro -> Activity -> Games.
            // If completed today, maybe just go straight to Intro (or Games)? 
            // Let's allow them to do the breathing again but maybe warn about no coins?
            // User request implied flow: Checkins -> Breathing -> Games.
            
            // To ensure they see the flow, we launch Intro. 
            // The Coin Awarding logic will handle the "Already collected" check inside Activity if needed,
            // or we just let them do it and overwrite date.
            
            val intent = Intent(this, DailyActivitiesWelcomeActivity::class.java)
            startActivity(intent)
        }




        // Rewards Store link
        findViewById<android.view.View>(R.id.cardRewardsStore).setOnClickListener {
            startActivity(Intent(this, RewardsStoreActivity::class.java))
        }
    }
    private fun setupNavigation() {
        findViewById<android.widget.ImageView>(R.id.ivSettings).setOnClickListener {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.cardMood).setOnClickListener {
            startActivity(android.content.Intent(this, MoodActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.cardStress).setOnClickListener {
            startActivity(android.content.Intent(this, StressActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.cardSleep).setOnClickListener {
            startActivity(android.content.Intent(this, SleepActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.cardGames).setOnClickListener {
            startActivity(android.content.Intent(this, GamesActivity::class.java))
        }
    }
}
