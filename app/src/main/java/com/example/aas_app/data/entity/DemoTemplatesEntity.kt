package com.example.aas_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "demotemplates")
data class DemoTemplatesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val templateName: String,
    val selectedItems: String // Comma-separated IDs, e.g., "1,3,5"
)