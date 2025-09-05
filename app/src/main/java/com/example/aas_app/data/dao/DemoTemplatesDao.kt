package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.DemoTemplateEntity

@Dao
interface DemoTemplatesDao {
    @Insert
    suspend fun insert(template: DemoTemplateEntity)

    @Update
    suspend fun update(template: DemoTemplateEntity)

    @Delete
    suspend fun delete(template: DemoTemplateEntity)

    @Query("SELECT * FROM demo_templates")
    suspend fun getAllDemoTemplates(): List<DemoTemplateEntity>

    @Query("SELECT * FROM demo_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): DemoTemplateEntity?
}