package com.tilicho.flexchatbox.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun generateUri(context: Context): Uri {
    val file = context.createImageFile()
    return FileProvider.getUriForFile(Objects.requireNonNull(context),
        "${context.packageName}.provider",
        file)
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    return File.createTempFile(imageFileName, ".jpg", externalCacheDir)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun checkPermission(context: Context, permission: String): Boolean {
    return when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(context, permission) -> {
            true
        }
        else -> {
            false
        }
    }
}

fun requestPermission(
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    permission: String,
) {
    permissionLauncher.launch(permission)
}

fun getLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    var location: Location? = null

    try {
        if (
            ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {

            location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            val providers: List<String> = locationManager?.getProviders(true) as List<String>
            for (provider in providers) {
                locationManager.requestLocationUpdates(provider, 1000, 0f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {}
                        override fun onProviderDisabled(provider: String) {}
                        override fun onProviderEnabled(provider: String) {}
                    })

                if (location == null) {
                    val criteria = Criteria()
                    criteria.accuracy = Criteria.ACCURACY_COARSE
                    val _provider: String? = locationManager.getBestProvider(criteria, true)
                    location = _provider?.let { locationManager.getLastKnownLocation(it) }
                }
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return location
}

