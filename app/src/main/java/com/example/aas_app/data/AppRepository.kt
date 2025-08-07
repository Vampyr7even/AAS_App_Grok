package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.entity.DemoTemplatesEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.ProjectEntity
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.QuestionRepositoryEntity
import com.example.aas_app.data.entity.ResponseEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.UserEntity

class AppRepository(private val database: AppDatabase) {

    suspend fun insertUser(user: UserEntity): Result<Unit> = try {
        database.userDao().insert(user)
        Log.d("AppRepository", "Inserted user: ${user.fullName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert user: ${user.fullName}", e)
        Result.Error(e)
    }

    suspend fun updateUser(user: UserEntity): Result<Unit> = try {
        database.userDao().update(user)
        Log.d("AppRepository", "Updated user: ${user.fullName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update user: ${user.fullName}", e)
        Result.Error(e)
    }

    suspend fun deleteUser(user: UserEntity): Result<Unit> = try {
        database.userDao().delete(user)
        Log.d("AppRepository", "Deleted user: ${user.fullName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete user: ${user.fullName}", e)
        Result.Error(e)
    }

    suspend fun getAllUsers(): Result<List<UserEntity>> = try {
        Result.Success(database.userDao().getAllUsers())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all users", e)
        Result.Error(e)
    }

    suspend fun getUsersByAssignedProject(project: String): Result<List<UserEntity>> = try {
        Result.Success(database.userDao().getUsersByAssignedProject(project))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get users by project: $project", e)
        Result.Error(e)
    }

    suspend fun insertQuestion(question: QuestionRepositoryEntity): Result<Unit> = try {
        database.questionRepositoryDao().insert(question)
        Log.d("AppRepository", "Inserted question: ${question.field}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert question: ${question.field}", e)
        Result.Error(e)
    }

    suspend fun updateQuestion(question: QuestionRepositoryEntity): Result<Unit> = try {
        database.questionRepositoryDao().update(question)
        Log.d("AppRepository", "Updated question: ${question.field}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update question: ${question.field}", e)
        Result.Error(e)
    }

    suspend fun deleteQuestion(question: QuestionRepositoryEntity): Result<Unit> = try {
        database.questionRepositoryDao().delete(question)
        Log.d("AppRepository", "Deleted question: ${question.field}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete question: ${question.field}", e)
        Result.Error(e)
    }

    suspend fun getAllQuestions(): Result<List<QuestionRepositoryEntity>> = try {
        Result.Success(database.questionRepositoryDao().getAllQuestions())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all questions", e)
        Result.Error(e)
    }

    suspend fun getQuestionsByIds(ids: List<Int>): Result<List<QuestionRepositoryEntity>> = try {
        Result.Success(database.questionRepositoryDao().getQuestionsByIds(ids))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get questions by IDs: $ids", e)
        Result.Error(e)
    }

    suspend fun insertTemplate(template: DemoTemplatesEntity): Result<Unit> = try {
        database.demoTemplatesDao().insert(template)
        Log.d("AppRepository", "Inserted template: ${template.templateName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert template: ${template.templateName}", e)
        Result.Error(e)
    }

    suspend fun updateDemoTemplate(template: DemoTemplatesEntity): Result<Unit> = try {
        database.demoTemplatesDao().update(template)
        Log.d("AppRepository", "Updated template: ${template.templateName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update template: ${template.templateName}", e)
        Result.Error(e)
    }

    suspend fun deleteDemoTemplate(template: DemoTemplatesEntity): Result<Unit> = try {
        database.demoTemplatesDao().delete(template)
        Log.d("AppRepository", "Deleted template: ${template.templateName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete template: ${template.templateName}", e)
        Result.Error(e)
    }

    suspend fun getAllDemoTemplates(): Result<List<DemoTemplatesEntity>> = try {
        Result.Success(database.demoTemplatesDao().getAllDemoTemplates())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all demo templates", e)
        Result.Error(e)
    }

    suspend fun insertResponse(response: ResponseEntity): Result<Unit> = try {
        database.responseDao().insert(response)
        Log.d("AppRepository", "Inserted response for user ${response.userId}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert response for user ${response.userId}", e)
        Result.Error(e)
    }

    suspend fun insertProject(project: ProjectEntity): Result<Unit> = try {
        database.projectDao().insert(project)
        Log.d("AppRepository", "Inserted project: ${project.projectName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert project: ${project.projectName}", e)
        Result.Error(e)
    }

    suspend fun updateProject(project: ProjectEntity): Result<Unit> = try {
        database.projectDao().update(project)
        Log.d("AppRepository", "Updated project: ${project.projectName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update project: ${project.projectName}", e)
        Result.Error(e)
    }

