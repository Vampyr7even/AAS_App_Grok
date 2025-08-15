package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.TaskPoiAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskPoiAssignmentDao {
    @Insert
    suspend fun insertAssignment(assignment: TaskPoiAssignmentEntity)

    @Query("DELETE FROM task_poi_assignments WHERE task_id = :taskId")
    suspend fun deleteAssignmentsForTask(taskId: Long)

    @Query("SELECT poi_id FROM task_poi_assignments WHERE task_id = :taskId")
    suspend fun getPoiIdsForTask(taskId: Long): List<Long>

    @Query("SELECT name FROM pecl_pois WHERE id IN (SELECT poi_id FROM task_poi_assignments WHERE task_id = :taskId)")
    fun getPoisForTask(taskId: Long): Flow<List<String>>
}