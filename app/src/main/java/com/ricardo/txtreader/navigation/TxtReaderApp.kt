package com.ricardo.txtreader.navigation

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ricardo.txtreader.ui.screens.LibraryScreen
import com.ricardo.txtreader.ui.screens.ReaderScreen
import com.ricardo.txtreader.ui.viewmodel.ReaderViewModel
import com.ricardo.txtreader.ui.viewmodel.ReaderViewModelFactory
import java.io.File
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun TxtReaderApp() {
    val navController = rememberNavController()
    val factory = remember { ReaderViewModelFactory() }
    val viewModel: ReaderViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var ttsSpeaking by remember { mutableStateOf(false) }
    var ttsMessage by remember { mutableStateOf<String?>(null) }
    var speechChunks by remember { mutableStateOf<List<String>>(emptyList()) }
    var speechIndex by remember { mutableIntStateOf(0) }

    fun stopSpeech() {
        tts?.stop()
        ttsSpeaking = false
        speechChunks = emptyList()
        speechIndex = 0
    }

    fun speakChunk(index: Int) {
        val engine = tts ?: return
        val chunk = speechChunks.getOrNull(index)
        if (chunk == null) {
            ttsSpeaking = false
            speechChunks = emptyList()
            speechIndex = 0
            return
        }
        speechIndex = index
        ttsSpeaking = true
        ttsMessage = "Leyendo ${index + 1}/${speechChunks.size}"
        engine.speak(
            chunk,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "txtreader-${System.nanoTime()}"
        )
    }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            scope.launch {
                if (status == TextToSpeech.SUCCESS) {
                    val result = engine?.setLanguage(Locale("es", "ES"))
                    ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                    ttsMessage = if (ttsReady) null else "El TTS español no está disponible en este móvil."
                } else {
                    ttsReady = false
                    ttsMessage = "No se pudo iniciar el TTS del sistema."
                }
            }
        }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    val next = speechIndex + 1
                    if (next < speechChunks.size) {
                        speakChunk(next)
                    } else {
                        ttsSpeaking = false
                        speechChunks = emptyList()
                        speechIndex = 0
                        ttsMessage = "Lectura terminada"
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                scope.launch {
                    ttsSpeaking = false
                    ttsMessage = "Error durante la lectura en voz alta."
                }
            }
        })
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    LaunchedEffect(state.exportJsonText) {
        val json = state.exportJsonText ?: return@LaunchedEffect
        val exportFile = File(context.cacheDir, "txtreader_leidos.json")
        exportFile.writeText(json)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", exportFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "TXT Reader - leídos")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Enviar JSON"))
        viewModel.clearExportJson()
    }

    NavHost(navController = navController, startDestination = Destination.Library.route) {
        composable(Destination.Library.route) {
            LibraryScreen(
                state = state,
                onPickFolder = viewModel::onFolderSelected,
                onOpenFolder = viewModel::openFolder,
                onNavigateUp = viewModel::navigateUp,
                onOpenFile = {
                    viewModel.openFile(it)
                    navController.navigate(Destination.Reader.route)
                },
                onRetryRestore = viewModel::restoreLastState
            )
        }
        composable(Destination.Reader.route) {
            ReaderScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onFontSizeChange = viewModel::setFontSize,
                onNextFile = viewModel::openNextFile,
                onPreviousFile = viewModel::openPreviousFile,
                onMarkAsRead = viewModel::markCurrentAsRead,
                onMarkAsUnread = viewModel::markCurrentAsUnread,
                onExportJson = viewModel::buildExportJson,
                onOpenLibrary = { navController.navigate(Destination.Library.route) },
                ttsReady = ttsReady,
                ttsSpeaking = ttsSpeaking,
                ttsMessage = ttsMessage,
                onStartReading = {
                    val content = speechTextFromContent(state.content, state.isMarkdownContent)
                    val chunks = splitSpeechText(content)
                    if (!ttsReady) {
                        ttsMessage = "El TTS del sistema aún no está listo."
                    } else if (chunks.isEmpty()) {
                        ttsMessage = "No hay texto para leer."
                    } else {
                        stopSpeech()
                        speechChunks = chunks
                        speechIndex = 0
                        speakChunk(0)
                    }
                },
                onStopReading = ::stopSpeech
            )
        }
    }
}

private fun speechTextFromContent(content: String, isMarkdown: Boolean): String {
    if (!isMarkdown) return content
    return content
        .replace(Regex("```[\\s\\S]*?```"), " ")
        .replace(Regex("`([^`]+)`"), "$1")
        .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("""!\[[^\]]*]\([^)]+\)"""), " ")
        .replace(Regex("""\[([^\]]+)]\([^)]+\)"""), "$1")
        .replace(Regex("""[*_>#~-]+"""), " ")
        .replace(Regex("""\|"""), " ")
}

private fun splitSpeechText(text: String, maxChars: Int = 2800): List<String> {
    val normalized = text
        .replace("\r\n", "\n")
        .replace(Regex("[ \\t]+"), " ")
        .trim()
    if (normalized.isBlank()) return emptyList()

    val chunks = mutableListOf<String>()
    var current = StringBuilder()
    val parts = normalized.split(Regex("(?<=[.!?…])\\s+|\\n{2,}"))
        .map { it.trim() }
        .filter { it.isNotBlank() }

    for (part in parts) {
        if (part.length > maxChars) {
            if (current.isNotBlank()) {
                chunks += current.toString().trim()
                current = StringBuilder()
            }
            part.chunked(maxChars).forEach { piece -> chunks += piece.trim() }
            continue
        }

        val candidateLength = current.length + part.length + 1
        if (current.isNotBlank() && candidateLength > maxChars) {
            chunks += current.toString().trim()
            current = StringBuilder()
        }
        if (current.isNotBlank()) current.append(' ')
        current.append(part)
    }

    if (current.isNotBlank()) chunks += current.toString().trim()
    return chunks
}
