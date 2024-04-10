package com.sipl.egstabdistribution.model.areamaster

data class MasterUpdateModel(
    val `data`: List<AreaUpdateData>,
    val message: String,
    val status: String
)