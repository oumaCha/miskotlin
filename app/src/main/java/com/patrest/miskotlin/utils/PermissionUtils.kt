package com.patrest.miskotlin.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.LocationServices

object PermissionUtils {

    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    fun requestLocationPermission(
        activity: ComponentActivity,
        onPermissionGranted: (() -> Unit)? = null,
        onPermissionDenied: (() -> Unit)? = null
    ) {
        val requestPermissionLauncher: ActivityResultLauncher<String> =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(activity, "Location permission granted", Toast.LENGTH_SHORT).show()
                    onPermissionGranted?.invoke()
                } else {
                    Toast.makeText(activity, "Location permission denied", Toast.LENGTH_SHORT).show()
                    onPermissionDenied?.invoke()
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun getLastKnownLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
        if (checkLocationPermission(context)) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    onLocationReceived(location)
                }
                .addOnFailureListener { exception ->
                    println("Error obtaining location: ${exception.message}")
                    onLocationReceived(null)
                }
        } else {
            println("Location permission not granted")
            onLocationReceived(null)
        }
    }

    fun checkMediaPermissions(context: Context): Boolean {
        val readImagesPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        val mediaLocationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_MEDIA_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Não
        }

        return readImagesPermission && mediaLocationPermission
    }
}
