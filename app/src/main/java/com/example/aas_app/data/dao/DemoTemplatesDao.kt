package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.DemoTemplatesEntity

@Dao
interface DemoTemplatesDao {
    @Insert
    suspend fun insert(template: DemoTemplatesEntity)

    @Update
    suspend fun update(template: DemoTemplatesEntity)

    @Delete
    suspend fun delete(template: DemoTemplatesEntity)

    @Query("SELECT * FROM demotemplates")
    suspend fun getAllDemoTemplates(): List<DemoTemplatesEntity>

    @Query("SELECT * FROM demotemplates WHERE id = :id")
    suspend fun getTemplateById(id: Int): DemoTemplatesEntity?
}