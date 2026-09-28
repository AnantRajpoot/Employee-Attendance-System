package com.example.employeeattendance.data.model

data class LoginResponse(
    val token: String,
    val employeeId: Long,
    val name: String,
    val role: String
)