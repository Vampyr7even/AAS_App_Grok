package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aas_app.data.entity.QuestionAssignmentEntity

@Dao
interface QuestionAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: QuestionAssignmentEntity)

    @Query("DELETE FROM question_assignments WHERE question_id = :questionId")
    suspend fun deleteAssignmentsForQuestion(questionId: Long)

    @Query("SELECT * FROM question_assignments WHERE task_id = :taskId")
    suspend fun getAssignmentsForTask(taskId: Long): List<QuestionAssignmentEntity>
}