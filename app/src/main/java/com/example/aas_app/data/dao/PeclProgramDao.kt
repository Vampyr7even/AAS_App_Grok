package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclProgramEntity

@Dao
interface PeclProgramDao {
    @Insert
    suspend fun insert(program: PeclProgramEntity): Long

    @Update
    suspend fun update(program: PeclProgramEntity)

    @Delete
    suspend fun delete(program: PeclProgramEntity)

    @Query("SELECT * FROM pecl_programs")
    suspend fun getAllPrograms(): List<PeclProgramEntity>

    @Query("SELECT * FROM pecl_programs WHERE id = :id")
    suspend fun getProgramById(id: Int): PeclProgramEntity?

    @Query("DELETE FROM pecl_programs")
    suspend fun deleteAll()
}