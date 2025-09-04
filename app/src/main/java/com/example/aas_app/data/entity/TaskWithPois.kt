package com.example.aas_app.data.entity

data class TaskWithPois(
    val task: PeclTaskEntity,
    val pois: List<PeclPoiEntity>
)