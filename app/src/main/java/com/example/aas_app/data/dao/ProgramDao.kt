package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Insert
    suspend fun insert(program: PeclProgramEntity)

    @Update
    suspend fun update(program: PeclProgramEntity)

    @Delete
    suspend fun delete(program: PeclProgramEntity)

    @Query("SELECT * FROM pecl_programs")
    fun getAllPrograms(): Flow<List<PeclProgramEntity>>

    @Query("SELECT * FROM pecl_programs WHERE id = :programId")
    suspend fun getProgramById(programId: Long): PeclProgramEntity?
}