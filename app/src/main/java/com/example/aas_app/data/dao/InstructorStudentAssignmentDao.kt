package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity

@Dao
interface InstructorStudentAssignmentDao {
    @Insert
    suspend fun insert(assignment: InstructorStudentAssignmentEntity)

    @Update
    suspend fun update(assignment: InstructorStudentAssignmentEntity)

    @Delete
    suspend fun delete(assignment: InstructorStudentAssignmentEntity)

    @Query("SELECT * FROM instructor_student_assignments")
    suspend fun getAllAssignments(): List<InstructorStudentAssignmentEntity>

    @Query("SELECT * FROM instructor_student_assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Int): InstructorStudentAssignmentEntity?

    @Query("DELETE FROM instructor_student_assignments")
    suspend fun deleteAll()
}