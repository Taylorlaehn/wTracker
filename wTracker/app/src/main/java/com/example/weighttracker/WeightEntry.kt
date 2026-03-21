package com.example.weighttracker

data class WeightEntry(
    val id: Int = 0,
    val weight: Double,
    val unit: String = "lbs",
    val timestamp: Long = System.currentTimeMillis()
)
