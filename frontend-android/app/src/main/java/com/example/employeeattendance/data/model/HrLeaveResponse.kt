package com.example.employeeattendance.data.model

data class HrLeaveResponse(
    val id: Long,
    val employee: EmployeeResponse?,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String,
    val appliedAt: String?,
    val reviewedAt: String?
)
