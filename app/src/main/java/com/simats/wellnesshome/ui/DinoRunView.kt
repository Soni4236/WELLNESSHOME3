package com.simats.wellnesshome.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DinoRunView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val dinoPaint = Paint().apply {
        color = Color.parseColor("#66BB6A") // Green
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val obstaclePaint = Paint().apply {
        color = Color.parseColor("#8D6E63") // Cactus Brown
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val groundPaint = Paint().apply {
        color = Color.parseColor("#757575") // Grey
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Game Objects
    private var dinoY = 0f
    private var dinoVelocity = 0f
    private val gravity = 2f
    private val jumpStrength = -40f
    private val dinoSize = 80f
    private var groundLevel = 0f
    private var isJumping = false

    data class Obstacle(var x: Float, var width: Float, var height: Float)
    private val obstacles = mutableListOf<Obstacle>()
    private var obstacleSpeed = 15f

    // Game State
    private var isPlaying = false
    var score = 0
    private var frameCount = 0
    
    var onGameOver: ((Boolean) -> Unit)? = null 
    var onScore: (() -> Unit)? = null 
    var onStartGame: (() -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        groundLevel = h - 200f
        dinoY = groundLevel - dinoSize
    }

    fun update() {
        if (!isPlaying) return

        frameCount++
        
        // Difficulty Ramp
        if (frameCount % 600 == 0) {
           obstacleSpeed += 1f
        }

        // Score
        if (frameCount % 10 == 0) {
            score++
        }

        // Physics
        dinoY += dinoVelocity
        dinoVelocity += gravity

        // Ground Collision
        if (dinoY > groundLevel - dinoSize) {
            dinoY = groundLevel - dinoSize
            dinoVelocity = 0f
            isJumping = false
        }

        // Spawning Obstacles
        val minGap = 500f // Minimum distance between obstacles
        val lastObstacleX = obstacles.lastOrNull()?.x ?: 0f
        
        if (obstacles.isEmpty() || (width - lastObstacleX > minGap && Math.random() < 0.02)) {
             // Ensure gap is sufficient
             if (width - lastObstacleX > minGap) {
                 spawnObstacle()
             }
        }

        // Update Obstacles
        val obstaclesToRemove = mutableListOf<Obstacle>()
        for (obs in obstacles) {
            obs.x -= obstacleSpeed
            
            // Collision Detection (Rectangle Overlap)
            if (obs.x < 150f + dinoSize && // Dino X is fixed at roughly 100-150px
                obs.x + obs.width > 150f &&
                dinoY + dinoSize > groundLevel - obs.height) { // Obstacle sits on ground
                
                // Hit!
                isPlaying = false
                onGameOver?.invoke(false)
            }

            if (obs.x + obs.width < 0) {
                obstaclesToRemove.add(obs)
                // Bonus score for clearing? Or just time based.
            }
        }
        obstacles.removeAll(obstaclesToRemove)

        invalidate()
    }

    private fun spawnObstacle() {
        val height = (Math.random() * 60 + 40).toFloat() // 40-100 height
        obstacles.add(Obstacle(width.toFloat(), 40f, height))
    }

    fun jump() {
        if (!isJumping) {
            dinoVelocity = jumpStrength
            isJumping = true
        }
    }

    fun startGame() {
        score = 0
        frameCount = 0
        obstacleSpeed = 15f
        obstacles.clear()
        dinoY = groundLevel - dinoSize
        dinoVelocity = 0f
        isPlaying = true
        invalidate()
    }
    
    fun stopGame() {
        isPlaying = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (!isPlaying) {
                onStartGame?.invoke()
            } else {
                jump()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        canvas.drawColor(Color.WHITE) 

        // Draw Ground
        canvas.drawRect(0f, groundLevel, width.toFloat(), height.toFloat(), groundPaint)

        // Draw Dino
        // Fixed X position at 150f
        canvas.drawRect(150f, dinoY, 150f + dinoSize, dinoY + dinoSize, dinoPaint)
        
        // Draw Obstacles
        for (obs in obstacles) {
            canvas.drawRect(
                obs.x,
                groundLevel - obs.height,
                obs.x + obs.width,
                groundLevel,
                obstaclePaint
            )
        }
    }
}
