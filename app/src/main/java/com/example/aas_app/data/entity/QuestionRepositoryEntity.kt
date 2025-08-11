package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_repository")
data class QuestionRepositoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val field: String,
    val inputType: String, // "TextBox" or "ComboBox"
    val options: String // Comma-separated for ComboBox, empty for TextBox
)