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
    suspend fun insert(project: ProjectEntity)

    @Query("SELECT * FROM projects")
    suspend fun getAllProjects(): List<ProjectEntity>

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
}