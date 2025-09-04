package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Insert
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM comments WHERE student_id = :studentId")
    fun getCommentsForStudent(studentId: Long): Flow<List<CommentEntity>>
}