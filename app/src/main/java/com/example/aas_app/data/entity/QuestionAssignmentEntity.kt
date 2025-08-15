package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_assignments",
    foreignKeys = [
        ForeignKey(
            entity = PeclQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PeclTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["question_id", "task_id"], unique = true),
        Index(value = ["task_id"])
    ]
)
data class QuestionAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val question_id: Long,
    val task_id: Long
)