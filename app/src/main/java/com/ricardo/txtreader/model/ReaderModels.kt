package com.ricardo.txtreader.model

import android.net.Uri

data class TxtDocument(
    val uri: Uri,
    val name: String,
    val parentUri: Uri?,
    val isRead: Boolean = false,
    val isMarkdown: Boolean = false
)

data class FolderEntry(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean
)

data class ReaderPreferences(
    val treeUri: String? = null,
    val currentFolderUri: String? = null,
    val currentFileUri: String? = null,
    val fontScaleSp: Float = 19f,
    val readEntries: Set<String> = emptySet()
)
