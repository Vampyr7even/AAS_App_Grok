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
    fun insert(template: DemoTemplatesEntity)

    @Update
    fun update(template: DemoTemplatesEntity)

    @Delete
    fun delete(template: DemoTemplatesEntity)

    @Query("SELECT * FROM demotemplates")
    fun getAllDemoTemplates(): List<DemoTemplatesEntity>

    @Query("SELECT * FROM demotemplates WHERE id = :id")
    fun getTemplateById(id: Int): DemoTemplatesEntity?
}