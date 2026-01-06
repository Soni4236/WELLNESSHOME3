package com.simats.wellnesshome.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class RetroBricksView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val paddlePaint = Paint().apply {
        color = Color.parseColor("#4FC3F7") // Light Blue
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val ballPaint = Paint().apply {
        color = Color.parseColor("#FFD54F") // Amber
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val brickColors = listOf(
        Color.parseColor("#EF5350"), // Red
        Color.parseColor("#FFA726"), // Orange
        Color.parseColor("#66BB6A"), // Green
        Color.parseColor("#42A5F5"), // Blue
        Color.parseColor("#AB47BC")  // Purple
    )
    
    private val brickPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Game Objects
    private var paddleX = 0f
    private val paddleWidth = 200f
    private val paddleHeight = 40f
    
    private var ballX = 0f
    private var ballY = 0f
    private val ballRadius = 20f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private val initialSpeed = 15f
    
    data class Brick(var rect: RectF, val color: Int, var active: Boolean)
    private val bricks = mutableListOf<Brick>()
    
    // Game State
    private var isPlaying = false
    var score = 0
    var lives = 3
    
    var onGameOver: ((Boolean) -> Unit)? = null // true if won
    var onScore: (() -> Unit)? = null
    var onLifeLost: ((Int) -> Unit)? = null
    var onStartGame: (() -> Unit)? = null


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetBall()
        paddleX = w / 2f
        if (bricks.isEmpty()) {
            setupBricks(w)
        }
    }

    private fun setupBricks(width: Int) {
        bricks.clear()
        val cols = 7
        val rows = 5
        val padding = 10f
        val brickWidth = (width - (cols + 1) * padding) / cols
        val brickHeight = 60f
        val startY = 150f // Leave space at top

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = padding + c * (brickWidth + padding)
                val top = startY + r * (brickHeight + padding)
                val rect = RectF(left, top, left + brickWidth, top + brickHeight)
                bricks.add(Brick(rect, brickColors[r % brickColors.size], true))
            }
        }
    }

    fun update() {
        if (!isPlaying) return

        // Ball Movement
        ballX += ballSpeedX
        ballY += ballSpeedY

        // Wall Collisions
        if (ballX - ballRadius < 0 || ballX + ballRadius > width) {
            ballSpeedX = -ballSpeedX
        }
        if (ballY - ballRadius < 0) {
            ballSpeedY = -ballSpeedY
        }

        // Paddle Collision
        val paddleTop = height - 150f
        val paddleRect = RectF(
            paddleX - paddleWidth / 2,
            paddleTop,
            paddleX + paddleWidth / 2,
            paddleTop + paddleHeight
        )
        
        if (ballY + ballRadius >= paddleTop && 
            ballY - ballRadius <= paddleTop + paddleHeight &&
            ballX >= paddleRect.left && 
            ballX <= paddleRect.right &&
            ballSpeedY > 0) { // Only bounce if moving down
                
            ballSpeedY = -ballSpeedY
            
            // Add english/spin based on where it hit the paddle
            val hitOffset = (ballX - paddleX) / (paddleWidth / 2) // -1 to 1
            ballSpeedX = hitOffset * 15f + (ballSpeedX * 0.5f) // Influence horizontal speed
            onScore?.invoke() // Sound effect
        }

        // Brick Collision
        var hitBrick = false
        for (brick in bricks) {
            if (brick.active) {
                // Simple box collision check
                // Expand brick rect slightly for ball radius
                if (ballX + ballRadius > brick.rect.left &&
                    ballX - ballRadius < brick.rect.right &&
                    ballY + ballRadius > brick.rect.top &&
                    ballY - ballRadius < brick.rect.bottom) {
                        
                    brick.active = false
                    score += 10
                    hitBrick = true
                    
                    // Determine bounce direction - simple flip Y for now
                    // Ideally we check overlap to see if it was a side hit
                    ballSpeedY = -ballSpeedY
                    
                    onScore?.invoke()
                    break // Only hit one brick per frame to prevent weirdness
                }
            }
        }
        
        if (hitBrick) {
            // Speed up slightly every 5 bricks or so?
            // Keep it simple for now
            checkWinCondition()
        }

        // Death Check
        if (ballY - ballRadius > height) {
            lives--
            onLifeLost?.invoke(lives)
            if (lives <= 0) {
                isPlaying = false
                onGameOver?.invoke(false)
            } else {
                resetBall()
            }
        }

        invalidate()
    }

    private fun checkWinCondition() {
        if (bricks.none { it.active }) {
            isPlaying = false
            onGameOver?.invoke(true)
        }
    }

    private fun resetBall() {
        if (width == 0) return // Not ready yet
        
        ballX = paddleX
        ballY = height - 170f // Just above paddle
        // Launch upwards with random angle
        ballSpeedY = -initialSpeed
        ballSpeedX = (Math.random().toFloat() - 0.5f) * 10f
    }
    
    fun startGame() {
        if (lives <= 0 || bricks.none { it.active }) {
            // New Game
            score = 0
            lives = 3
            setupBricks(width)
        }
        resetBall()
        isPlaying = true
        invalidate()
    }
    
    fun stopGame() {
        isPlaying = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                paddleX = event.x
                paddleX = paddleX.coerceIn(paddleWidth/2, width - paddleWidth/2)
                if (!isPlaying) {
                     onStartGame?.invoke()
                }
                invalidate()

            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw Bricks
        for (brick in bricks) {
            if (brick.active) {
                canvas.drawRoundRect(brick.rect, 8f, 8f, brickPaint.apply { color = brick.color })
            }
        }

        // Draw Paddle
        val paddleTop = height - 150f
        val paddleRect = RectF(
            paddleX - paddleWidth / 2,
            paddleTop,
            paddleX + paddleWidth / 2,
            paddleTop + paddleHeight
        )
        canvas.drawRoundRect(paddleRect, 10f, 10f, paddlePaint)

        // Draw Ball
        canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)
    }
}
