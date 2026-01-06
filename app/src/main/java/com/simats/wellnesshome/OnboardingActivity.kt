package com.simats.wellnesshome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: androidx.appcompat.widget.AppCompatButton
    private lateinit var btnBack: androidx.appcompat.widget.AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        // Updated list with 2 items
        val items = listOf(
            OnboardingItem(
                "Track your wellbeing\neasily",
                "Monitor your mood, stress, and sleep\npatterns all in one place",
                R.drawable.ic_waveform,
                R.drawable.bg_icon_container
            ),
            OnboardingItem(
                "Express your emotions",
                "Check in daily with simple mood\ntracking and personalized insights",
                R.drawable.ic_emoji_smile,
                R.drawable.bg_icon_container_pink
            ),
            OnboardingItem(
                "Discover patterns",
                "Get personalized insights and see\nhow your habits affect your wellbeing",
                R.drawable.ic_trending_up,
                R.drawable.bg_icon_container_green
            ),
            OnboardingItem(
                "Earn rewards",
                "Complete activities and track your\nprogress to unlock badges and rewards",
                R.drawable.ic_rewards,
                R.drawable.bg_icon_container_yellow
            )
        )

        viewPager.adapter = OnboardingAdapter(items)

        // Dots removed as per request

        btnNext.setOnClickListener {
            if (viewPager.currentItem < items.size - 1) {
                viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            }
        }

        btnBack.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    btnBack.visibility = View.GONE // GONE allows Next button to expand/center if using weights
                } else {
                    btnBack.visibility = View.VISIBLE
                }

                if (position == items.size - 1) {
                    btnNext.text = "Continue to Login"
                } else {
                    btnNext.text = "Next"
                }
            }
        })
    }
}
