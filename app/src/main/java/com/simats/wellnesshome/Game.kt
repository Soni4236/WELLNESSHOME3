package com.simats.wellnesshome

data class Game(
    val id: Int,
    val title: String,
    val description: String,
    val iconRes: Int,
    var isLocked: Boolean,
    val cost: Int,
    val targetActivity: Class<*>? = null
)
