package com.example.employeeattendance.ui.screens.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.employeeattendance.data.model.AttendanceResponse
import com.example.employeeattendance.data.model.AttendanceStatistics
import com.example.employeeattendance.data.local.SessionManager
import com.example.employeeattendance.data.network.RetrofitClient
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.example.employeeattendance.ui.components.AppBottomBar
import com.example.employeeattendance.ui.components.AppHeaderBar
import com.example.employeeattendance.ui.components.BackToTopButton
import com.example.employeeattendance.ui.components.LazyListScrollBar
import com.example.employeeattendance.ui.components.NavigationItem
import com.example.employeeattendance.ui.components.AttendanceBarChart
import com.example.employeeattendance.ui.components.ScrollbarLazyColumn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// ================================================================
// ATTENDANCE SCREEN
// ================================================================

@Composable
fun AttendanceScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {

    /*
     * ------------------------------------------------------------
     * EMPLOYEE ID
     * ------------------------------------------------------------
     */

    val employeeId =
        sessionManager.getEmployeeId()


    /*
     * ------------------------------------------------------------
     * LAZY LIST STATE
     * ------------------------------------------------------------
     */

    val listState =
        rememberLazyListState()


    /*
     * ------------------------------------------------------------
     * UI STATE
     * ------------------------------------------------------------
     */

    var attendance by remember {
        mutableStateOf<List<AttendanceResponse>>(
            emptyList()
        )
    }

    var statistics by remember {
        mutableStateOf<AttendanceStatistics?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }


    /*
     * ------------------------------------------------------------
     * LOAD ATTENDANCE
     * ------------------------------------------------------------
     */

    LaunchedEffect(employeeId) {

        try {

            /*
             * Current date:
             *
             * yyyy-MM-dd
             */

            val today =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(Date())


            /*
             * First day of current month.
             *
             * Example:
             *
             * 2026-09-24
             *
             * becomes:
             *
             * 2026-09-01
             */

            val startDate =
                today.substring(0, 8) + "01"


            val endDate =
                today


            /*
             * Load complete attendance history.
             */

            attendance =
                RetrofitClient
                    .attendanceApi
                    .getEmployeeAttendance(
                        employeeId
                    )


            /*
             * Load statistics for current month.
             */

            statistics =
                RetrofitClient
                    .attendanceApi
                    .getAttendanceStatistics(
                        employeeId,
                        startDate,
                        endDate
                    )

        } catch (e: Exception) {

            errorMessage =
                "Failed to load attendance: ${e.message}"

        } finally {

            isLoading = false
        }
    }


    /*
     * ------------------------------------------------------------
     * SCREEN
     * ------------------------------------------------------------
     */

    val navItems = listOf(
        NavigationItem("home", "Home", Icons.Default.Home),
        NavigationItem("attendance", "Attendance", Icons.Default.Schedule),
        NavigationItem("leaves", "Leaves", Icons.Default.DateRange),
        NavigationItem("profile", "Profile", Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            AppHeaderBar(
                title = "My Attendance",
                subtitle = "Attendance History & Stats",
                userName = sessionManager.getName() ?: "Employee",
                onBackClick = onBack
            )
        },
        bottomBar = {
            AppBottomBar(
                items = navItems,
                currentRoute = "attendance",
                onItemSelected = { route ->
                    when (route) {
                        "home" -> onNavigate("dashboard")
                        "attendance" -> { }
                        "leaves" -> onNavigate("leaves")
                        "profile" -> onNavigate("profile")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {


            /*
             * ----------------------------------------------------
             * LOADING STATE
             * ----------------------------------------------------
             */

            if (isLoading) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    CircularProgressIndicator()


                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )


                    Text(
                        text =
                            "Loading attendance..."
                    )
                }


                /*
                 * ----------------------------------------------------
                 * ERROR STATE
                 * ----------------------------------------------------
                 */

            } else if (errorMessage != null) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Text(
                        text =
                            "Unable to load attendance",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    Text(
                        text =
                            errorMessage
                                ?: "Unknown error",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }


                /*
                 * ----------------------------------------------------
                 * CONTENT
                 * ----------------------------------------------------
                 */

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    /*
                     * ------------------------------------------------
                     * ATTENDANCE LIST
                     * ------------------------------------------------
                     */

                    LazyColumn(
                        state = listState,

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 20.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        /*
                         * ------------------------------------------------
                         * HEADER
                         * ------------------------------------------------
                         */

                        item {

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )


                            Text(
                                text =
                                    "Attendance Overview",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge,

                                fontWeight =
                                    FontWeight.Bold
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )


                            Text(
                                text =
                                    "Your attendance statistics for this month",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )
                        }


                        /*
                         * ------------------------------------------------
                         * STATISTICS
                         * ------------------------------------------------
                         */

                        statistics?.let { stats ->

                            item {

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.spacedBy(
                                            10.dp
                                        )
                                ) {

                                    StatisticCard(
                                        modifier =
                                            Modifier.weight(
                                                1f
                                            ),

                                        icon =
                                            Icons.Default.CalendarMonth,

                                        title =
                                            "Total Days",

                                        value =
                                            stats.totalDays
                                                .toString()
                                    )


                                    StatisticCard(
                                        modifier =
                                            Modifier.weight(
                                                1f
                                            ),

                                        icon =
                                            Icons.Default.CheckCircle,

                                        title =
                                            "Present",

                                        value =
                                            stats.presentDays
                                                .toString()
                                    )
                                }
                            }


                            /*
                             * ------------------------------------------------
                             * TOTAL HOURS
                             * ------------------------------------------------
                             */

                            item {

                                Card(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    shape =
                                        RoundedCornerShape(
                                            18.dp
                                        ),

                                    elevation =
                                        CardDefaults
                                            .cardElevation(
                                                defaultElevation =
                                                    2.dp
                                            )
                                ) {

                                    Row(
                                        modifier =
                                            Modifier.padding(
                                                18.dp
                                            ),

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Surface(
                                            modifier =
                                                Modifier.size(
                                                    48.dp
                                                ),

                                            shape =
                                                RoundedCornerShape(
                                                    14.dp
                                                ),

                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .secondaryContainer
                                        ) {

                                            Icon(
                                                imageVector =
                                                    Icons.Default.AccessTime,

                                                contentDescription =
                                                    "Hours worked",

                                                modifier =
                                                    Modifier.padding(
                                                        12.dp
                                                    )
                                            )
                                        }


                                        Spacer(
                                            modifier =
                                                Modifier.size(
                                                    14.dp
                                                )
                                        )


                                        Column {

                                            Text(
                                                text =
                                                    "Total Hours Worked",

                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .bodyMedium
                                            )


                                            Text(
                                                text =
                                                    "${stats.totalHoursWorked} hours",

                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .titleMedium,

                                                fontWeight =
                                                    FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }


                        /*
                         * ------------------------------------------------
                         * HISTORY HEADER
                         * ------------------------------------------------
                         */

                        item {

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )


                            Text(
                                text =
                                    "Attendance History",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }


                        /*
                         * ------------------------------------------------
                         * EMPTY STATE
                         * ------------------------------------------------
                         */

                        if (attendance.isEmpty()) {

                            item {

                                Card(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    shape =
                                        RoundedCornerShape(
                                            18.dp
                                        )
                                ) {

                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    24.dp
                                                ),

                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Default.CalendarMonth,

                                            contentDescription =
                                                null,

                                            modifier =
                                                Modifier.size(
                                                    42.dp
                                                )
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.height(
                                                    12.dp
                                                )
                                        )


                                        Text(
                                            text =
                                                "No attendance records",

                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .titleMedium,

                                            fontWeight =
                                                FontWeight.Bold
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.height(
                                                    4.dp
                                                )
                                        )


                                        Text(
                                            text =
                                                "Your attendance records will appear here."
                                        )
                                    }
                                }
                            }


                        } else {

                            /*
                             * ------------------------------------------------
                             * ATTENDANCE RECORDS
                             * ------------------------------------------------
                             */

                            items(
                                items = attendance,

                                key = {
                                    it.id
                                }
                            ) { record ->

                                AttendanceHistoryCard(
                                    record = record
                                )
                            }
                        }


                        /*
                         * ------------------------------------------------
                         * BOTTOM SPACING
                         * ------------------------------------------------
                         */

                        item {

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        80.dp
                                    )
                            )
                        }
                    }


                    /*
                     * ------------------------------------------------
                     * SCROLL BAR
                     * ------------------------------------------------
                     */

                    LazyListScrollBar(
                        listState = listState,

                        modifier =
                            Modifier
                                .align(
                                    Alignment.CenterEnd
                                )
                                .padding(
                                    end = 4.dp
                                )
                                .zIndex(1f)
                    )


                    /*
                     * ------------------------------------------------
                     * BACK TO TOP BUTTON
                     * ------------------------------------------------
                     */

                    BackToTopButton(
                        listState = listState,

                        modifier =
                            Modifier
                                .align(
                                    Alignment.BottomEnd
                                )
                                .padding(
                                    20.dp
                                )
                                .zIndex(2f)
                    )
                }
            }
        }
    }
}


