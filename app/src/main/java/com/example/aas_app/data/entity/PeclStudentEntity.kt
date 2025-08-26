package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_students")
data class PeclStudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val firstName: String,
    val lastName: String,
    val grade: String,
    val pin: Int?,
    val fullName: String // Computed as "lastName, firstName"
)