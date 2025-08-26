package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instructor_student_assignments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["instructor_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PeclStudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PeclProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["instructor_id"]),
        Index(value = ["student_id"]),
        Index(value = ["program_id"])
    ]
)
data class InstructorStudentAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val instructor_id: Long,
    val student_id: Long,
    val program_id: Long? = null
)