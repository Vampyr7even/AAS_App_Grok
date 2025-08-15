package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeclProgramDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: PeclProgramEntity): Long

    @Update
    suspend fun updateProgram(program: PeclProgramEntity)

    @Delete
    suspend fun deleteProgram(program: PeclProgramEntity)

    @Query("SELECT * FROM pecl_programs")
    fun getAllPrograms(): Flow<List<PeclProgramEntity>>

    @Query("SELECT * FROM pecl_programs WHERE id = :id")
    suspend fun getProgramById(id: Long): PeclProgramEntity?

    @Query("SELECT * FROM pecl_programs WHERE name = :name")
    suspend fun getProgramByName(name: String): PeclProgramEntity?
}