package com.simats.wellnesshome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GameStatisticsActivity : AppCompatActivity() {

    private lateinit var rvGameStats: RecyclerView
    private lateinit var adapter: GameStatsAdapter
    private val gamesList = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_statistics)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.cardHabitStreaks).setOnClickListener {
            startActivity(Intent(this, HabitStreaksActivity::class.java))
        }
        rvGameStats = findViewById(R.id.rvGameStats)

        setupGamesList()
        setupRecyclerView()
        updateTotalStats()
    }

    private fun setupGamesList() {
        // Populate same games list but maybe with extra stats "metadata" if we had it
        // For now using the same Game object, and we'll fake/randomize the "stats" display in the adapter for visual effect
        // as requested by user to match the design image.

        gamesList.add(Game(1, "Daily Riddles", "Challenge your mind", R.drawable.ic_brain, false, 0, DailyRiddlesActivity::class.java))
        gamesList.add(Game(2, "Word Builder", "Expand your vocabulary", R.drawable.ic_brain, false, 0, WordBuilderActivity::class.java))
        gamesList.add(Game(3, "Memory Match", "Pattern matching", R.drawable.ic_gamepad, false, 0, MemoryMatchActivity::class.java))
        gamesList.add(Game(4, "Breathing Patterns", "Anxiety relief", R.drawable.ic_emoji_smile, false, 0, BreathingExerciseActivity::class.java))
        gamesList.add(Game(5, "Classic Snake", "Snake game", R.drawable.ic_gamepad, false, 0, SnakeGameActivity::class.java))
        
        gamesList.add(Game(6, "Pixel Pong", "Arcade tennis", R.drawable.ic_gamepad, checkLocked(6), 50, PixelPongActivity::class.java))
        gamesList.add(Game(7, "Retro Bricks", "Break the bricks", R.drawable.ic_gamepad, checkLocked(7), 50, RetroBricksActivity::class.java))
        gamesList.add(Game(8, "Space Defender", "Space shooter", R.drawable.ic_gamepad, checkLocked(8), 75, SpaceDefenderActivity::class.java))
        gamesList.add(Game(9, "Meteor Dodge", "Dodge meteors", R.drawable.ic_gamepad, checkLocked(9), 100, MeteorDodgeActivity::class.java))
        gamesList.add(Game(10, "Dino Run", "Infinite runner", R.drawable.ic_gamepad, checkLocked(10), 150, DinoRunActivity::class.java))
    }

    private fun checkLocked(id: Int): Boolean {
        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        return !sharedPref.getBoolean("GAME_${id}_UNLOCKED", false)
    }

    private fun setupRecyclerView() {
        adapter = GameStatsAdapter(gamesList) { game ->
            handleGameClick(game)
        }
        rvGameStats.layoutManager = LinearLayoutManager(this)
        rvGameStats.adapter = adapter
    }
    
    private fun updateTotalStats() {
        // In a real app, calculate from DB
        // Setting randomish plausible values
        findViewById<TextView>(R.id.tvTotalGames).text = (12..48).random().toString()
        findViewById<TextView>(R.id.tvAvgScore).text = "${(75..98).random()}%"
        findViewById<TextView>(R.id.tvStreak).text = (3..15).random().toString()
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
        val userPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val newBalance = currentCoins - game.cost
        userPref.edit().putInt("COIN_BALANCE", newBalance).apply()
        
        val gamePref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        gamePref.edit().putBoolean("GAME_${game.id}_UNLOCKED", true).apply()
        
        game.isLocked = false
        adapter.notifyDataSetChanged()
        
        Toast.makeText(this, "Unlocked!", Toast.LENGTH_SHORT).show()
    }
    
    // Adapter Class
    inner class GameStatsAdapter(
        private val games: List<Game>,
        private val onGameClick: (Game) -> Unit
    ) : RecyclerView.Adapter<GameStatsAdapter.StatViewHolder>() {

        inner class StatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvGameTitle)
            val tvCoin: TextView = view.findViewById(R.id.tvCoinReward)
            val tvPlayed: TextView = view.findViewById(R.id.tvPlayedCount)
            val tvAccuracy: TextView = view.findViewById(R.id.tvAccuracy)
            
            fun bind(game: Game) {
                tvTitle.text = game.title
                
                if (game.isLocked) {
                    tvCoin.text = "Locked (${game.cost} coins)"
                    tvCoin.setTextColor(android.graphics.Color.GRAY)
                } else {
                    // Fake stats for UI demo
                    val coins = if (game.id < 6) 20 else 50
                    val played = (1..20).random()
                    val acc = (70..100).random()
                    
                    tvCoin.text = "+ $coins coins"
                    tvCoin.setTextColor(android.graphics.Color.parseColor("#CE93D8"))
                    tvPlayed.text = "$played played"
                    tvAccuracy.text = "$acc% accuracy"
                }
                
                itemView.setOnClickListener { onGameClick(game) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_stat, parent, false)
            return StatViewHolder(view)
        }

        override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
            holder.bind(games[position])
        }

        override fun getItemCount() = games.size
    }
}
