package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.QuestionAssignmentEntity

@Dao
interface QuestionAssignmentDao {
    @Insert
    suspend fun insert(assignment: QuestionAssignmentEntity)

    @Update
    suspend fun update(assignment: QuestionAssignmentEntity)

    @Delete
    suspend fun delete(assignment: QuestionAssignmentEntity)

    @Query("SELECT * FROM question_assignments")
    suspend fun getAllAssignments(): List<QuestionAssignmentEntity>

    @Query("SELECT * FROM question_assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Int): QuestionAssignmentEntity?

    @Query("DELETE FROM question_assignments")
    suspend fun deleteAll()
}