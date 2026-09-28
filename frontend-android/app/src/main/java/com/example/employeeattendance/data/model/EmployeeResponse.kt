package com.example.employeeattendance.data.model

data class EmployeeResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val department: String?,
    val joiningDate: String?,
    val leaveBalance: Double?,
    val createdAt: String?
)
