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
import cafe.adriel.voyager.core.stack.popUntil
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.data.CompletionStatus
import sigma.dataAccess.impl.data.Day
import sigma.dataAccess.impl.data.DayState
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin

class StatisticsScreen(
    private val manager: ResolutionsManager
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val date = LocalDate.now()

        // Use mutableStateOf instead of derivedStateOf to ensure recomposition
        var score by remember { mutableStateOf(manager.getMonthScore(date)) }

        // The resolutions list typically doesn't change as frequently:
        val resolutions = remember { manager.getResolutions() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            StatisticsHeader(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .fillMaxWidth()
                    .padding(16.dp),
                onBackClick = { navigator.popUntil<HomeScreen, Screen>() },
                month = "${date.month} ${date.year}"
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
                    PieChartWithLabels(manager.getMonthStatusesCounts(date))
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
        }
    }

    @Composable
    private fun PieChartWithLabels(counts: List<Int>) {
        val total = counts.sum().toFloat()

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
    private fun StatisticsHeader(
        modifier: Modifier = Modifier,
        onBackClick: () -> Unit,
        month: String,
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
                text = month,
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}