    suspend fun deleteProject(project: ProjectEntity): Result<Unit> = try {
        database.projectDao().delete(project)
        Log.d("AppRepository", "Deleted project: ${project.projectName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete project: ${project.projectName}", e)
        Result.Error(e)
    }

    suspend fun getAllProjects(): Result<List<ProjectEntity>> = try {
        Result.Success(database.projectDao().getAllProjects())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all projects", e)
        Result.Error(e)
    }

    suspend fun insertPeclQuestion(question: PeclQuestionEntity): Result<Long> = try {
        val id = database.peclQuestionDao().insert(question)
        Log.d("AppRepository", "Inserted PECL question: ${question.subTask}, ID: $id")
        Result.Success(id)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert PECL question: ${question.subTask}", e)
        Result.Error(e)
    }

    suspend fun updatePeclQuestion(question: PeclQuestionEntity): Result<Unit> = try {
        database.peclQuestionDao().update(question)
        Log.d("AppRepository", "Updated PECL question: ${question.subTask}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update PECL question: ${question.subTask}", e)
        Result.Error(e)
    }

    suspend fun deletePeclQuestion(question: PeclQuestionEntity): Result<Unit> = try {
        database.peclQuestionDao().delete(question)
        Log.d("AppRepository", "Deleted PECL question: ${question.subTask}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete PECL question: ${question.subTask}", e)
        Result.Error(e)
    }

    suspend fun getAllPeclQuestions(): Result<List<PeclQuestionEntity>> = try {
        Result.Success(database.peclQuestionDao().getAllQuestions())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all PECL questions", e)
        Result.Error(e)
    }

    suspend fun getPeclQuestionById(id: Int): Result<PeclQuestionEntity?> = try {
        Result.Success(database.peclQuestionDao().getQuestionById(id))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get PECL question by ID: $id", e)
        Result.Error(e)
    }

    suspend fun getQuestionsForPoi(program: String, poi: String): Result<List<PeclQuestionEntity>> = try {
        Result.Success(database.peclQuestionDao().getQuestionsForPoi(program, poi))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get questions for POI: $poi in program: $program", e)
        Result.Error(e)
    }

    suspend fun insertPeclProgram(program: PeclProgramEntity): Result<Unit> = try {
        database.peclProgramDao().insert(program)
        Log.d("AppRepository", "Inserted PECL program: ${program.peclProgram}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert PECL program: ${program.peclProgram}", e)
        Result.Error(e)
    }

    suspend fun updatePeclProgram(program: PeclProgramEntity): Result<Unit> = try {
        database.peclProgramDao().update(program)
        Log.d("AppRepository", "Updated PECL program: ${program.peclProgram}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update PECL program: ${program.peclProgram}", e)
        Result.Error(e)
    }

    suspend fun deletePeclProgram(program: PeclProgramEntity): Result<Unit> = try {
        database.peclProgramDao().delete(program)
        Log.d("AppRepository", "Deleted PECL program: ${program.peclProgram}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete PECL program: ${program.peclProgram}", e)
        Result.Error(e)
    }

    suspend fun getAllPeclPrograms(): Result<List<PeclProgramEntity>> = try {
        Result.Success(database.peclProgramDao().getAllPrograms())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all PECL programs", e)
        Result.Error(e)
    }

    suspend fun getPeclProgramById(id: Int): Result<PeclProgramEntity?> = try {
        Result.Success(database.peclProgramDao().getProgramById(id))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get PECL program by ID: $id", e)
        Result.Error(e)
    }

    suspend fun insertPeclPoi(poi: PeclPoiEntity): Result<Unit> = try {
        // Validation: Check if program exists
        if (database.peclProgramDao().getProgramById(poi.program_id) == null) {
            throw Exception("Invalid program_id: ${poi.program_id}")
        }
        database.peclPoiDao().insert(poi)
        Log.d("AppRepository", "Inserted PECL POI: ${poi.peclPoi}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert PECL POI: ${poi.peclPoi}", e)
        Result.Error(e)
    }

    suspend fun updatePeclPoi(poi: PeclPoiEntity): Result<Unit> = try {
        database.peclPoiDao().update(poi)
        Log.d("AppRepository", "Updated PECL POI: ${poi.peclPoi}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update PECL POI: ${poi.peclPoi}", e)
        Result.Error(e)
    }

