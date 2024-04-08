package com.sumagoinfotech.egsregistration.model.TabDistList

data class TabDistListModel(
    val `data`: List<TabUser>,
    val message: String,
    val status: String,
    val iTotalRecords:Int,
    val totalPages:Int,
)