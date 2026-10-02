package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MenuBook
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.GitHubUser
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import com.example.data.model.SavedAccount
import com.example.ui.viewmodel.DeleteProgress
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GitHubContentItem
import com.example.ui.theme.CleanBluePrimary
import com.example.ui.theme.CleanPillBackground
import com.example.ui.theme.CleanPillText
import com.example.ui.theme.CleanRedAccent
import com.example.ui.viewmodel.UploadProgress

@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (fileName: String, initialContent: String, commitMessage: String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var initialContent by remember { mutableStateOf("") }
    var commitMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New File",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = {
                        fileName = it
                        errorMessage = null
                    },
                    label = { Text("File Name (e.g. index.js, README.md)") },
                    singleLine = true,
                    isError = errorMessage != null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_file_name_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message (Optional)") },
                    placeholder = { Text("Create ${if (fileName.isNotBlank()) fileName else "new file"}") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = initialContent,
                    onValueChange = { initialContent = it },
                    label = { Text("Initial Content (Optional)") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.trim().isBlank()) {
                        errorMessage = "Please enter a valid file name"
                    } else if (fileName.contains("/") || fileName.contains("\\")) {
                        errorMessage = "File name cannot contain slashes"
                    } else {
                        val msg = commitMessage.ifBlank { "Create ${fileName.trim()}" }
                        onConfirm(fileName.trim(), initialContent, msg)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_create_file_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (folderName: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Folder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "A .gitkeep file will be created inside this folder so Git tracks the directory.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        errorMessage = null
                    },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    isError = errorMessage != null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_folder_name_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.trim().isBlank()) {
                        errorMessage = "Please enter a folder name"
                    } else {
                        onConfirm(folderName.trim())
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_create_folder_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailsBottomSheet(
    item: GitHubContentItem,
    onDismiss: () -> Unit,
    onOpenEdit: () -> Unit,
    onReplaceFile: () -> Unit,
    onDeleteRequest: () -> Unit,
    onShare: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val visual = FileIconHelper.getVisualForFile(item)
    var copiedText by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with icon and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(visual.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = item.name,
                        tint = visual.color,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.path,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Size", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FileIconHelper.formatFileSize(item.size), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Type", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(visual.badgeLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    if (item.sha.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SHA", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.sha.take(8),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(item.sha))
                                        copiedText = "SHA copied"
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy SHA",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onOpenEdit()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open / Edit")
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onReplaceFile()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Replace")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onShare()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Link")
                }

                Button(
                    onClick = {
                        onDismiss()
                        onDeleteRequest()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CleanRedAccent),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun DeleteFileDialog(
    item: GitHubContentItem,
    onDismiss: () -> Unit,
    onConfirm: (commitMessage: String) -> Unit
) {
    var commitMessage by remember { mutableStateOf("Delete ${item.name}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = CleanRedAccent,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Delete File?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Are you sure you want to delete '${item.name}' from the repository? This creates a commit deleting the file.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(commitMessage.ifBlank { "Delete ${item.name}" }) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanRedAccent),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Delete File")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UploadProgressDialog(
    progress: UploadProgress,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!progress.isUploading) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (progress.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = CleanBluePrimary
                    )
                } else if (progress.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                } else if (progress.errorMessage != null) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = CleanRedAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (progress.isUploading) "Uploading Files..." else if (progress.isCompleted) "Upload Complete!" else "Upload Failed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (progress.isUploading) {
                    val progressFraction = if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = CleanBluePrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "File ${progress.current} of ${progress.total}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = progress.currentFileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (progress.isCompleted) {
                    Text(
                        text = "Successfully uploaded ${progress.total} files to the repository.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (progress.errorMessage != null) {
                    Text(
                        text = progress.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleanRedAccent
                    )
                }
            }
        },
        confirmButton = {
            if (progress.isUploading) {
                TextButton(onClick = onCancel) {
                    Text("Cancel Upload")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done")
                }
            }
        }
    )
}

@Composable
fun CreateRepoDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, isPrivate: Boolean) -> Unit
) {
    var repoName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Repository", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = repoName,
                    onValueChange = {
                        repoName = it
                        errorMessage = null
                    },
                    label = { Text("Repository Name") },
                    singleLine = true,
                    isError = errorMessage != null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPrivate) Icons.Default.Lock else Icons.Default.Public,
                            contentDescription = null,
                            tint = if (isPrivate) CleanPillText else CleanBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(if (isPrivate) "Private" else "Public", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(if (isPrivate) "Only you can see this repo" else "Anyone on the internet can see it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (repoName.trim().isBlank()) {
                        errorMessage = "Repository name is required"
                    } else {
                        onConfirm(repoName.trim(), description.ifBlank { null }, isPrivate)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Repository")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailsBottomSheet(
    item: GitHubContentItem,
    onDismiss: () -> Unit,
    onOpenFolder: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val visual = FileIconHelper.getVisualForFile(item)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with icon and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(visual.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = item.name,
                        tint = visual.color,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.path,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onOpenFolder()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Folder")
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(item.path))
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Path")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onDismiss()
                    onDeleteRequest()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanRedAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Folder & All Files")
            }
        }
    }
}

