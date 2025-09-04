package com.example.aas_app.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class QuestionWithTask(
    @Embedded val question: PeclQuestionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = QuestionTaskAssignmentEntity::class,
            parentColumn = "question_id",
            entityColumn = "task_id"
        )
    )
    val task: PeclTaskEntity?
)