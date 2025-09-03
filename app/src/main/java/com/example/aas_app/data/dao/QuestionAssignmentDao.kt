package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.viewmodel.QuestionWithTask
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionAssignmentDao {

    @Insert
    suspend fun insertAssignment(assignment: QuestionAssignmentEntity): Long

    @Query("DELETE FROM question_assignments WHERE question_id = :questionId")
    suspend fun deleteAssignmentsForQuestion(questionId: Long)

    @Query("SELECT * FROM question_assignments WHERE question_id = :questionId LIMIT 1")
    fun getAssignmentByQuestionId(questionId: Long): Flow<QuestionAssignmentEntity?>

    @Query("""
        SELECT
            q.id AS question_id,
            q.sub_task AS question_sub_task,
            q.control_type AS question_control_type,
            q.scale AS question_scale,
            q.critical_task AS question_critical_task,
            t.name AS taskName
        FROM pecl_questions q
        INNER JOIN question_assignments a ON q.id = a.question_id
        INNER JOIN pecl_tasks t ON a.task_id = t.id
    """)
    fun getAllQuestionsWithTasks(): Flow<List<QuestionWithTask>>

    @Query("""
        SELECT pecl_students.* 
        FROM pecl_students 
        JOIN instructor_student_assignments 
        ON pecl_students.id = instructor_student_assignments.student_id 
        WHERE instructor_student_assignments.instructor_id = :instructorId 
        AND instructor_student_assignments.program_id = :programId
    """)
    fun getStudentsForInstructorAndProgram(instructorId: Long, programId: Long): Flow<List<PeclStudentEntity>>
}