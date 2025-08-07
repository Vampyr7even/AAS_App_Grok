package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclEvaluationResultEntity

@Dao
interface PeclEvaluationResultDao {
    @Insert
    suspend fun insert(result: PeclEvaluationResultEntity)

    @Update
    suspend fun update(result: PeclEvaluationResultEntity)

    @Delete
    suspend fun delete(result: PeclEvaluationResultEntity)

    @Query("SELECT * FROM pecl_evaluation_results WHERE program = :program AND poi = :poi")
    suspend fun getResultsForProgramAndPoi(program: String, poi: String): List<PeclEvaluationResultEntity>

    @Query("SELECT AVG(CAST(score AS REAL)) FROM pecl_evaluation_results WHERE student_id = :studentId AND poi = :poi")
    suspend fun getAverageScore(studentId: Int, poi: String): Double?
}