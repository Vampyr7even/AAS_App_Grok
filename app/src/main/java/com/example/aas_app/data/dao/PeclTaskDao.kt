package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeclTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PeclTaskEntity): Long

    @Update
    suspend fun updateTask(task: PeclTaskEntity)

    @Delete
    suspend fun deleteTask(task: PeclTaskEntity)

    @Query("SELECT t.* FROM pecl_tasks t JOIN task_poi_assignments a ON t.id = a.task_id WHERE a.poi_id = :poiId")
    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>>

    @Query("SELECT * FROM pecl_tasks")
    fun getAllTasks(): Flow<List<PeclTaskEntity>>

    @Query("SELECT * FROM pecl_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): PeclTaskEntity?

    @Query("SELECT * FROM pecl_tasks WHERE name = :name")
    suspend fun getTaskByName(name: String): PeclTaskEntity?
}