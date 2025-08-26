package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instructor_program_assignments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["instructor_id"],
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
        Index(value = ["instructor_id", "program_id"], unique = true),
        Index(value = ["program_id"])
    ]
)
data class InstructorProgramAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val instructor_id: Long,
    val program_id: Long
)