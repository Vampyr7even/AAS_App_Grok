package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_programs")
data class PeclProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)