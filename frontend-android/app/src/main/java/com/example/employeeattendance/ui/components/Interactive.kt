package com.example.employeeattendance.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

fun Modifier.interactiveClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {

    return clickable(
        enabled = enabled,
        onClick = onClick
    )
}