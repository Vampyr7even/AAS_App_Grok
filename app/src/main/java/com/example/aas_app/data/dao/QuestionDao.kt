package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.aas_app.data.entity.PeclTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeclTaskDao {
    @Insert
    suspend fun insertTask(task: PeclTaskEntity): Long

    @Update
    suspend fun updateTask(task: PeclTaskEntity)

    @Delete
    suspend fun deleteTask(task: PeclTaskEntity)

    @Query("SELECT * FROM pecl_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): PeclTaskEntity?

    @Query("SELECT * FROM pecl_tasks")
    fun getAllTasks(): Flow<List<PeclTaskEntity>>

    @Transaction
    @Query("SELECT * FROM pecl_tasks WHERE id IN (SELECT task_id FROM task_poi_assignments WHERE poi_id = :poiId)")
    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>>
}