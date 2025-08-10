package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val grade: String,
    val pin: Int?,
    val fullName: String, // Computed as "lastName, firstName"
    val assignedProject: String? = null,
    val role: String? = null // New: "instructor", "student", etc.
)