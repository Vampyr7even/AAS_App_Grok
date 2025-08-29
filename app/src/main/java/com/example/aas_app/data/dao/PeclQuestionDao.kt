package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.QuestionWithTask
import kotlinx.coroutines.flow.Flow

@Dao
interface PeclQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: PeclQuestionEntity): Long

    @Update
    suspend fun updateQuestion(question: PeclQuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: PeclQuestionEntity)

    @Query("SELECT * FROM pecl_questions")
    fun getAllQuestions(): Flow<List<PeclQuestionEntity>>

    @Query("""
        SELECT q.* FROM pecl_questions q
        INNER JOIN question_assignments a ON q.id = a.question_id
        INNER JOIN pecl_tasks t ON a.task_id = t.id
        INNER JOIN task_poi_assignments tp ON t.id = tp.task_id
        WHERE tp.poi_id = :poiId
    """)
    fun getQuestionsForPoi(poiId: Long): Flow<List<PeclQuestionEntity>>

    @Query("SELECT * FROM pecl_questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): PeclQuestionEntity?

    @Query("""
        SELECT q.* FROM pecl_questions q
        INNER JOIN question_assignments a ON q.id = a.question_id
        WHERE a.task_id = :taskId
    """)
    fun getQuestionsForTask(taskId: Long): Flow<List<PeclQuestionEntity>>

    @Query("SELECT * FROM pecl_questions WHERE sub_task = :subTask")
    suspend fun getQuestionBySubTask(subTask: String): PeclQuestionEntity?

    @Query("""
        SELECT 
            q.id AS question_id, 
            q.sub_task AS question_sub_task, 
            q.control_type AS question_control_type, 
            q.scale AS question_scale, 
            q.critical_task AS question_critical_task, 
            t.name AS taskName 
        FROM pecl_questions q
        INNER JOIN question_assignments a ON q.id = a.question_id
        INNER JOIN pecl_tasks t ON a.task_id = t.id
    """)
    @RewriteQueriesToDropUnusedColumns
    fun getAllQuestionsWithTasks(): Flow<List<QuestionWithTask>>
}