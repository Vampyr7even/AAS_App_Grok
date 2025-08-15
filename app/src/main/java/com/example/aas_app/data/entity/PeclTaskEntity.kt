package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pecl_tasks",
    foreignKeys = [
        ForeignKey(
            entity = PeclPoiEntity::class,
            parentColumns = ["id"],
            childColumns = ["poi_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class PeclTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val poi_id: Long
)