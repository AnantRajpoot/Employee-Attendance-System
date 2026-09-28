package com.example.employeeattendance.data.api

import com.example.employeeattendance.data.model.LoginRequest
import com.example.employeeattendance.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}