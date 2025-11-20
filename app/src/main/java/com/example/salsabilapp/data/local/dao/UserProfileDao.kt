package com.example.salsabilapp.data.local.dao

import androidx.room.*
import com.example.salsabilapp.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile): Long

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun deleteAllProfiles()
}
