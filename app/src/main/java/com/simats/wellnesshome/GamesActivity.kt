package com.simats.wellnesshome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GamesActivity : AppCompatActivity() {

    private lateinit var rvGames: RecyclerView
    private lateinit var tvCoinBalance: TextView
    private lateinit var adapter: GameAdapter
    private val gamesList = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_games)

        rvGames = findViewById(R.id.rvGames)
        tvCoinBalance = findViewById(R.id.tvCoinBalance)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        setupGamesList()
        setupRecyclerView()
        updateCoinDisplay()
    }

    private fun setupGamesList() {
        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        
        // Games 1-5 (Unlocked by default)
        // Games 6-10 (Locked)
        
        gamesList.add(Game(1, "Daily Riddles", "Challenge your mind with daily puzzles", R.drawable.ic_brain, false, 0, DailyRiddlesActivity::class.java))
        gamesList.add(Game(2, "Word Builder", "Create words and expand your vocabulary", R.drawable.ic_brain, false, 0, WordBuilderActivity::class.java))
        gamesList.add(Game(3, "Memory Match", "Improve memory with pattern matching", R.drawable.ic_gamepad, false, 0, MemoryMatchActivity::class.java))
        gamesList.add(Game(4, "Breathing Patterns", "Guided breathing for anxiety relief", R.drawable.ic_emoji_smile, false, 0, BreathingExerciseActivity::class.java))
        gamesList.add(Game(5, "Classic Snake", "The legendary Nokia snake game!", R.drawable.ic_gamepad, false, 0, SnakeGameActivity::class.java))
        
        gamesList.add(Game(6, "Pixel Pong", "Retro arcade tennis action", R.drawable.ic_gamepad, checkLocked(6), 50, PixelPongActivity::class.java))
        gamesList.add(Game(7, "Retro Bricks", "Break the bricks, stack the score", R.drawable.ic_gamepad, checkLocked(7), 50, RetroBricksActivity::class.java))
        gamesList.add(Game(8, "Space Defender", "Defend the galaxy from pixel invaders", R.drawable.ic_gamepad, checkLocked(8), 75, SpaceDefenderActivity::class.java))
        gamesList.add(Game(9, "Meteor Dodge", "Dodge falling meteors in space", R.drawable.ic_gamepad, checkLocked(9), 100, MeteorDodgeActivity::class.java))
        gamesList.add(Game(10, "Dino Run", "Run for your life in prehistoric times", R.drawable.ic_gamepad, checkLocked(10), 150, DinoRunActivity::class.java))
    }

    private fun checkLocked(id: Int): Boolean {
        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        // If "UNLOCKED_GAME_ID" exists, it's unlocked. Default true (locked) for ID > 5
        return !sharedPref.getBoolean("GAME_${id}_UNLOCKED", false)
    }

    private fun setupRecyclerView() {
        adapter = GameAdapter(gamesList) { game ->
            handleGameClick(game)
        }
        rvGames.layoutManager = LinearLayoutManager(this)
        rvGames.adapter = adapter
    }

    private fun handleGameClick(game: Game) {
        if (game.isLocked) {
            showUnlockDialog(game)
        } else {
            if (game.targetActivity != null) {
                startActivity(Intent(this, game.targetActivity))
            } else {
                Toast.makeText(this, "Starting ${game.title}...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUnlockDialog(game: Game) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)

        AlertDialog.Builder(this)
            .setTitle("Unlock ${game.title}?")
            .setMessage("This game costs ${game.cost} coins. You have $currentCoins coins.")
            .setPositiveButton("Unlock") { _, _ ->
                if (currentCoins >= game.cost) {
                    unlockGame(game, currentCoins)
                } else {
                    Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun unlockGame(game: Game, currentCoins: Int) {
        // Deduct Coins
        val userPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val newBalance = currentCoins - game.cost
        userPref.edit().putInt("COIN_BALANCE", newBalance).apply()
        
        // Unlock Game
        val gamePref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        gamePref.edit().putBoolean("GAME_${game.id}_UNLOCKED", true).apply()
        
        // Update Local List
        game.isLocked = false
        adapter.notifyDataSetChanged()
        
        updateCoinDisplay()
        Toast.makeText(this, "Unlocked!", Toast.LENGTH_SHORT).show()
    }

    private fun updateCoinDisplay() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentCoins = sharedPref.getInt("COIN_BALANCE", 0)
        tvCoinBalance.text = "$currentCoins Coins"
    }
}
