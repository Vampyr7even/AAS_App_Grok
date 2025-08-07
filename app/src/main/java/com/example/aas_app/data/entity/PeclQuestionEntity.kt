package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_questions")
data class PeclQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subTask: String,
    val controlType: String,
    val scale: String,
    val criticalTask: String,
    val taskId: Int = 0 // Added for grouping by task in forms
)