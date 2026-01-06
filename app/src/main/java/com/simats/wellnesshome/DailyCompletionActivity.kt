package com.simats.wellnesshome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class DailyCompletionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_completion)

        val tvBalance = findViewById<TextView>(R.id.tvBalance)
        val btnGames = findViewById<AppCompatButton>(R.id.btnGames)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
        tvBalance.text = "Total Balance: $currentCoins Coins"

        btnGames.setOnClickListener {
            val intent = Intent(this, GamesActivity::class.java)
            // Clear all activities and start Games
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
