package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ]
)
data class PeclEvaluationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val student_id: Long,
    val instructor_id: Long,
    val question_id: Long,
    val score: Double,
    val comment: String = "",
    val timestamp: Long
)