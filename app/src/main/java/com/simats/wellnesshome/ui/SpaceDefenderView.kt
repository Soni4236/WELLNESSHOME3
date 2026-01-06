package com.simats.wellnesshome.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SpaceDefenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val shipPaint = Paint().apply {
        color = Color.parseColor("#42A5F5") // Blue
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val enemyPaint = Paint().apply {
        color = Color.parseColor("#EF5350") // Red
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val bulletPaint = Paint().apply {
        color = Color.parseColor("#FFEE58") // Yellow
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
    private val shipSize = 80f
    
    data class Bullet(var x: Float, var y: Float, var active: Boolean)
    private val bullets = mutableListOf<Bullet>()
    private val bulletSpeed = 25f
    private val bulletRadius = 8f

    data class Enemy(var x: Float, var y: Float, var speed: Float, var active: Boolean)
    private val enemies = mutableListOf<Enemy>()
    private val enemySize = 60f
    
    data class Star(var x: Float, var y: Float, var speed: Float)
    private val stars = mutableListOf<Star>()

    // Game State
    private var isPlaying = false
    var score = 0
    var lives = 3
    private var frameCount = 0
    
    var onGameOver: ((Boolean) -> Unit)? = null // true if won (reached score limit?)
    var onScore: (() -> Unit)? = null
    var onLifeLost: ((Int) -> Unit)? = null
    var onStartGame: (() -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shipX = w / 2f
        shipY = h - 150f
        
        // Initialize Stars
        if (stars.isEmpty()) {
            for (i in 0..50) {
                stars.add(Star(
                    (Math.random() * w).toFloat(),
                    (Math.random() * h).toFloat(),
                    (Math.random() * 5 + 2).toFloat()
                ))
            }
        }
    }

    fun update() {
        if (!isPlaying) return

        frameCount++

        // Update Stars
        for (star in stars) {
            star.y += star.speed
            if (star.y > height) {
                star.y = 0f
                star.x = (Math.random() * width).toFloat()
            }
        }

        // Spawn Enemies
        if (frameCount % 60 == 0) { // Approx every second
            spawnEnemy()
        }

        // Update Bullets
        val bulletsToRemove = mutableListOf<Bullet>()
        for (bullet in bullets) {
            bullet.y -= bulletSpeed
            if (bullet.y < 0) {
                bulletsToRemove.add(bullet)
            }
        }
        bullets.removeAll(bulletsToRemove)

        // Update Enemies
        for (enemy in enemies) {
            if (!enemy.active) continue
            
            enemy.y += enemy.speed
            
            // Check Collision with Ship
            if (enemy.y + enemySize > shipY - shipSize/2 && 
                enemy.y < shipY + shipSize/2 &&
                enemy.x + enemySize > shipX - shipSize/2 &&
                enemy.x < shipX + shipSize/2) {
                
                enemy.active = false
                lives--
                onLifeLost?.invoke(lives)
                checkGameOver()
            }
            
            // Out of bounds
            if (enemy.y > height) {
                enemy.active = false
                // Optional: Penalize for missing enemies? For now, no.
            }
        }
        
        // Check Bullet-Enemy Collisions
        for (bullet in bullets) {
            for (enemy in enemies) {
                if (enemy.active && 
                    bullet.x > enemy.x - enemySize/2 && 
                    bullet.x < enemy.x + enemySize/2 &&
                    bullet.y > enemy.y - enemySize/2 && 
                    bullet.y < enemy.y + enemySize/2) {
                    
                    enemy.active = false
                    bullet.active = false // Mark bullet for removal (needs cleanup logic)
                    score += 10
                    onScore?.invoke()
                    
                    // Win Condition? E.g. Score 500
                    if (score >= 500) {
                        isPlaying = false
                        onGameOver?.invoke(true)
                    }
                }
            }
        }
        // Cleanup inactive enemies/bullets
        enemies.removeAll { !it.active }
        bullets.removeAll { !it.active && it.y >= 0 } // tricky remove logic

        // Auto Fire? Or Tap? Let's auto fire every 20 frames
        if (frameCount % 20 == 0) {
            fireBullet()
        }

        invalidate()
    }

    private fun spawnEnemy() {
        val x = (Math.random() * (width - enemySize)).toFloat() + enemySize/2
        val speed = (Math.random() * 5 + 5).toFloat() // 5 to 10
        enemies.add(Enemy(x, -50f, speed, true))
    }

    private fun fireBullet() {
        bullets.add(Bullet(shipX, shipY - shipSize/2, true))
    }
    
    private fun checkGameOver() {
        if (lives <= 0) {
            isPlaying = false
            onGameOver?.invoke(false)
        }
    }

    fun startGame() {
        score = 0
        lives = 3
        enemies.clear()
        bullets.clear()
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
                // Also update position
                shipX = event.x
                shipX = shipX.coerceIn(shipSize/2, width - shipSize/2)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPlaying) {
                    shipX = event.x
                    shipX = shipX.coerceIn(shipSize/2, width - shipSize/2)
                    invalidate()
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        canvas.drawColor(Color.parseColor("#ECEFF1")) // Very Light Gray background fails for space
        canvas.drawColor(Color.BLACK) 

        // Draw Stars
        for (star in stars) {
            canvas.drawCircle(star.x, star.y, 3f, starPaint)
        }

        // Draw Ship (Triangle)
        val path = Path()
        path.moveTo(shipX, shipY - shipSize/2) // Top
        path.lineTo(shipX - shipSize/2, shipY + shipSize/2) // Bottom Left
        path.lineTo(shipX + shipSize/2, shipY + shipSize/2) // Bottom Right
        path.close()
        canvas.drawPath(path, shipPaint)

        // Draw Enemies (Blocky Invaders)
        for (enemy in enemies) {
            canvas.drawRect(
                enemy.x - enemySize/2,
                enemy.y - enemySize/2,
                enemy.x + enemySize/2,
                enemy.y + enemySize/2,
                enemyPaint
            )
        }

        // Draw Bullets
        for (bullet in bullets) {
            canvas.drawCircle(bullet.x, bullet.y, bulletRadius, bulletPaint)
        }
    }
}
