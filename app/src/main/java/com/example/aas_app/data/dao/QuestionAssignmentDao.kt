package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aas_app.data.entities.PeclQuestionAssignmentEntity

@Dao
interface QuestionAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: PeclQuestionAssignmentEntity)

    @Query("DELETE FROM question_assignments WHERE question_id = :questionId")
    suspend fun deleteAssignmentsForQuestion(questionId: Long)

    @Query("SELECT * FROM question_assignments WHERE task_id = :taskId")
    suspend fun getAssignmentsForTask(taskId: Long): List<PeclQuestionAssignmentEntity>
}