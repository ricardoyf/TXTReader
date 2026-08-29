package com.ricardo.txtreader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardo.txtreader.ui.components.EmptyStateCard
import com.ricardo.txtreader.ui.components.MarkdownText
import com.ricardo.txtreader.ui.viewmodel.ReaderUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    state: ReaderUiState,
    onBack: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onNextFile: () -> Unit,
    onPreviousFile: () -> Unit,
    onMarkAsRead: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onExportJson: () -> Unit,
    onOpenLibrary: () -> Unit,
    ttsReady: Boolean,
    ttsSpeaking: Boolean,
    ttsMessage: String?,
    onStartReading: () -> Unit,
    onStopReading: () -> Unit
) {
    var barsVisible by remember { mutableStateOf(true) }
    var fontBarVisible by remember { mutableStateOf(false) }

    val verticalGestureModifier = Modifier.pointerInput(state.selectedFileIndex) {
        var accumulatedDrag = 0f
        detectVerticalDragGestures(
            onVerticalDrag = { _, dragAmount ->
                accumulatedDrag += dragAmount
            },
            onDragEnd = {
                when {
                    accumulatedDrag < -80f -> barsVisible = false
                    accumulatedDrag > 80f -> barsVisible = true
                }
                accumulatedDrag = 0f
            }
        )
    }

    Scaffold(
        topBar = {
            if (barsVisible) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.selectedFile?.name ?: "Lector")
                            Text(
                                text = if (state.selectedFileIndex >= 0 && state.allTxtFiles.isNotEmpty()) {
                                    "${state.selectedFileIndex + 1} / ${state.allTxtFiles.size}"
                                } else {
                                    "Sin archivo"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        if (ttsSpeaking) {
                            IconButton(onClick = onStopReading) {
                                Icon(Icons.Default.Stop, contentDescription = "Parar lectura")
                            }
                        } else {
                            IconButton(onClick = onStartReading, enabled = ttsReady) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Leer en voz alta")
                            }
                        }
                        IconButton(onClick = { fontBarVisible = !fontBarVisible }) {
                            Icon(Icons.Default.TextIncrease, contentDescription = "Mostrar tamaño")
                        }
                        if (state.selectedFile?.isRead == true) {
                            TextButton(onClick = onMarkAsUnread) {
                                Icon(Icons.Default.RemoveDone, contentDescription = "No leído", modifier = Modifier.size(16.dp))
                                Text("No leído")
                            }
                        } else {
                            TextButton(onClick = onMarkAsRead) {
                                Icon(Icons.Default.Done, contentDescription = "Leído", modifier = Modifier.size(16.dp))
                                Text("Leído")
                            }
                        }
                        IconButton(onClick = onExportJson) {
                            Icon(Icons.Default.Share, contentDescription = "Exportar JSON")
                        }
                        IconButton(onClick = onOpenLibrary) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Biblioteca")
                        }
                    }
                )
            }
        },
    ) { padding ->
        if (state.selectedFile == null) {
            EmptyStateCard(
                title = "No hay archivo abierto",
                description = "Vuelve a la biblioteca y elige un .txt.",
                actionLabel = "Ir a biblioteca",
                onAction = onOpenLibrary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 10.dp)
                .navigationBarsPadding()
        ) {
            if (barsVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.selectedFile?.isRead == true) "✓ Leído" else "Pendiente",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.selectedFile?.isRead == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ttsMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (barsVisible && fontBarVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.TextIncrease,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = state.fontSizeSp,
                        onValueChange = onFontSizeChange,
                        valueRange = 14f..30f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${state.fontSizeSp.toInt()}sp",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(verticalGestureModifier)
                    .padding(top = if (barsVisible) 6.dp else 0.dp, bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                ) {
                    if (state.isMarkdownContent) {
                        MarkdownText(
                            markdown = state.content,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        SelectionContainer {
                            Text(
                                text = state.content,
                                fontSize = state.fontSizeSp.sp,
                                lineHeight = (state.fontSizeSp * 1.65f).sp,
                                letterSpacing = 0.15.sp,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onPreviousFile, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Anterior",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNextFile, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Siguiente",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