@Composable
fun DeleteFolderDialog(
    item: GitHubContentItem,
    onDismiss: () -> Unit,
    onConfirm: (commitMessage: String) -> Unit
) {
    var commitMessage by remember { mutableStateOf("Delete directory ${item.name}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = CleanRedAccent,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Delete Folder '${item.name}'?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = CleanRedAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Warning: Deleting this folder will permanently delete all files and sub-folders inside it on GitHub.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanRedAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Are you sure you want to delete '${item.path}' and all files inside it from GitHub?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(commitMessage.ifBlank { "Delete folder ${item.name}" }) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanRedAccent),
                modifier = Modifier.testTag("confirm_delete_folder_button")
            ) {
                Text("Delete Entire Folder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BulkDeleteDialog(
    selectedItems: List<GitHubContentItem>,
    onDismiss: () -> Unit,
    onConfirm: (commitMessage: String) -> Unit
) {
    val folderCount = selectedItems.count { it.isDirectory }
    val fileCount = selectedItems.count { it.isFile }
    var commitMessage by remember {
        mutableStateOf(
            if (folderCount > 0 && fileCount > 0) "Batch delete $fileCount files and $folderCount folders"
            else if (folderCount > 0) "Batch delete $folderCount folders"
            else "Batch delete $fileCount files"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = CleanRedAccent,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Delete ${selectedItems.size} Selected Items?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = CleanRedAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Selected Items to Delete:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanRedAccent
                        )
                        Text(
                            text = "• $fileCount file(s)\n• $folderCount folder(s) (including all nested files)",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanRedAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(commitMessage.ifBlank { "Batch delete ${selectedItems.size} items" }) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanRedAccent),
                modifier = Modifier.testTag("confirm_bulk_delete_button")
            ) {
                Text("Delete All Selected")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteProgressDialog(
    progress: DeleteProgress,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!progress.isDeleting) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (progress.isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = CleanRedAccent
                    )
                } else if (progress.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                } else if (progress.errorMessage != null) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = CleanRedAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (progress.isDeleting) "Deleting Items..." else if (progress.isCompleted) "Deletion Complete!" else "Delete Failed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (progress.isDeleting) {
                    val progressFraction = if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = CleanRedAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Deleting ${progress.current} of ${progress.total}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = progress.currentItemName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (progress.isCompleted) {
                    Text(
                        text = "Successfully deleted ${progress.total} items from the repository.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (progress.errorMessage != null) {
                    Text(
                        text = progress.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleanRedAccent
                    )
                }
            }
        },
        confirmButton = {
            if (!progress.isDeleting) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherBottomSheet(
    accounts: List<SavedAccount>,
    activeAccountId: Long?,
    onDismiss: () -> Unit,
    onSelectAccount: (Long) -> Unit,
    onAddNewAccount: () -> Unit,
    onRemoveAccount: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = CleanBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GitHub Accounts (${accounts.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accounts, key = { it.user.id }) { acc ->
                    val isActive = acc.user.id == activeAccountId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (!isActive) {
                                    onSelectAccount(acc.user.id)
                                    onDismiss()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (acc.user.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = acc.user.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(CleanBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = acc.user.name ?: acc.user.login,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = CleanBluePrimary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "@${acc.user.login}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!isActive) {
                                IconButton(
                                    onClick = { onRemoveAccount(acc.user.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove account",
                                        tint = CleanRedAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active account",
                                    tint = CleanBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onDismiss()
                    onAddNewAccount()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Another GitHub Account")
            }
        }
    }
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (token: String) -> Unit
) {
    var tokenInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CleanBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add GitHub Account", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter a GitHub Personal Access Token (classic or fine-grained) to link another account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Duplicate accounts are prevented. Each GitHub account can only be linked once.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = {
                        tokenInput = it
                        errorMessage = null
                    },
                    label = { Text("Personal Access Token") },
                    placeholder = { Text("ghp_... or github_pat_...") },
                    singleLine = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (tokenInput.isNotEmpty()) {
                                IconButton(onClick = { tokenInput = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            } else {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        tokenInput = clip.trim()
                                    }
                                }) {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste")
                                }
                            }
                        }
                    },
                    isError = errorMessage != null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_account_token_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Generate Token on GitHub button
                OutlinedButton(
                    onClick = {
                        val url = "https://github.com/settings/tokens/new?scopes=repo,read:user,user:email,workflow&description=GitHub%20File%20Manager%20App"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Token on GitHub", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Setup Guide Link
                TextButton(
                    onClick = {
                        val url = "https://imalimrans.pages.dev/blog-posts/GithubFileManagerapk"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Read Token Setup Guide", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tokenInput.trim().isBlank()) {
                        errorMessage = "Please enter a valid token"
                    } else {
                        onConfirm(tokenInput.trim())
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_add_account_button")
            ) {
                Text("Add Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TokenExpiredDialog(
    user: GitHubUser?,
    message: String?,
    onDismiss: () -> Unit,
    onUpdateToken: (newToken: String) -> Unit,
    onSwitchAccount: () -> Unit,
    onSignOut: () -> Unit
) {
    var newTokenInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CleanRedAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CleanRedAccent,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Text(
                text = "Session Expired or Invalid Token",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // User Banner
                if (user != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (user.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column {
                                Text(
                                    text = user.name ?: user.login,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${user.login}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = message ?: "Your GitHub Personal Access Token has expired or was revoked. Please enter a newly generated token to reconnect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newTokenInput,
                    onValueChange = {
                        newTokenInput = it
                        inputError = null
                    },
                    label = { Text("New Personal Access Token") },
                    placeholder = { Text("ghp_... or github_pat_...") },
                    singleLine = true,
                    isError = inputError != null,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (newTokenInput.isNotEmpty()) {
                                IconButton(onClick = { newTokenInput = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            } else {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        newTokenInput = clip.trim()
                                    }
                                }) {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (inputError != null) {
                    Text(
                        text = inputError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Generate Token Button
                OutlinedButton(
                    onClick = {
                        val url = "https://github.com/settings/tokens/new?scopes=repo,read:user,user:email,workflow&description=GitHub%20File%20Manager%20App"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Token on GitHub", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Setup Guide Link
                TextButton(
                    onClick = {
                        val url = "https://imalimrans.pages.dev/blog-posts/GithubFileManagerapk"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Setup & Token Guide", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newTokenInput.trim().isBlank()) {
                        inputError = "Please enter your new token"
                    } else {
                        onUpdateToken(newTokenInput.trim())
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Update & Reconnect")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSwitchAccount) {
                    Text("Switch Account")
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    )
}


