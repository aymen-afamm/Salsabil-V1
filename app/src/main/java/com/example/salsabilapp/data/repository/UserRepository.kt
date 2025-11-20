package com.example.salsabilapp.data.repository

import com.example.salsabilapp.data.local.dao.UserProfileDao
import com.example.salsabilapp.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userProfileDao: UserProfileDao
) {
    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? = userProfileDao.getUserProfileOnce()

    suspend fun saveUserProfile(userProfile: UserProfile): Long {
        return userProfileDao.insertUserProfile(userProfile.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        userProfileDao.updateUserProfile(userProfile.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteAllProfiles() {
        userProfileDao.deleteAllProfiles()
    }
}
