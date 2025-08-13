package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.QuestionRepositoryEntity

@Dao
interface QuestionRepositoryDao {
    @Insert
    fun insert(question: QuestionRepositoryEntity)

    @Update
    fun update(question: QuestionRepositoryEntity)

    @Delete
    fun delete(question: QuestionRepositoryEntity)

    @Query("SELECT * FROM question_repository")
    fun getAllQuestions(): List<QuestionRepositoryEntity>

    @Query("SELECT * FROM question_repository WHERE id IN (:ids)")
    fun getQuestionsByIds(ids: List<Int>): List<QuestionRepositoryEntity>

    @Query("SELECT * FROM question_repository WHERE id = :id")
    fun getQuestionById(id: Int): QuestionRepositoryEntity?
}