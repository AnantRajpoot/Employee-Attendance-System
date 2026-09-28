package com.example.employeeattendance.ui.screens.employee

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.employeeattendance.data.local.SessionManager
import com.example.employeeattendance.data.network.RetrofitClient
import com.example.employeeattendance.ui.components.AppBottomBar
import com.example.employeeattendance.ui.components.AppHeaderBar
import com.example.employeeattendance.ui.components.ColorfulActionCard
import com.example.employeeattendance.ui.components.InitialsAvatar
import com.example.employeeattendance.ui.components.NotificationsSheet
import com.example.employeeattendance.ui.components.NavigationItem
import com.example.employeeattendance.ui.components.ScrollbarColumn
import com.example.employeeattendance.ui.components.StatusBadge
import com.example.employeeattendance.ui.components.TodayScheduleTimeline
import com.example.employeeattendance.ui.theme.AccentGreen
import com.example.employeeattendance.ui.theme.AccentPink
import com.example.employeeattendance.ui.theme.AccentPurple
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.employeeattendance.data.model.LeaveResponse
import com.example.employeeattendance.ui.components.NotificationItemData
import com.example.employeeattendance.ui.theme.SoftBlueDark
import com.example.employeeattendance.ui.theme.SoftBlueLight
import com.example.employeeattendance.ui.theme.SoftGreenDark
import com.example.employeeattendance.ui.theme.SoftGreenLight
import com.example.employeeattendance.ui.theme.SoftOrangeDark
import com.example.employeeattendance.ui.theme.SoftOrangeLight
import com.example.employeeattendance.ui.theme.SoftPurpleLight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    onAttendance: () -> Unit,
    onLeaves: () -> Unit,
    onProfile: () -> Unit
) {
    val name = sessionManager.getName() ?: "Employee"
    val employeeId = sessionManager.getEmployeeId()
    val role = sessionManager.getRole() ?: "EMPLOYEE"

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var isCheckingIn by remember { mutableStateOf(false) }
    var isCheckingOut by remember { mutableStateOf(false) }
    var attendanceId by remember { mutableStateOf<Long?>(null) }
    var hasCheckedIn by remember { mutableStateOf(false) }
    var hasCheckedOut by remember { mutableStateOf(false) }
    var checkInTime by remember { mutableStateOf<String?>(null) }
    var checkOutTime by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    val greetingPrefix = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    var myLeaves by remember { mutableStateOf<List<LeaveResponse>>(emptyList()) }
    var readNotificationIds by remember {
        mutableStateOf(sessionManager.getReadNotificationIds())
    }

    LaunchedEffect(employeeId) {
        try {
            val attendanceList = RetrofitClient.attendanceApi.getEmployeeAttendance(employeeId)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayAttendance = attendanceList.find { it.attendanceDate == today }

            if (todayAttendance != null) {
                attendanceId = todayAttendance.id
                hasCheckedIn = true
                hasCheckedOut = todayAttendance.checkOutTime != null
                checkInTime = todayAttendance.checkInTime
                checkOutTime = todayAttendance.checkOutTime
            }

            myLeaves = RetrofitClient.leaveApi.getMyLeaves()
        } catch (_: Exception) {
            message = "Unable to load today's attendance."
        } finally {
            isLoading = false
        }
    }

    val unreadNotifications = remember(myLeaves, readNotificationIds) {
        val realNotifications = myLeaves.filter { it.status == "APPROVED" || it.status == "REJECTED" }.map { leave ->
            val statusFormatted = if (leave.status == "APPROVED") "Approved 🎉" else "Rejected ❌"
            NotificationItemData(
                id = leave.id.toString(),
                title = "Leave Request $statusFormatted",
                description = "Your leave request (${leave.startDate} to ${leave.endDate}) was ${leave.status.lowercase()}."
            )
        }
        realNotifications.filter { it.id !in readNotificationIds }
    }

    var showNotifications by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavigationItem("home", "Home", Icons.Default.Home),
        NavigationItem("attendance", "Attendance", Icons.Default.Schedule),
        NavigationItem("leaves", "Leaves", Icons.Default.DateRange),
        NavigationItem("profile", "Profile", Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            AppHeaderBar(
                title = "Employee Attendance",
                subtitle = "Employee Dashboard",
                userName = name,
                onNotificationClick = { showNotifications = true },
                onProfileClick = onProfile,
                onLogoutClick = onLogout,
                notificationCount = unreadNotifications.size
            )
        },
        bottomBar = {
            AppBottomBar(
                items = navItems,
                currentRoute = "home",
                onItemSelected = { route ->
                    when (route) {
                        "attendance" -> onAttendance()
                        "leaves" -> onLeaves()
                        "profile" -> onProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        ScrollbarColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Greeting & Profile Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InitialsAvatar(name = name, size = 56)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greetingPrefix,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$name 👋",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$role • Dept",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = if (hasCheckedIn) "Present" else "Pending")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Check-In / Check-Out Dual Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Check-In Box
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGreenLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = AccentGreen.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        tint = AccentGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check In",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = formatDisplayTime(checkInTime),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF064E3B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isCheckingIn = true
                                        try {
                                            val realPhoneTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                            val res = RetrofitClient.attendanceApi.checkIn(employeeId)
                                            hasCheckedIn = true
                                            checkInTime = if (!res.checkInTime.isNullOrBlank()) formatDisplayTime(res.checkInTime) else realPhoneTime
                                            attendanceId = res.id
                                        } catch (e: Exception) {
                                            message = e.message
                                        } finally {
                                            isCheckingIn = false
                                        }
                                    }
                                },
                                enabled = !hasCheckedIn && !isCheckingIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentGreen,
                                    disabledContainerColor = Color(0xFFBBF7D0)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isCheckingIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (hasCheckedIn) "Checked In" else "Check In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Check-Out Box
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftPurpleLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = AccentPink.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = AccentPink
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check Out",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF831843)
                            )
                            Text(
                                text = formatDisplayTime(checkOutTime),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF831843)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        if (attendanceId != null) {
                                            isCheckingOut = true
                                            try {
                                                val realPhoneTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                                val res = RetrofitClient.attendanceApi.checkOut(attendanceId!!)
                                                hasCheckedOut = true
                                                checkOutTime = if (!res.checkOutTime.isNullOrBlank()) formatDisplayTime(res.checkOutTime) else realPhoneTime
                                            } catch (e: Exception) {
                                                message = e.message
                                            } finally {
                                                isCheckingOut = false
                                            }
                                        }
                                    }
                                },
                                enabled = hasCheckedIn && !hasCheckedOut && !isCheckingOut && attendanceId != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentPink,
                                    disabledContainerColor = Color(0xFFFBCFE8)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isCheckingOut) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (hasCheckedOut) "Checked Out" else "Check Out",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mini Stat Summary Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatBox(
                        value = "12",
                        label = "Present Days",
                        icon = Icons.Default.CalendarMonth,
                        lightBg = SoftGreenLight,
                        darkBg = SoftGreenDark,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatBox(
                        value = "2",
                        label = "Leave Days",
                        icon = Icons.Default.EventNote,
                        lightBg = SoftOrangeLight,
                        darkBg = SoftOrangeDark,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatBox(
                        value = "1",
                        label = "Late Days",
                        icon = Icons.Default.AccessTime,
                        lightBg = SoftBlueLight,
                        darkBg = SoftBlueDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Actions Section
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorfulActionCard(
                        title = "Apply Leave",
                        icon = Icons.Default.EventNote,
                        containerColor = AccentPurple,
                        modifier = Modifier.weight(1f),
                        onClick = onLeaves
                    )
                    ColorfulActionCard(
                        title = "My Attendance",
                        icon = Icons.Default.Schedule,
                        containerColor = AccentGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onAttendance
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Today's Schedule Timeline
                TodayScheduleTimeline(
                    checkInTime = formatDisplayTime(checkInTime),
                    checkOutTime = formatDisplayTime(checkOutTime)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showNotifications) {
            NotificationsSheet(
                notifications = unreadNotifications,
                onDismiss = { showNotifications = false },
                onNotificationClick = { item ->
                    val updated = readNotificationIds + item.id
                    readNotificationIds = updated
                    sessionManager.saveReadNotificationIds(updated)
                    onLeaves()
                },
                onClearAll = {
                    val updated = readNotificationIds + unreadNotifications.map { it.id }.toSet()
                    readNotificationIds = updated
                    sessionManager.saveReadNotificationIds(updated)
                }
            )
        }
    }
}

@Composable
private fun MiniStatBox(
    value: String,
    label: String,
    icon: ImageVector,
    lightBg: Color,
    darkBg: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) darkBg else lightBg
    val contentColor = if (isDark) Color.White else Color(0xFF0F172A)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private fun formatDisplayTime(rawTime: String?): String {
    if (rawTime.isNullOrBlank()) return "--:--"
    return try {
        if (rawTime.contains("AM", ignoreCase = true) || rawTime.contains("PM", ignoreCase = true)) {
            rawTime
        } else if (rawTime.contains("T")) {
            val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = isoParser.parse(rawTime)
            if (date != null) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date) else rawTime
        } else {
            val timeParser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = timeParser.parse(rawTime)
            if (date != null) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date) else rawTime
        }
    } catch (_: Exception) {
        rawTime
    }
}
