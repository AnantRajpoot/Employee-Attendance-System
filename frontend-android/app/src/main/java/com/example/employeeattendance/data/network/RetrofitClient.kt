package com.example.employeeattendance.data.network

import android.content.Context
import com.example.employeeattendance.data.local.SessionManager
import com.example.employeeattendance.data.api.AttendanceApi
import com.example.employeeattendance.data.api.AuthApi
import com.example.employeeattendance.data.api.EmployeeApi
import com.example.employeeattendance.data.api.LeaveApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://employee-attendance-system-zshf.onrender.com/"

    lateinit var authApi: AuthApi
        private set

    lateinit var attendanceApi: AttendanceApi
        private set

    lateinit var leaveApi: LeaveApi
        private set

    lateinit var employeeApi: EmployeeApi
        private set

    fun initialize(context: Context) {

        val sessionManager =
            SessionManager(context.applicationContext)

        val loggingInterceptor =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

        val authInterceptor =
            AuthInterceptor(sessionManager)

        val okHttpClient =
            OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

        val retrofit =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()

        authApi =
            retrofit.create(AuthApi::class.java)

        attendanceApi =
            retrofit.create(AttendanceApi::class.java)

        leaveApi =
            retrofit.create(LeaveApi::class.java)

        employeeApi =
            retrofit.create(EmployeeApi::class.java)
    }
}
