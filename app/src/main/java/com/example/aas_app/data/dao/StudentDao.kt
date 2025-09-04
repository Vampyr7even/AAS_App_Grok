package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclStudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: PeclStudentEntity)

    @Update
    suspend fun update(student: PeclStudentEntity)

    @Delete
    suspend fun delete(student: PeclStudentEntity)

    @Query("SELECT * FROM pecl_students")
    fun getAllStudents(): Flow<List<PeclStudentEntity>>

    @Query("SELECT s.* FROM pecl_students s INNER JOIN instructor_student_assignments isa ON s.id = isa.student_id WHERE isa.instructor_id = :instructorId AND isa.program_id = :programId")
    fun getStudentsForInstructorAndProgram(instructorId: Long, programId: Long): Flow<List<PeclStudentEntity>>

    @Query("SELECT * FROM pecl_students WHERE id = :studentId")
    fun getStudentById(studentId: Long): Flow<PeclStudentEntity>
}