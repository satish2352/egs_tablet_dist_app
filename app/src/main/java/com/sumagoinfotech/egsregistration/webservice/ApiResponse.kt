package com.sumagoinfotech.egsregistration.webservice
data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)