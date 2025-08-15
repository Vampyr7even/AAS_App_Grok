package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.PoiProgramAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiProgramAssignmentDao {
    @Insert
    suspend fun insertAssignment(assignment: PoiProgramAssignmentEntity)

    @Query("DELETE FROM poi_program_assignments WHERE poi_id = :poiId")
    suspend fun deleteAssignmentsForPoi(poiId: Long)

    @Query("SELECT program_id FROM poi_program_assignments WHERE poi_id = :poiId")
    suspend fun getProgramIdsForPoi(poiId: Long): List<Long>

    @Query("SELECT name FROM pecl_programs WHERE id IN (SELECT program_id FROM poi_program_assignments WHERE poi_id = :poiId)")
    fun getProgramsForPoi(poiId: Long): Flow<List<String>>
}