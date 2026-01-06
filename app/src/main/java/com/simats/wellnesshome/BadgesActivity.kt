package com.simats.wellnesshome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BadgesActivity : AppCompatActivity() {

    data class BadgeItem(val name: String, val iconRes: Int, val isUnlocked: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val rvBadges = findViewById<RecyclerView>(R.id.rvBadges)
        rvBadges.layoutManager = GridLayoutManager(this, 3)

        val badges = generateBadges()
        rvBadges.adapter = BadgeAdapter(badges)
    }

    private fun generateBadges(): List<BadgeItem> {
        val list = mutableListOf<BadgeItem>()
        
        // Define some "real" badges
        list.add(BadgeItem("7-Day Warrior", R.drawable.ic_fire, true))
        list.add(BadgeItem("Mood Master", R.drawable.ic_emoji_smile, true))
        list.add(BadgeItem("Early Bird", R.drawable.ic_moon, true)) // Using moon as placeholder for sunset/sunrise
        list.add(BadgeItem("Zen Master", R.drawable.ic_heart, true))
        
        // Fill the rest with Locked Badges (up to 50)
        for (i in 5..50) {
            list.add(BadgeItem("Locked Badge $i", R.drawable.ic_lock, false))
        }
        return list
    }

    class BadgeAdapter(private val items: List<BadgeItem>) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

        class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.ivBadgeIcon)
            val tvName: TextView = view.findViewById(R.id.tvBadgeName)
            val card: androidx.cardview.widget.CardView = view as androidx.cardview.widget.CardView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge_grid, parent, false)
            return BadgeViewHolder(view)
        }

        override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            
            if (item.isUnlocked) {
                holder.ivIcon.setImageResource(item.iconRes)
                holder.ivIcon.drawable.setTintList(null) // Clear tint for original colors
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#333333"))
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_lock)
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#BDBDBD"))
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#757575"))
            }
        }

        override fun getItemCount() = items.size
    }
}
