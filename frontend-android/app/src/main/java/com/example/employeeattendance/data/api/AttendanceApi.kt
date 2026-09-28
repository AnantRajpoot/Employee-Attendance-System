package com.example.employeeattendance.data.api

import com.example.employeeattendance.data.model.AttendanceResponse
import com.example.employeeattendance.data.model.AttendanceStatistics
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceApi {

    @POST("api/attendance/check-in/{employeeId}")
    suspend fun checkIn(
        @Path("employeeId") employeeId: Long
    ): AttendanceResponse

    @PUT("api/attendance/check-out/{attendanceId}")
    suspend fun checkOut(
        @Path("attendanceId") attendanceId: Long
    ): AttendanceResponse

    @GET("api/attendance/employee/{employeeId}")
    suspend fun getEmployeeAttendance(
        @Path("employeeId") employeeId: Long
    ): List<AttendanceResponse>

    @GET("api/attendance")
    suspend fun getAllAttendance(): List<AttendanceResponse>

    @GET("api/attendance/employee/{employeeId}/range")
    suspend fun getEmployeeAttendanceByRange(
        @Path("employeeId") employeeId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): List<AttendanceResponse>

    @GET("api/attendance/employee/{employeeId}/statistics")
    suspend fun getAttendanceStatistics(
        @Path("employeeId") employeeId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): AttendanceStatistics
}
