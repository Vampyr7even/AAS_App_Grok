package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aas_app.data.entities.PeclEvaluationResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluationResult(result: PeclEvaluationResultEntity): Long

    @Query("SELECT * FROM pecl_evaluation_results WHERE student_id = :studentId")
    fun getEvaluationResultsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>>

    @Query("SELECT AVG(score) FROM pecl_evaluation_results WHERE student_id = :studentId AND question_id IN (SELECT q.id FROM pecl_questions q JOIN question_assignments a ON q.id = a.question_id JOIN pecl_tasks t ON a.task_id = t.id JOIN pecl_pois p ON t.poi_id = p.id WHERE p.id = :poiId)")
    suspend fun getAverageScoreForStudent(studentId: Long, poiId: Long): Double

    @Query("SELECT * FROM pecl_evaluation_results WHERE question_id = :questionId")
    suspend fun getEvaluationsForQuestion(questionId: Long): List<PeclEvaluationResultEntity>
}