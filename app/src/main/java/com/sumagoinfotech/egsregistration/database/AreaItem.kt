package com.sumagoinfotech.egsregistration.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "area")
data class AreaItem(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    val is_active: String,
    val is_visible: String,
    val location_id: String,
    val location_type: String,
    val name: String,
    val parent_id: String
)