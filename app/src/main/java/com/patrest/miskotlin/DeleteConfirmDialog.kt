package com.patrest.miskotlin

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DeleteConfirmDialog(
    itemTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Medium löschen") },
        text = {
            Text(
                text = buildAnnotatedString {
                    append("Möchten Sie ")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(itemTitle)
                    pop()
                    append(" wirklich löschen?")
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Löschen", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
