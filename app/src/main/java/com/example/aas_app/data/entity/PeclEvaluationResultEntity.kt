package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_evaluation_results")
data class PeclEvaluationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val student_id: Long,
    val instructor_id: Long,
    val question_id: Long,
    val score: Double,
    val comment: String = "",
    val timestamp: Long
)