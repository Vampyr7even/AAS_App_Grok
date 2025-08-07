package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instructor_student_assignments")
data class InstructorStudentAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val instructor_id: Int,
    val student_id: Int,
    val program_id: Int? = null // Optional scoping to program
)