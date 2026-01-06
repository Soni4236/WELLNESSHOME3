package com.simats.wellnesshome

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlin.random.Random
import com.simats.wellnesshome.utils.FeedbackUtils

data class Riddle(
    val question: String,
    val options: List<String>,
    val correctIndex: Int // 0 for A, 1 for B, 2 for C, 3 for D
)

class DailyRiddlesActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var btnOptionA: AppCompatButton
    private lateinit var btnOptionB: AppCompatButton
    private lateinit var btnOptionC: AppCompatButton
    private lateinit var btnOptionD: AppCompatButton
    private lateinit var btnSubmit: AppCompatButton
    
    private var selectedOption: AppCompatButton? = null
    private var currentRiddle: Riddle? = null

    // Mix of Old (90s) and New (2025) riddles
    private val riddlesList = listOf(
        Riddle("I have keys, but no locks. I have a space, but no room. You can enter, but never go outside.", listOf("A. A Keyboard", "B. A Map", "C. A Piano", "D. A House"), 0),
        Riddle("I have cities, but no houses. I have mountains, but no trees. I have water, but no fish. What am I?", listOf("A. A Globe", "B. A Map", "C. A Dream", "D. A Cinema"), 1),
        Riddle("The more of this there is, the less you see. What is it?", listOf("A. Darkness", "B. Fog", "C. Light", "D. Smoke"), 0),
        Riddle("I speak without a mouth and hear without ears. I have no body, but I come alive with wind.", listOf("A. A Ghost", "B. An Echo", "C. A Cloud", "D. A Whisper"), 1),
        Riddle("I am not alive, but I grow; I don't have lungs, but I need air; I don't have a mouth, but water kills me.", listOf("A. Ice", "B. Fire", "C. A Plant", "D. A Virus"), 1),
        Riddle("I have a screen but no face. I have a mouse but no life. What am I?", listOf("A. A TV", "B. A Computer", "C. A Robot", "D. A Phone"), 1),
        Riddle("I live in the cloud, but I never rain. I can store your memories, but I have no brain.", listOf("A. A Server", "B. Digital Storage", "C. Facebook", "D. A Dream"), 1),
        Riddle("I am a box that holds keys without locks, yet they can unlock your soul.", listOf("A. A Piano", "B. A Radio", "C. A Player", "D. A Heart"), 0),
        Riddle("What connects two people but touches only one?", listOf("A. A Wedding Ring", "B. A Phone Call", "C. A Secret", "D. A Shadow"), 0),
        Riddle("I can go viral without being sick. I have millions of followers but no leader.", listOf("A. A Trend", "B. An Influencer", "C. A Meme", "D. A News"), 2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_riddles)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        tvQuestion = findViewById(R.id.tvQuestionText)
        btnOptionA = findViewById(R.id.btnOptionA)
        btnOptionB = findViewById(R.id.btnOptionB)
        btnOptionC = findViewById(R.id.btnOptionC)
        btnOptionD = findViewById(R.id.btnOptionD)
        btnSubmit = findViewById(R.id.btnSubmit)

        loadNewRiddle()

        btnOptionA.setOnClickListener { selectOption(btnOptionA) }
        btnOptionB.setOnClickListener { selectOption(btnOptionB) }
        btnOptionC.setOnClickListener { selectOption(btnOptionC) }
        btnOptionD.setOnClickListener { selectOption(btnOptionD) }

        btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun loadNewRiddle() {
        // Randomly select one
        val randomIndex = Random.nextInt(riddlesList.size)
        currentRiddle = riddlesList[randomIndex]
        
        // Bind data
        tvQuestion.text = currentRiddle?.question
        btnOptionA.text = currentRiddle?.options?.get(0)
        btnOptionB.text = currentRiddle?.options?.get(1)
        btnOptionC.text = currentRiddle?.options?.get(2)
        btnOptionD.text = currentRiddle?.options?.get(3)
        
        // Reset state
        selectedOption = null
        btnSubmit.isEnabled = false
        btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B0BEC5")) // Disabled Gray
        resetOption(btnOptionA)
        resetOption(btnOptionB)
        resetOption(btnOptionC)
        resetOption(btnOptionD)
    }

    private fun selectOption(button: AppCompatButton) {
        resetOption(btnOptionA)
        resetOption(btnOptionB)
        resetOption(btnOptionC)
        resetOption(btnOptionD)

        button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E1BEE7"))
        selectedOption = button
        
        btnSubmit.isEnabled = true
        btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CE93D8"))
    }

    private fun resetOption(button: AppCompatButton) {
        button.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
    }

    private fun checkAnswer() {
        val riddle = currentRiddle ?: return
        
        val correctButton = when(riddle.correctIndex) {
            0 -> btnOptionA
            1 -> btnOptionB
            2 -> btnOptionC
            3 -> btnOptionD
            else -> btnOptionA
        }

        if (selectedOption == correctButton) {
            FeedbackUtils.playSuccessSound(this)
            FeedbackUtils.showSuccessVisual(this, "Correct! +5 Coins")
            
            // Award Coins
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val currentCoins = sharedPref.getInt("COIN_BALANCE", 125)
            sharedPref.edit().putInt("COIN_BALANCE", currentCoins + 5).apply()
            
            finish()
        } else {
            Toast.makeText(this, "Oops! That's not right. Try again!", Toast.LENGTH_SHORT).show()
        }
    }
}
