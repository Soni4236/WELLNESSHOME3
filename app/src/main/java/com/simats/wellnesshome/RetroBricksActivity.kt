package com.simats.wellnesshome

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.wellnesshome.ui.RetroBricksView
import com.simats.wellnesshome.utils.FeedbackUtils

class RetroBricksActivity : AppCompatActivity() {

    private lateinit var bricksView: RetroBricksView
    private lateinit var tvGameMessage: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvLives: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    private val gameLoop = object : Runnable {
        override fun run() {
            if (isPlaying) {
                bricksView.update()
                tvScore.text = "Score: ${bricksView.score}"
                handler.postDelayed(this, 16) // ~60fps
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retro_bricks)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        bricksView = findViewById(R.id.bricksView)
        tvGameMessage = findViewById(R.id.tvGameMessage)
        tvScore = findViewById(R.id.tvScore)
        tvLives = findViewById(R.id.tvLives)

        // Game Events
        // Game Events
        bricksView.onStartGame = {
            if (!isPlaying) {
                startGame()
            }
        }

        
        bricksView.onScore = {
            // Optional: Play tick sound
        }
        
        bricksView.onLifeLost = { lives ->
            tvLives.text = "Lives: $lives"
            FeedbackUtils.playErrorSound(this) // Shake or error sound
        }

        bricksView.onGameOver = { playerWon ->
            gameOver(playerWon)
        }
    }
    
    private fun startGame() {
        if (isPlaying) return
        
        isPlaying = true
        tvGameMessage.visibility = View.GONE
        bricksView.startGame()
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
             val bonus = 50
             tvGameMessage.text = "LEVEL CLEARED!\n\n+$bonus Coins\nTap to Play Again"
             FeedbackUtils.showSuccessVisual(this, "Victory! +$bonus Coins")
             FeedbackUtils.playSuccessSound(this)
             
             // Award
             val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
             val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
             sharedPref.edit().putInt("COIN_BALANCE", currentCoins + bonus).apply()
        } else {
             val pity = 5
             tvGameMessage.text = "GAME OVER\nScore: ${bricksView.score}\n\n+$pity Coins\nTap to Try Again"
             
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
