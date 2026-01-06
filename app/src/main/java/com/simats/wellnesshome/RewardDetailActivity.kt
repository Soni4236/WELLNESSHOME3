package com.simats.wellnesshome

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RewardDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward_detail)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val btnRedeem = findViewById<android.widget.Button>(R.id.btnRedeem)
        val tvUserBalance = findViewById<TextView>(R.id.tvUserBalance)

        // Get current coins
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 125)
        tvUserBalance.text = "$currentCoins Coins"

        // Get badge details from Intent
        val badgeId = intent.getStringExtra("BADGE_ID") ?: "UNKNOWN"
        val badgeName = intent.getStringExtra("BADGE_NAME") ?: "Unknown Badge"
        val badgeDesc = intent.getStringExtra("BADGE_DESC") ?: "Description not available"
        val badgeIconRes = intent.getIntExtra("BADGE_ICON", R.drawable.ic_lock)
        val cost = intent.getIntExtra("BADGE_COST", 50)

        findViewById<TextView>(R.id.tvRewardTitle).text = badgeName
        findViewById<TextView>(R.id.tvRewardDescription).text = badgeDesc
        findViewById<android.widget.ImageView>(R.id.ivRewardIcon).setImageResource(badgeIconRes)
        findViewById<TextView>(R.id.tvRewardCost).text = "$cost Coins"

        // --- Unlock Criteria Check ---
        var isUnlockable = true
        var progressText = ""
        
        when(badgeId) {
            "7_DAY_WARRIOR" -> {
                val streak = CheckinManager.getStreak(this)
                val target = 7
                isUnlockable = streak >= target
                progressText = "Streak: $streak / $target Days"
            }
            "MOOD_MASTER" -> {
                val moodCount = CheckinManager.getMoodCount(this)
                val target = 10
                isUnlockable = moodCount >= target
                progressText = "Mood Logs: $moodCount / $target"
            }
            "SLEEP_CHAMPION" -> {
                val sleepCount = CheckinManager.getSleepGoalCount(this)
                val target = 5
                isUnlockable = sleepCount >= target
                progressText = "Sleep Goals: $sleepCount / $target"
            }
        }
        
        // Show progress if exists
        if (progressText.isNotEmpty()) {
             // We can append this to description or show in a toast, or a dedicated text view. 
             // For simplicity, let's append to description for now if we don't have a dedicated view.
             // Or better, set it on the Cost text if locked.
             val currentDesc = findViewById<TextView>(R.id.tvRewardDescription).text
             findViewById<TextView>(R.id.tvRewardDescription).text = "$currentDesc\n\n$progressText"
        }
        
        // Ownership Check
        val ownershipKey = "OWNED_BADGE_$badgeId"
        val isOwned = sharedPref.getBoolean(ownershipKey, false)
        
        if (isOwned) {
            btnRedeem.isEnabled = false
            btnRedeem.text = "Already Owned"
            btnRedeem.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)
        } else if (!isUnlockable) {
            btnRedeem.isEnabled = false
            btnRedeem.text = "Locked"
            btnRedeem.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF5350")) // Red
        } else {
             btnRedeem.text = "Redeem for $cost Coins"
             btnRedeem.isEnabled = true
             btnRedeem.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#AB47BC")) // Purple
        }

        btnRedeem.setOnClickListener {
            if (currentCoins >= cost) {
                // Deduct coins
                val newBalance = currentCoins - cost
                sharedPref.edit().putInt("COIN_BALANCE", newBalance).apply()
                
                // Set Ownership
                sharedPref.edit().putBoolean(ownershipKey, true).apply()
                
                // Update UI
                tvUserBalance.text = "$newBalance Coins"
                
                // Launch Badge Unlocked Screen
                val successIntent = android.content.Intent(this, BadgeUnlockedActivity::class.java)
                successIntent.putExtra("BADGE_NAME", badgeName)
                successIntent.putExtra("BADGE_DESC", badgeDesc)
                successIntent.putExtra("BADGE_ICON", badgeIconRes)
                startActivity(successIntent)
                finish()
            } else {
                Toast.makeText(this, "Insufficient coins!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
