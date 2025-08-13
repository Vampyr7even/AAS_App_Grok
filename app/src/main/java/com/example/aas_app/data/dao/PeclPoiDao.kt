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
    fun insertPoi(poi: PeclPoiEntity): Long

    @Update
    fun updatePoi(poi: PeclPoiEntity)

    @Delete
    fun deletePoi(poi: PeclPoiEntity)

    @Query("SELECT * FROM pecl_pois WHERE program_id = :programId")
    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>>

    @Query("SELECT * FROM pecl_pois WHERE id = :id")
    fun getPoiById(id: Long): PeclPoiEntity?

    @Query("SELECT * FROM pecl_pois WHERE name = :name")
    fun getPoiByName(name: String): PeclPoiEntity?
}