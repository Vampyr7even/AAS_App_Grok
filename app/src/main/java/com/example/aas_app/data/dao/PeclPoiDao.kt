package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclPoiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeclPoiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
}