package com.example.salsabilapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val age: Int = 0,
    val sex: String = "male", // male, female, other
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val hasKidneyCondition: Boolean = false,
    val dailyGoalMl: Int = 2000,
    val isGoalOverridden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
