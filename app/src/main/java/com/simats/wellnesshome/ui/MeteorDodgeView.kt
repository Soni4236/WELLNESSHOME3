package com.simats.wellnesshome.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class MeteorDodgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val shipPaint = Paint().apply {
        color = Color.parseColor("#29B6F6") // Light Blue
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val meteorPaint = Paint().apply {
        color = Color.parseColor("#795548") // Brown/Rock color
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // For meteor details/texture effect (simple)
    private val meteorDetailPaint = Paint().apply {
        color = Color.parseColor("#5D4037") // Darker Brown
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val starPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Game Objects
    private var shipX = 0f
    private var shipY = 0f
    private val shipSize = 70f
    
    data class Meteor(var x: Float, var y: Float, var radius: Float, var speed: Float)
    private val meteors = mutableListOf<Meteor>()
    
    data class Star(var x: Float, var y: Float, var speed: Float)
    private val stars = mutableListOf<Star>()

    // Game State
    private var isPlaying = false
    var score = 0
    private var frameCount = 0
    private var difficultyMultiplier = 1.0f
    
    var onGameOver: ((Boolean) -> Unit)? = null // true if won (not really applicable for survival, but maybe high score?)
    var onScore: (() -> Unit)? = null // Tick sound
    var onStartGame: (() -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shipX = w / 2f
        shipY = h - 200f
        
        // Initialize Stars
        if (stars.isEmpty()) {
            for (i in 0..60) {
                stars.add(Star(
                    (Math.random() * w).toFloat(),
                    (Math.random() * h).toFloat(),
                    (Math.random() * 8 + 2).toFloat()
                ))
            }
        }
    }

    fun update() {
        if (!isPlaying) return

        frameCount++
        
        // Difficulty ramp up
        if (frameCount % 600 == 0) { // Every ~10 seconds
            difficultyMultiplier += 0.1f
        }

        // Update Stars (Starfield effect)
        for (star in stars) {
            star.y += star.speed * difficultyMultiplier // Stars speed up too to give feeling of speed
            if (star.y > height) {
                star.y = 0f
                star.x = (Math.random() * width).toFloat()
            }
        }

        // Spawn Meteors
        // Spawn rate increases with difficulty
        val spawnThreshold = (30 / difficultyMultiplier).coerceAtLeast(5f).toInt()
        if (frameCount % spawnThreshold == 0) {
            spawnMeteor()
        }

        // Update Meteors
        val meteorsToRemove = mutableListOf<Meteor>()
        for (meteor in meteors) {
            meteor.y += meteor.speed * difficultyMultiplier
            
            // Check Collision
            val dx = shipX - meteor.x
            val dy = shipY - meteor.y
            val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            
            if (distance < meteor.radius + shipSize/2) {
                // Crash!
                isPlaying = false
                onGameOver?.invoke(false)
            }
            
            if (meteor.y > height + meteor.radius) {
                meteorsToRemove.add(meteor)
                score += 10 // Points for dodging
                onScore?.invoke()
            }
        }
        meteors.removeAll(meteorsToRemove)

        invalidate()
    }

    private fun spawnMeteor() {
        val radius = (Math.random() * 40 + 20).toFloat() // 20 to 60 radius
        val x = (Math.random() * (width - radius * 2)).toFloat() + radius
        val speed = (Math.random() * 10 + 5).toFloat() // 5 to 15 speed
        meteors.add(Meteor(x, -radius, radius, speed))
    }

    fun startGame() {
        score = 0
        frameCount = 0
        difficultyMultiplier = 1.0f
        meteors.clear()
        isPlaying = true
        invalidate()
    }
    
    fun stopGame() {
        isPlaying = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isPlaying) {
                    onStartGame?.invoke()
                }
                shipX = event.x
                shipX = shipX.coerceIn(shipSize, width - shipSize)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPlaying) {
                    shipX = event.x
                    shipX = shipX.coerceIn(shipSize, width - shipSize)
                    invalidate()
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        canvas.drawColor(Color.parseColor("#121212")) // Dark background

        // Draw Stars
        for (star in stars) {
            canvas.drawCircle(star.x, star.y, (Math.random() * 4 + 1).toFloat(), starPaint)
        }

        // Draw Meteors
        for (meteor in meteors) {
            canvas.drawCircle(meteor.x, meteor.y, meteor.radius, meteorPaint)
            // Simple detail: a smaller circle inside
            canvas.drawCircle(meteor.x - meteor.radius/3, meteor.y - meteor.radius/3, meteor.radius/2, meteorDetailPaint)
        }

        // Draw Ship (Futuristic Shape)
        val path = Path()
        path.moveTo(shipX, shipY - shipSize) // Nose
        path.lineTo(shipX - shipSize/2, shipY + shipSize/2) // Left Wing
        path.lineTo(shipX, shipY + shipSize/4) // Engine indent
        path.lineTo(shipX + shipSize/2, shipY + shipSize/2) // Right Wing
        path.close()
        canvas.drawPath(path, shipPaint)
        
        // Engine flame
        val flamePaint = Paint().apply { color = Color.parseColor("#FF5722"); style = Paint.Style.FILL }
        canvas.drawCircle(shipX, shipY + shipSize/2 + 10, 10f, flamePaint)
    }
}
