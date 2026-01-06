package com.simats.wellnesshome

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class BadgeUnlockedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_unlocked)

        val btnViewCollection = findViewById<AppCompatButton>(R.id.btnViewCollection)
        val btnShare = findViewById<AppCompatButton>(R.id.btnShare)
        val tvBadgeName = findViewById<TextView>(R.id.tvBadgeName)
        val ivBadgeIcon = findViewById<ImageView>(R.id.ivBadgeIcon)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        
        // Animation Views
        val ivHappyDog = findViewById<ImageView>(R.id.ivHappyDog)
        val ivHeartAnim = findViewById<ImageView>(R.id.ivHeartAnim)

        // Get data from intent (with defaults)
        val badgeName = intent.getStringExtra("BADGE_NAME") ?: "7-Day Warrior Achievement"
        val badgeDesc = intent.getStringExtra("BADGE_DESC") ?: "You've maintained a 7-day streak!\nYour dedication to wellness is\ninspiring."
        val badgeIconResId = intent.getIntExtra("BADGE_ICON", R.drawable.ic_fire)

        tvBadgeName.text = badgeName
        tvDescription.text = badgeDesc
        ivBadgeIcon.setImageResource(badgeIconResId)
        
        // Start Dog Animation with Delay
        ivHappyDog.postDelayed({
            ivHappyDog.visibility = View.VISIBLE
            ivHeartAnim.visibility = View.VISIBLE
            
            // Pop in Dog
            val scaleX = ObjectAnimator.ofFloat(ivHappyDog, "scaleX", 0f, 1f)
            val scaleY = ObjectAnimator.ofFloat(ivHappyDog, "scaleY", 0f, 1f)
            scaleX.duration = 500
            scaleY.duration = 500
            scaleX.interpolator = BounceInterpolator()
            scaleY.interpolator = BounceInterpolator()
            scaleX.start()
            scaleY.start()
            
            // Pulse Heart
            val heartScaleX = ObjectAnimator.ofFloat(ivHeartAnim, "scaleX", 0.5f, 1.2f, 1f)
            val heartScaleY = ObjectAnimator.ofFloat(ivHeartAnim, "scaleY", 0.5f, 1.2f, 1f)
            heartScaleX.duration = 800
            heartScaleY.duration = 800
            heartScaleX.repeatCount = ObjectAnimator.INFINITE
            heartScaleY.repeatCount = ObjectAnimator.INFINITE
            heartScaleX.repeatMode = ObjectAnimator.REVERSE
            heartScaleY.repeatMode = ObjectAnimator.REVERSE
            heartScaleX.start()
            heartScaleY.start()
            
        }, 300)

        // Modified Click Listener to go to Badge Collection
        btnViewCollection.setOnClickListener {
            val intent = Intent(this, BadgesActivity::class.java)
            // If BadgesActivity was launched from Dashboard, this stacks on top.
            // If we want to return to dashboard first, we'd clear top.
            // For now, let's just go there.
            startActivity(intent)
            finish()
        }

        btnShare.setOnClickListener {
            val shareText = "I just unlocked the $badgeName in WellnessHome! #WellnessJourney"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Achievement"))
        }
    }
}
