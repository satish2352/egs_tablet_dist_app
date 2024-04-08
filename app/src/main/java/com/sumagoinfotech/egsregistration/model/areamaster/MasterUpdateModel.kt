package com.sumagoinfotech.egsregistration.model.areamaster

data class MasterUpdateModel(
    val `data`: List<AreaUpdateData>,
    val message: String,
    val status: String
)