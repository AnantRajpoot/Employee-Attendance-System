package com.example.employeeattendance.data.model

data class AttendanceStatistics(
    val employeeId: Long,
    val totalDays: Long,
    val presentDays: Long,
    val totalHoursWorked: Double
)