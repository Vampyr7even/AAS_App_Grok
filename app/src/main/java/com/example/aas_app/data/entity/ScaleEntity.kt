package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_scales")
data class ScaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scaleName: String,
    val options: String
)