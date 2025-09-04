package com.example.aas_app.data.entity

data class PoiWithPrograms(
    val poi: PeclPoiEntity,
    val programs: List<PeclProgramEntity>
)