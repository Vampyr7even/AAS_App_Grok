package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclTaskEntity

@Dao
interface PeclTaskDao {
    @Insert
    suspend fun insert(task: PeclTaskEntity)

    @Update
    suspend fun update(task: PeclTaskEntity)

    @Delete
    suspend fun delete(task: PeclTaskEntity)

    @Query("SELECT * FROM pecl_tasks")
    suspend fun getAllTasks(): List<PeclTaskEntity>

    @Query("SELECT * FROM pecl_tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): PeclTaskEntity?

    @Query("DELETE FROM pecl_tasks")
    suspend fun deleteAll()
}