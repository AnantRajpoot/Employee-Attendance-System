package com.example.employeeattendance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/*
 * ---------------------------------------------------------
 * INITIALS AVATAR
 * ---------------------------------------------------------
 */

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val initials = remember(name) {
        name
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") {
                it.first().uppercase()
            }
    }

    Surface(
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.ifBlank { "U" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/*
 * ---------------------------------------------------------
 * APP HEADER BAR
 * ---------------------------------------------------------
 */

@Composable
fun AppHeaderBar(
    title: String,
    subtitle: String,
    userName: String = "User",
    onBackClick: (() -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    notificationCount: Int = 3
) {
    var profileMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (onNotificationClick != null) {
            Box {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
                if (notificationCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = notificationCount.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        if (onProfileClick != null || onLogoutClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Box {
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { profileMenuExpanded = true }
                ) {
                    InitialsAvatar(name = userName, size = 38)
                }

                DropdownMenu(
                    expanded = profileMenuExpanded,
                    onDismissRequest = { profileMenuExpanded = false }
                ) {
                    if (onProfileClick != null) {
                        DropdownMenuItem(
                            text = { Text("My Profile") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            onClick = {
                                profileMenuExpanded = false
                                onProfileClick()
                            }
                        )
                    }
                    if (onLogoutClick != null) {
                        DropdownMenuItem(
                            text = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                profileMenuExpanded = false
                                onLogoutClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

data class NotificationItemData(
    val id: String,
    val title: String,
    val description: String
)

/*
 * ---------------------------------------------------------
 * NOTIFICATIONS DIALOG / SHEET
 * ---------------------------------------------------------
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<NotificationItemData>,
    onDismiss: () -> Unit,
    onNotificationClick: (NotificationItemData) -> Unit,
    onClearAll: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Notifications", fontWeight = FontWeight.Bold)
                }
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Clear All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No new notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    notifications.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNotificationClick(item)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/*
 * ---------------------------------------------------------
 * APP BOTTOM NAVIGATION BAR
 * ---------------------------------------------------------
 */

data class NavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppBottomBar(
    items: List<NavigationItem>,
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.id
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item.id) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/*
 * ---------------------------------------------------------
 * COLORED METRIC STAT CARD
 * ---------------------------------------------------------
 */

@Composable
fun ColoredStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardElevation().let {
            CardDefaults.cardColors(containerColor = containerColor)
        }
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

/*
 * ---------------------------------------------------------
 * COLORFUL ACTION CARD
 * ---------------------------------------------------------
 */

@Composable
fun ColorfulActionCard(
    title: String,
    description: String? = null,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = contentColor.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/*
 * ---------------------------------------------------------
 * FILTER CHIPS ROW
 * ---------------------------------------------------------
 */

@Composable
fun FilterChipsRow(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val selected = option.equals(selectedOption, ignoreCase = true)
            FilterChip(
                selected = selected,
                onClick = { onOptionSelected(option) },
                label = {
                    Text(
                        text = option,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/*
 * ---------------------------------------------------------
 * ATTENDANCE BAR CHART
 * ---------------------------------------------------------
 */

@Composable
fun AttendanceBarChart(
    presentData: List<Float> = listOf(30f, 25f, 28f, 29f, 24f, 10f, 15f),
    absentData: List<Float> = listOf(2f, 4f, 1f, 2f, 5f, 1f, 2f),
    onLeaveData: List<Float> = listOf(3f, 2f, 2f, 1f, 2f, 0f, 1f),
    labels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    modifier: Modifier = Modifier
) {
    val presentColor = Color(0xFF10B981)
    val absentColor = Color(0xFFEF4444)
    val onLeaveColor = Color(0xFFF59E0B)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("Present", presentColor)
                    LegendItem("Absent", absentColor)
                    LegendItem("On Leave", onLeaveColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val barWidth = 14.dp.toPx()
                val spacing = (size.width - (labels.size * barWidth)) / (labels.size + 1)
                val maxVal = 35f

                labels.forEachIndexed { i, _ ->
                    val x = spacing + i * (barWidth + spacing)
                    val pVal = (presentData.getOrElse(i) { 0f } / maxVal) * size.height
                    val aVal = (absentData.getOrElse(i) { 0f } / maxVal) * size.height
                    val lVal = (onLeaveData.getOrElse(i) { 0f } / maxVal) * size.height

                    var currentY = size.height

                    // Present bar
                    if (pVal > 0) {
                        currentY -= pVal
                        drawRoundRect(
                            color = presentColor,
                            topLeft = Offset(x, currentY),
                            size = Size(barWidth, pVal),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Absent bar
                    if (aVal > 0) {
                        currentY -= aVal
                        drawRoundRect(
                            color = absentColor,
                            topLeft = Offset(x, currentY),
                            size = Size(barWidth, aVal),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // On Leave bar
                    if (lVal > 0) {
                        currentY -= lVal
                        drawRoundRect(
                            color = onLeaveColor,
                            topLeft = Offset(x, currentY),
                            size = Size(barWidth, lVal),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/*
 * ---------------------------------------------------------
 * TODAY'S SCHEDULE TIMELINE
 * ---------------------------------------------------------
 */

@Composable
fun TodayScheduleTimeline(
    checkInTime: String?,
    checkOutTime: String? = null,
    expectedCheckOutTime: String = "06:00 PM"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Check-In Timeline Node
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (!checkInTime.isNullOrBlank() && checkInTime != "--:--") Color(0xFF10B981) else Color.Gray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (!checkInTime.isNullOrBlank() && checkInTime != "--:--") checkInTime else "Not Checked In Yet",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Checked In",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 5.dp, top = 4.dp, bottom = 4.dp)
                    .width(2.dp)
                    .height(24.dp)
                    .background(
                        if (!checkOutTime.isNullOrBlank() && checkOutTime != "--:--") Color(0xFFEC4899)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )

            // Check-Out Timeline Node
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (!checkOutTime.isNullOrBlank() && checkOutTime != "--:--") Color(0xFFEC4899)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (!checkOutTime.isNullOrBlank() && checkOutTime != "--:--") checkOutTime else expectedCheckOutTime,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (!checkOutTime.isNullOrBlank() && checkOutTime != "--:--") "Checked Out" else "Expected Check Out",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/*
 * ---------------------------------------------------------
 * STATUS BADGE
 * ---------------------------------------------------------
 */

@Composable
fun StatusBadge(
    status: String
) {
    val normalized = status.trim().uppercase()
    val isDark = isSystemInDarkTheme()

    val backgroundColor = when (normalized) {
        "APPROVED", "PRESENT", "CHECKED IN", "COMPLETED" ->
            if (isDark) Color(0xFF0F3822) else Color(0xFFDCFCE7)

        "PENDING", "HALF DAY" ->
            if (isDark) Color(0xFF3D2F00) else Color(0xFFFEF3C7)

        "REJECTED", "ABSENT", "NOT CHECKED IN" ->
            if (isDark) Color(0xFF421010) else Color(0xFFFFE4E6)

        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (normalized) {
        "APPROVED", "PRESENT", "CHECKED IN", "COMPLETED" ->
            if (isDark) Color(0xFF75F0B3) else Color(0xFF15803D)

        "PENDING", "HALF DAY" ->
            if (isDark) Color(0xFFFFD57A) else Color(0xFFB45309)

        "REJECTED", "ABSENT", "NOT CHECKED IN" ->
            if (isDark) Color(0xFFFF9B9B) else Color(0xFFB91C1C)

        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor
    ) {
        Text(
            text = status,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/*
 * ---------------------------------------------------------
 * BACK TO TOP
 * ---------------------------------------------------------
 */

@Composable
fun BackToTopButton(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    AnimatedVisibility(
        visible = showButton,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {
                scope.launch { listState.animateScrollToItem(0) }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Back to top"
            )
        }
    }
}

/*
 * ---------------------------------------------------------
 * LAZY LIST SCROLLBAR
 * ---------------------------------------------------------
 */

@Composable
fun LazyListScrollBar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val isScrolling = listState.isScrollInProgress

    AnimatedVisibility(
        visible = isScrolling,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .padding(vertical = 8.dp)
        ) {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount

            if (totalItems > 0) {
                val firstVisible = listState.firstVisibleItemIndex
                val visibleItems = layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                val fraction = visibleItems.toFloat() / totalItems.toFloat()
                val thumbFraction = fraction.coerceIn(0.12f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxHeight(thumbFraction)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

/*
 * ---------------------------------------------------------
 * CONFIRMATION DIALOG
 * ---------------------------------------------------------
 */

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/*
 * ---------------------------------------------------------
 * REFRESH BUTTON
 * ---------------------------------------------------------
 */

@Composable
fun RefreshButton(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    IconButton(
        onClick = onRefresh,
        enabled = !isRefreshing
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh"
            )
        }
    }
}

/*
 * ---------------------------------------------------------
 * SNACKBAR HELPER
 * ---------------------------------------------------------
 */

@Composable
fun AppSnackbarHost(
    snackbarHostState: SnackbarHostState
) {
    SnackbarHost(hostState = snackbarHostState)
}
