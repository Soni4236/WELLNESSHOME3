package com.simats.wellnesshome

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.wellnesshome.ui.DinoRunView
import com.simats.wellnesshome.utils.FeedbackUtils

class DinoRunActivity : AppCompatActivity() {

    private lateinit var dinoView: DinoRunView
    private lateinit var tvGameMessage: TextView
    private lateinit var tvScore: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    private val gameLoop = object : Runnable {
        override fun run() {
            if (isPlaying) {
                dinoView.update()
                tvScore.text = "Distance: ${dinoView.score}m"
                handler.postDelayed(this, 16) // ~60fps
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dino_run)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        dinoView = findViewById(R.id.dinoView)
        tvGameMessage = findViewById(R.id.tvGameMessage)
        tvScore = findViewById(R.id.tvScore)

        // Game Events
        dinoView.onStartGame = {
            if (!isPlaying) {
                startGame()
            }
        }

        dinoView.onGameOver = { _ ->
            gameOver()
        }
    }

    private fun startGame() {
        if (isPlaying) return

        isPlaying = true
        tvGameMessage.visibility = View.GONE
        dinoView.startGame()
        tvScore.text = "Distance: 0m"
        handler.removeCallbacks(gameLoop)
        handler.post(gameLoop)
    }

    private fun gameOver() {
        isPlaying = false
        handler.removeCallbacks(gameLoop)
        
        tvGameMessage.visibility = View.VISIBLE
        
        // Reward based on distance
        val reward = 20 + (dinoView.score / 20) // 20 base + 1 per 20m
        val maxReward = 150
        val finalReward = reward.coerceAtMost(maxReward)

        tvGameMessage.text = "GAME OVER\nDistance: ${dinoView.score}m\n\n+$finalReward Coins\nTap to Try Again"
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
