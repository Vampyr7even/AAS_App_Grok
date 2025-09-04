package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.example.aas_app.data.entity.QuestionTaskAssignmentEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.QuestionWithTask
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert
    suspend fun insertQuestion(question: PeclQuestionEntity): Long

    @Update
    suspend fun updateQuestion(question: PeclQuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: PeclQuestionEntity)

    @Query("SELECT * FROM pecl_questions")
    fun getAllQuestions(): Flow<List<PeclQuestionEntity>>

    @Query("SELECT q.* FROM pecl_questions q INNER JOIN question_task_assignments qta ON q.id = qta.question_id WHERE qta.task_id = :taskId")
    fun getQuestionsForTask(taskId: Long): Flow<List<PeclQuestionEntity>>

    @Query("SELECT q.* FROM pecl_questions q INNER JOIN question_task_assignments qta ON q.id = qta.question_id INNER JOIN task_poi_assignments tpa ON qta.task_id = tpa.task_id WHERE tpa.poi_id = :poiId")
    fun getQuestionsForPoi(poiId: Long): Flow<List<PeclQuestionEntity>>

    @Insert
    suspend fun insertQuestionTaskAssignment(assignment: QuestionTaskAssignmentEntity)

    @Query("DELETE FROM question_task_assignments WHERE question_id = :questionId")
    suspend fun deleteQuestionTaskAssignmentsForQuestion(questionId: Long)

    @Query("SELECT * FROM pecl_questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Long): PeclQuestionEntity?

    @Query("SELECT task_id FROM question_task_assignments WHERE question_id = :questionId LIMIT 1")
    suspend fun getTaskIdForQuestion(questionId: Long): Long?

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT q.*, t.* FROM pecl_questions q LEFT JOIN question_task_assignments qta ON q.id = qta.question_id LEFT JOIN pecl_tasks t ON qta.task_id = t.id")
    fun getAllQuestionsWithTasks(): Flow<List<QuestionWithTask>>
}