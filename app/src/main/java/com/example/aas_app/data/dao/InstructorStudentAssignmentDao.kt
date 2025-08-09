package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aas_app.data.entities.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorStudentAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: InstructorStudentAssignmentEntity): Long

    @Query("SELECT u.* FROM users u JOIN instructor_student_assignments a ON u.id = a.student_id WHERE a.instructor_id = :instructorId")
    fun getStudentsForInstructor(instructorId: Long): Flow<List<UserEntity>>
}