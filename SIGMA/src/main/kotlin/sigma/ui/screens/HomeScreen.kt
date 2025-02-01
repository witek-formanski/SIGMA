package sigma.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pl.edu.mimuw.sigma.generated.resources.Res
import pl.edu.mimuw.sigma.generated.resources.settings
import pl.edu.mimuw.sigma.generated.resources.statistics
import pl.edu.mimuw.sigma.generated.resources.calendar
import pl.edu.mimuw.sigma.generated.resources.today
import sigma.businessLogic.impl.managers.ResolutionsManager
import java.time.LocalDate

class HomeScreen(private val manager: ResolutionsManager) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Create a grid-like structure for the four tiles
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Tile(
                    title = "Calendar",
                    iconRes = Res.drawable.calendar,
                    onClick = { navigator.push(CalendarScreen(manager)) },
                    modifier = Modifier.weight(1f)
                )
                Tile(
                    title = "Today",
                    iconRes = Res.drawable.today,
                    onClick = { navigator.push(DayScreen(manager, LocalDate.now())) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Tile(
                    title = "Statistics",
                    iconRes = Res.drawable.statistics,
                    onClick = { navigator.push(StatisticsScreen(manager)) },
                    modifier = Modifier.weight(1f)
                )
                Tile(
                    title = "Settings",
                    iconRes = Res.drawable.settings,
                    onClick = { navigator.push(SettingsScreen(manager)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    @Composable
    fun Tile(title: String, iconRes: DrawableResource, onClick: () -> Unit, modifier: Modifier) {
        Card(
            modifier = modifier
                .clickable(onClick = onClick),
            elevation = 8.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6.copy(fontSize = 32.sp),
                    color = MaterialTheme.colors.onSurface
                )
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = "$title Icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                        .padding(16.dp)
                )
            }
        }
    }
}
