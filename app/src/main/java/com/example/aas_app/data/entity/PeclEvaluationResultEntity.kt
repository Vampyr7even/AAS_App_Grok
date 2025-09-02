package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pecl_evaluation_results",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["instructor_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PeclQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["student_id"]),
        Index(value = ["instructor_id"]),
        Index(value = ["question_id"])
    ]
)
data class PeclEvaluationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val student_id: Long,
    val instructor_id: Long,
    val question_id: Long,
    val score: Double,
    val comment: String = "",
    val timestamp: Long,
    val task_id: Long = 0L,
    val task_grade: Double? = null
)