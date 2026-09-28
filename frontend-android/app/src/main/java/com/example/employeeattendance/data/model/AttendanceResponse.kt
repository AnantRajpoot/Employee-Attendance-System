package com.example.employeeattendance.data.model

data class AttendanceResponse(
    val id: Long,
    val employee: EmployeeResponse?,
    val attendanceDate: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val status: String
)
