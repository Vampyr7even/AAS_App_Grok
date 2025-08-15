package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poi_program_assignments",
    foreignKeys = [
        ForeignKey(
            entity = PeclPoiEntity::class,
            parentColumns = ["id"],
            childColumns = ["poi_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PeclProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["poi_id", "program_id"], unique = true),
        Index(value = ["program_id"])
    ]
)
data class PoiProgramAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val poi_id: Long,
    val program_id: Long
)