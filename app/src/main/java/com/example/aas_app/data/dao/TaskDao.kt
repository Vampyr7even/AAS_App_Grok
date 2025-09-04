package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.TaskPoiAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: PeclTaskEntity): Long

    @Update
    suspend fun update(task: PeclTaskEntity)

    @Delete
    suspend fun delete(task: PeclTaskEntity)

    @Query("SELECT t.* FROM pecl_tasks t JOIN task_poi_assignments a ON t.id = a.task_id WHERE a.poi_id = :poiId")
    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>>

    @Query("SELECT * FROM pecl_tasks")
    fun getAllTasks(): Flow<List<PeclTaskEntity>>

    @Query("SELECT * FROM pecl_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): PeclTaskEntity?

    @Insert
    suspend fun insertTaskPoiAssignment(assignment: TaskPoiAssignmentEntity)

    @Query("DELETE FROM task_poi_assignments WHERE task_id = :taskId")
    suspend fun deleteTaskPoiAssignmentsForTask(taskId: Long)

    @Query("SELECT p.* FROM pecl_pois p INNER JOIN task_poi_assignments tpa ON p.id = tpa.poi_id WHERE tpa.task_id = :taskId")
    fun getPoisForTask(taskId: Long): Flow<List<PeclPoiEntity>>
}