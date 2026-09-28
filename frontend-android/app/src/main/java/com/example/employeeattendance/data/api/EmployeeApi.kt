package com.example.employeeattendance.data.api

import com.example.employeeattendance.data.model.EmployeeCreateRequest
import com.example.employeeattendance.data.model.EmployeeResponse
import com.example.employeeattendance.data.model.EmployeeUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EmployeeApi {

    @GET("api/employees/me")
    suspend fun getMyProfile(): EmployeeResponse

    @GET("api/employees")
    suspend fun getAllEmployees(): List<EmployeeResponse>

    @GET("api/employees/{id}")
    suspend fun getEmployeeById(
        @Path("id") id: Long
    ): EmployeeResponse

    @POST("api/employees")
    suspend fun createEmployee(
        @Body request: EmployeeCreateRequest
    ): EmployeeResponse

    @PUT("api/employees/{id}")
    suspend fun updateEmployee(
        @Path("id") id: Long,
        @Body request: EmployeeUpdateRequest
    ): EmployeeResponse

    @DELETE("api/employees/{id}")
    suspend fun deleteEmployee(
        @Path("id") id: Long
    ): String
}
