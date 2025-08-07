package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_tasks")
data class PeclTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val peclTask: String,
    val poi_id: Int // New FK to pecl_poi.id
)