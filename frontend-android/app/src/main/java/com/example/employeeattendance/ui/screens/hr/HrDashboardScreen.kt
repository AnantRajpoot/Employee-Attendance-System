package com.example.employeeattendance.ui.screens.hr

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventNote
import com.example.employeeattendance.data.util.PdfReportGenerator
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.employeeattendance.data.local.SessionManager
import com.example.employeeattendance.data.model.AttendanceResponse
import com.example.employeeattendance.data.model.EmployeeCreateRequest
import com.example.employeeattendance.data.model.EmployeeResponse
import com.example.employeeattendance.data.model.EmployeeUpdateRequest
import com.example.employeeattendance.data.model.HrLeaveResponse
import com.example.employeeattendance.data.network.RetrofitClient
import com.example.employeeattendance.ui.components.AppBottomBar
import com.example.employeeattendance.ui.components.AppHeaderBar
import com.example.employeeattendance.ui.components.NotificationItemData
import com.example.employeeattendance.ui.components.NotificationsSheet
import com.example.employeeattendance.ui.components.AttendanceBarChart
import com.example.employeeattendance.ui.components.ColoredStatCard
import com.example.employeeattendance.ui.components.ColorfulActionCard
import com.example.employeeattendance.ui.components.FilterChipsRow
import com.example.employeeattendance.ui.components.InitialsAvatar
import com.example.employeeattendance.ui.components.NavigationItem
import com.example.employeeattendance.ui.components.ScrollbarColumn
import com.example.employeeattendance.ui.components.ScrollbarLazyColumn
import com.example.employeeattendance.ui.components.StatusBadge
import com.example.employeeattendance.ui.theme.AccentBlue
import com.example.employeeattendance.ui.theme.AccentGreen
import com.example.employeeattendance.ui.theme.AccentOrange
import com.example.employeeattendance.ui.theme.AccentPurple
import com.example.employeeattendance.ui.theme.AccentRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HrDashboardScreen(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    onProfile: () -> Unit
) {
    val context = LocalContext.current
    var section by remember { mutableStateOf("dashboard") }

    var employees by remember { mutableStateOf<List<EmployeeResponse>>(emptyList()) }
    var attendance by remember { mutableStateOf<List<AttendanceResponse>>(emptyList()) }
    var leaves by remember { mutableStateOf<List<HrLeaveResponse>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedEmployee by remember { mutableStateOf<EmployeeResponse?>(null) }
    var employeeToDelete by remember { mutableStateOf<EmployeeResponse?>(null) }
    var showEmployeeDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<EmployeeResponse?>(null) }
    var showEmployeeDetails by remember { mutableStateOf<EmployeeResponse?>(null) }
    var attendanceEmployeeId by remember { mutableStateOf<Long?>(null) }

    var reportStartDate by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date(System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L)
            )
        )
    }

    var reportEndDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    val scope = rememberCoroutineScope()

    fun refreshAll() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                employees = RetrofitClient.employeeApi.getAllEmployees()
                attendance = RetrofitClient.attendanceApi.getAllAttendance()
                leaves = RetrofitClient.leaveApi.getAllLeaves()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load HR data"
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshEmployeesOnly() {
        scope.launch {
            try {
                employees = RetrofitClient.employeeApi.getAllEmployees()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to refresh employees"
            }
        }
    }

    fun refreshLeavesOnly() {
        scope.launch {
            try {
                leaves = RetrofitClient.leaveApi.getAllLeaves()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to refresh leaves"
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val presentTodayCount = remember(attendance) {
        attendance.count { it.attendanceDate == today && (it.status == "PRESENT" || it.status == "CHECKED IN") }
    }

    val pendingLeavesCount = remember(leaves) {
        leaves.count { it.status == "PENDING" }
    }

    val approvedLeavesCount = remember(leaves) {
        leaves.count { it.status == "APPROVED" }
    }

    val timeGreeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning! ☀️"
            in 12..16 -> "Good Afternoon! 🌤️"
            else -> "Good Evening! 🌙"
        }
    }

    var readHrNotificationIds by remember {
        mutableStateOf(sessionManager.getReadNotificationIds())
    }

    val unreadHrNotifications = remember(leaves, readHrNotificationIds) {
        val realNotifications = leaves.filter { it.status == "PENDING" }.map { leave ->
            NotificationItemData(
                id = leave.id.toString(),
                title = "New Leave Request (${leave.employee?.name ?: "Employee"})",
                description = "${leave.reason} (${leave.startDate} to ${leave.endDate})"
            )
        }
        realNotifications.filter { it.id !in readHrNotificationIds }
    }

    var showNotifications by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavigationItem("dashboard", "Home", Icons.Default.Home),
        NavigationItem("employees", "Employees", Icons.Default.People),
        NavigationItem("attendance", "Attendance", Icons.Default.CalendarMonth),
        NavigationItem("leaves", "Leaves", Icons.Default.DateRange)
    )

    Scaffold(
        topBar = {
            AppHeaderBar(
                title = "Employee Attendance",
                subtitle = when (section) {
                    "employees" -> "Employee Management"
                    "attendance" -> "Team Attendance Overview"
                    "leaves" -> "Manage Leave Requests"
                    else -> "HR Dashboard"
                },
                userName = sessionManager.getName() ?: "HR Manager",
                onNotificationClick = { showNotifications = true },
                onProfileClick = onProfile,
                onLogoutClick = onLogout,
                notificationCount = unreadHrNotifications.size
            )
        },
        bottomBar = {
            AppBottomBar(
                items = navItems,
                currentRoute = section,
                onItemSelected = { route -> section = route }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading HR Dashboard...")
                    }
                }
            } else {
                when (section) {
                    "dashboard" -> {
                        DashboardSection(
                            employeeCount = employees.size,
                            presentToday = presentTodayCount,
                            pendingLeaves = pendingLeavesCount,
                            approvedLeaves = approvedLeavesCount,
                            timeGreeting = timeGreeting,
                            onEmployees = { section = "employees" },
                            onAttendance = { section = "attendance" },
                            onLeaves = { section = "leaves" },
                            onAddEmployee = {
                                editingEmployee = null
                                showEmployeeDialog = true
                            }
                        )
                    }

                    "employees" -> {
                        EmployeeSection(
                            employees = employees,
                            onAdd = {
                                editingEmployee = null
                                showEmployeeDialog = true
                            },
                            onView = { showEmployeeDetails = it },
                            onEdit = {
                                editingEmployee = it
                                showEmployeeDialog = true
                            },
                            onDelete = { employeeToDelete = it },
                            onBack = { section = "dashboard" }
                        )
                    }

                    "attendance" -> {
                        AttendanceSection(
                            employees = employees,
                            attendance = attendance,
                            leaves = leaves,
                            selectedEmployeeId = attendanceEmployeeId,
                            onSelectEmployee = { attendanceEmployeeId = it },
                            onBack = { section = "dashboard" }
                        )
                    }

                    "leaves" -> {
                        LeaveSection(
                            leaves = leaves,
                            onApprove = { leaveId ->
                                scope.launch {
                                    try {
                                        RetrofitClient.leaveApi.approveLeave(leaveId)
                                        refreshLeavesOnly()
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Failed to approve leave"
                                    }
                                }
                            },
                            onReject = { leaveId ->
                                scope.launch {
                                    try {
                                        RetrofitClient.leaveApi.rejectLeave(leaveId)
                                        refreshLeavesOnly()
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Failed to reject leave"
                                    }
                                }
                            },
                            onBack = { section = "dashboard" }
                        )
                    }

                    "reports" -> {
                        ReportsSection(
                            employees = employees,
                            attendance = attendance,
                            leaves = leaves,
                            startDate = reportStartDate,
                            endDate = reportEndDate,
                            onStartDateChange = { reportStartDate = it },
                            onEndDateChange = { reportEndDate = it },
                            onBack = { section = "dashboard" }
                        )
                    }
                }
            }

            // Employee Create/Edit Dialog
            if (showEmployeeDialog) {
                EmployeeFormDialog(
                    employee = editingEmployee,
                    onDismiss = {
                        showEmployeeDialog = false
                        editingEmployee = null
                    },
                    onSave = { req ->
                        scope.launch {
                            try {
                                if (editingEmployee == null) {
                                    RetrofitClient.employeeApi.createEmployee(req)
                                } else {
                                    RetrofitClient.employeeApi.updateEmployee(
                                        editingEmployee!!.id,
                                        EmployeeUpdateRequest(
                                            name = req.name,
                                            email = req.email.ifBlank { editingEmployee!!.email },
                                            password = if (req.password.isBlank()) null else req.password,
                                            role = req.role,
                                            department = req.department,
                                            joiningDate = editingEmployee!!.joiningDate ?: "",
                                            leaveBalance = editingEmployee!!.leaveBalance ?: 20.0
                                        )
                                    )
                                }
                                showEmployeeDialog = false
                                editingEmployee = null
                                refreshEmployeesOnly()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to save employee"
                            }
                        }
                    }
                )
            }

            // Employee Details Dialog
            showEmployeeDetails?.let { emp ->
                AlertDialog(
                    onDismissRequest = { showEmployeeDetails = null },
                    title = { Text(emp.name, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Email: ${emp.email}")
                            Text("Role: ${emp.role}")
                            Text("Department: ${emp.department ?: "—"}")
                            Text("Employee ID: #${emp.id}")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showEmployeeDetails = null }) {
                            Text("Close")
                        }
                    }
                )
            }

            // Employee Delete Dialog
            employeeToDelete?.let { emp ->
                AlertDialog(
                    onDismissRequest = { employeeToDelete = null },
                    title = { Text("Delete Employee?") },
                    text = { Text("Are you sure you want to delete ${emp.name}? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        RetrofitClient.employeeApi.deleteEmployee(emp.id)
                                        employeeToDelete = null
                                        refreshEmployeesOnly()
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Failed to delete employee"
                                    }
                                }
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { employeeToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        if (showNotifications) {
            NotificationsSheet(
                notifications = unreadHrNotifications,
                onDismiss = { showNotifications = false },
                onNotificationClick = { item ->
                    val updated = readHrNotificationIds + item.id
                    readHrNotificationIds = updated
                    sessionManager.saveReadNotificationIds(updated)
                    section = "leaves"
                },
                onClearAll = {
                    val updated = readHrNotificationIds + unreadHrNotifications.map { it.id }.toSet()
                    readHrNotificationIds = updated
                    sessionManager.saveReadNotificationIds(updated)
                }
            )
        }
    }
}

@Composable
private fun DashboardSection(
    employeeCount: Int,
    presentToday: Int,
    pendingLeaves: Int,
    approvedLeaves: Int,
    timeGreeting: String,
    onEmployees: () -> Unit,
    onAttendance: () -> Unit,
    onLeaves: () -> Unit,
    onAddEmployee: () -> Unit
) {
    val scrollState = rememberScrollState()

    ScrollbarColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InitialsAvatar(name = "HR Manager", size = 56)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "HR Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Manage your team effectively",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Colored Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColoredStatCard(
                    title = "Total\nEmployees",
                    value = employeeCount.toString(),
                    icon = Icons.Default.People,
                    containerColor = AccentBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onEmployees
                )
                ColoredStatCard(
                    title = "Present\nToday",
                    value = presentToday.toString(),
                    icon = Icons.Default.AccessTime,
                    containerColor = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onAttendance
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColoredStatCard(
                    title = "Pending\nLeaves",
                    value = pendingLeaves.toString(),
                    icon = Icons.Default.EventNote,
                    containerColor = AccentOrange,
                    modifier = Modifier.weight(1f),
                    onClick = onLeaves
                )
                ColoredStatCard(
                    title = "Approved\nLeaves",
                    value = approvedLeaves.toString(),
                    icon = Icons.Default.Check,
                    containerColor = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onLeaves
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = timeGreeting,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Here's what's happening with your team today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColorfulActionCard(
                    title = "Add Employee",
                    icon = Icons.Default.Add,
                    containerColor = AccentBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onAddEmployee
                )
                ColorfulActionCard(
                    title = "View Employees",
                    icon = Icons.Default.People,
                    containerColor = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onEmployees
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColorfulActionCard(
                    title = "Attendance",
                    icon = Icons.Default.CalendarMonth,
                    containerColor = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onAttendance
                )
                ColorfulActionCard(
                    title = "Leave Requests",
                    icon = Icons.Default.EventNote,
                    containerColor = AccentOrange,
                    modifier = Modifier.weight(1f),
                    onClick = onLeaves
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmployeeSection(
    employees: List<EmployeeResponse>,
    onAdd: () -> Unit,
    onView: (EmployeeResponse) -> Unit,
    onEdit: (EmployeeResponse) -> Unit,
    onDelete: (EmployeeResponse) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Employee Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        // Search Bar for Employee Management
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            placeholder = { Text("Search employee") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        val filteredEmployees = employees.filter { emp ->
            emp.name.contains(searchQuery, ignoreCase = true) ||
            emp.email.contains(searchQuery, ignoreCase = true) ||
            (emp.department?.contains(searchQuery, ignoreCase = true) == true)
        }

        if (filteredEmployees.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (searchQuery.isEmpty()) "No employees found." else "No employee matching \"$searchQuery\"")
            }
        } else {
            ScrollbarLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEmployees, key = { it.id }) { employee ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    InitialsAvatar(name = employee.name, size = 44)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = employee.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${employee.role} • ${employee.department ?: "—"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = employee.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onView(employee) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View")
                                    }
                                    OutlinedButton(
                                        onClick = { onEdit(employee) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit")
                                    }
                                    OutlinedButton(
                                        onClick = { onDelete(employee) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceSection(
    employees: List<EmployeeResponse>,
    attendance: List<AttendanceResponse>,
    leaves: List<HrLeaveResponse>,
    selectedEmployeeId: Long?,
    onSelectEmployee: (Long?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf("Monthly View") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    var dayCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val dayText = remember(dayCalendar.timeInMillis) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCalendar.time)
    }

    var monthCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val monthText = remember(monthCalendar.timeInMillis) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCalendar.time)
    }

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // View Mode Toggle & PDF Report Download Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChipsRow(
                options = listOf("Daily View", "Monthly View"),
                selectedOption = viewMode,
                onOptionSelected = { viewMode = it },
                modifier = Modifier.weight(1f)
            )

            if (viewMode == "Monthly View") {
                Button(
                    onClick = {
                        PdfReportGenerator.generateMonthlyReport(
                            context = context,
                            monthText = monthText,
                            employees = employees,
                            attendance = attendance,
                            leaves = leaves
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "PDF Report", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Date / Month Selector Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (viewMode == "Daily View") {
                        dayCalendar = (dayCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
                    } else {
                        monthCalendar = (monthCalendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    }
                }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (viewMode == "Daily View") "Day: $dayText" else monthText, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = {
                    if (viewMode == "Daily View") {
                        dayCalendar = (dayCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
                    } else {
                        monthCalendar = (monthCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    }
                }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next")
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            placeholder = { Text("Search employee") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        // Filter Chips Row (All, Present, Absent, On Leave)
        FilterChipsRow(
            options = listOf("All", "Present", "Absent", "On Leave"),
            selectedOption = selectedFilter,
            onOptionSelected = { selectedFilter = it },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Calculate Daily vs Monthly Stats
        val dailyAttendance = remember(attendance, dayText) {
            attendance.filter { it.attendanceDate == dayText }
        }

        val dailyPresent = dailyAttendance.count { it.status == "PRESENT" || it.status == "CHECKED IN" }
        val dailyAbsent = dailyAttendance.count { it.status == "ABSENT" }
        val dailyOnLeave = leaves.count { it.status == "APPROVED" && it.startDate <= dayText && it.endDate >= dayText }

        val monthlyPresent = attendance.count { it.status == "PRESENT" || it.status == "CHECKED IN" }
        val monthlyAbsent = attendance.count { it.status == "ABSENT" }
        val monthlyOnLeave = leaves.count { it.status == "APPROVED" }

        val filteredEmployees = employees.filter { emp ->
            emp.name.contains(searchQuery, ignoreCase = true) ||
            emp.email.contains(searchQuery, ignoreCase = true) ||
            (emp.department?.contains(searchQuery, ignoreCase = true) == true)
        }

        ScrollbarLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary Stat Row for Daily or Monthly
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColoredStatCard(
                            title = if (viewMode == "Daily View") "Present Today" else "Total Present",
                            value = if (viewMode == "Daily View") dailyPresent.toString() else monthlyPresent.toString(),
                            icon = Icons.Default.Check,
                            containerColor = AccentGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ColoredStatCard(
                            title = if (viewMode == "Daily View") "Absent Today" else "Total Absent",
                            value = if (viewMode == "Daily View") dailyAbsent.toString() else monthlyAbsent.toString(),
                            icon = Icons.Default.Close,
                            containerColor = AccentRed,
                            modifier = Modifier.weight(1f)
                        )
                        ColoredStatCard(
                            title = if (viewMode == "Daily View") "On Leave Today" else "Total On Leave",
                            value = if (viewMode == "Daily View") dailyOnLeave.toString() else monthlyOnLeave.toString(),
                            icon = Icons.Default.EventNote,
                            containerColor = AccentOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (viewMode == "Monthly View") {
                    item {
                        AttendanceBarChart(modifier = Modifier.padding(bottom = 12.dp))
                    }
                }

                items(filteredEmployees, key = { it.id }) { employee ->
                    val empRecord = if (viewMode == "Daily View") dailyAttendance.find { it.employee?.id == employee.id } else null
                    val empStatus = empRecord?.status ?: if (viewMode == "Daily View") "Not Marked" else "Active"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectEmployee(employee.id) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InitialsAvatar(name = employee.name, size = 44)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = employee.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${employee.role} • ${employee.department ?: "—"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = empStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaveSection(
    leaves: List<HrLeaveResponse>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Pending") }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        FilterChipsRow(
            options = listOf(
                "Pending (${leaves.count { it.status == "PENDING" }})",
                "Approved (${leaves.count { it.status == "APPROVED" }})",
                "Rejected (${leaves.count { it.status == "REJECTED" }})"
            ),
            selectedOption = selectedFilter,
            onOptionSelected = { selectedFilter = it },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        val filteredLeaves = leaves.filter { leave ->
            when {
                selectedFilter.startsWith("Pending") -> leave.status == "PENDING"
                selectedFilter.startsWith("Approved") -> leave.status == "APPROVED"
                selectedFilter.startsWith("Rejected") -> leave.status == "REJECTED"
                else -> true
            }
        }

        ScrollbarLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLeaves, key = { it.id }) { leave ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                InitialsAvatar(name = leave.employee?.name ?: "Employee", size = 44)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = leave.employee?.name ?: "Employee #${leave.employee?.id ?: "?"}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = leave.employee?.role ?: "Staff",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = leave.status)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${leave.startDate} - ${leave.endDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Reason: ${leave.reason}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (leave.status == "PENDING") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = { onReject(leave.id) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                                    ) {
                                        Text("Reject", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { onApprove(leave.id) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                    ) {
                                        Text("Approve", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsSection(
    employees: List<EmployeeResponse>,
    attendance: List<AttendanceResponse>,
    leaves: List<HrLeaveResponse>,
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    ScrollbarColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AttendanceBarChart(modifier = Modifier.padding(bottom = 16.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Department breakdown
            Text(
                text = "Department Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val deptCounts = employees.groupingBy { it.department ?: "Other" }.eachCount()
            deptCounts.forEach { (dept, count) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = dept, fontWeight = FontWeight.SemiBold)
                        Text(text = "$count employees", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeFormDialog(
    employee: EmployeeResponse?,
    onDismiss: () -> Unit,
    onSave: (EmployeeCreateRequest) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(employee?.role ?: "EMPLOYEE") }
    var department by remember { mutableStateOf(employee?.department ?: "Engineering") }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Add Employee" else "Edit Employee", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (employee == null) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = if (role.equals("HR", ignoreCase = true)) "HR Manager" else "Employee",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Employee") },
                            onClick = {
                                role = "EMPLOYEE"
                                roleExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("HR Manager") },
                            onClick = {
                                role = "HR"
                                roleExpanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        EmployeeCreateRequest(
                            name = name,
                            email = email,
                            password = password,
                            role = role,
                            department = department,
                            joiningDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                            leaveBalance = 20.0
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
