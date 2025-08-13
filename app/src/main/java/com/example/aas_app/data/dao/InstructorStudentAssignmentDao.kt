package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorStudentAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssignment(assignment: InstructorStudentAssignmentEntity): Long

    @Query("SELECT u.* FROM users u JOIN instructor_student_assignments a ON u.id = a.student_id WHERE a.instructor_id = :instructorId")
    fun getStudentsForInstructor(instructorId: Long): Flow<List<UserEntity>>

    @Query("SELECT u.* FROM users u JOIN instructor_student_assignments a ON u.id = a.student_id WHERE a.program_id = :programId")
    fun getStudentsForProgram(programId: Long): Flow<List<UserEntity>>
}