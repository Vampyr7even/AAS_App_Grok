package com.example.aas_app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    val grade: String,
    val pin: Int?,
    val role: String
)