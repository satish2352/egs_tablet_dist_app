package com.sipl.egstabdistribution.database.model

data class UsersWithAreaNames(
    var id: Int,
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
    val villageName: String?,
    val districtName: String?,
    val talukaName: String?,
    var syncFailedReason:String,
    var isSyncFailed:Boolean,
    var grampanchayatName:String,
)