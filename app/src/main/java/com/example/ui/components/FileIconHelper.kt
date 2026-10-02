package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.GitHubContentItem
import com.example.ui.theme.CleanArchiveBg
import com.example.ui.theme.CleanArchiveIcon
import com.example.ui.theme.CleanCodeBg
import com.example.ui.theme.CleanCodeIcon
import com.example.ui.theme.CleanDocsBg
import com.example.ui.theme.CleanDocsIcon
import com.example.ui.theme.CleanFolderBg
import com.example.ui.theme.CleanFolderIcon
import com.example.ui.theme.CleanMediaBg
import com.example.ui.theme.CleanMediaIcon
import java.util.Locale

enum class FileCategory {
    DIRECTORY,
    CODE_KOTLIN_JAVA,
    CODE_PYTHON,
    CODE_JAVASCRIPT_TYPESCRIPT,
    CODE_WEB_HTML_CSS,
    CODE_GENERIC,
    CONFIG_JSON_YAML_XML,
    MARKDOWN,
    PDF,
    IMAGE,
    AUDIO,
    VIDEO,
    ZIP_ARCHIVE,
    SETTINGS_GIT,
    GENERIC_FILE
}

data class FileTypeVisual(
    val category: FileCategory,
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color,
    val badgeLabel: String,
    val isTextEditable: Boolean,
    val isImage: Boolean,
    val isMarkdown: Boolean
)

object FileIconHelper {

