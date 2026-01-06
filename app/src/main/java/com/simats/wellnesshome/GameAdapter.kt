package com.simats.wellnesshome

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameAdapter(
    private val games: List<Game>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGameIcon: ImageView = itemView.findViewById(R.id.ivGameIcon)
        val tvGameTitle: TextView = itemView.findViewById(R.id.tvGameTitle)
        val tvGameDesc: TextView = itemView.findViewById(R.id.tvGameDesc)
        val btnAction: LinearLayout = itemView.findViewById(R.id.btnAction)
        val ivLockIcon: ImageView = itemView.findViewById(R.id.ivLockIcon)
        val tvCost: TextView = itemView.findViewById(R.id.tvCost)
        val tvPlay: TextView = itemView.findViewById(R.id.tvPlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        
        holder.tvGameTitle.text = game.title
        holder.tvGameDesc.text = game.description
        holder.ivGameIcon.setImageResource(game.iconRes)
        
        if (game.isLocked) {
            holder.btnAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E1BEE7")) // Light Purple for Locked
            holder.ivLockIcon.visibility = View.VISIBLE
            holder.tvCost.visibility = View.VISIBLE
            holder.tvCost.text = "${game.cost} coins"
            holder.tvPlay.visibility = View.GONE
        } else {
            holder.btnAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0F2F1")) // Light Teal for Play
            holder.ivLockIcon.visibility = View.GONE
            holder.tvCost.visibility = View.GONE
            holder.tvPlay.visibility = View.VISIBLE
        }
        
        holder.itemView.setOnClickListener {
            onGameClick(game)
        }
    }

    override fun getItemCount() = games.size
}
