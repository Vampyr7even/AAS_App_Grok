package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: QuestionAssignmentEntity): Long

    @Query("DELETE FROM question_assignments WHERE question_id = :questionId")
    suspend fun deleteAssignmentsForQuestion(questionId: Long)

    // New: Get assignment by question ID
    @Query("SELECT * FROM question_assignments WHERE question_id = :questionId LIMIT 1")
    fun getAssignmentByQuestionId(questionId: Long): Flow<QuestionAssignmentEntity?>
}