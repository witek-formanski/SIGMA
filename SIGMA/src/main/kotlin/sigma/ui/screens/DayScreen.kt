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
import androidx.compose.runtime.Composable
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Row: Navigation and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = date.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h5
                )
                Row {
                    val yesterday = manager.getDayState(diff - 1)
                    if (yesterday == DayState.RECORDED || yesterday == DayState.EMPTY) {
                        IconButton(onClick = {
                            navigator.replace(DayScreen(manager, date.minusDays(1)))
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day"
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                    val tomorrow = manager.getDayState(diff + 1)
                    if (tomorrow == DayState.RECORDED || tomorrow == DayState.EMPTY) {
                        IconButton(onClick = {
                            navigator.replace(DayScreen(manager, date.plusDays(1)))
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day"
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(160.dp))

            // Pie Chart and Score
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                // Pie Chart
                Box(
                    modifier = Modifier.align(Alignment.Center).size(700.dp)
                ) {
                    PieChartWithLabels(
                        manager.getCountOf(CompletionStatus.UNKNOWN, date),
                        manager.getCountOf(CompletionStatus.COMPLETED, date),
                        manager.getCountOf(CompletionStatus.PARTIAL, date),
                        manager.getCountOf(CompletionStatus.UNCOMPLETED, date),
                    )
                }

                // Score
                Text(
                    text = String.format("%.2f%%", manager.getScore(date) * 100),
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
                    items(manager.getResolutions().size) { index ->
                        ResolutionBox(
                            resolutionName = manager.getResolutions()[index].name,
                            completionStatus = manager.getCompletionStatus(date, index),
                            color = manager.getColorOfCompletionStatus(
                                manager.getCompletionStatus(date, index)
                            )
                        )
                    }
                }
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    adapter = rememberScrollbarAdapter(
                        scrollState = state
                    ),
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
    private fun ResolutionBox(resolutionName: String, completionStatus: CompletionStatus, color: Color) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .size(150.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = resolutionName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Status Box
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (completionStatus) {
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
    }

    @Composable
    private fun PieChartWithLabels(unknown: Int, completed: Int, partial: Int, uncompleted: Int) {
        val total = manager.getResolutions().size
        val counts = listOf(
            unknown,
            completed,
            partial,
            uncompleted
        )

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

                counts.forEachIndexed { index, count ->

                    val sweepAngle = count.toFloat() / total * 360
                    val angleInRadians = (startAngle + sweepAngle / 2).degreeToAngle

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
}

private val Float.degreeToAngle
    get() = (this * Math.PI / 180f).toFloat()
