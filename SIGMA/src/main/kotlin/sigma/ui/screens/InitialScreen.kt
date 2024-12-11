package sigma.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.data.Timeline
import sigma.dataAccess.impl.managers.ConfigurationManager
import sigma.dataAccess.model.loggers.ILogger
import sigma.dataAccess.model.parsers.ITimelineParser
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.time.LocalDate

class InitialScreen(
    val logger: ILogger,
    val parser: ITimelineParser,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var importStatus by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to SIGMA!", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                val configuration : ConfigurationManager = TODO()

                val manager = ResolutionsManager(
                    mutableListOf(),
                    Timeline(LocalDate.now(), mutableListOf()),
                    logger,
                    parser,
                    configuration
                )
            }) {
                Text("Start New Challenge")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                try {
                    val selectedFile = selectCsvFile()
                    if (selectedFile != null) {
                        var timeline = parser.read(selectedFile.path)

                        importStatus = "Successfully imported: ${selectedFile.name}"
                        navigator.replace(HomeScreen(manager!!))
                    } else {
                        importStatus = "No file selected"
                    }
                } catch (e: Exception) {
                    importStatus = "Error importing file: ${e.message}"
                }
            }) {
                Text("Import Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(importStatus, fontSize = 14.sp)
        }
    }

    private fun selectCsvFile(): File? {
        val fileDialog = FileDialog(Frame(), "Select CSV File", FileDialog.LOAD).apply {
            isVisible = true
        }

        val filePath = fileDialog.directory?.let { dir ->
            fileDialog.file?.let { file ->
                File(dir, file).absolutePath
            }
        }

        return if (filePath != null) File(filePath) else null
    }
}
