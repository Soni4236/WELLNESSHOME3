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
import com.simats.wellnesshome.ui.PixelPongView
import com.simats.wellnesshome.utils.FeedbackUtils

class PixelPongActivity : AppCompatActivity() {

    private lateinit var pongView: PixelPongView
    private lateinit var tvStart: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    private val gameLoop = object : Runnable {
        override fun run() {
            if (isPlaying) {
                pongView.update()
                handler.postDelayed(this, 16) // ~60fps
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pixel_pong)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        pongView = findViewById(R.id.pongView)
        tvStart = findViewById(R.id.tvStart)

        // START GAME AUTOMATICALLY
        // Wait briefly for view to layout
        handler.postDelayed({
            startGame()
        }, 500)
        
        pongView.setOnClickListener {
            // Only used for restart if game over
            if (!isPlaying && pongView.playerScore > 0 || pongView.aiScore > 0) {
                 startGame()
            }
        }
        
        pongView.onGameOver = { playerWon ->
            gameOver(playerWon)
        }
    }
    
    private fun startGame() {
        if (isPlaying) return
        
        isPlaying = true
        tvStart.visibility = View.GONE
        pongView.startGame()
        handler.removeCallbacks(gameLoop)
        handler.post(gameLoop)
        Toast.makeText(this, "Game Started!", Toast.LENGTH_SHORT).show()
    }

    private fun gameOver(playerWon: Boolean) {
        isPlaying = false
        handler.removeCallbacks(gameLoop)
        
        tvStart.visibility = View.VISIBLE
        
        if (playerWon) {
             tvStart.text = "You Won!\n+20 Coins\nTap to Play Again"
             FeedbackUtils.showSuccessVisual(this, "Victory! +20 Coins")
             FeedbackUtils.playSuccessSound(this)
             
             // Award
             val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
             val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
             sharedPref.edit().putInt("COIN_BALANCE", currentCoins + 20).apply()
        } else {
             tvStart.text = "Game Over\n+5 Coins\nTap to Play Again"
             Toast.makeText(this, "Nice try! +5 Coins", Toast.LENGTH_SHORT).show()
             
             // Award Pity Coins
             val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
             val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
             sharedPref.edit().putInt("COIN_BALANCE", currentCoins + 5).apply()
        }
    }

    override fun onPause() {
        super.onPause()
        isPlaying = false
        handler.removeCallbacks(gameLoop)
        if (tvStart.visibility == View.GONE) {
             tvStart.text = "Paused\nTap to Resume"
             tvStart.visibility = View.VISIBLE
        }
    }
}
