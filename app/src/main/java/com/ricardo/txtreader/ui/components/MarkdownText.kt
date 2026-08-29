package com.ricardo.txtreader.ui.components

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextIsSelectable(true)
            }
        },
        update = { view ->
            markwon.setMarkdown(view, markdown)
        }
    )
}
