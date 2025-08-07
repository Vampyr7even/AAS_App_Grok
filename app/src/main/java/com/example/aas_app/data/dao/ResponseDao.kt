package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.ResponseEntity

@Dao
interface ResponseDao {
    @Insert
    suspend fun insert(response: ResponseEntity)

    @Query("SELECT * FROM demographics_results WHERE userId = :userId")
    suspend fun getResponsesForUser(userId: Int): List<ResponseEntity>
}