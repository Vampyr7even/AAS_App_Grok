package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_pois")
data class PeclPoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val program_id: Long
)