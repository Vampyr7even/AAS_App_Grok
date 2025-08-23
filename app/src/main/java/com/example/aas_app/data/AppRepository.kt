package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.dao.EvaluationResultDao
import com.example.aas_app.data.dao.InstructorStudentAssignmentDao
import com.example.aas_app.data.dao.PeclPoiDao
import com.example.aas_app.data.dao.PeclProgramDao
import com.example.aas_app.data.dao.PeclQuestionDao
import com.example.aas_app.data.dao.PeclTaskDao
import com.example.aas_app.data.dao.PoiProgramAssignmentDao
import com.example.aas_app.data.dao.QuestionAssignmentDao
import com.example.aas_app.data.dao.ScaleDao
import com.example.aas_app.data.dao.TaskPoiAssignmentDao
import com.example.aas_app.data.dao.UserDao
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.PoiProgramAssignmentEntity
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.TaskPoiAssignmentEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : AppResult<Nothing>()
}

@ViewModelScoped
class AppRepository @Inject constructor(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val peclProgramDao = db.peclProgramDao()
    private val peclPoiDao = db.peclPoiDao()
    private val peclTaskDao = db.peclTaskDao()
    private val peclQuestionDao = db.peclQuestionDao()
    private val questionAssignmentDao = db.questionAssignmentDao()
    private val instructorStudentAssignmentDao = db.instructorStudentAssignmentDao()
    private val evaluationResultDao = db.evaluationResultDao()
    private val scaleDao = db.scaleDao()
    private val poiProgramAssignmentDao = db.poiProgramAssignmentDao()
    private val taskPoiAssignmentDao = db.taskPoiAssignmentDao()

    // Prepopulation method with transaction and error handling
    suspend fun prePopulateAll(): AppResult<Unit> {
        return try {
            db.withTransaction {
                // Clear existing data if needed for dev (comment out in production)
                // Programs
                val programMap = mutableMapOf<String, Long>()
                val programs = listOf("AASB", "RSLC", "USMC Fires", "USMC PFT_CFT")
                programs.forEach { name ->
                    val program = peclProgramDao.getProgramByName(name)
                    if (program == null) {
                        val id = peclProgramDao.insertProgram(PeclProgramEntity(name = name))
                        programMap[name] = id
                        Log.d("AppRepository", "Inserted program: $name with ID: $id")
                    } else {
                        programMap[name] = program.id
                        Log.d("AppRepository", "Existing program: $name with ID: ${program.id}")
                    }
                }
                // POIs - Updated with user's provided data, assigned to programs
                val poiMap = mutableMapOf<String, Long>()
                val poiAssignments = listOf(
                    "Boat Operations" to listOf(programMap["AASB"]!!),
                    "Team Leader Planning" to listOf(programMap["RSLC"]!!),
                    "ATL Planning" to listOf(programMap["RSLC"]!!),
                    "RTO Planning" to listOf(programMap["RSLC"]!!),
                    "Fire Support Marine Artillery" to listOf(programMap["USMC Fires"]!!),
                    "PFTCFT" to listOf(programMap["USMC PFT_CFT"]!!)
                )
                poiAssignments.forEach { (name, programIds) ->
                    val poi = peclPoiDao.getPoiByName(name)
                    val poiId = if (poi == null) {
                        val id = peclPoiDao.insertPoi(PeclPoiEntity(name = name))
                        programIds.forEach { programId ->
                            poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = id, program_id = programId))
                            Log.d("AppRepository", "Assigned POI: $name to program ID: $programId")
                        }
                        Log.d("AppRepository", "Inserted POI: $name with ID: $id")
                        id
                    } else {
                        // For existing, update assignments if needed (assume add if not present)
                        Log.d("AppRepository", "Existing POI: $name with ID: ${poi.id}")
                        poi.id
                    }
                    poiMap[name] = poiId
                }
                // Tasks - Updated with user's provided data, assigned to multiple POIs
                val taskMap = mutableMapOf<String, Long>()
                val taskData = listOf(
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
                    "Conduct Rehearsals" to "Team Leader Planning,ATL Planning,RTO Planning",
                    "Conduct Backbrief" to "Team Leader Planning,ATL Planning,RTO Planning",
                    "Evaluation Data" to "Fire Support Marine Artillery",
                    "Leadership" to "Fire Support Marine Artillery",
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
                taskData.forEach { (name, poiNamesStr) ->
                    val poiNames = poiNamesStr.split(",").map { it.trim() }
                    val poiIds = poiNames.mapNotNull { poiMap[it] }
                    if (poiIds.isNotEmpty()) {
                        val task = peclTaskDao.getTaskByName(name)
                        val taskId = if (task == null) {
                            val id = peclTaskDao.insertTask(PeclTaskEntity(name = name))
                            poiIds.forEach { poiId ->
                                taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = id, poi_id = poiId))
                                Log.d("AppRepository", "Assigned task: $name to POI ID: $poiId")
                            }
                            Log.d("AppRepository", "Inserted task: $name with ID: $id")
                            id
                        } else {
                            // For existing, update assignments if needed (assume add if not present)
                            Log.d("AppRepository", "Existing task: $name with ID: ${task.id}")
                            task.id
                        }
                        taskMap[name] = taskId
                    } else {
                        Log.w("AppRepository", "Skipping task '$name': No valid POIs found for '$poiNamesStr'")
                    }
                }
                // Scales - Expanded with additional scales from provided data
                val scaleData = listOf(
                    ScaleEntity(scaleName = "1-10", options = "1,2,3,4,5,6,7,8,9,10"),
                    ScaleEntity(scaleName = "Scale_Instructors", options = ""), // Assume dynamic or empty
                    ScaleEntity(scaleName = "Scale_Comment", options = ""),
                    ScaleEntity(scaleName = "Scale_PECL", options = "1-10"), // Example, adjust as needed
                    ScaleEntity(scaleName = "Scale_Yes_No", options = "Yes,No"),
                    ScaleEntity(scaleName = "Scale_Go_NOGO", options = "Go,No Go")
                    // Add more unique scales if identified
                )
                scaleData.forEach { scale ->
                    val existing = scaleDao.getScaleByName(scale.scaleName)
                    if (existing == null) {
                        scaleDao.insertScale(scale)
                        Log.d("AppRepository", "Inserted scale: ${scale.scaleName}")
                    } else {
                        Log.d("AppRepository", "Existing scale: ${scale.scaleName}")
                    }
                }
                // Questions - Full dataset from provided content
                val questionData = listOf(
                    PeclQuestionEntity(subTask = "Instructor Name", controlType = "ComboBox", scale = "Scale_Instructors", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Name", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Operator Name", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Launch Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Launch",
                    PeclQuestionEntity(subTask = "Moor Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Radar-Nav-FLIR Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "Plotting Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Plotting",
                    PeclQuestionEntity(subTask = "Radio Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Radio",
                    PeclQuestionEntity(subTask = "Depart Dock Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Maneuvering the AASB Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "M-O-B Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "M-O-B",
                    PeclQuestionEntity(subTask = "Maintain Station Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Maintain Station",
                    PeclQuestionEntity(subTask = "Recover AASB Grade", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Execute Pre Mission Checks", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Launch",
                    PeclQuestionEntity(subTask = "Connect Emergency Kill Switch", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Launch",
                    PeclQuestionEntity(subTask = "Start Motors", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Launch",
                    PeclQuestionEntity(subTask = "Back boat off trailer", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Launch",
                    PeclQuestionEntity(subTask = "Maneuver to dock", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Launch",
                    PeclQuestionEntity(subTask = "Command the line handlers for mooring", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Command: Standby for STEEP side approach", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Command: Port/Starboard side", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Command: Ready Bow Line, Ready Stern Line", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Approach dock at 90-degree angle", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Change approach to 45-degree angle and shift to Neutral around 15-20ft from dock", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Shift to Reverse around 10-15ft from dock", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Gently square up AASB to dock", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Direct crew to throw bow line to cleat", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Direct crew to throw stern line to cleat", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Command: All Out/All In - Moor", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Turn on the radar", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "Open a combination of radar chart overlays", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "Radar: Adjust the gain", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "Turn on the FLIR system and display it on the Garmin display", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "FLIR: Pan to the left and right", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "FLIR: Tilt up and down", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "FLIR: Toggle white hot and black hot", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "FLIR: Zoom in and out", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "Set a waypoint", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Plotting",
                    PeclQuestionEntity(subTask = "Name a waypoint", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Plotting",
                    PeclQuestionEntity(subTask = "Use the route function to create turns through hazards; navigate safely to the waypoint.", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Plotting",
                    PeclQuestionEntity(subTask = "Cancel a route", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Plotting",
                    PeclQuestionEntity(subTask = "Turn on the radio", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radio",
                    PeclQuestionEntity(subTask = "Adjust the volume and squelch", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radio",
                    PeclQuestionEntity(subTask = "Go to a channel (not channel 16) using any method", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radio",
                    PeclQuestionEntity(subTask = "Go to channel 16 using the 16/S button", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Radio",
                    PeclQuestionEntity(subTask = "Command: All Out/All In - Depart", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Verify motors are powered up", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Verify water pressure/discharge", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Turn helm away from dock", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Direct crew to recover aft and bow mooring line", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Place motors in reverse idle", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Get the AASB positioned at a 45-degree angle from dock", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Direct Crew to release bow line", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Back away from dock", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Proceed to open water", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Turn the AASB to the Right", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Turn the AASB to the Left", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Stop the AASB", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Drive the AASB in Reverse", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Turn the AASB in a Tight Circle", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Stop the AASB and remain in one location", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Perform sharp turns with the AASB", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Turn sharp to the side of the MOB", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "M-O-B",
                    PeclQuestionEntity(subTask = "Quickly bring the AASB to the reciprocal heading (Back Azimuth)", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "M-O-B",
                    PeclQuestionEntity(subTask = "Recover the MOB", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "M-O-B",
                    PeclQuestionEntity(subTask = "Apply power to the motor to maintain station within 10 yds. location", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maintain Station",
                    PeclQuestionEntity(subTask = "Adjust power to account for wind conditions", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maintain Station",
                    PeclQuestionEntity(subTask = "Adjust power to account for sea state", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Maintain Station",
                    PeclQuestionEntity(subTask = "Check scuppers", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Line up the AASB with the trailer", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Throttle up slightly", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Keep throttles in forward position", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Direct ground guides to connect winch cable", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Shut off motors and trim", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Remove Emergency Kill Switch", controlType = "OptionButton", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Instructor Rating", controlType = "Comment", scale = "Scale_Comment", criticalTask = "NO") to "Comments",
                    PeclQuestionEntity(subTask = "PECL Comments", controlType = "Comment", scale = "Scale_Comment", criticalTask = "NO") to "Comments",
                    PeclQuestionEntity(subTask = "Team Leader Planning Grade", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "ATL Planning Grade", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "RTO Planning Grade", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "Problem Number", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "Day", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "Position", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "Team", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "Leadership Grade", controlType = "ScoreBox", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "A. Team Leader met the commander's intent", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "*B. Team Leader did not violate the Principals of Patrolling (4 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "YES") to "Leadership",
                    PeclQuestionEntity(subTask = "1. Planning", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "2. Reconnaissance", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "3. Security", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "4. Control", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "5. Common Sense", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "C. Team Leader issued clear planning guidance with task conditions and standards throughout the entire planning process", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "D. Team Leader ensured all team members continuously conducted mission planning", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "E. Team Leader demonstrated initiative and set the example", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "F. Team Leader took responsibility for the planning process and products at all time", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "Leadership Comments", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Leadership",
                    PeclQuestionEntity(subTask = "Confirmation Brief Grade", controlType = "ScoreBox", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "A. Team Leader states AO and AI with CARR using provided graphics (OBTF)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "B. Team Leader states the general enemy situation from (BIG) to (SMALL) and pinpoints objective", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "*C. Team Leader briefs teams probable operational type and next higher's mission statement", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "YES") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "D. Team Leader states the commander's intent", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "E. Team Leader states specific tasks", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "F. Team Leader states IC tasks", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "G. Team Leader states key times", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "H. Team Leader states any initial concerns", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "Confirmation Brief Comments", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "Issue a Warning Order Grade", controlType = "ScoreBox", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "A. Team Leader ensures all personnel are present", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "B. Team Leader briefs given mission type, task and purpose with be prepared to missions stated", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "*C. Team Leader briefs the general enemy situation using AO, AI with CARR and pinpoints OBJ", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "YES") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "D. Team Leader briefs Mission, Intent and Concept 2 levels up using concept sketch", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "E. Team Leader briefs CCIR, IR and IC tasks", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "F. Team Leader issues initial planning guidance to all team members and SURV/HIDE elements", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "G. Team Leader  briefs initial planning and operational timeline", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "Issue a Warning Order Comments", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "Mission Analysis/IPB Grade", controlType = "ScoreBox", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "A. Team conduct mission analysis using METT-TC", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "B. Analysis of Mission (4 of 6)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "1. Identified Mission Intent and Concept 2 levels up", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "2. Identified Mission Intent and Concept 1 level up", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "3. Identified constraints, prohibitions and requirements", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "4. Identified specific tasks to build implied tasks", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "5. Determined Mission Essential Task to nest with Higher", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "6. Developed Team mission statement nested with Higher", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "*C. Analyzed enemy using AGADP (MPCOA/MDCOA) (4 of 6)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "YES") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "1. Analyzed Enemy Relative Combat Power", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "2. Generated Options (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "a. Determined a nested purpose of the operation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "b. Determined a Defensive Task which nest with Higher", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "c. Determined the Decisive Point and why", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "d. Engagement Area Development (4 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "1) Determine the Enemy Avenue of Approach", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "2) Determine the Enemy Scheme of Maneuver", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "3) Determine where to kill the Enemy", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "4) Emplace Direct Fire Weapons", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "5) Emplace Indirect Fires", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "3. Arrayed Forces with use of a Red Check Book", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "4. Developed a concept of the enemy operation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "5. Assigned responsibility by associating a Doctrinal Task and Purpose", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "6. Prepare course of action statement and sketch", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "a. MPCOA sketch complete", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "b. MDCOA sketch complete", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "D. Analyzed Terrain and Weather (2 of 2)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "1. Analyzed military aspects of terrain and developed MCOO", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "2. Analyzed military aspects of weather and effects on friendly and enemy", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "E. Identified Higher's task organization and adjacent units throughout operation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "F. Identified all troops to task and support available", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "G. Analyzed Time available (2 of 2)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "1. Planning timeline completed", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "2. Operational timeline updated", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "H. Analyze civil considerations using ASCOPE and the effects on the mission", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "Mission Analysis/IPB Comments", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "Conduct Mission Analysis Brief Grade", controlType = "ScoreBox", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "A. TL ensures all team members are present and prepared", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "B. Team Leader briefs Troops and Support (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "1. Briefs task organization of team and additional duties", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "2. Briefs Higher's Task Organization with adjacent units in the AO", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "3. Briefs all CAS, CCA, Artillery and all support units available", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "C. Team Leader Briefs analysis of terrain and weather (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "1. Defines the limits of the AO, AI with CARR and pinpoints OBJ", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "2. Briefs the weather and light data for duration of the mission and its effects on friendly and enemy", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "3. Briefs the military aspect of terrain using OAKOC and MCOO", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "D. Team Leader briefs Civil Considerations (2 of 2)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "1. Gives general overview of AO using ASCOPE", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "2. Describes effects of ASCOPE around OBJ", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "E. Team Leader briefs Enemy Situation and Analysis (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "1. Briefs general enemy situation (who are they, where are they coming from, why are they here, what are they doing now?)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "2. Describes suspected enemy task organization to include HVTs, HPTs, uniforms and equipment team expected to see", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "3. Briefs enemy Relative Combat Power Analysis work sheet and describes effects of each war fighting function on the team and mission", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "4. Talks through concept of the operation for MPCOA and MDCOA using completed statement and sketch", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "F. Team Leader briefs Analysis of the Mission (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "1. Briefs the Mission, Intent and Concept 2 and 1 levels up with the use of graphics", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "2. Briefs all constraints, requirements, and prohibitions", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "3. Issues mission statement team personalized and includes the mission essential task", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "G. Team Leader reviews and briefs the updated planning and operational timeline", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "Conduct Mission Analysis Brief Comments", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "Develop Teams Course of Action Grade", controlType = "ScoreBox", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "A. Team Leader's course of action (4 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "1. Suitable", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "2. Feasible", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "3. Acceptable", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "4. Distiguishable", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "5. Complete", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "B. Team Leader developes COA using AGADAP from start to finish covering all 5 phases of the operation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "C. Team Leader properly analyzes team's relative combat power using worksheet", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "D. Team Leader generates options (2 of 2)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "1. Team Leader determines decisive point based on task and purpose", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "2. Team Leader determines why that is the decisive point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "E. Team Leader arrays forces (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "1. Team Leader determines size of element needed to complete the mission", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "2. Team Leader determines SURV site location to best collect information", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "3. Team Leader determines HIDE site location to best support SURV site", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "F. Team Leader developes a concept of the operation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "1. Team Leader determines equipment needed to achive maximum stand off", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "G. Team Leader assigns responsibility", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "H. Team Leader completes COA statement and Sketch (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "1. Tentative ORP", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "2. Primary and Alternate SURV/HIDE positions", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "3. NAIs location", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "4. Illustration task organizatiopn", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "*I. Team Leadrs COA Statement and Sketch is complete and supports the commanders intent", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "YES") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "J. Team Leader splits team into two groups and assigns each group a primary and alternate insertion and extraction methods and location areas", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "K. Team Leader compares and determines which COA to use", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "L. Team Leadert prepares a risk management worksheet identifying any risk associated with his plan from start to finish", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "Develop Teams Course of Action Comments", controlType = "TextBox", scale = "Scale_Comment", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "Issue an Operations Order Grade", controlType = "ScoreBox", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "0. ADMINISTRATION (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "A. Team Leader conducts roll call and orients the team to the ISOF AC", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "B. Team Leader briefs all special teams and key individuals", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "C. Team Leader briefs all SOP boards", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1. SITUATION (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "A. Define the Area of Operation (4 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1) Describe the AO, AI from Big to Small down to the NAI", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2) Weather and light using VWPCT (effects on friendly and enemy)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3) Terrain using OAKOC (effects on friendly and enemy)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4) Civil Considerations big to small ending with effects on NAI", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5) Identified enemy forces: Task organization, HVTs, HPTs, uniforms, equipment BIG to small ending with NAI", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "B. Enemy Forces (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1) Activities last 72, 48, 24 hrs. to include friendly", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2) Disposition, Composition, Strength, war fighting functions", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3) EMPCOA - what the enemy is doing at this moment", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4) EMDCOA - how the enemy is postured for LRS team", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "C. Friendly Forces", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1) Mission Intent and Concept 2 levels up", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2. MISSION x2", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "A. Who, What (type of operation and mission essential task), When, Where, and Why (purpose)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "*3. EXECUTION (6 of 8)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "YES") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "A. Commanders Intent", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "B. Concept of the Operation in general terms KDDMK covering all 5 phases of the operation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "C. Decisive point of the operation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "D. Task and purpose of each element", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "E. Purpose of key warfighting function", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "F. MANEUVER (6 of 8)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1) INSERTION (AIRCRAFT) (9 of 12)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "a. Friendly Situation (11 of 15)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Unit providing lift", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Escorts and their actions", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- CSAR teams", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- CAS", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5- Departure (NLT) time", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "6- PZ or airfield location", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "7- Time on station", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "8- Number of aircraft", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "9- Team load plan, number of personnel and avg. weight with gear", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "10- A/C configuration", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "11- Location of Team Leader and LNO during flight", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "12- Method of transportation to PZ or airfield", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "13- Take off time", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "14- Flight time (total)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "15- Insertion time (TOT on LZ or PZ)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "b. Primary route (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Check points: Time, Distance, Direction, Description (terrain reference) - PR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- False insertion - PR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Time warnings - PR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Decision point - PR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "c. Alternate routes (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Check points: Time, Distance, Direction, Description (terrain reference) - AR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- False insertion - AR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Time warnings - AR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Decision point - AR", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "d. Location of LZ or DZ (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- LZ or DZ Primary", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- LZ or DZ Alternate", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- LZ or DZ Emergency", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "e. Actions at LZ or DZ (4 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Landing direction", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Action upon landing", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Off load plan", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Assembly plan", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5- Cache of equipment", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "f. Actions on enemy contact (4 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1-  LZ or DZ En route", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Landing", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- While unloading", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- While aircraft are in loiter area", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5- Method of marking team's position if in contact DAY/NIGHT", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "g. Emergency extraction PZ", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "h. Loiter times, area and specific instructions for A/C", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "i. Emergency proceedures (5 of 7)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Aircraft shot down before decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Aircraft shot down after decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Mechanical failure before decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Mechanical failure after decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5- Loss of communication with aircraft after insertion", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "6- Abort criteria (company OPORD)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "7- En route evasion plan", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "j. Time Schedule", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "k. Rehersals", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "l. Special equipment needed", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2) Insertion Vehicle (8 of 11)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "a. Friendly situation (9 of 12)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Unit providing transportation", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Quick reation force", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- En Trucking Point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Number and type of vehicles", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5- Team load plan, number of personnel", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "6- Tactical preparation (who does it and whats being done)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "7- Location of Team Leader and LNO during travel", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "8- Method of transportation to en trucking point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "9- Load time", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "10- Departure (NLT) time", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "11- Travel time (total)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "12- Insertion time at de trucking point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "b. Vehicle Primary route (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Primary Check points: Time, Distance, Direction, Description (terrain reference)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Primary False insertions", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Primary Time warnings", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Primary Decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "c. Vehicle Alternate routes (3 of 4)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Alternate Check points: Time, Distance, Direction, Description (terrain reference)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Alternate False insertions", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Alternate Time warnings", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Alternate Decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "d. Location of de trucking point (3 of 3)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- de trucking Primary", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- de trucking Alternate", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- de trucking Emergency", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "e. Actions of Enemy Contact (5 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- de trucking En route", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Ambush", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Road blocks", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Check points", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5- While loading or unloading", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "6- While vehicles are in the loiter area", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "7- Method of marking team position in contact DAY/NIGHT", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "f. Emergency extraction PZ", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "g. Loiter times, area and specific instructions for the vehicles", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "h. Emergency procedures (4 of 5)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "1- Mechanical failure before decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "2- Mechanical failure after decision point", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "3- Loss of communication with vehicle after insertion", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "4- Abort criteria (company OPORD)", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "5- En route evasion plan", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "i. Time schedule", controlType = "OptionButton", scale = "Scale_Go_NOGO", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask