package com.patrest.miskotlin

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(mediaItems: List<MediaItem>, onMenuClick: () -> Unit) {

    val itemsWithLocation = mediaItems.filter { it.latitude != 0.0 && it.longitude != 0.0 }

    // Log os itens com localização
    LaunchedEffect(itemsWithLocation) {
        Log.d("MapScreen", "Media items with location: $itemsWithLocation")
    }
    // val initialPosition = LatLng(52.545995, 13.351148) // BHT (Berlin)
    val initialPosition = LatLng(37.4220936, -122.083922)

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.Builder()
            .target(initialPosition)
            .zoom(12f)
            .build()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map View", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sandwich),
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2E2E2E))
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                mediaItems.forEach { item ->
                    if (item.latitude != null && item.longitude != null && item.latitude != 0.0 && item.longitude != 0.0) {
                        Marker(
                            state = MarkerState(position = LatLng(item.latitude!!, item.longitude!!)),
                            title = item.title,
                            snippet = "Added from gallery"
                        )
                    }
                }
            }
        }
    }
}
