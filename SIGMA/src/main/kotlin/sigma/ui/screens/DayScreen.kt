package sigma.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.data.CompletionStatus
import sigma.dataAccess.impl.data.Day
import sigma.dataAccess.impl.data.DayState
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin

class DayScreen(
    private val manager: ResolutionsManager,
    private val date: LocalDate
) : Screen {
    private val diff = manager.getDiff(date)

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Use mutableStateOf instead of derivedStateOf to ensure recomposition
        var day by remember { mutableStateOf(manager.getDay(date)) }
        var score by remember { mutableStateOf(manager.getScore(date)) }

        // The resolutions list typically doesn't change as frequently:
        val resolutions = remember { manager.getResolutions() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DayHeader(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .fillMaxWidth()
                    .padding(16.dp),
                onBackClick = { navigator.pop() },
                date = date.toString(),
                onPreviousClick = {
                    val newDate = date.minusDays(1)
                    day = manager.getDay(newDate)
                    score = manager.getScore(newDate)
                    navigator.replace(DayScreen(manager, newDate))
                },
                onNextClick = {
                    val newDate = date.plusDays(1)
                    day = manager.getDay(newDate)
                    score = manager.getScore(newDate)
                    navigator.replace(DayScreen(manager, newDate))
                }
            )

            Spacer(modifier = Modifier.height(160.dp))

            // Pie Chart and Score
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.Center).size(700.dp)
                ) {
                    PieChartWithLabels(day = day)
                }

                Text(
                    text = String.format("%.2f%%", score * 100),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(200.dp)
                )
            }

            // Resolutions Row
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                val state = rememberLazyListState()

                LazyRow(Modifier, state) {
                    items(resolutions.size) { index ->
                        ResolutionBox(
                            index = index,
                            day = day,
                            onStatusChanged = { newStatus ->
                                // Update status in the manager
                                manager.setCompletionStatus(date, manager.getResolutions()[index].name, newStatus)

                                // Update local day data and recompute score to trigger recomposition
                                day = manager.getDay(date)
                                score = manager.getScore(date)
                            }
                        )
                    }
                }
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    adapter = rememberScrollbarAdapter(scrollState = state),
                    style = ScrollbarStyle(
                        hoverColor = MaterialTheme.colors.primary,
                        unhoverColor = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                        minimalHeight = 16.dp,
                        thickness = 8.dp,
                        shape = MaterialTheme.shapes.small,
                        hoverDurationMillis = 100
                    )
                )
            }
        }
    }

    @Composable
    private fun ResolutionBox(
        index: Int,
        day: Day,
        onStatusChanged: (CompletionStatus) -> Unit
    ) {
        var showDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .size(150.dp)
                .clickable { showDialog = true },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = manager.getResolutions()[index].name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(manager.getColorOfCompletionStatus(day[index]), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (day[index]) {
                        CompletionStatus.UNKNOWN -> "?"
                        CompletionStatus.COMPLETED -> "✔"
                        CompletionStatus.PARTIAL -> "±"
                        CompletionStatus.UNCOMPLETED -> "✘"
                    },
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Change Completion Status") },
                text = {
                    Column {
                        CompletionStatus.entries.forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onStatusChanged(status)
                                        showDialog = false
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = status.name,
                                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    @Composable
    private fun PieChartWithLabels(day: Day) {
        val total = manager.getResolutions().size

        val colors = listOf(
            manager.getColorOfCompletionStatus(CompletionStatus.UNKNOWN),
            manager.getColorOfCompletionStatus(CompletionStatus.COMPLETED),
            manager.getColorOfCompletionStatus(CompletionStatus.PARTIAL),
            manager.getColorOfCompletionStatus(CompletionStatus.UNCOMPLETED)
        )

        val textMeasurer = rememberTextMeasurer()

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(.7f)
                    .aspectRatio(1f)
            ) {
                val width = size.width
                val radius = width / 2f
                val strokeWidth = 20.dp.toPx()

                var startAngle = 0f

                day.getStatusesCounts().forEachIndexed { index, count ->
                    val sweepAngle = count.toFloat() / total * 360
                    val angleInRadians = ((startAngle + sweepAngle / 2) * Math.PI / 180f).toFloat()

                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(width - strokeWidth, width - strokeWidth),
                        style = Stroke(strokeWidth)
                    )

                    if (count > 0) {
                        drawLine(
                            color = Color.Black,
                            start = Offset(
                                radius + (radius - 20.dp.toPx()) * cos(angleInRadians),
                                radius + (radius - 20.dp.toPx()) * sin(angleInRadians)
                            ),
                            end = Offset(
                                radius + (radius - 40.dp.toPx()) * cos(angleInRadians),
                                radius + (radius - 40.dp.toPx()) * sin(angleInRadians)
                            ),
                            strokeWidth = 2.dp.toPx()
                        )

                        val countString = count.toString()
                        val textLayoutResult = textMeasurer.measure(text = AnnotatedString(countString))

                        drawText(
                            text = countString,
                            textMeasurer = textMeasurer,
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            topLeft = Offset(
                                x = radius + (radius - 60.dp.toPx()) * cos(angleInRadians) - textLayoutResult.size.width.toFloat() / 2,
                                y = radius + (radius - 60.dp.toPx()) * sin(angleInRadians) - textLayoutResult.size.height.toFloat() / 2
                            )
                        )
                    }

                    startAngle += sweepAngle
                }
            }
        }
    }

    @Composable
    private fun DayHeader(
        modifier: Modifier = Modifier,
        onBackClick: () -> Unit,
        onPreviousClick: () -> Unit,
        onNextClick: () -> Unit,
        date: String
    ) {
        Box(modifier = modifier) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
            Text(
                text = date,
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                val yesterday = manager.getDayState(diff - 1)
                if (yesterday == DayState.RECORDED || yesterday == DayState.EMPTY) {
                    IconButton(onClick = { onPreviousClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                val tomorrow = manager.getDayState(diff + 1)
                if (tomorrow == DayState.RECORDED || tomorrow == DayState.EMPTY) {
                    IconButton(onClick = { onNextClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    }
}