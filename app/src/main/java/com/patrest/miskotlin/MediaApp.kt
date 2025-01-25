package com.patrest.miskotlin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.maps.model.LatLng
import com.patrest.miskotlin.components.DeleteConfirmDialog
import com.patrest.miskotlin.components.SideMenu
import com.patrest.miskotlin.data.MediaItem
import com.patrest.miskotlin.pages.MapScreen
import com.patrest.miskotlin.pages.MediaItemList
import com.patrest.miskotlin.pages.MediaReadView
import com.patrest.miskotlin.viewmodel.MediaViewModel


// MediaApp verwaltet die Navigation und merkt sich die zuletzt besuchte Route
// für korrektes Zurückkehren nach Aktionen.



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaApp(
    viewModel: MediaViewModel,
    isSideMenuVisible: Boolean,
    onSideMenuToggle: () -> Unit,
    onImageSelect: () -> Unit,
    navController: NavHostController,
    onSaveMediaItem: (String, String?, MediaItem?, Boolean) -> Unit,
    deviceLocation: LatLng,
) {
    val showDeleteDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val itemToDelete by viewModel.itemToDelete.collectAsState()
    val lastVisitedRoute = remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog) {
        val item = itemToDelete
        if (item != null) {
            DeleteConfirmDialog(
                itemTitle = item.title,
                onDismiss = { viewModel.dismissDeleteConfirmation() },
                onConfirm = {
                    viewModel.confirmDelete { success ->
                        if (success) {
                            when (lastVisitedRoute.value) {
                                "karte" -> navController.navigate("karte") {
                                    popUpTo("karte") { inclusive = true }
                                }

                                "medien" -> navController.navigate("medien") {
                                    popUpTo("medien") { inclusive = true }
                                }

                                else -> navController.navigate("medien")
                            }
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = "medien",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("medien") {
                        lastVisitedRoute.value = "medien"
                        MediaItemList(
                            viewModel = viewModel,
                            navController = navController,
                            onImageSelect = onImageSelect,
                            onSideMenuToggle = onSideMenuToggle,
                            onSaveMediaItem = onSaveMediaItem
                        )
                    }
                    composable("karte") {
                        lastVisitedRoute.value = "karte"
                        MapScreen(
                            viewModel = viewModel,
                            onMenuClick = onSideMenuToggle,
                            onMarkerClick = { itemId ->
                                navController.navigate("readview/$itemId")
                            },
                            deviceLocation = deviceLocation
                        )
                    }


                    composable("readview/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()
                        MediaReadView(
                            itemId = itemId,
                            viewModel = viewModel,
                            onMenuClick = onSideMenuToggle,
                            onDelete = {
                                itemId?.let {
                                    val item =
                                        viewModel.mediaItems.value.find { mediaItem -> mediaItem.id.toInt() == itemId }
                                    if (item != null) {
                                        viewModel.requestDeleteConfirmation(item)
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                if (isSideMenuVisible) {
                    SideMenu(
                        navController = navController,
                        onMenuItemClick = { menuItem ->
                            when (menuItem) {
                                "karte" -> navController.navigate("karte")
                                "medien" -> navController.navigate("medien")
                            }
                            onSideMenuToggle()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .fillMaxHeight()
                            .background(Color.DarkGray)
                    )
                }
            }
        }
    )
}