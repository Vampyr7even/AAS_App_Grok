package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_poi_assignments",
    foreignKeys = [
        ForeignKey(
            entity = PeclTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PeclPoiEntity::class,
            parentColumns = ["id"],
            childColumns = ["poi_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["task_id", "poi_id"], unique = true),
        Index(value = ["poi_id"])
    ]
)
data class TaskPoiAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val task_id: Long,
    val poi_id: Long
)