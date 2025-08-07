package com.example.aas_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aas_app.data.entity.PeclPoiEntity

@Dao
interface PeclPoiDao {
    @Insert
    suspend fun insert(poi: PeclPoiEntity)

    @Update
    suspend fun update(poi: PeclPoiEntity)

    @Delete
    suspend fun delete(poi: PeclPoiEntity)

    @Query("SELECT * FROM pecl_poi")
    suspend fun getAllPois(): List<PeclPoiEntity>

    @Query("SELECT * FROM pecl_poi WHERE id = :id")
    suspend fun getPoiById(id: Int): PeclPoiEntity?

    @Query("DELETE FROM pecl_poi")
    suspend fun deleteAll()
}