package com.simats.wellnesshome

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.simats.wellnesshome.ui.SnakeBoardView
import com.simats.wellnesshome.utils.FeedbackUtils
import java.util.LinkedList
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

class SnakeGameActivity : AppCompatActivity() {

    private lateinit var snakeBoard: SnakeBoardView
    private lateinit var tvScore: TextView
    private lateinit var tvStartLabel: TextView
    private lateinit var fabStart: FloatingActionButton
    
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var score = 0
    
    // Game Config
    private val numColumns = 15
    private val numRows = 20
    private var currentDirection = Direction.RIGHT
    private var snakeBody = LinkedList<Point>()
    private var foodPosition = Point(0, 0)
    
    private val gameLoopRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                updateGame()
                handler.postDelayed(this, 180) // Speed (lower = faster)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake_game)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        snakeBoard = findViewById(R.id.snakeBoard)
        tvScore = findViewById(R.id.tvScore)
        tvStartLabel = findViewById(R.id.tvStartLabel)
        fabStart = findViewById(R.id.btnStart)

        setupControls()

        fabStart.setOnClickListener {
            if (!isPlaying) {
                startGame()
            }
        }
    }

    private fun setupControls() {
        val gestureDetector = android.view.GestureDetector(this, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: android.view.MotionEvent?,
                e2: android.view.MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false // Null check for safety
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
                    if (kotlin.math.abs(diffX) > 100 && kotlin.math.abs(velocityX) > 100) {
                        if (diffX > 0) {
                            if (currentDirection != Direction.LEFT) currentDirection = Direction.RIGHT
                        } else {
                            if (currentDirection != Direction.RIGHT) currentDirection = Direction.LEFT
                        }
                        return true
                    }
                } else {
                    if (kotlin.math.abs(diffY) > 100 && kotlin.math.abs(velocityY) > 100) {
                        if (diffY > 0) {
                            if (currentDirection != Direction.UP) currentDirection = Direction.DOWN
                        } else {
                            if (currentDirection != Direction.DOWN) currentDirection = Direction.UP
                        }
                        return true
                    }
                }
                return false
            }
        })

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun startGame() {
        isPlaying = true
        score = 0
        updateScore()
        tvStartLabel.visibility = View.GONE
        fabStart.setImageResource(R.drawable.ic_pause) // Pause icon
        fabStart.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EF5350")) // Red for Stop/Pause
        
        // Init Snake (Center)
        snakeBody.clear()
        val centerX = numColumns / 2
        val centerY = numRows / 2
        snakeBody.add(Point(centerX, centerY))
        snakeBody.add(Point(centerX - 1, centerY))
        snakeBody.add(Point(centerX - 2, centerY))
        
        currentDirection = Direction.RIGHT
        spawnFood()
        
        handler.removeCallbacks(gameLoopRunnable)
        handler.post(gameLoopRunnable)
    }

    private fun updateGame() {
        val head = snakeBody.first
        var newHeadX = head.x
        var newHeadY = head.y

        when (currentDirection) {
            Direction.UP -> newHeadY--
            Direction.DOWN -> newHeadY++
            Direction.LEFT -> newHeadX--
            Direction.RIGHT -> newHeadX++
        }

        // Check Wall Collision
        if (newHeadX < 0 || newHeadX >= numColumns || newHeadY < 0 || newHeadY >= numRows) {
            gameOver()
            return
        }

        val newHead = Point(newHeadX, newHeadY)

        // Check Self Collision
        if (snakeBody.contains(newHead) && newHead != snakeBody.last) {
            gameOver()
            return
        }

        snakeBody.addFirst(newHead)

        // Check Food
        if (newHeadX == foodPosition.x && newHeadY == foodPosition.y) {
            // Eat Food
            score++
            updateScore()
            FeedbackUtils.playSuccessSound(this) // Ding!
            spawnFood()
        } else {
            // Remove Tail (Move)
            snakeBody.removeLast()
        }

        snakeBoard.updateGame(snakeBody, foodPosition)
    }

    private fun spawnFood() {
        var x: Int
        var y: Int
        do {
            x = Random.nextInt(numColumns)
            y = Random.nextInt(numRows)
        } while (snakeBody.contains(Point(x, y)))
        
        foodPosition = Point(x, y)
    }

    private fun gameOver() {
        isPlaying = false
        handler.removeCallbacks(gameLoopRunnable)
        FeedbackUtils.showSuccessVisual(this, "Game Over! Score: $score")
        
        // Award Coins (1 per apple)
        if (score > 0) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
            sharedPref.edit().putInt("COIN_BALANCE", currentCoins + score).apply()
            Toast.makeText(this, "+$score Coins Earned!", Toast.LENGTH_SHORT).show()
        }

        tvStartLabel.text = "Game Over\nTap to Restart"
        tvStartLabel.visibility = View.VISIBLE
        fabStart.setImageResource(R.drawable.ic_play)
        fabStart.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00695C"))
    }

    private fun updateScore() {
        tvScore.text = score.toString()
    }

    override fun onPause() {
        super.onPause()
        isPlaying = false
        handler.removeCallbacks(gameLoopRunnable)
        if (score > 0) {
            tvStartLabel.text = "Paused"
            tvStartLabel.visibility = View.VISIBLE
            fabStart.setImageResource(R.drawable.ic_play)
            fabStart.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00695C"))
        }
    }
}
