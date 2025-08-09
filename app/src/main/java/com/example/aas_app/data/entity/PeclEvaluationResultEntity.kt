package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_evaluation_results")
data class PeclEvaluationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val student_id: Int,
    val instructor_id: Int,
    val question_id: Int,
    val score: Double,
    val comment: String = "",
    val timestamp: Long
)