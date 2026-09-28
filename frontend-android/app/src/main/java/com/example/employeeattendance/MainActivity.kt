package com.example.employeeattendance

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.employeeattendance.data.local.SessionManager
import com.example.employeeattendance.data.network.RetrofitClient
import com.example.employeeattendance.ui.navigation.AppNavigation
import com.example.employeeattendance.ui.theme.EmployeeAttendanceTheme

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        /*
         * -----------------------------------------------------
         * SESSION
         * -----------------------------------------------------
         */

        sessionManager =
            SessionManager(this)


        /*
         * -----------------------------------------------------
         * RETROFIT
         * -----------------------------------------------------
         */

        RetrofitClient.initialize(this)


        /*
         * -----------------------------------------------------
         * EDGE TO EDGE
         * -----------------------------------------------------
         */

        enableEdgeToEdge()


        /*
         * -----------------------------------------------------
         * ANDROID 17 LOCAL NETWORK PERMISSION
         * -----------------------------------------------------
         */

        if (
            android.os.Build.VERSION.SDK_INT >= 37 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(

                this,

                arrayOf(
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                ),

                100
            )
        }


        /*
         * -----------------------------------------------------
         * UI
         * -----------------------------------------------------
         */

        setContent {

            EmployeeAttendanceTheme {

                AppNavigation(
                    sessionManager =
                        sessionManager
                )
            }
        }
    }
}