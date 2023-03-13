package com.tilicho.flexchatbox.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tilicho.flexchatbox.enums.MediaType
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


fun generateUri(context: Context): Uri {
    val file = context.createImageFile()
    return FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "${context.packageName}.provider",
        file
    )
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
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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


val primaryMobileNumberRegex = Regex("^(91){1}[1-9]{1}[0-9]{9}\$")
val secondaryMobileNumberRegex = Regex("^[1-9]{1}[0-9]{9}\$")

@SuppressLint("Range")
fun getContacts(applicationContext: Context): List<ContactData> {
    val list: MutableList<ContactData> = java.util.ArrayList()
    val contentResolver: ContentResolver = applicationContext.contentResolver
    val orderBy = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC"
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone._ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
        ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.Contacts.STARRED,
        ContactsContract.Contacts.LOOKUP_KEY
    )

    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        null,
        null,
        orderBy
    )
    cursor?.moveToFirst()

    if (cursor != null && cursor.count > 0) {
        val mobileNoSet = HashSet<String>()
        while (!cursor.isAfterLast) {
            val info = ContactData()
            val number =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .replace(" ", "")
            info.name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            if (!mobileNoSet.contains(number)) {
                info.mobileNumber = number
                list.add(info)
                mobileNoSet.add(number)
            }
            cursor.moveToNext()
        }
        cursor.close()
    }
    return list.distinct()
        .sortedBy { it.name }
        .filter { it ->
            it.mobileNumber?.replaceFirst("+", "").let { number ->
                number?.matches(primaryMobileNumberRegex)!! ||
                        number.matches(secondaryMobileNumberRegex)
            }
        }
}


const val ZERO = 0
const val ONE = 1
const val TWO = 2

data class ContactData(
    var id: String? = null,
    var name: String? = null,
    var mobileNumber: String? = null,
    var photo: Bitmap? = null,
    var unknownContact: Boolean? = false,

    ) : Serializable {
    fun getInitial(): String {
        return try {
            if (name?.split(" ")?.size!! > ONE) {
                name?.get(ZERO).toString()
                    .uppercase(Locale.getDefault()) + name?.split(" ")?.get(ONE)
                    ?.get(ZERO).toString().uppercase(Locale.getDefault())
            } else if (name?.trim()?.length!! > TWO) {
                name?.trim()?.get(ZERO).toString().uppercase(Locale.getDefault()) + name?.get(ONE)
                    .toString()
                    .uppercase(Locale.getDefault())
            } else {
                name?.trim()?.get(ZERO).toString().uppercase(Locale.getDefault())
            }
        } catch (e: NullPointerException) {
            name?.trim()?.get(ZERO).toString().uppercase(Locale.getDefault())
        }
    }
}