package com.simats.wellnesshome

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.wellnesshome.ui.SpaceDefenderView
import com.simats.wellnesshome.utils.FeedbackUtils

class SpaceDefenderActivity : AppCompatActivity() {

    private lateinit var spaceView: SpaceDefenderView
    private lateinit var tvGameMessage: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvLives: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    private val gameLoop = object : Runnable {
        override fun run() {
            if (isPlaying) {
                spaceView.update()
                tvScore.text = "Score: ${spaceView.score}"
                handler.postDelayed(this, 16) // ~60fps
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_space_defender)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        spaceView = findViewById(R.id.spaceView)
        tvGameMessage = findViewById(R.id.tvGameMessage)
        tvScore = findViewById(R.id.tvScore)
        tvLives = findViewById(R.id.tvLives)

        // Game Events
        spaceView.onStartGame = {
            if (!isPlaying) {
                startGame()
            }
        }

        spaceView.onScore = {
            // Optional sound
        }

        spaceView.onLifeLost = { lives ->
            tvLives.text = "Lives: $lives"
            FeedbackUtils.playErrorSound(this)
        }

        spaceView.onGameOver = { playerWon ->
            gameOver(playerWon)
        }
    }

    private fun startGame() {
        if (isPlaying) return

        isPlaying = true
        tvGameMessage.visibility = View.GONE
        spaceView.startGame()
        tvScore.text = "Score: 0"
        tvLives.text = "Lives: 3"
        handler.removeCallbacks(gameLoop)
        handler.post(gameLoop)
    }

    private fun gameOver(playerWon: Boolean) {
        isPlaying = false
        handler.removeCallbacks(gameLoop)
        
        tvGameMessage.visibility = View.VISIBLE
        
        if (playerWon) {
             val bonus = 75
             tvGameMessage.text = "MISSION ACCOMPLISHED!\n\n+$bonus Coins\nTap to Play Again"
             FeedbackUtils.showSuccessVisual(this, "Victory! +$bonus Coins")
             FeedbackUtils.playSuccessSound(this)
             
             // Award
             val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
             val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
             sharedPref.edit().putInt("COIN_BALANCE", currentCoins + bonus).apply()
        } else {
             val pity = 5
             tvGameMessage.text = "GAME OVER\nScore: ${spaceView.score}\n\n+$pity Coins\nTap to Try Again"
             
             // Award Pity Coins
             val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
             val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
             sharedPref.edit().putInt("COIN_BALANCE", currentCoins + pity).apply()
        }
    }

    override fun onPause() {
        super.onPause()
        isPlaying = false
        handler.removeCallbacks(gameLoop)
        if (tvGameMessage.visibility == View.GONE) {
             tvGameMessage.text = "PAUSED\n\nTap to Resume"
             tvGameMessage.visibility = View.VISIBLE
        }
    }
}
