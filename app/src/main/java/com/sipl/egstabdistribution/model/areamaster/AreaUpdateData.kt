package com.sipl.egstabdistribution.model.areamaster

data class AreaUpdateData(
    val id: Int,
    val is_active: String,
    val is_new: String,
    val is_visible: String,
    val location_id: String,
    val location_type: String,
    val name: String,
    val parent_id: String
)