package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.ScaleEntity

@Dao
interface ScaleDao {
    @Insert
    suspend fun insert(scale: ScaleEntity)

    @Update
    suspend fun update(scale: ScaleEntity)

    @Delete
    suspend fun delete(scale: ScaleEntity)

    @Query("SELECT * FROM scales")
    suspend fun getAllScales(): List<ScaleEntity>

    @Query("SELECT * FROM scales WHERE id = :id")
    suspend fun getScaleById(id: Int): ScaleEntity?

    @Query("DELETE FROM scales")
    suspend fun deleteAll()
}