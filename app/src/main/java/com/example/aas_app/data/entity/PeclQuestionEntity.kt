package com.example.aas_app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pecl_questions")
data class PeclQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "sub_task") val subTask: String,
    @ColumnInfo(name = "control_type") val controlType: String,
    val scale: String,
    @ColumnInfo(name = "critical_task") val criticalTask: String
)