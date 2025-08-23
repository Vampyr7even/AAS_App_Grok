package com.example.aas_app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pecl_questions",
    foreignKeys = [
        ForeignKey(
            entity = PeclTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["task_id"])]
)
data class PeclQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val task_id: Long? = null,
    @ColumnInfo(name = "sub_task") val subTask: String,
    @ColumnInfo(name = "control_type") val controlType: String,
    val scale: String,
    @ColumnInfo(name = "critical_task") val criticalTask: String
)