package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_poi")
data class PeclPoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val peclPoi: String,
    val program_id: Int // New FK to pecl_programs.id
)