// ================================================================
// STATISTIC CARD
// ================================================================

@Composable
private fun StatisticCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {

    Card(
        modifier = modifier,

        shape =
            RoundedCornerShape(
                18.dp
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Surface(
                modifier =
                    Modifier.size(
                        42.dp
                    ),

                shape =
                    RoundedCornerShape(
                        12.dp
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Icon(
                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.padding(
                            10.dp
                        )
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            Text(
                text = title,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )


            Text(
                text = value,

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// ================================================================
// ATTENDANCE HISTORY CARD
// ================================================================

@Composable
private fun AttendanceHistoryCard(
    record: AttendanceResponse
) {

    val isPresent =
        record.status.equals(
            "PRESENT",
            ignoreCase = true
        )


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {

            /*
             * --------------------------------------------------------
             * DATE + STATUS
             * --------------------------------------------------------
             */

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    modifier =
                        Modifier.size(
                            46.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            13.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,

                        contentDescription =
                            "Date",

                        modifier =
                            Modifier.padding(
                                11.dp
                            )
                    )
                }


                Spacer(
                    modifier =
                        Modifier.size(
                            14.dp
                        )
                )


                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(
                        text =
                            formatAttendanceDate(
                                record.attendanceDate
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(
                        text =
                            record.attendanceDate,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }


                /*
                 * STATUS BADGE
                 */

                Surface(
                    shape =
                        RoundedCornerShape(
                            20.dp
                        ),

                    color =
                        if (isPresent) {

                            MaterialTheme
                                .colorScheme
                                .primaryContainer

                        } else {

                            MaterialTheme
                                .colorScheme
                                .errorContainer
                        }
                ) {

                    Text(
                        text =
                            record.status,

                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            HorizontalDivider()


            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )


            /*
             * --------------------------------------------------------
             * CHECK IN / CHECK OUT
             * --------------------------------------------------------
             */

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                AttendanceTime(
                    icon =
                        Icons.Default.Work,

                    title =
                        "Check In",

                    value =
                        formatAttendanceTime(
                            record.checkInTime
                        )
                )


                AttendanceTime(
                    icon =
                        Icons.Default.Schedule,

                    title =
                        "Check Out",

                    value =
                        formatAttendanceTime(
                            record.checkOutTime
                        )
                )
            }
        }
    }
}


// ================================================================
// ATTENDANCE TIME
// ================================================================

@Composable
private fun AttendanceTime(
    icon: ImageVector,
    title: String,
    value: String
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector =
                icon,

            contentDescription =
                null,

            modifier =
                Modifier.size(
                    20.dp
                )
        )


        Spacer(
            modifier =
                Modifier.size(
                    8.dp
                )
        )


        Column {

            Text(
                text = title,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium
            )


            Text(
                text = value,

                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}


// ================================================================
// FORMAT DATE
// ================================================================

private fun formatAttendanceDate(
    value: String
): String {

    return try {

        val inputFormat =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )


        val outputFormat =
            SimpleDateFormat(
                "EEE, dd MMM yyyy",
                Locale.getDefault()
            )


        val date =
            inputFormat.parse(
                value
            )


        if (date != null) {

            outputFormat.format(
                date
            )

        } else {

            value
        }

    } catch (_: Exception) {

        value
    }
}


// ================================================================
// FORMAT TIME
// ================================================================

/*
 * Handles:
 *
 * 2026-09-24T01:42:22
 *
 * and:
 *
 * 2026-09-24T01:42:22.123456
 */

private fun formatAttendanceTime(
    value: String?
): String {

    if (value.isNullOrBlank()) {

        return "—"
    }


    return try {

        /*
         * Remove fractional seconds.
         */

        val cleanedValue =
            value.substringBefore(".")


        val inputFormat =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            )


        val outputFormat =
            SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            )


        val date =
            inputFormat.parse(
                cleanedValue
            )


        if (date != null) {

            outputFormat.format(
                date
            )

        } else {

            value
        }

    } catch (_: Exception) {

        value
    }
}