    fun getVisualForFile(item: GitHubContentItem): FileTypeVisual {
        if (item.isDirectory) {
            return FileTypeVisual(
                category = FileCategory.DIRECTORY,
                icon = Icons.Default.Folder,
                color = CleanFolderIcon,
                backgroundColor = CleanFolderBg,
                badgeLabel = "DIR",
                isTextEditable = false,
                isImage = false,
                isMarkdown = false
            )
        }

        val nameLower = item.name.lowercase(Locale.ROOT)
        val ext = nameLower.substringAfterLast('.', "")

        return when {
            nameLower == "readme.md" || ext == "md" || ext == "markdown" || ext == "rst" -> FileTypeVisual(
                category = FileCategory.MARKDOWN,
                icon = Icons.Default.MenuBook,
                color = CleanDocsIcon,
                backgroundColor = CleanDocsBg,
                badgeLabel = "MD",
                isTextEditable = true,
                isImage = false,
                isMarkdown = true
            )
            ext in listOf("kt", "kts", "java", "scala", "groovy") -> FileTypeVisual(
                category = FileCategory.CODE_KOTLIN_JAVA,
                icon = Icons.Default.Code,
                color = CleanCodeIcon,
                backgroundColor = CleanCodeBg,
                badgeLabel = ext.uppercase(Locale.ROOT),
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("py", "pyw", "ipynb") -> FileTypeVisual(
                category = FileCategory.CODE_PYTHON,
                icon = Icons.Default.Terminal,
                color = Color(0xFFD97706),
                backgroundColor = Color(0xFFFFFBEB),
                badgeLabel = "PY",
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("js", "jsx", "ts", "tsx", "mjs", "cjs") -> FileTypeVisual(
                category = FileCategory.CODE_JAVASCRIPT_TYPESCRIPT,
                icon = Icons.Default.Javascript,
                color = Color(0xFF0284C7),
                backgroundColor = Color(0xFFE0F2FE),
                badgeLabel = ext.uppercase(Locale.ROOT),
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("html", "htm", "vue", "svelte") -> FileTypeVisual(
                category = FileCategory.CODE_WEB_HTML_CSS,
                icon = Icons.Default.Html,
                color = Color(0xFFEA580C),
                backgroundColor = Color(0xFFFFEDD5),
                badgeLabel = ext.uppercase(Locale.ROOT),
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("css", "scss", "sass", "less") -> FileTypeVisual(
                category = FileCategory.CODE_WEB_HTML_CSS,
                icon = Icons.Default.Code,
                color = Color(0xFF0284C7),
                backgroundColor = Color(0xFFE0F2FE),
                badgeLabel = "CSS",
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("json", "yaml", "yml", "xml", "toml", "properties", "env", "conf") || nameLower.startsWith(".env") -> FileTypeVisual(
                category = FileCategory.CONFIG_JSON_YAML_XML,
                icon = Icons.Default.DataObject,
                color = CleanCodeIcon,
                backgroundColor = CleanCodeBg,
                badgeLabel = ext.ifEmpty { "CFG" }.uppercase(Locale.ROOT),
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            nameLower.startsWith(".git") || nameLower == "license" || nameLower.contains("dockerfile") || nameLower == "makefile" -> FileTypeVisual(
                category = FileCategory.SETTINGS_GIT,
                icon = Icons.Default.Settings,
                color = CleanDocsIcon,
                backgroundColor = CleanDocsBg,
                badgeLabel = "CFG",
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("png", "jpg", "jpeg", "gif", "webp", "svg", "bmp", "ico") -> FileTypeVisual(
                category = FileCategory.IMAGE,
                icon = Icons.Default.Image,
                color = CleanMediaIcon,
                backgroundColor = CleanMediaBg,
                badgeLabel = ext.uppercase(Locale.ROOT),
                isTextEditable = false,
                isImage = true,
                isMarkdown = false
            )
            ext == "pdf" -> FileTypeVisual(
                category = FileCategory.PDF,
                icon = Icons.Default.PictureAsPdf,
                color = Color(0xFFE11D48),
                backgroundColor = Color(0xFFFFE4E6),
                badgeLabel = "PDF",
                isTextEditable = false,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("zip", "tar", "gz", "tgz", "7z", "rar", "jar", "apk", "aar") -> FileTypeVisual(
                category = FileCategory.ZIP_ARCHIVE,
                icon = Icons.Default.Archive,
                color = CleanArchiveIcon,
                backgroundColor = CleanArchiveBg,
                badgeLabel = "ZIP",
                isTextEditable = false,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("mp3", "wav", "ogg", "m4a", "flac") -> FileTypeVisual(
                category = FileCategory.AUDIO,
                icon = Icons.Default.AudioFile,
                color = Color(0xFF9333EA),
                backgroundColor = CleanMediaBg,
                badgeLabel = "AUD",
                isTextEditable = false,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("mp4", "mkv", "mov", "webm", "avi") -> FileTypeVisual(
                category = FileCategory.VIDEO,
                icon = Icons.Default.VideoFile,
                color = Color(0xFFE11D48),
                backgroundColor = Color(0xFFFFE4E6),
                badgeLabel = "VID",
                isTextEditable = false,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("c", "cpp", "h", "hpp", "cs", "go", "rs", "rb", "php", "swift", "sh", "bash", "zsh", "sql", "dart") -> FileTypeVisual(
                category = FileCategory.CODE_GENERIC,
                icon = Icons.Default.IntegrationInstructions,
                color = Color(0xFF0D9488),
                backgroundColor = Color(0xFFCCFBF1),
                badgeLabel = ext.uppercase(Locale.ROOT),
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("txt", "log", "in", "out") -> FileTypeVisual(
                category = FileCategory.GENERIC_FILE,
                icon = Icons.Default.Description,
                color = CleanDocsIcon,
                backgroundColor = CleanDocsBg,
                badgeLabel = "TXT",
                isTextEditable = true,
                isImage = false,
                isMarkdown = false
            )
            ext in listOf("ttf", "otf", "woff", "woff2") -> FileTypeVisual(
                category = FileCategory.GENERIC_FILE,
                icon = Icons.Default.FontDownload,
                color = Color(0xFFC026D3),
                backgroundColor = Color(0xFFFAE8FF),
                badgeLabel = "FONT",
                isTextEditable = false,
                isImage = false,
                isMarkdown = false
            )
            else -> FileTypeVisual(
                category = FileCategory.GENERIC_FILE,
                icon = Icons.Default.Description,
                color = CleanDocsIcon,
                backgroundColor = CleanDocsBg,
                badgeLabel = ext.ifEmpty { "FILE" }.take(4).uppercase(Locale.ROOT),
                isTextEditable = isLikelyTextFile(item.name),
                isImage = false,
                isMarkdown = false
            )
        }
    }

    private fun isLikelyTextFile(fileName: String): Boolean {
        val nonTextExtensions = setOf(
            "zip", "tar", "gz", "7z", "rar", "jar", "apk", "aar", "exe", "bin", "iso", "dmg",
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "pdf", "mp3", "wav", "mp4", "mov",
            "so", "dylib", "dll", "class", "pyc", "db", "sqlite"
        )
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return ext.isNotEmpty() && !nonTextExtensions.contains(ext)
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return String.format(Locale.US, "%.1f %s", size, units[unitIndex])
    }
}
