package com.sipl.egstabdistribution.model.TabDistList

data class TabDistListModel(
    val `data`: List<TabUser>,
    val message: String,
    val status: String,
    val iTotalRecords:Int,
    val totalPages:Int,
    val page_no_to_hilight:String,
)