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
    fun insertTask(task: PeclTaskEntity): Long

    @Update
    fun updateTask(task: PeclTaskEntity)

    @Delete
    fun deleteTask(task: PeclTaskEntity)

    @Query("SELECT * FROM pecl_tasks WHERE poi_id = :poiId")
    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>>

    @Query("SELECT * FROM pecl_tasks WHERE id = :id")
    fun getTaskById(id: Long): PeclTaskEntity?

    @Query("SELECT * FROM pecl_tasks WHERE name = :name")
    fun getTaskByName(name: String): PeclTaskEntity?
}