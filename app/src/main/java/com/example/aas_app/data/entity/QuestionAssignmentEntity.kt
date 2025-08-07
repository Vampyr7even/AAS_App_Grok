package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_assignments")
data class QuestionAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question_id: Int,
    val task_id: Int
)