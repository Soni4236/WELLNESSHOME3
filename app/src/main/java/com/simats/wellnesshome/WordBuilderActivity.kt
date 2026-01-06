package com.simats.wellnesshome

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.setMargins
import java.util.Stack
import com.simats.wellnesshome.utils.FeedbackUtils

data class WordLevel(
    val letters: String,
    val validWords: List<String>
)

class WordBuilderActivity : AppCompatActivity() {

    private lateinit var tvInput: TextView
    private lateinit var tvFoundWords: TextView
    private lateinit var tvProgress: TextView
    private lateinit var btnSubmit: AppCompatButton
    private lateinit var gridLayout: GridLayout
    
    private var currentInput = StringBuilder()
    private val foundWords = mutableListOf<String>()
    private var currentLevel: WordLevel? = null
    
    private val pressedButtonsStack = Stack<AppCompatButton>()

    // Expanded levels list
    private val levels = listOf(
        WordLevel("TEKITCH", listOf("KIT", "ITCH", "HIT", "THE", "TICK", "CHICK", "THICK", "KITE")),
        WordLevel("SNETALP", listOf("PLAN", "ANT", "NET", "TAP", "TEN", "SET", "LATE", "TALE", "PAST", "LANE", "PLANET")),
        WordLevel("DREGSAN", listOf("RED", "SEA", "EAR", "SAD", "RAN", "NEAR", "READ", "SAND", "SEND", "GEAR", "GARDEN")),
        WordLevel("REHTAWE", listOf("EAT", "TEA", "RAW", "HAT", "WET", "HER", "THE", "WAR", "EAR", "HEAT", "WEAR", "HERE")),
        WordLevel("GNIMROM", listOf("RIG", "RIM", "NOR", "GIN", "RING", "TRIM", "GRIN", "IRON", "MORNING")),
        WordLevel("ELBBUB", listOf("PUB", "RUB", "TUB", "BET", "LET", "BLEU", "BLUE", "BULB", "DAUB", "BUBBLE")),
        WordLevel("SREKLAW", listOf("ASK", "RAW", "SAW", "SEW", "WAR", "EAR", "ERA", "LAKE", "REAL", "SEAL", "WALK", "WALKER")),
        WordLevel("YADILOH", listOf("AID", "HAY", "HID", "LAD", "LAY", "LID", "OIL", "OLD", "DAILY", "HOLD", "HOLIDAY")),
        WordLevel("REMMUS", listOf("USE", "EMS", "MUSE", "USER", "SURE", "SERUM", "SUMMER")),
        WordLevel("REHCAET", listOf("ACT", "ARE", "ART", "AWE", "CAR", "CAT", "EAR", "EAT", "ERA", "HAT", "HER", "RAT", "TEA", "THE", "CARE", "CART", "CHAT", "EACH", "HATE", "HEAR", "HEAT", "HERE", "RACE", "RATE", "TEAR", "TEACHER"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_builder)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        tvInput = findViewById(R.id.tvInput)
        tvFoundWords = findViewById(R.id.tvFoundWords)
        tvProgress = findViewById(R.id.tvProgress)
        btnSubmit = findViewById(R.id.btnSubmit)
        gridLayout = findViewById(R.id.gridLayoutLetters)

        startNewLevel()

        btnSubmit.setOnClickListener {
            checkWord()
        }
    }

    private fun startNewLevel() {
        currentLevel = levels.random()
        foundWords.clear()
        currentInput.clear()
        pressedButtonsStack.clear() // Safety clear
        updateUI()
        setupGrid()
        
        // Reset submit button state
        btnSubmit.isEnabled = false
        btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B0BEC5"))
        tvInput.text = ""
        tvInput.hint = "Tap letters to build words"
    }

    private fun setupGrid() {
        gridLayout.removeAllViews()
        val letters = currentLevel?.letters?.toCharArray()?.toList()?.shuffled() ?: return

        // Calculate exact button size
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val padding = (24 * displayMetrics.density).toInt() * 2 // 24dp padding on each side of parent layout
        val spacing = (4 * displayMetrics.density).toInt() * 8 // 4dp margins * 2 per button * 4 buttons = approx spacing overhead
        // More robust calculation
        // Parent padding is 24dp. Grid width is match_parent inside padding.
        // We want 4 columns.
        val availableWidth = screenWidth - padding
        val itemWidth = availableWidth / 4 - (16 * displayMetrics.density).toInt() // safety margin subtraction
        val buttonSize = maxOf(itemWidth, (60 * displayMetrics.density).toInt())

        // 7 Letter Buttons
        letters.forEach { char ->
            val btn = createLetterButton(char.toString(), buttonSize)
            gridLayout.addView(btn)
        }

        // Add Refresh/Clear Button
        val btnClear = createActionIcon(buttonSize)
        gridLayout.addView(btnClear)
    }

    private fun createLetterButton(text: String, sizePx: Int): AppCompatButton {
        val btn = AppCompatButton(this)
        val marginPx = (4 * resources.displayMetrics.density).toInt()

        val params = GridLayout.LayoutParams()
        params.width = sizePx
        params.height = sizePx
        params.setMargins(marginPx, marginPx, marginPx, marginPx)
        btn.layoutParams = params
        
        btn.text = text
        btn.textSize = 22f
        btn.typeface = Typeface.DEFAULT_BOLD
        btn.setTextColor(Color.parseColor("#333333"))
        btn.gravity = Gravity.CENTER
        btn.setPadding(0, 0, 0, 0)
        btn.background = getDrawable(R.drawable.rounded_card)
        btn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        btn.stateListAnimator = null

        btn.setOnClickListener {
            appendLetter(text, btn)
        }
        return btn
    }

    private fun createActionIcon(sizePx: Int): AppCompatButton {
        val btn = AppCompatButton(this)
        val marginPx = (4 * resources.displayMetrics.density).toInt()

        val params = GridLayout.LayoutParams()
        params.width = sizePx
        params.height = sizePx
        params.setMargins(marginPx, marginPx, marginPx, marginPx)
        btn.layoutParams = params
        
        btn.background = getDrawable(R.drawable.rounded_card)
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F48FB1")) // Pink
        
        // Use standard text just in case drawable confuses things, but try foreground if valid
        // Actually, let's use a simple character for reliability if standard icon fails, 
        // Or specific drawable. I'll use the drawable I created.
        btn.foreground = getDrawable(R.drawable.ic_refresh)
        btn.foregroundGravity = Gravity.CENTER
        
        btn.setOnClickListener {
            clearInput() 
        }
        return btn
    }

    private fun appendLetter(text: String, btn: AppCompatButton) {
        currentInput.append(text)
        tvInput.text = currentInput.toString()
        
        btn.isEnabled = false
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
        btn.setTextColor(Color.LTGRAY)
        
        pressedButtonsStack.push(btn)
        
        btnSubmit.isEnabled = true
        btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#80CBC4")) // Teal
    }

    private fun clearInput() {
        while (pressedButtonsStack.isNotEmpty()) {
            val lastBtn = pressedButtonsStack.pop()
            lastBtn.isEnabled = true
            lastBtn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            lastBtn.setTextColor(Color.parseColor("#333333"))
        }
        currentInput.clear()
        tvInput.text = ""
        tvInput.hint = "Tap letters to build words"
        
        btnSubmit.isEnabled = false
        btnSubmit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B0BEC5"))
    }

    private fun checkWord() {
        val word = currentInput.toString()
        val validWords = currentLevel?.validWords ?: return

        if (foundWords.contains(word)) {
            Toast.makeText(this, "Already found!", Toast.LENGTH_SHORT).show()
            clearInput()
            return
        }

        if (validWords.contains(word) || CommonWords.isValid(word)) {
            // Correct!
            foundWords.add(word)
            FeedbackUtils.playSuccessSound(this)
            FeedbackUtils.showSuccessVisual(this, "Found: $word")
            
            // Award Coins
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
            sharedPref.edit().putInt("COIN_BALANCE", currentCoins + 2).apply()
            
            updateUI()
            clearInput()
            
            if (foundWords.size >= 5) { 
                 Toast.makeText(this, "Level Complete! +10 Bonus", Toast.LENGTH_LONG).show()
                 sharedPref.edit().putInt("COIN_BALANCE", currentCoins + 10).apply()
            }
        } else {
             Toast.makeText(this, "Not a valid word", Toast.LENGTH_SHORT).show()
        }
    }
    
    object CommonWords {
        // A small subset of common 3-7 letter words for fallback validation
        private val dictionary = setOf(
            "THE", "AND", "THA", "ENT", "ION", "TIO", "FOR", "NDE", "HAS", "NCE", "EDT", "TIS", "OFT", "STH", "MEN",
            "CAT", "DOG", "BAT", "RUN", "EAT", "ATE", "TEA", "ART", "RAT", "TAR", "CAR", "EAR", "ERA", "ARE", 
            "NET", "TEN", "SET", "PET", "LET", "BET", "GET", "MET", "WET", "YET", "HIT", "SIT", "FIT", "KIT", "LIT", "BIT",
            "HOT", "POT", "COT", "DOT", "LOT", "NOT", "ROT", "GOT", "NUT", "CUT", "BUT", "HUT", "RUT", "GUT",
            "MAN", "PAN", "CAN", "TAN", "RAN", "FAN", "VAN", "BAN", "DAN", "JAM", "HAM", "SAM", "RAM", "DAM",
            "LIP", "TIP", "SIP", "RIP", "DIP", "HIP", "ZIP", "NIP", "SAP", "LAP", "TAP", "RAP", "MAP", "CAP", "GAP",
            "RED", "BED", "FED", "LED", "WED", "SAD", "MAD", "LAD", "PAD", "DAD", "BAD", "HAD", "FAD", "TAD",
            "BIG", "DIG", "FIG", "IGW", "JIG", "PIG", "RIG", "WIG", "BOG", "DOG", "FOG", "HOG", "JOG", "LOG",
            "BUG", "DUG", "HUG", "JUG", "LUG", "MUG", "PUG", "RUG", "TUG", "FUN", "BUN", "GUN", "PUN", "RUN", "SUN",
            "DAY", "HAY", "LAY", "MAY", "PAY", "RAY", "SAY", "WAY", "JAY", "BAY", "GAY", "NAY",
            "BOX", "FOX", "POX", "SIX", "FIX", "MIX", "TAX", "WAX", "AXE", "EYE", "DYE", "BYE", "RYE",
            "ICE", "AGE", "APE", "ACE", "ACT", "ADD", "ADO", "AID", "AIM", "AIR", "ALE", "ALL", "AMP", "AND", "ANT", "ANY", "ARC", "ARM", "ART", "ASH", "ASK", "ASP", "ASS",
            "PLAN", "PLANT" // Add more as needed or use a real resource file approach later
        )
        
        fun isValid(word: String): Boolean {
            return dictionary.contains(word.uppercase())
        }
    }

    private fun updateUI() {
        tvFoundWords.text = foundWords.joinToString(", ")
        tvProgress.text = "${foundWords.size} words found"
    }
}
