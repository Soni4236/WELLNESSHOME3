package com.simats.wellnesshome.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class PixelPongView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val playerPaint = Paint().apply {
        color = Color.parseColor("#009688") // Teal
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val aiPaint = Paint().apply {
        color = Color.parseColor("#7E57C2") // Purple
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val ballPaint = Paint().apply {
        color = Color.parseColor("#F48FB1") // Pink
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.parseColor("#B0BEC5") // Light Gray
        style = Paint.Style.STROKE
        strokeWidth = 5f
        pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#546E7A") // Gray Blue
        textSize = 100f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    // Game State
    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 15f
    private var ballSpeedY = 15f
    
    private var playerPaddleX = 0f
    private var aiPaddleX = 0f
    
    // Bounds
    private val paddleWidth = 200f
    private val paddleHeight = 40f
    private val ballRadius = 25f
    
    // Scoring
    var playerScore = 0
    var aiScore = 0
    var onGameOver: ((Boolean) -> Unit)? = null // true if player won
    var onScore: (() -> Unit)? = null // Sound effect trigger

    private var isPlaying = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetBall()
        playerPaddleX = w / 2f
        aiPaddleX = w / 2f
    }

    fun update() {
        if (!isPlaying) return

        // Move Ball
        ballX += ballSpeedX
        ballY += ballSpeedY

        // Wall Collisions (Left/Right)
        if (ballX - ballRadius < 0 || ballX + ballRadius > width) {
            ballSpeedX = -ballSpeedX
        }

        // Paddle Collisions
        // Player (Bottom)
        val playerPaddleY = height - 100f // Padding from bottom
        if (ballY + ballRadius >= playerPaddleY && ballY - ballRadius <= playerPaddleY + paddleHeight) {
            if (ballX >= playerPaddleX - paddleWidth / 2 && ballX <= playerPaddleX + paddleWidth / 2) {
                ballSpeedY = -abs(ballSpeedY * 1.05f) // Bounce up + Speed up slightly
                ballSpeedX += (ballX - playerPaddleX) * 0.1f // Add spin effect based on hit location
                onScore?.invoke() // Actually just a hit sound, reusing onScore for simplicity callback
            }
        }

        // AI (Top)
        val aiPaddleY = 100f // Padding from top
        if (ballY - ballRadius <= aiPaddleY + paddleHeight && ballY + ballRadius >= aiPaddleY) {
            if (ballX >= aiPaddleX - paddleWidth / 2 && ballX <= aiPaddleX + paddleWidth / 2) {
                ballSpeedY = abs(ballSpeedY * 1.05f) // Bounce down
                onScore?.invoke() // Hit sound
            }
        }

        // Goal Check
        if (ballY > height) {
            // AI Scored
            aiScore++
            checkWinCondition()
            resetBall()
        } else if (ballY < 0) {
            // Player Scored
            playerScore++
            checkWinCondition()
            resetBall()
        }

        // AI Movement
        // Simple AI: Move towards ballX
        val aiSpeed = 12f
        if (ballX > aiPaddleX + 20) {
            aiPaddleX += aiSpeed
        } else if (ballX < aiPaddleX - 20) {
            aiPaddleX -= aiSpeed
        }
        
        // Clamp AI Paddle
        aiPaddleX = aiPaddleX.coerceIn(paddleWidth/2, width - paddleWidth/2)

        invalidate()
    }

    private fun checkWinCondition() {
        if (playerScore >= 5) {
            isPlaying = false
            onGameOver?.invoke(true)
        } else if (aiScore >= 5) {
            isPlaying = false
            onGameOver?.invoke(false)
        }
    }

    private fun resetBall() {
        ballX = width / 2f
        ballY = height / 2f
        ballSpeedX = if (Math.random() > 0.5) 15f else -15f
        ballSpeedY = if (Math.random() > 0.5) 15f else -15f
    }

    fun startGame() {
        isPlaying = true
        playerScore = 0
        aiScore = 0
        resetBall()
        invalidate()
    }
    
    fun stopGame() {
        isPlaying = false
    }
    
    // Add public accessors for score checks
    // (Variables are already public properties by default in Kotlin unless private)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                playerPaddleX = event.x
                // Clamp
                playerPaddleX = playerPaddleX.coerceIn(paddleWidth/2, width - paddleWidth/2)
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw Center Line
        canvas.drawLine(0f, height/2f, width.toFloat(), height/2f, linePaint)

        // Draw Scores
        canvas.drawText("$aiScore", width/2f, height/2f - 100f, textPaint)
        canvas.drawText("$playerScore", width/2f, height/2f + 180f, textPaint)

        // Draw Player Paddle
        val playerRect = RectF(
            playerPaddleX - paddleWidth/2,
            height - 100f,
            playerPaddleX + paddleWidth/2,
            height - 100f + paddleHeight
        )
        canvas.drawRoundRect(playerRect, 10f, 10f, playerPaint)

        // Draw AI Paddle
        val aiRect = RectF(
            aiPaddleX - paddleWidth/2,
            100f,
            aiPaddleX + paddleWidth/2,
            100f + paddleHeight
        )
        canvas.drawRoundRect(aiRect, 10f, 10f, aiPaint)

        // Draw Ball
        canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)
    }
}
