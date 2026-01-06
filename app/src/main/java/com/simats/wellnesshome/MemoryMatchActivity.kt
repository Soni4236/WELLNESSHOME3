package com.simats.wellnesshome

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.simats.wellnesshome.utils.FeedbackUtils

data class MemoryCard(
    val id: Int,
    val iconRes: Int,
    val colorHex: String,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)

class MemoryMatchActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var tvMoves: TextView
    private lateinit var tvScore: TextView

    private val cards = mutableListOf<MemoryCard>()
    private val cardViews = mutableListOf<CardView>()
    
    private var selectedCards = mutableListOf<Int>() // Indices of selected
    private var isProcessing = false
    private var moves = 0
    private var matches = 0

    // Icons to use (2 pairs of each to make 16 total = 8 pairs)
    // We only have Star, Heart, Moon, Cloud, Check (5).
    // Let's reuse them or add existing defaults.
    // We need 8 unique icons.
    // Defined: ic_star, ic_heart, ic_moon, ic_cloud, ic_check_circle (5 unique)
    // We need 3 more triggers.
    // Let's use standard Android common ones or just duplicate colors of stars?
    // Better: Reuse ic_brain (from logo), ic_emoji_smile, ic_arrow_back (maybe confusing).
    // Let's use: Star, Heart, Moon, Cloud, Check, Brain, Smile, Coins.
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_match)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        gridLayout = findViewById(R.id.gridLayoutCards)
        tvMoves = findViewById(R.id.tvMoves)
        tvScore = findViewById(R.id.tvScore)

        setupGame()
    }

    private fun setupGame() {
        // defined pairs of (IconRes, ColorHash)
        val gameIcons = listOf(
            Pair(R.drawable.ic_star, "#FFD700"), // Gold
            Pair(R.drawable.ic_heart, "#F06292"), // Pink/Red
            Pair(R.drawable.ic_moon, "#7986CB"), // Blue
            Pair(R.drawable.ic_cloud, "#4FC3F7"), // Light Blue
            Pair(R.drawable.ic_check_circle, "#66BB6A"), // Green
            Pair(R.drawable.ic_brain, "#AB47BC"), // Purple
            Pair(R.drawable.ic_emoji_smile, "#FFCA28"), // Orange
            Pair(R.drawable.ic_gamepad, "#5C6BC0") // Indigo (Coins replacement since ic_coins might not exist/be distinct)
        )

        cards.clear()
        gameIcons.forEachIndexed { index, (icon, color) ->
            cards.add(MemoryCard(index, icon, colorHex = color))
            cards.add(MemoryCard(index, icon, colorHex = color))
        }
        cards.shuffle()

        gridLayout.removeAllViews()
        cardViews.clear()
        
        // Calculate size
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val padding = (24 * displayMetrics.density).toInt() * 2
        val spacing = (8 * displayMetrics.density).toInt() * 4
        val availableWidth = screenWidth - padding - spacing
        val cardSize = availableWidth / 4

        cards.forEachIndexed { index, card ->
            val cardView = createCardView(index, cardSize)
            gridLayout.addView(cardView)
            cardViews.add(cardView)
        }
    }

    private fun createCardView(index: Int, sizePx: Int): CardView {
        val cardView = CardView(this)
        val marginPx = (4 * resources.displayMetrics.density).toInt()
        val params = GridLayout.LayoutParams()
        params.width = sizePx
        params.height = sizePx
        params.setMargins(marginPx, marginPx, marginPx, marginPx)
        cardView.layoutParams = params
        
        cardView.radius = (12 * resources.displayMetrics.density)
        cardView.cardElevation = 4f
        cardView.setCardBackgroundColor(Color.parseColor("#CE93D8")) // Hidden Color (Purple)

        // Text/Icon container (Hidden by default)
        val imageView = ImageView(this)
        imageView.layoutParams =  android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT, android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
        val padding = (16 * resources.displayMetrics.density).toInt()
        imageView.setPadding(padding, padding, padding, padding)
        imageView.setImageResource(cards[index].iconRes)
        imageView.imageTintList = ColorStateList.valueOf(Color.parseColor(cards[index].colorHex))
        imageView.visibility = android.view.View.INVISIBLE // Hide icon initially
        cardView.addView(imageView)

        // Question mark text for hidden state
        val tvQuestion = TextView(this)
        tvQuestion.text = "?"
        tvQuestion.textSize = 24f
        tvQuestion.setTextColor(Color.WHITE)
        tvQuestion.gravity = Gravity.CENTER
        tvQuestion.layoutParams = android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT, android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
        cardView.addView(tvQuestion)

        cardView.setOnClickListener {
            onCardClick(index)
        }

        return cardView
    }

    private fun onCardClick(index: Int) {
        if (isProcessing) return
        val card = cards[index]
        if (card.isFaceUp || card.isMatched) return

        // Flip Face Up
        flipCard(index, true)
        selectedCards.add(index)

        if (selectedCards.size == 2) {
            moves++
            tvMoves.text = "Moves: $moves"
            checkForMatch()
        }
    }

    private fun flipCard(index: Int, faceUp: Boolean) {
        val card = cards[index]
        val view = cardViews[index]
        card.isFaceUp = faceUp

        val imageView = view.getChildAt(0) as ImageView
        val tvQuestion = view.getChildAt(1) as TextView

        if (faceUp) {
            view.setCardBackgroundColor(Color.WHITE)
            imageView.visibility = android.view.View.VISIBLE
            tvQuestion.visibility = android.view.View.INVISIBLE
        } else {
            view.setCardBackgroundColor(Color.parseColor("#CE93D8"))
            imageView.visibility = android.view.View.INVISIBLE
            tvQuestion.visibility = android.view.View.VISIBLE
        }
    }

    private fun checkForMatch() {
        val index1 = selectedCards[0]
        val index2 = selectedCards[1]
        val card1 = cards[index1]
        val card2 = cards[index2]

        isProcessing = true
        
        if (card1.id == card2.id) {
            // Match!
            card1.isMatched = true
            card2.isMatched = true
            matches++
            tvScore.text = "$matches / 8"
            
            // Effect
            FeedbackUtils.playSuccessSound(this)
            FeedbackUtils.showSuccessVisual(this, "Matched!")
            
            selectedCards.clear()
            isProcessing = false
            
            if (matches == 8) {
                FeedbackUtils.showSuccessVisual(this, "All Matched! +15 Coins")
                // Award Coins
            }
        } else {
            // No Match
            Handler(Looper.getMainLooper()).postDelayed({
                flipCard(index1, false)
                flipCard(index2, false)
                selectedCards.clear()
                isProcessing = false
            }, 800)
        }
    }
}
