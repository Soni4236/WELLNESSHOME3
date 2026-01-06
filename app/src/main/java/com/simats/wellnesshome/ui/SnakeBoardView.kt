package com.simats.wellnesshome.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.View

class SnakeBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val snakePaint = Paint().apply {
        color = Color.parseColor("#00695C") // Dark Teal
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val foodPaint = Paint().apply {
        color = Color.parseColor("#FF8A80") // Coral
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.parseColor("#B2DFDB") // Light Teal Grid Lines
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    // Game State
    private var snakeBody: List<Point> = emptyList()
    private var foodPosition: Point? = null
    
    // Grid Config
    private val numColumns = 15
    private val numRows = 20
    private var cellSize = 0f

    fun updateGame(snake: List<Point>, food: Point) {
        this.snakeBody = snake
        this.foodPosition = food
        invalidate() // Redraw
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Calculate cell size to best fit the view
        val cellWidth = w.toFloat() / numColumns
        val cellHeight = h.toFloat() / numRows
        cellSize = minOf(cellWidth, cellHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val boardWidth = cellSize * numColumns
        val boardHeight = cellSize * numRows
        val offsetX = (width - boardWidth) / 2
        val offsetY = (height - boardHeight) / 2

        // Draw Snake Body
        // To look like a "real snake", we draw circles for segments.
        // For a smoother look, we could draw rects between connecting segments, but circles are a good start for "organic".
        
        snakeBody.forEachIndexed { index, point ->
            val cx = offsetX + point.x * cellSize + cellSize / 2
            val cy = offsetY + point.y * cellSize + cellSize / 2
            val radius = (cellSize / 2) - 2 // small padding
            
            if (index == 0) {
                // Head - Draw distinctive
                snakePaint.color = Color.parseColor("#004D40") // Darker Head
                canvas.drawCircle(cx, cy, radius, snakePaint)
                
                // Eyes (Small white dots)
                snakePaint.color = Color.WHITE
                canvas.drawCircle(cx - radius/3, cy - radius/3, 4f, snakePaint)
                canvas.drawCircle(cx + radius/3, cy - radius/3, 4f, snakePaint)
                
                snakePaint.color = Color.parseColor("#004D40") // Reset
            } else {
                // Body
                snakePaint.color = Color.parseColor("#00695C")
                canvas.drawCircle(cx, cy, radius, snakePaint)
            }
        }

        // Draw Food (Apple)
        foodPosition?.let { point ->
            val cx = offsetX + point.x * cellSize + cellSize / 2
            val cy = offsetY + point.y * cellSize + cellSize / 2
            val radius = (cellSize / 2) - 4
            
            foodPaint.color = Color.parseColor("#FF5252") // Red Apple
            canvas.drawCircle(cx, cy, radius, foodPaint)
            
            // Stem
            foodPaint.color = Color.parseColor("#33691E")
            canvas.drawRect(cx - 2, cy - radius, cx + 2, cy - radius + 5, foodPaint) 
        }
    }
}
