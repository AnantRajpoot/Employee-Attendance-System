package com.example.employeeattendance.data.model

data class LeaveResponse(
    val id: Long,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String,
    val appliedAt: String?,
    val reviewedAt: String?
)