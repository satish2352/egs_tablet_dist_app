package com.sipl.egstabdistribution.webservice
data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)