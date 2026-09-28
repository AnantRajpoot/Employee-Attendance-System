package com.example.employeeattendance.ui.screens.employee

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import com.example.employeeattendance.ui.components.AppBottomBar
import com.example.employeeattendance.ui.components.AppHeaderBar
import com.example.employeeattendance.ui.components.NavigationItem
import com.example.employeeattendance.ui.components.ScrollbarLazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.employeeattendance.data.model.LeaveRequest
import com.example.employeeattendance.data.model.LeaveResponse
import com.example.employeeattendance.data.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LeaveScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    var leaves by remember {
        mutableStateOf<List<LeaveResponse>>(emptyList())
    }

    var isLoadingLeaves by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun openDatePicker(
        currentDate: String,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        if (currentDate.isNotBlank()) {
            try {
                val formatter = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )
                formatter.isLenient = false
                formatter.parse(currentDate)?.let {
                    calendar.time = it
                }
            } catch (_: Exception) {
                // Keep today's date.
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(
                    String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                    )
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun isValidDate(value: String): Boolean {
        return try {
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )
            formatter.isLenient = false
            formatter.parse(value)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isDateRangeValid(): Boolean {
        return try {
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )
            formatter.isLenient = false

            val start = formatter.parse(startDate)
            val end = formatter.parse(endDate)

            start != null && end != null && !end.before(start)
        } catch (_: Exception) {
            false
        }
    }

    fun submitLeave() {
        message = null
        isError = false

        when {
            startDate.isBlank() -> {
                message = "Please select a start date."
                isError = true
                return
            }

            endDate.isBlank() -> {
                message = "Please select an end date."
                isError = true
                return
            }

            reason.trim().length < 5 -> {
                message = "Please enter a little more detail for the reason."
                isError = true
                return
            }

            !isValidDate(startDate) -> {
                message = "Invalid start date."
                isError = true
                return
            }

            !isValidDate(endDate) -> {
                message = "Invalid end date."
                isError = true
                return
            }

            !isDateRangeValid() -> {
                message = "End date cannot be before start date."
                isError = true
                return
            }
        }

        scope.launch {
            isSubmitting = true
            message = null
            isError = false

            try {
                val response = RetrofitClient.leaveApi.applyLeave(
                    LeaveRequest(
                        startDate = startDate,
                        endDate = endDate,
                        reason = reason.trim()
                    )
                )

                leaves = listOf(response) + leaves
                startDate = ""
                endDate = ""
                reason = ""
                message = "Leave request submitted successfully."
            } catch (e: Exception) {
                message = "Leave request failed: ${e.message ?: "Unknown error"}"
                isError = true
            } finally {
                isSubmitting = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            leaves = RetrofitClient.leaveApi.getMyLeaves()
        } catch (e: Exception) {
            message = "Failed to load leave requests: ${e.message ?: "Unknown error"}"
            isError = true
        } finally {
            isLoadingLeaves = false
        }
    }

    val navItems = listOf(
        NavigationItem("home", "Home", Icons.Default.Home),
        NavigationItem("attendance", "Attendance", Icons.Default.Schedule),
        NavigationItem("leaves", "Leaves", Icons.Default.DateRange),
        NavigationItem("profile", "Profile", Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            AppHeaderBar(
                title = "Leave Management",
                subtitle = "Apply and track your leaves",
                onBackClick = onBack
            )
        },
        bottomBar = {
            AppBottomBar(
                items = navItems,
                currentRoute = "leaves",
                onItemSelected = { route ->
                    when (route) {
                        "home" -> onNavigate("dashboard")
                        "attendance" -> onNavigate("attendance")
                        "leaves" -> { }
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

            HorizontalDivider()

            val listState = rememberLazyListState()

            ScrollbarLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Request Leave",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Submit a leave request for your manager to review."
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EventNote,
                                            contentDescription = null,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.size(14.dp))

                                    Column {
                                        Text(
                                            text = "Apply for Leave",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("Select dates and enter the reason.")
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                DateField(
                                    label = "Start Date",
                                    value = startDate,
                                    enabled = !isSubmitting,
                                    onClick = {
                                        openDatePicker(startDate) {
                                            startDate = it
                                            message = null
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                DateField(
                                    label = "End Date",
                                    value = endDate,
                                    enabled = !isSubmitting,
                                    onClick = {
                                        openDatePicker(endDate) {
                                            endDate = it
                                            message = null
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = {
                                        reason = it
                                        message = null
                                    },
                                    enabled = !isSubmitting,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Reason") },
                                    placeholder = { Text("Enter the reason for your leave") },
                                    minLines = 4,
                                    maxLines = 6
                                )

                                message?.let { currentMessage ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isError) {
                                                MaterialTheme.colorScheme.errorContainer
                                            } else {
                                                MaterialTheme.colorScheme.primaryContainer
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = currentMessage,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = ::submitLeave,
                                    enabled = !isSubmitting,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text("Submitting...")
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text("Submit Leave Request")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "My Leave Requests",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Track the status of your submitted requests.")
                    }

                    if (isLoadingLeaves) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Loading your leave requests...")
                            }
                        }
                    } else if (leaves.isEmpty()) {
                        item {
                            EmptyLeaveState()
                        }
                    } else {
                        items(
                            items = leaves,
                            key = { it.id }
                        ) { leave ->
                            LeaveRequestCard(leave)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        label = { Text(label) },
        placeholder = { Text("Select date") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onClick,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select $label"
                )
            }
        },
        singleLine = true
    )
}

@Composable
private fun EmptyLeaveState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No leave requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Your submitted leave requests will appear here.")
        }
    }
}

@Composable
private fun LeaveRequestCard(leave: LeaveResponse) {
    val status = leave.status.uppercase()

    val statusColor = when (status) {
        "APPROVED" -> MaterialTheme.colorScheme.primaryContainer
        "REJECTED" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val statusIcon = when (status) {
        "APPROVED" -> Icons.Default.CheckCircle
        "REJECTED" -> Icons.Default.Cancel
        else -> Icons.Default.Pending
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = statusColor
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        modifier = Modifier.padding(11.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Leave Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${leave.startDate} → ${leave.endDate}")
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor
                ) {
                    Text(
                        text = leave.status,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Reason",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(leave.reason)

            leave.appliedAt?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Applied: ${formatLeaveTimestamp(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            leave.reviewedAt?.let {
                Text(
                    text = "Reviewed: ${formatLeaveTimestamp(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatLeaveTimestamp(value: String): String {
    return try {
        val cleaned = value.substringBefore(".")
        val input = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        )
        val output = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )
        val date = input.parse(cleaned)
        if (date != null) output.format(date) else value
    } catch (_: Exception) {
        value
    }
}
