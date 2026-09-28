package com.example.employeeattendance.data.model

data class LeaveRequest(
    val startDate: String,
    val endDate: String,
    val reason: String
)