    suspend fun deletePeclPoi(poi: PeclPoiEntity): Result<Unit> = try {
        database.peclPoiDao().delete(poi)
        Log.d("AppRepository", "Deleted PECL POI: ${poi.peclPoi}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete PECL POI: ${poi.peclPoi}", e)
        Result.Error(e)
    }

    suspend fun getAllPeclPois(): Result<List<PeclPoiEntity>> = try {
        Result.Success(database.peclPoiDao().getAllPois())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all PECL POIs", e)
        Result.Error(e)
    }

    suspend fun insertScale(scale: ScaleEntity): Result<Unit> = try {
        database.scaleDao().insert(scale)
        Log.d("AppRepository", "Inserted scale: ${scale.scaleName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert scale: ${scale.scaleName}", e)
        Result.Error(e)
    }

    suspend fun updateScale(scale: ScaleEntity): Result<Unit> = try {
        database.scaleDao().update(scale)
        Log.d("AppRepository", "Updated scale: ${scale.scaleName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update scale: ${scale.scaleName}", e)
        Result.Error(e)
    }

    suspend fun deleteScale(scale: ScaleEntity): Result<Unit> = try {
        database.scaleDao().delete(scale)
        Log.d("AppRepository", "Deleted scale: ${scale.scaleName}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete scale: ${scale.scaleName}", e)
        Result.Error(e)
    }

    suspend fun getAllScales(): Result<List<ScaleEntity>> = try {
        Result.Success(database.scaleDao().getAllScales())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all scales", e)
        Result.Error(e)
    }

    suspend fun getScaleById(id: Int): Result<ScaleEntity?> = try {
        Result.Success(database.scaleDao().getScaleById(id))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get scale by ID: $id", e)
        Result.Error(e)
    }

    suspend fun insertPeclTask(task: PeclTaskEntity): Result<Unit> = try {
        // Validation: Check if POI exists
        if (database.peclPoiDao().getPoiById(task.poi_id) == null) {
            throw Exception("Invalid poi_id: ${task.poi_id}")
        }
        database.peclTaskDao().insert(task)
        Log.d("AppRepository", "Inserted PECL task: ${task.peclTask}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert PECL task: ${task.peclTask}", e)
        Result.Error(e)
    }

    suspend fun updatePeclTask(task: PeclTaskEntity): Result<Unit> = try {
        database.peclTaskDao().update(task)
        Log.d("AppRepository", "Updated PECL task: ${task.peclTask}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update PECL task: ${task.peclTask}", e)
        Result.Error(e)
    }

    suspend fun deletePeclTask(task: PeclTaskEntity): Result<Unit> = try {
        database.peclTaskDao().delete(task)
        Log.d("AppRepository", "Deleted PECL task: ${task.peclTask}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete PECL task: ${task.peclTask}", e)
        Result.Error(e)
    }

    suspend fun getAllPeclTasks(): Result<List<PeclTaskEntity>> = try {
        Result.Success(database.peclTaskDao().getAllTasks())
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get all PECL tasks", e)
        Result.Error(e)
    }

    suspend fun getPeclTaskById(id: Int): Result<PeclTaskEntity?> = try {
        Result.Success(database.peclTaskDao().getTaskById(id))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get PECL task by ID: $id", e)
        Result.Error(e)
    }

    suspend fun insertPeclEvaluationResult(result: PeclEvaluationResultEntity): Result<Unit> = try {
        database.peclEvaluationResultDao().insert(result)
        Log.d("AppRepository", "Inserted PECL evaluation result for ${result.program}/${result.poi}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert PECL evaluation result", e)
        Result.Error(e)
    }

    suspend fun getPeclEvaluationResults(program: String, poi: String): Result<List<PeclEvaluationResultEntity>> = try {
        Result.Success(database.peclEvaluationResultDao().getResultsForProgramAndPoi(program, poi))
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get PECL evaluation results for $program/$poi", e)
        Result.Error(e)
    }

    suspend fun updatePeclEvaluationResult(result: PeclEvaluationResultEntity): Result<Unit> = try {
        database.peclEvaluationResultDao().update(result)
        Log.d("AppRepository", "Updated PECL evaluation result for ${result.program}/${result.poi}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to update PECL evaluation result", e)
        Result.Error(e)
    }

    suspend fun deletePeclEvaluationResult(result: PeclEvaluationResultEntity): Result<Unit> = try {
        database.peclEvaluationResultDao().delete(result)
        Log.d("AppRepository", "Deleted PECL evaluation result for ${result.program}/${result.poi}")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to delete PECL evaluation result", e)
        Result.Error(e)
    }

