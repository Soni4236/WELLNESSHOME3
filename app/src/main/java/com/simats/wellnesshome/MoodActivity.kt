package com.simats.wellnesshome

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.ImageViewCompat

class MoodActivity : AppCompatActivity() {

    private var selectedMood = 0 // 0 = None, 1=Bad, 2=Poor, 3=Okay, 4=Good, 5=Great

    private lateinit var ivBad: ImageView
    private lateinit var ivPoor: ImageView
    private lateinit var ivOkay: ImageView
    private lateinit var ivGood: ImageView
    private lateinit var ivGreat: ImageView
    private lateinit var btnSubmit: AppCompatButton
    private lateinit var etNotes: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        // Init Views
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        ivBad = findViewById(R.id.ivBad)
        ivPoor = findViewById(R.id.ivPoor)
        ivOkay = findViewById(R.id.ivOkay)
        ivGood = findViewById(R.id.ivGood)
        ivGreat = findViewById(R.id.ivGreat)
        btnSubmit = findViewById(R.id.btnSubmit)
        etNotes = findViewById(R.id.etNotes)

        // Listeners
        btnBack.setOnClickListener { finish() }

        ivBad.setOnClickListener { selectMood(1) }
        ivPoor.setOnClickListener { selectMood(2) }
        ivOkay.setOnClickListener { selectMood(3) }
        ivGood.setOnClickListener { selectMood(4) }
        ivGreat.setOnClickListener { selectMood(5) }

        btnSubmit.setOnClickListener {
            if (selectedMood == 0) {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
            } else {
                saveMoodData()
            }
        }
    }

    private fun selectMood(mood: Int) {
        selectedMood = mood
        
        // Reset all icons to default state (Gray tint, White bg)
        resetIcon(ivBad)
        resetIcon(ivPoor)
        resetIcon(ivOkay)
        resetIcon(ivGood)
        resetIcon(ivGreat)

        // Highlight selected icon
        // We can tint the icon or the background. Let's tint the icon to a color and bg to something else or just keep it simple.
        // Design shows outlined/filled. Let's tint the icon color based on mood.
        
        when (mood) {
            1 -> highlightIcon(ivBad, "#F44336") // Red for Bad
            2 -> highlightIcon(ivPoor, "#FF9800") // Orange for Poor
            3 -> highlightIcon(ivOkay, "#FFC107") // Amber for Okay
            4 -> highlightIcon(ivGood, "#8BC34A") // Light Green for Good
            5 -> highlightIcon(ivGreat, "#4CAF50") // Green for Great
        }

        // Enable Submit Button
        btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#80DEEA")) // Cyan/Blueish tint like design
        btnSubmit.isEnabled = true
    }

    private fun resetIcon(imageView: ImageView) {
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(Color.parseColor("#CCCCCC"))) // Gray
        imageView.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
    }

    private fun highlightIcon(imageView: ImageView, colorHex: String) {
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(Color.parseColor(colorHex)))
        // Optional: Change background tint slightly if desired, currently staying white
    }

    private fun saveMoodData() {
        val notes = etNotes.text.toString().trim()
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            // Save mood data
            putInt("LATEST_MOOD_SCORE", selectedMood)
            putString("LATEST_MOOD_NOTES", notes)
            putLong("LATEST_MOOD_TIMESTAMP", System.currentTimeMillis())
            apply()
        }
        
        // Mark done and check trigger
        CheckinManager.markMoodDone(this)
        CheckinManager.incrementMoodCount(this)
        CheckinManager.updateGenericStreak(this)
        
        // Always go to Success Screen for individual checkin feedback
        val intent = Intent(this, MoodSuccessActivity::class.java)
        intent.putExtra("NEXT_SCREEN", "DASHBOARD")
        startActivity(intent)
        finish()
    }
}
