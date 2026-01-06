package com.simats.wellnesshome

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.wellnesshome.ui.MeteorDodgeView
import com.simats.wellnesshome.utils.FeedbackUtils

class MeteorDodgeActivity : AppCompatActivity() {

    private lateinit var gameView: MeteorDodgeView
    private lateinit var tvGameMessage: TextView
    private lateinit var tvScore: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    private val gameLoop = object : Runnable {
        override fun run() {
            if (isPlaying) {
                gameView.update()
                tvScore.text = "Score: ${gameView.score}"
                handler.postDelayed(this, 16) // ~60fps
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meteor_dodge)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        gameView = findViewById(R.id.gameView)
        tvGameMessage = findViewById(R.id.tvGameMessage)
        tvScore = findViewById(R.id.tvScore)

        // Game Events
        gameView.onStartGame = {
            if (!isPlaying) {
                startGame()
            }
        }

        gameView.onScore = {
            // Optional sound
        }

        gameView.onGameOver = { _ ->
            gameOver()
        }
    }

    private fun startGame() {
        if (isPlaying) return

        isPlaying = true
        tvGameMessage.visibility = View.GONE
        gameView.startGame()
        tvScore.text = "Score: 0"
        handler.removeCallbacks(gameLoop)
        handler.post(gameLoop)
    }

    private fun gameOver() {
        isPlaying = false
        handler.removeCallbacks(gameLoop)
        
        tvGameMessage.visibility = View.VISIBLE
        
        // Pity Coins
        val reward = 10 + (gameView.score / 50) // 10 base + 1 per 50 points
        val maxReward = 100
        val finalReward = reward.coerceAtMost(maxReward)

        tvGameMessage.text = "CRASHED!\nScore: ${gameView.score}\n\n+$finalReward Coins\nTap to Try Again"
        FeedbackUtils.playErrorSound(this)
             
        // Award Coins
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
        sharedPref.edit().putInt("COIN_BALANCE", currentCoins + finalReward).apply()
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
