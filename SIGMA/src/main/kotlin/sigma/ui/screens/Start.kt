package sigma.ui.screens

import androidx.compose.runtime.*
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.data.Timeline
import sigma.dataAccess.impl.loggers.ConsoleLogger
import sigma.dataAccess.impl.managers.ConfigurationManager
import sigma.dataAccess.impl.parsers.CsvParser
import java.time.LocalDate

@Composable
fun Start() {
    var initialized by remember { mutableStateOf(false) }
    var showFirstUseInfo by remember { mutableStateOf(false) }
    var configuration: ConfigurationManager? = null
    var manager: ResolutionsManager? = null
    val logger = ConsoleLogger()
    val parser = CsvParser(logger)

    LaunchedEffect(Unit) {
        try {
            configuration = ConfigurationManager()
            val resolutions = configuration!!.getResolutionsList()
            val timelinePath = configuration!!.getTimelinePath()
            val timeline = parser.readTimeline(timelinePath)
            manager = ResolutionsManager(resolutions.toMutableList(), timeline, logger, parser, configuration!!)
            initialized = true
        } catch (e: Exception) {
            showFirstUseInfo = true
        }
    }

    if (!initialized) {
        if (showFirstUseInfo) {
            InitialScreen(onStartNewChallenge = {
                manager = ResolutionsManager(mutableListOf(), Timeline(LocalDate.now(), mutableListOf()), logger, parser, configuration!!)
                initialized = true
            }, onImportData = {
                //TODO("import files")
                initialized = true
            })
        } else {
            SplashScreen()
        }
    } else {
        HomeScreen()
    }
}
