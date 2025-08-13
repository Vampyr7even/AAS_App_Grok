package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeclQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuestion(question: PeclQuestionEntity): Long

    @Update
    fun updateQuestion(question: PeclQuestionEntity)

    @Delete
    fun deleteQuestion(question: PeclQuestionEntity)

    @Query("SELECT * FROM pecl_questions")
    fun getAllQuestions(): Flow<List<PeclQuestionEntity>>

    @Query("""
        SELECT q.* FROM pecl_questions q
        INNER JOIN question_assignments a ON q.id = a.question_id
        INNER JOIN pecl_tasks t ON a.task_id = t.id
        INNER JOIN pecl_pois p ON t.poi_id = p.id
        INNER JOIN pecl_programs pr ON p.program_id = pr.id
        WHERE pr.name = :program AND p.name = :poi
    """)
    fun getQuestionsForPoi(program: String, poi: String): Flow<List<PeclQuestionEntity>>

    @Query("SELECT * FROM pecl_questions WHERE id = :id")
    fun getQuestionById(id: Long): PeclQuestionEntity?

    @Query("""
        SELECT q.* FROM pecl_questions q
        INNER JOIN question_assignments a ON q.id = a.question_id
        INNER JOIN pecl_tasks t ON a.task_id = t.id
        WHERE t.id = :taskId
    """)
    fun getQuestionsForTask(taskId: Long): Flow<List<PeclQuestionEntity>>

    @Query("""
        SELECT q.* FROM pecl_questions q
        INNER JOIN question_assignments a ON q.id = a.question_id
        INNER JOIN pecl_tasks t ON a.task_id = t.id
        WHERE t.poi_id = :poiId
    """)
    fun getQuestionsForPoi(poiId: Long): Flow<List<PeclQuestionEntity>>
}