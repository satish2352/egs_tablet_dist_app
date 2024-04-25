package com.sipl.egstabdistribution.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var fullName: String,
    var district: String,
    var taluka: String,
    var village: String,
    var mobile: String,
    var aadharCardId: String,
    var latitude: String,
    var longitude: String,
    var beneficaryPhoto: String,
    var gramsevakIdCardPhoto: String,
    var aadharIdCardPhoto: String,
    var tabletImeiPhoto: String,
    var isSynced: Boolean,
    var syncFailedReason:String? = "",
    var isSyncFailed:Boolean?=false
)