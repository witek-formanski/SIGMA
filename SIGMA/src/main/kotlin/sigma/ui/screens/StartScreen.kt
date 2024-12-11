package sigma.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.loggers.ConsoleLogger
import sigma.dataAccess.impl.managers.ConfigurationManager
import sigma.dataAccess.impl.parsers.CsvTimelineParser

class StartScreen : Screen {
    @Composable
    override fun Content() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("SIGMA", fontSize = 64.sp, style = MaterialTheme.typography.caption)
        }

        val navigator = LocalNavigator.currentOrThrow
        val logger = ConsoleLogger() // TODO("use DI")
        val parser = CsvTimelineParser(logger) // TODO("use DI")

        LaunchedEffect(Unit) {
            try {
                val configuration = ConfigurationManager()
                val resolutions = configuration.getResolutionsList()
                val timelinePath = configuration.getTimelinePath()
                val timeline = parser.read(timelinePath, resolutions.size)
                val manager = ResolutionsManager(resolutions.toMutableList(), timeline, logger, parser, configuration)
                navigator.push(HomeScreen(manager))
            } catch (e: Exception) {
                navigator.push(InitialScreen(logger, parser))
            }
        }
    }
}
