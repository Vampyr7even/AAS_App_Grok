package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scales")
data class ScaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scaleName: String,
    val scaleData: String = "" // Added field for comma-separated items
)