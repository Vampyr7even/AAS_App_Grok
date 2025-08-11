package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instructor_student_assignments")
data class InstructorStudentAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val instructor_id: Long,
    val student_id: Long,
    val program_id: Long? = null
)