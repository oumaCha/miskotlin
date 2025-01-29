package com.patrest.miskotlin.pages

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.patrest.miskotlin.R
import com.patrest.miskotlin.viewmodel.MediaViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MediaViewModel,
    onMenuClick: () -> Unit,
    onMarkerClick: (Int) -> Unit,
    deviceLocation: LatLng
) {

    val itemsWithLocation = viewModel.mediaItemsWithLocation.collectAsState().value

    LaunchedEffect(itemsWithLocation) {
        Log.d("MapScreen", "Media items with location: $itemsWithLocation")
    }

    // val initialPosition = LatLng(37.4220936, -122.083922)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(deviceLocation)
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
                itemsWithLocation.forEach { item ->
                    Log.d("MapScreen", "Adding marker for item: ${item.title} at (${item.latitude}, ${item.longitude})")
                    Marker(
                        state = MarkerState(position = LatLng(item.latitude!!, item.longitude!!)),
                        title = item.title,
                        onClick = { marker ->
                            Log.d("MapScreen", "Clicked on marker: ${item.title}")
                            marker.showInfoWindow()
                            true
                        },
                        onInfoWindowClick = { marker ->
                            Log.d("MapScreen", "Info window clicked for marker: ${item.title}")
                            onMarkerClick(item.id.toInt())
                        }
                    )

                }
            }
        }
    }
}
