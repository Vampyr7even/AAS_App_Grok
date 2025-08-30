package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorStudentAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: InstructorStudentAssignmentEntity): Long

    @Query("SELECT s.* FROM pecl_students s JOIN instructor_student_assignments a ON s.id = a.student_id WHERE a.instructor_id = :instructorId")
    fun getStudentsForInstructor(instructorId: Long): Flow<List<PeclStudentEntity>>

    @Query("SELECT s.* FROM pecl_students s JOIN instructor_student_assignments a ON s.id = a.student_id WHERE a.program_id = :programId")
    fun getStudentsForProgram(programId: Long): Flow<List<PeclStudentEntity>>

    @Query("SELECT s.* FROM pecl_students s JOIN instructor_student_assignments a ON s.id = a.student_id WHERE a.instructor_id = :instructorId AND a.program_id = :programId")
    fun getStudentsForInstructorAndProgram(instructorId: Long, programId: Long): Flow<List<PeclStudentEntity>>

    @Query("SELECT * FROM instructor_student_assignments WHERE instructor_id = :instructorId")
    fun getAssignmentsForInstructor(instructorId: Long): Flow<List<InstructorStudentAssignmentEntity>>

    @Query("DELETE FROM instructor_student_assignments WHERE instructor_id = :instructorId")
    suspend fun deleteAssignmentsForInstructor(instructorId: Long)

    // New method for fetching assignment by student ID
    @Query("SELECT * FROM instructor_student_assignments WHERE student_id = :studentId LIMIT 1")
    suspend fun getAssignmentForStudent(studentId: Long): InstructorStudentAssignmentEntity?
}