package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationResultDao {
    @Insert
    suspend fun insert(result: PeclEvaluationResultEntity)

    @Query("SELECT * FROM pecl_evaluation_results WHERE student_id = :studentId")
    fun getEvaluationResultsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>>

    @Query("SELECT * FROM pecl_evaluation_results WHERE student_id = :studentId AND task_id = :taskId")
    fun getEvaluationsForStudentAndTask(studentId: Long, taskId: Long): Flow<List<PeclEvaluationResultEntity>>

    @Query("SELECT AVG(task_grade) FROM pecl_evaluation_results WHERE student_id = :studentId AND task_id = :taskId")
    fun getTaskGradeForStudent(studentId: Long, taskId: Long): Flow<Double?>

    @Query("DELETE FROM pecl_evaluation_results WHERE student_id = :studentId AND task_id = :taskId")
    suspend fun deleteEvaluationsForStudentAndTask(studentId: Long, taskId: Long)
}