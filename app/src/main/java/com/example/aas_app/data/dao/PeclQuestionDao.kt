package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.example.aas_app.data.entity.PeclQuestionEntity

@Dao
interface PeclQuestionDao {
    @Insert
    suspend fun insert(question: PeclQuestionEntity): Long

    @Update
    suspend fun update(question: PeclQuestionEntity)

    @Delete
    suspend fun delete(question: PeclQuestionEntity)

    @Query("SELECT * FROM pecl_questions")
    suspend fun getAllQuestions(): List<PeclQuestionEntity>

    @Query("SELECT * FROM pecl_questions WHERE id = :id")
    suspend fun getQuestionById(id: Int): PeclQuestionEntity?

    // Updated: Proper JOIN query using new FK schema to get questions for a program/POI
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT q.* FROM pecl_questions q
        JOIN question_assignments qa ON q.id = qa.question_id
        JOIN pecl_tasks t ON qa.task_id = t.id
        JOIN pecl_poi p ON t.poi_id = p.id
        JOIN pecl_programs pr ON p.program_id = pr.id
        WHERE pr.peclProgram = :program AND p.peclPoi = :poi
    """)
    suspend fun getQuestionsForPoi(program: String, poi: String): List<PeclQuestionEntity>
}