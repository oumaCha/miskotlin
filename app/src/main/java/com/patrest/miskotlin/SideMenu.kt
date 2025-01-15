package com.patrest.miskotlin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SideMenu(
    navController: NavController,
    onMenuItemClick: (String) -> Unit, // Pass this to handle the click event
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Medien item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onMenuItemClick("medien") // Handle click and dismiss the sidebar
                    navController.navigate("medien") // Navigate to Medien screen
                }
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_media),
                contentDescription = "Medien",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Medien",
                color = Color.White
            )
        }

        // Karte item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onMenuItemClick("karte")
                    navController.navigate("karte")
                }
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = "Karte",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Karte",
                color = Color.White
            )
        }
    }
}
