package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.ResponseEntity

@Dao
interface ResponseDao {
    @Insert
    fun insert(response: ResponseEntity)

    @Query("SELECT * FROM demographics_results WHERE userId = :userId")
    fun getResponsesForUser(userId: Int): List<ResponseEntity>
}