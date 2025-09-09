package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.aas_app.data.entity.ScaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScaleDao {
    @Insert
    suspend fun insertScale(scale: ScaleEntity): Long

    @Transaction
    suspend fun insertScales(vararg scales: ScaleEntity) {
        scales.forEach { insertScale(it) }
    }

    @Update
    suspend fun updateScale(scale: ScaleEntity)

    @Delete
    suspend fun deleteScale(scale: ScaleEntity)

    @Query("SELECT * FROM pecl_scales")
    fun getAllScales(): Flow<List<ScaleEntity>>

    @Query("SELECT * FROM pecl_scales WHERE scale_name = :name")
    suspend fun getScaleByName(name: String): ScaleEntity?

    @Query("SELECT * FROM pecl_scales WHERE id = :id")
    fun getScaleById(id: Long): Flow<ScaleEntity?>
}