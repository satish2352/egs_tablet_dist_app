package com.sumagoinfotech.egsregistration.model.areamaster

data class AreaUpdateData(
    val id: Int,
    val is_active: String,
    val is_new: Int,
    val is_visible: String,
    val location_id: Int,
    val location_type: String,
    val name: String,
    val parent_id: Int
)