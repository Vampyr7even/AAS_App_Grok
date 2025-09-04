package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PoiProgramAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {
    @Insert
    suspend fun insert(poi: PeclPoiEntity): Long

    @Update
    suspend fun update(poi: PeclPoiEntity)

    @Delete
    suspend fun delete(poi: PeclPoiEntity)

    @Query("SELECT * FROM pecl_pois")
    fun getAllPois(): Flow<List<PeclPoiEntity>>

    @Query("SELECT p.* FROM pecl_pois p INNER JOIN poi_program_assignments ppa ON p.id = ppa.poi_id WHERE ppa.program_id = :programId")
    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>>

    @Insert
    suspend fun insertPoiProgramAssignment(assignment: PoiProgramAssignmentEntity)

    @Query("DELETE FROM poi_program_assignments WHERE poi_id = :poiId")
    suspend fun deletePoiProgramAssignmentsForPoi(poiId: Long)

    @Query("SELECT pr.* FROM pecl_programs pr INNER JOIN poi_program_assignments ppa ON pr.id = ppa.program_id WHERE ppa.poi_id = :poiId")
    fun getProgramsForPoi(poiId: Long): Flow<List<PeclProgramEntity>>
}