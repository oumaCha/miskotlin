package com.patrest.miskotlin.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.patrest.miskotlin.data.MediaItem

@Composable
fun ActionMenuDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = item.title)
        },
        text = {
            Column {
                TextButton(onClick = onEdit) {
                    Text("Edit  ")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