    suspend fun prePopulateTemplates() {
        try {
            insertTemplate(DemoTemplatesEntity(templateName = "Generic Template", selectedItems = "Age,Combat Tour,Gender,Military Status,MOS/AFSC/Rating,Service Branch,Total number of Combat Tours,Years of Service"))
            Log.d("AppRepository", "Prepopulated templates")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate templates", e)
        }
    }

    suspend fun prePopulateQuestions() {
        try {
            val questions = listOf(
                QuestionRepositoryEntity(field = "Age", inputType = "TextBox", options = ""),
                QuestionRepositoryEntity(field = "Gender", inputType = "ComboBox", options = "Male,Female"),
                QuestionRepositoryEntity(field = "Civilian Boat Operation Experience", inputType = "ComboBox", options = "YES,NO"),
                QuestionRepositoryEntity(field = "Years of Service", inputType = "TextBox", options = "")
            )
            questions.forEach { insertQuestion(it) }
            Log.d("AppRepository", "Prepopulated question_repository")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate question_repository", e)
        }
    }

    suspend fun prePopulateScales() {
        try {
            val scales = listOf(
                ScaleEntity(scaleName = "Scale_Affect", scaleData = "No Affect,Minor Affect,Neutral,Moderate Affect,Major Affect"),
                ScaleEntity(scaleName = "Scale_Agree", scaleData = "Strongly Agree,Agree,Neither Agree nor Disagree,Disagree,Strongly Disagree,NA"),
                ScaleEntity(scaleName = "Scale_Yes_No", scaleData = "YES,NO,N/A"),
                ScaleEntity(scaleName = "Scale_PECL", scaleData = "1,2,3,4,5"),
                ScaleEntity(scaleName = "Scale_Go_NOGO", scaleData = "GO,NO GO,NA"),
                ScaleEntity(scaleName = "Scale_RSLC_Position", scaleData = "Team Leader,Assistant Team Leader,RTO,Medic,Machine Gunner,Assistant Machine Gunner")
            )
            scales.forEach { insertScale(it) }
            Log.d("AppRepository", "Prepopulated scales")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate scales", e)
        }
    }

    suspend fun prePopulatePeclPrograms() {
        try {
            val programs = listOf("AASB", "RSLC", "USMC Fires", "USMC PFT_CFT")
            val programMap = mutableMapOf<String, Int>()
            programs.forEach {
                insertPeclProgram(PeclProgramEntity(peclProgram = it))
                val idResult = getPeclProgramById(database.peclProgramDao().getAllPrograms().last().id)
                if (idResult is Result.Success) {
                    programMap[it] = idResult.data!!.id
                }
            }
            Log.d("AppRepository", "Prepopulated pecl_programs")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate pecl_programs", e)
        }
    }

    suspend fun prePopulatePeclPois() {
        try {
            val programResult = getAllPeclPrograms()
            val programMap = if (programResult is Result.Success) programResult.data.associate { it.peclProgram to it.id } else return
            val pois = listOf(
                "Boat Operations" to "AASB",
                "Team Leader Planning" to "RSLC",
                "ATL Planning" to "RSLC",
                "RTO Planning" to "RSLC",
                "Fire Support Marine Artillery" to "USMC Fires",
                "PFTCFT" to "USMC PFT_CFT"
            )
            pois.forEach { (poiName, programName) ->
                val programId = programMap[programName] ?: return@forEach
                insertPeclPoi(PeclPoiEntity(peclPoi = poiName, program_id = programId))
            }
            Log.d("AppRepository", "Prepopulated pecl_poi")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate pecl_poi", e)
        }
    }

