package com.example.employeeattendance.data.model

data class EmployeeUpdateRequest(
    val name: String,
    val email: String,
    val password: String?,
    val role: String,
    val department: String,
    val joiningDate: String,
    val leaveBalance: Double
)
