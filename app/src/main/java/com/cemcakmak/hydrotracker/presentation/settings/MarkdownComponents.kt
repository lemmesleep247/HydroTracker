package com.cemcakmak.hydrotracker.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shared building blocks for the settings document sheets (Changelog, Sources, Privacy, License).
 *
 * A small in-house markdown renderer — no external libraries. Handles `#`/`##`/`###` headers,
 * `- ` bullets, fully-bold `**…**` lines, inline `**bold**`, and `---` dividers.
 */

/** A bottom sheet that loads [assetFileName] from `assets/` and renders it as markdown. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MarkdownBottomSheet(
    title: String,
    assetFileName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var content by remember { mutableStateOf("Loading…") }

    LaunchedEffect(assetFileName) {
        content = try {
            withContext(Dispatchers.IO) {
                context.assets.open(assetFileName).bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            "Error loading $assetFileName: ${e.message}"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MarkdownText(text = content, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** Renders a (small subset of) markdown [text] as a vertical stack of Text rows. */
@Composable
internal fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = text.split("\n")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (line in lines) {
            when {
                line.startsWith("# ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = line.substring(2),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = line.substring(3),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                line.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = line.substring(4),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.startsWith("- ") -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(line.substring(2)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                line.trim().startsWith("**") && line.trim().endsWith("**") -> {
                    Text(
                        text = line.trim().removeSurrounding("**"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                line.trim() == "---" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                }
            }
        }
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()

        for (match in boldRegex.findAll(text)) {
            append(text.substring(currentIndex, match.range.first))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
