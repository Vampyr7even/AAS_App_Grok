package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclStudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeclStudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: PeclStudentEntity): Long

    @Update
    suspend fun updateStudent(student: PeclStudentEntity)

    @Delete
    suspend fun deleteStudent(student: PeclStudentEntity)

    @Query("SELECT * FROM pecl_students")
    fun getAllStudents(): Flow<List<PeclStudentEntity>>

    @Query("SELECT * FROM pecl_students WHERE id = :id")
    fun getStudentById(id: Long): Flow<PeclStudentEntity?>
}