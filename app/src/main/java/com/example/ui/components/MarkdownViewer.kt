package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownViewer(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lines = markdownText.lines()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        var inCodeBlock = false
        var codeBlockContent = StringBuilder()
        var codeBlockLang = ""

        lines.forEach { line ->
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    CodeBlock(code = codeBlockContent.toString().trimEnd(), language = codeBlockLang)
                    Spacer(modifier = Modifier.height(12.dp))
                    inCodeBlock = false
                    codeBlockContent = StringBuilder()
                    codeBlockLang = ""
                } else {
                    inCodeBlock = true
                    codeBlockLang = trimmed.removePrefix("```").trim()
                }
            } else if (inCodeBlock) {
                codeBlockContent.appendLine(line)
            } else {
                when {
                    trimmed.startsWith("# ") -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = trimmed.removePrefix("# ").trim(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    }
                    trimmed.startsWith("## ") -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = trimmed.removePrefix("## ").trim(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                    trimmed.startsWith("### ") -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = trimmed.removePrefix("### ").trim(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    trimmed.startsWith("> ") -> {
                        QuoteBlock(quote = trimmed.removePrefix("> ").trim())
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                        Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = trimmed.substring(2).trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    trimmed.matches(Regex("^\\d+\\..*")) -> {
                        val num = trimmed.substringBefore('.')
                        val rest = trimmed.substringAfter('.').trim()
                        Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                            Text("$num. ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = rest,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    trimmed.startsWith("---") || trimmed.startsWith("***") -> {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)
                    }
                    trimmed.isBlank() -> {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    else -> {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        if (inCodeBlock && codeBlockContent.isNotEmpty()) {
            CodeBlock(code = codeBlockContent.toString().trimEnd(), language = codeBlockLang)
        }
    }
}

@Composable
private fun CodeBlock(code: String, language: String) {
    val horizScroll = rememberScrollState()
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (language.isNotEmpty()) {
                Text(
                    text = language.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Box(modifier = Modifier.horizontalScroll(horizScroll)) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuoteBlock(quote: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = quote,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
