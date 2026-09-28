package com.example.employeeattendance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ScrollbarColumn(
    state: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(state.isScrollInProgress) {
            if (state.isScrollInProgress) {
                visible = true
                delay(900)
                visible = false
            }
        }

        if (visible && state.maxValue > 0) {
            ScrollThumb(
                fraction = state.value.toFloat() / state.maxValue.toFloat(),
                viewportFraction = 1f / (1f + state.maxValue.toFloat() / 1000f)
            )
        }
    }
}

@Composable
fun ScrollbarLazyColumn(
    state: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(state.isScrollInProgress) {
            if (state.isScrollInProgress) {
                visible = true
                delay(900)
                visible = false
            }
        }

        val layout = state.layoutInfo
        val total = layout.totalItemsCount
        val visibleItems = layout.visibleItemsInfo.size

        if (visible && total > visibleItems && visibleItems > 0) {
            val first = layout.visibleItemsInfo.firstOrNull()?.index ?: 0
            val maxFirst = (total - visibleItems).coerceAtLeast(1)

            ScrollThumb(
                fraction = (first.toFloat() / maxFirst).coerceIn(0f, 1f),
                viewportFraction =
                    (visibleItems.toFloat() / total.toFloat())
                        .coerceIn(0.08f, 1f)
            )
        }
    }
}

@Composable
private fun ScrollThumb(
    fraction: Float,
    viewportFraction: Float
) {
    val thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)

    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        val width = 4.dp.toPx()
        val horizontalPadding = 3.dp.toPx()
        val trackHeight = size.height
        val thumbHeight =
            (trackHeight * viewportFraction)
                .coerceAtLeast(36.dp.toPx())
                .coerceAtMost(trackHeight)

        val maxOffset = (trackHeight - thumbHeight)
            .coerceAtLeast(0f)

        val y = maxOffset * fraction

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(
                size.width - width - horizontalPadding,
                y
            ),
            size = androidx.compose.ui.geometry.Size(
                width,
                thumbHeight
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                width / 2f,
                width / 2f
            )
        )
    }
}
