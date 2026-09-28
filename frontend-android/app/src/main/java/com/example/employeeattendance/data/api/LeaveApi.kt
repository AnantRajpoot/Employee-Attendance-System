package com.example.employeeattendance.data.api

import com.example.employeeattendance.data.model.HrLeaveResponse
import com.example.employeeattendance.data.model.LeaveRequest
import com.example.employeeattendance.data.model.LeaveResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LeaveApi {

    @POST("api/leaves")
    suspend fun applyLeave(
        @Body request: LeaveRequest
    ): LeaveResponse

    @GET("api/leaves/my")
    suspend fun getMyLeaves(): List<LeaveResponse>

    @GET("api/leaves")
    suspend fun getAllLeaves(): List<HrLeaveResponse>

    @GET("api/leaves/pending")
    suspend fun getPendingLeaves(): List<HrLeaveResponse>

    @PUT("api/leaves/{id}/approve")
    suspend fun approveLeave(
        @Path("id") id: Long
    ): HrLeaveResponse

    @PUT("api/leaves/{id}/reject")
    suspend fun rejectLeave(
        @Path("id") id: Long
    ): HrLeaveResponse
}
