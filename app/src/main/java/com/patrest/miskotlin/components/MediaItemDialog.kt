package com.patrest.miskotlin.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.patrest.miskotlin.R
import com.patrest.miskotlin.data.MediaItem

@Composable
fun MediaItemDialog(
    defaultTitle: String,
    mediaItem: MediaItem? = null,
    onDismiss: () -> Unit,
    onSave: (String, String?, MediaItem?) -> Unit,
    onImageClick: () -> Unit,
    selectedImagePath: String?
) {
    var title by remember { mutableStateOf(mediaItem?.title ?: defaultTitle) }
    var showError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (mediaItem != null) "Medium editieren" else "Neues Medium") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titel") },
                        isError = showError,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    IconButton(onClick = onImageClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_camera),
                            contentDescription = "Bild auswählen"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val imageToDisplay = selectedImagePath ?: mediaItem?.source
                if (imageToDisplay != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageToDisplay),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(8.dp)
                    )
                }

                if (showError) {
                    Text(
                        text = "Titel erforderlich",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, selectedImagePath ?: mediaItem?.source, mediaItem)
                        onDismiss()
                    } else {
                        showError = true
                    }
                }
            ) {
                Text(if (mediaItem != null) "Speichern" else "Erstellen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}





