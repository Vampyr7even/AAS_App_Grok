package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.InstructorProgramAssignmentEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorProgramAssignmentDao {
    @Insert
    suspend fun insert(assignment: InstructorProgramAssignmentEntity): Long

    @Query("DELETE FROM instructor_program_assignments WHERE instructor_id = :instructorId")
    suspend fun deleteAssignmentsForInstructor(instructorId: Long)

    @Query("SELECT * FROM pecl_programs WHERE id IN (SELECT program_id FROM instructor_program_assignments WHERE instructor_id = :instructorId)")
    fun getProgramsForInstructor(instructorId: Long): Flow<List<PeclProgramEntity>>

    @Query("SELECT program_id FROM instructor_program_assignments WHERE instructor_id = :instructorId")
    fun getProgramIdsForInstructor(instructorId: Long): Flow<List<Long>>
}