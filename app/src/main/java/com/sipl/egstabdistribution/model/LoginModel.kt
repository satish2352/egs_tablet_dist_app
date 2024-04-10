package com.sipl.egstabdistribution.model

data class LoginModel(
    val `data`: Data,
    val status: String,
    val message: String,
    val token_type: String
)