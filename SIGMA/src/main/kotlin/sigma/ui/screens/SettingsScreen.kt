package sigma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.data.Resolution

class SettingsScreen(private val manager: ResolutionsManager) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val resolutions = remember { mutableStateListOf(*manager.getResolutions().toTypedArray()) }
        var draggedItemIndex by remember { mutableStateOf<Int?>(null) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Settings", style = MaterialTheme.typography.h4)

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(resolutions) { index, resolution ->
                    ResolutionItem(
                        resolution = resolution,
                        onDrag = { draggedItemIndex = index },
                        onDrop = { targetIndex ->
                            if (draggedItemIndex != null && draggedItemIndex != targetIndex) {
                                manager.moveResolution(draggedItemIndex!!, targetIndex)
                                resolutions.add(targetIndex, resolutions.removeAt(draggedItemIndex!!))
                            }
                            draggedItemIndex = null
                        },
                        onDelete = {
                            manager.removeResolution(resolution.name)
                            resolutions.removeAt(index)
                        },
                        onModify = { /* Implement modify logic */ }
                    )
                }
            }

            Button(onClick = { /* Implement add logic */ }) {
                Text("Add Resolution")
            }
        }
    }

    @Composable
    fun ResolutionItem(
        resolution: Resolution,
        onDrag: () -> Unit,
        onDrop: (Int) -> Unit,
        onDelete: () -> Unit,
        onModify: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colors.surface)
                .pointerInput(Unit) {
//                    detectDragGestures(
//                        onDragStart = { onDrag() },
//                        onDragEnd = { onDrop() }
//                    )
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(resolution.name, style = MaterialTheme.typography.body1)
                Row {
                    IconButton(onClick = onModify) {
                        Icon(Icons.Default.Edit, contentDescription = "Modify")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}