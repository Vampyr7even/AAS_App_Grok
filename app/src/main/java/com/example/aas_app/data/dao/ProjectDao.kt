package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.ProjectEntity

@Dao
interface ProjectDao {
    @Insert
    fun insert(project: ProjectEntity)

    @Query("SELECT * FROM projects")
    fun getAllProjects(): List<ProjectEntity>

    @Update
    fun update(project: ProjectEntity)

    @Delete
    fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Int): ProjectEntity?

    @Query("DELETE FROM projects")
    fun deleteAll()
}