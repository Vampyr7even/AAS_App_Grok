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
interface PeclPoiDao {
    @Insert
    suspend fun insertPoi(poi: PeclPoiEntity): Long

    @Update
    suspend fun updatePoi(poi: PeclPoiEntity)

    @Delete
    suspend fun deletePoi(poi: PeclPoiEntity)

    @Query("SELECT * FROM pecl_pois WHERE id IN (SELECT poi_id FROM poi_program_assignments WHERE program_id = :programId)")
    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>>

    @Query("SELECT * FROM pecl_pois")
    fun getAllPois(): Flow<List<PeclPoiEntity>>

    @Query("SELECT * FROM pecl_pois WHERE id = :id")
    suspend fun getPoiById(id: Long): PeclPoiEntity?

    @Query("SELECT * FROM pecl_pois WHERE name = :name")
    suspend fun getPoiByName(name: String): PeclPoiEntity?

    @Insert
    suspend fun insertPoiProgramAssignment(assignment: PoiProgramAssignmentEntity)

    @Query("DELETE FROM poi_program_assignments WHERE poi_id = :poiId")
    suspend fun deletePoiProgramAssignmentsForPoi(poiId: Long)

    @Query("SELECT pr.* FROM pecl_programs pr INNER JOIN poi_program_assignments ppa ON pr.id = ppa.program_id WHERE ppa.poi_id = :poiId")
    fun getProgramsForPoi(poiId: Long): Flow<List<PeclProgramEntity>>
}