    suspend fun prePopulatePeclTasks() {
        try {
            val poiResult = getAllPeclPois()
            val poiMap = if (poiResult is Result.Success) poiResult.data.associate { it.peclPoi to it.id } else return
            val tasks = listOf(
                "Launch" to "Boat Operations",
                "Moor" to "Boat Operations",
                "Radar-Nav-FLIR" to "Boat Operations",
                "Plotting" to "Boat Operations",
                "Radio" to "Boat Operations",
                "Depart Dock" to "Boat Operations",
                "Maneuvering the AASB" to "Boat Operations",
                "M-O-B" to "Boat Operations",
                "Maintain Station" to "Boat Operations",
                "Recover AASB" to "Boat Operations",
                "Comments" to "Boat Operations",
                "Confirmation Brief" to "Team Leader Planning",
                "Issue a Warning Order" to "Team Leader Planning",
                "Mission Analysis/IPB" to "Team Leader Planning",
                "Conduct Mission Analysis Brief" to "Team Leader Planning",
                "Develop Teams Course of Action" to "Team Leader Planning",
                "Issue an Operations Order" to "Team Leader Planning",
                "Conduct Rehearsals" to "Team Leader Planning",
                "Conduct Backbrief" to "Team Leader Planning",
                "Evaluation Data" to "Team Leader Planning",
                "Leadership" to "Team Leader Planning",
                "Conduct Initial Inspections" to "ATL Planning",
                "Prepare for Mission" to "ATL Planning",
                "Prepare and Issue an OPORD" to "ATL Planning",
                "Issues 4 Para OPORD" to "ATL Planning",
                "Conduct Final Inspection" to "ATL Planning",
                "Prepare for OPORD and Operations" to "RTO Planning",
                "Issue 5 para OPORD" to "RTO Planning",
                "Preexecution" to "Fire Support Marine Artillery",
                "Call for Fire" to "Fire Support Marine Artillery",
                "Spottings/Corrections" to "Fire Support Marine Artillery",
                "RREMS" to "Fire Support Marine Artillery",
                "Idividual Data" to "PFTCFT",
                "PFT Performance Data" to "PFTCFT",
                "CFT Performance Data" to "PFTCFT"
            )
            tasks.forEach { (taskName, poiName) ->
                val poiId = poiMap[poiName] ?: return@forEach
                insertPeclTask(PeclTaskEntity(peclTask = taskName, poi_id = poiId))
            }
            Log.d("AppRepository", "Prepopulated pecl_tasks")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate pecl_tasks", e)
        }
    }

    suspend fun prePopulateProjects() {
        try {
            insertProject(ProjectEntity(projectName = "AASB"))
            Log.d("AppRepository", "Prepopulated projects")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate projects", e)
        }
    }

    suspend fun prePopulatePeclQuestions() {
        try {
            val taskResult = getAllPeclTasks()
            val taskMap = if (taskResult is Result.Success) taskResult.data.associate { it.peclTask to it.id } else return
            val questions = listOf(
                PeclQuestionEntity(subTask = "Instructor Name", controlType = "ComboBox", scale = "Scale_Instructors", criticalTask = "NO"),
                PeclQuestionEntity(subTask = "Name", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO"),
                PeclQuestionEntity(subTask = "Operator Name", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO"),
                PeclQuestionEntity(subTask = "Launch Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO"),
                PeclQuestionEntity(subTask = "Pass/Fail", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO"),
                PeclQuestionEntity(subTask = "Moor Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO")
                // ... add all ~200 questions as per your data; for brevity, assuming you have the full list in code
            )
            questions.forEach { question ->
                val insertResult = insertPeclQuestion(question)
                if (insertResult is Result.Success) {
                    val questionId = insertResult.data
                    if (questionId > 0) {  // Skip if -1 (duplicate)
                        val taskName = when {
                            question.subTask.contains("Launch") -> "Launch"
                            question.subTask.contains("Moor") -> "Moor"
                            // Add mappings for all subtasks to their tasks
                            else -> "Comments" // Default or throw if not mapped
                        }
                        val taskId = taskMap[taskName] ?: run {
                            Log.w("AppRepository", "Task $taskName not found for subTask ${question.subTask}")
                            return@forEach
                        }
                        database.questionAssignmentDao().insert(QuestionAssignmentEntity(question_id = questionId.toInt(), task_id = taskId))
                    } else {
                        Log.w("AppRepository", "Skipped duplicate question: ${question.subTask}")
                    }
                } else {
                    Log.e("AppRepository", "Failed to insert question for assignment: ${(insertResult as Result.Error).exception.message}")
                }
            }
            Log.d("AppRepository", "Prepopulated pecl_questions")
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to prepopulate pecl_questions", e)
        }
    }

    suspend fun insertQuestionWithAssignment(question: PeclQuestionEntity, taskId: Int): Result<Unit> = try {
        database.withTransaction {
            val id = database.peclQuestionDao().insert(question)
            if (id > 0) {
                database.questionAssignmentDao().insert(QuestionAssignmentEntity(question_id = id.toInt(), task_id = taskId))
            } else {
                throw Exception("Duplicate question ignored")
            }
        }
        Log.d("AppRepository", "Inserted question with assignment")
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to insert question with assignment", e)
        Result.Error(e)
    }

    suspend fun getAverageScorePerStudent(studentId: Int, poi: String): Result<Double> = try {
        val average = database.peclEvaluationResultDao().getAverageScore(studentId, poi) ?: 0.0
        Result.Success(average)
    } catch (e: Exception) {
        Log.e("AppRepository", "Failed to get average score for student $studentId in POI $poi", e)
        Result.Error(e)
    }
}