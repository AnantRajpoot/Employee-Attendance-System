package com.example.employeeattendance.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import com.example.employeeattendance.data.local.SessionManager
import com.example.employeeattendance.data.model.LoginResponse
import com.example.employeeattendance.ui.screens.auth.LoginScreen
import com.example.employeeattendance.ui.screens.employee.AttendanceScreen
import com.example.employeeattendance.ui.screens.employee.DashboardScreen
import com.example.employeeattendance.ui.screens.employee.LeaveScreen
import com.example.employeeattendance.ui.screens.hr.HrDashboardScreen
import com.example.employeeattendance.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation(
    sessionManager: SessionManager
) {

    /*
     * ---------------------------------------------------------
     * INITIAL SCREEN
     * ---------------------------------------------------------
     */

    var currentScreen by remember {

        mutableStateOf(

            when {

                sessionManager.getToken() == null ->
                    "login"

                sessionManager
                    .getRole()
                    ?.equals(
                        "HR",
                        ignoreCase = true
                    ) == true ->
                    "hr_dashboard"

                else ->
                    "dashboard"
            }
        )
    }


    /*
     * ---------------------------------------------------------
     * BACK BUTTON
     * ---------------------------------------------------------
     */

    BackHandler(
        enabled = currentScreen != "login"
    ) {

        currentScreen = when (currentScreen) {

            "attendance",
            "leaves",
            "profile" -> {

                if (
                    sessionManager
                        .getRole()
                        ?.equals(
                            "HR",
                            ignoreCase = true
                        ) == true
                ) {
                    "hr_dashboard"
                } else {
                    "dashboard"
                }
            }

            "hr_dashboard" ->
                "hr_dashboard"

            "dashboard" ->
                "dashboard"

            else ->
                "login"
        }
    }


    /*
     * ---------------------------------------------------------
     * NAVIGATION
     * ---------------------------------------------------------
     */

    when (currentScreen) {

        /*
         * =====================================================
         * LOGIN
         * =====================================================
         */

        "login" -> {

            LoginScreen(

                onLoginSuccess = { response: LoginResponse ->

                    sessionManager.saveSession(

                        token =
                            response.token,

                        employeeId =
                            response.employeeId,

                        name =
                            response.name,

                        role =
                            response.role
                    )

                    currentScreen =

                        if (
                            response.role
                                .equals(
                                    "HR",
                                    ignoreCase = true
                                )
                        ) {

                            "hr_dashboard"

                        } else {

                            "dashboard"
                        }
                }
            )
        }


        /*
         * =====================================================
         * EMPLOYEE DASHBOARD
         * =====================================================
         */

        "dashboard" -> {

            DashboardScreen(

                sessionManager =
                    sessionManager,

                onAttendance = {

                    currentScreen =
                        "attendance"
                },

                onLeaves = {

                    currentScreen =
                        "leaves"
                },

                onProfile = {

                    currentScreen =
                        "profile"
                },

                onLogout = {

                    sessionManager.clearSession()

                    currentScreen =
                        "login"
                }
            )
        }


        /*
         * =====================================================
         * HR DASHBOARD
         * =====================================================
         */

        "hr_dashboard" -> {

            HrDashboardScreen(

                sessionManager =
                    sessionManager,

                onProfile = {

                    currentScreen =
                        "profile"
                },

                onLogout = {

                    sessionManager.clearSession()

                    currentScreen =
                        "login"
                }
            )
        }


        /*
         * =====================================================
         * ATTENDANCE
         * =====================================================
         */

        "attendance" -> {

            AttendanceScreen(

                sessionManager =
                    sessionManager,

                onBack = {

                    currentScreen =

                        if (
                            sessionManager
                                .getRole()
                                ?.equals(
                                    "HR",
                                    ignoreCase = true
                                ) == true
                        ) {

                            "hr_dashboard"

                        } else {

                            "dashboard"
                        }
                },

                onNavigate = { route ->
                    currentScreen = route
                }
            )
        }


        /*
         * =====================================================
         * LEAVES
         * =====================================================
         */

        "leaves" -> {

            LeaveScreen(

                onBack = {

                    currentScreen =

                        if (
                            sessionManager
                                .getRole()
                                ?.equals(
                                    "HR",
                                    ignoreCase = true
                                ) == true
                        ) {

                            "hr_dashboard"

                        } else {

                            "dashboard"
                        }
                },

                onNavigate = { route ->
                    currentScreen = route
                }
            )
        }


        /*
         * =====================================================
         * PROFILE
         * =====================================================
         */

        "profile" -> {

            ProfileScreen(

                onBack = {

                    currentScreen =

                        if (
                            sessionManager
                                .getRole()
                                ?.equals(
                                    "HR",
                                    ignoreCase = true
                                ) == true
                        ) {

                            "hr_dashboard"

                        } else {

                            "dashboard"
                        }
                },

                onLogout = {

                    sessionManager.clearSession()

                    currentScreen =
                        "login"
                },

                onNavigate = { route ->
                    currentScreen = route
                }
            )
        }
    }
}