package com.simats.wellnesshome

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RewardsStoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards_store)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val tabBadges = findViewById<android.widget.LinearLayout>(R.id.tabBadges)
        val tabSounds = findViewById<android.widget.LinearLayout>(R.id.tabSounds)
        val tabWalls = findViewById<android.widget.LinearLayout>(R.id.tabWalls)

        val layoutBadges = findViewById<android.widget.LinearLayout>(R.id.layoutBadges)
        val layoutSounds = findViewById<android.widget.LinearLayout>(R.id.layoutSounds)
        val layoutWalls = findViewById<android.widget.LinearLayout>(R.id.layoutWalls)

        fun updateTabs(selected: Int) {
            // 0=Badges, 1=Sounds, 2=Walls
            
            // Reset styles
            val inactiveBg = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F5F5F5"))
            val inactiveText = android.graphics.Color.parseColor("#555555")
            val activeBg = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CE93D8"))
            val activeText = android.graphics.Color.WHITE

            // Reset all
            tabBadges.backgroundTintList = inactiveBg
            findViewById<android.widget.TextView>(R.id.tvTabBadges).setTextColor(inactiveText)
            findViewById<android.widget.ImageView>(R.id.ivTabBadges).imageTintList = android.content.res.ColorStateList.valueOf(inactiveText)

            tabSounds.backgroundTintList = inactiveBg
            findViewById<android.widget.TextView>(R.id.tvTabSounds).setTextColor(inactiveText)
            findViewById<android.widget.ImageView>(R.id.ivTabSounds).imageTintList = android.content.res.ColorStateList.valueOf(inactiveText)

            tabWalls.backgroundTintList = inactiveBg
            findViewById<android.widget.TextView>(R.id.tvTabWalls).setTextColor(inactiveText)
            findViewById<android.widget.ImageView>(R.id.ivTabWalls).imageTintList = android.content.res.ColorStateList.valueOf(inactiveText)

            // Hide all layouts
            layoutBadges.visibility = android.view.View.GONE
            layoutSounds.visibility = android.view.View.GONE
            layoutWalls.visibility = android.view.View.GONE

            // Select active
            when(selected) {
                0 -> { // Badges
                    tabBadges.backgroundTintList = activeBg
                    findViewById<android.widget.TextView>(R.id.tvTabBadges).setTextColor(activeText)
                    findViewById<android.widget.ImageView>(R.id.ivTabBadges).imageTintList = android.content.res.ColorStateList.valueOf(activeText)
                    layoutBadges.visibility = android.view.View.VISIBLE
                }
                1 -> { // Sounds
                    tabSounds.backgroundTintList = activeBg
                    findViewById<android.widget.TextView>(R.id.tvTabSounds).setTextColor(activeText)
                    findViewById<android.widget.ImageView>(R.id.ivTabSounds).imageTintList = android.content.res.ColorStateList.valueOf(activeText)
                    layoutSounds.visibility = android.view.View.VISIBLE
                }
                2 -> { // Walls
                    tabWalls.backgroundTintList = activeBg
                    findViewById<android.widget.TextView>(R.id.tvTabWalls).setTextColor(activeText)
                    findViewById<android.widget.ImageView>(R.id.ivTabWalls).imageTintList = android.content.res.ColorStateList.valueOf(activeText)
                    layoutWalls.visibility = android.view.View.VISIBLE
                }
            }
        }

        tabBadges.setOnClickListener { updateTabs(0) }
        tabSounds.setOnClickListener { updateTabs(1) }
        tabWalls.setOnClickListener { updateTabs(2) }

        // Default
        updateTabs(0)

        // Find and set click listener for 7-Day Warrior
        setupBadgeListeners()
    }

    private fun setupBadgeListeners() {
        // 7-Day Warrior
        findViewById<androidx.cardview.widget.CardView>(R.id.card7DayWarrior)?.setOnClickListener {
            openBadgeDetail("7_DAY_WARRIOR", "7-Day Warrior", "You've maintained a 7-day streak!\nYour dedication to wellness is inspiring.", R.drawable.ic_fire, 50)
        }
        
        // Mood Master
        findViewById<androidx.cardview.widget.CardView>(R.id.cardMoodMaster)?.setOnClickListener {
            openBadgeDetail("MOOD_MASTER", "Mood Master", "You've logged your mood for 10 days in a row!\nGreat emotional awareness.", R.drawable.ic_emoji_smile, 50)
        }
        
        // Sleep Champion
        findViewById<androidx.cardview.widget.CardView>(R.id.cardSleepChampion)?.setOnClickListener {
            openBadgeDetail("SLEEP_CHAMPION", "Sleep Champion", "You've achieved your 8-hour sleep goal consistently.\nWell rested!", R.drawable.ic_moon, 50)
        }
    }

    private fun openBadgeDetail(id: String, name: String, desc: String, iconRes: Int, cost: Int) {
        val intent = android.content.Intent(this, RewardDetailActivity::class.java)
        intent.putExtra("BADGE_ID", id)
        intent.putExtra("BADGE_NAME", name)
        intent.putExtra("BADGE_DESC", desc)
        intent.putExtra("BADGE_ICON", iconRes)
        intent.putExtra("BADGE_COST", cost)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        updateCoinBalance()
    }

    private fun updateCoinBalance() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 125)
        findViewById<android.widget.TextView>(R.id.tvCoinBalance).text = "$currentCoins Coins"
    }
}
