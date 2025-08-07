package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_evaluation_results")
data class PeclEvaluationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val evaluatorId: Int,
    val evaluateeName: String? = null,
    val program: String,
    val poi: String,
    val task: String,
    val subTask: String,
    val score: String,
    val comment: String? = null,
    val timestamp: String,
    val student_id: Int, // New FK to users.id (student)
    val instructor_id: Int, // New FK to users.id (instructor)
    val question_id: Int // New FK to pecl_questions.id
)