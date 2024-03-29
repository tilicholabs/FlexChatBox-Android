package `in`.tilicho.flexchatbox.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Size
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import `in`.tilicho.flexchatbox.enums.MediaType
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.*


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
                    val locationProvider: String? = locationManager.getBestProvider(criteria, true)
                    location = locationProvider?.let { locationManager.getLastKnownLocation(it) }
                }
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return location
}

fun cameraIntent(videoLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
    val chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video")
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takeVideoIntent))
    videoLauncher.launch(chooserIntent)
}

@SuppressLint("QueryPermissionsNeeded")
fun openFiles(
    context: Context,
    fileLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
        addCategory(Intent.CATEGORY_OPENABLE)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }.also { fileIntent ->
        fileIntent.resolveActivity(context.packageManager)?.also {
            fileLauncher.launch(Intent.createChooser(fileIntent,
                "Choose File to Upload.."))
        }
    }
}

fun getImageUri(context: Context, inImage: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path =
        MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
    return Uri.parse(path)
}

val primaryMobileNumberRegex = Regex("^(91){1}[1-9]{1}[0-9]{9}\$")
val secondaryMobileNumberRegex = Regex("^[1-9]{1}[0-9]{9}\$")

@SuppressLint("Range")
fun getContacts(applicationContext: Context): List<ContactData> {
    val list: MutableList<ContactData> = ArrayList()
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
        .filter {
            it.mobileNumber?.replaceFirst("+", "").let { number ->
                number?.matches(primaryMobileNumberRegex) == true ||
                        number?.matches(secondaryMobileNumberRegex) == true
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

fun getMediaType(context: Context, source: Uri?): MediaType {
    val mediaTypeRaw = source?.let { context.contentResolver.getType(it) }
    if (mediaTypeRaw?.startsWith("image") == true)
        return MediaType.MediaTypeImage
    if (mediaTypeRaw?.startsWith("video") == true)
        return MediaType.MediaTypeVideo
    return MediaType.Unknown
}

fun getThumbnail(context: Context, uri: Uri): Bitmap? {
    var bitmap: Bitmap? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val mSize = Size(512, 512)
        val cancellationSignal = CancellationSignal()
        bitmap = context.contentResolver.loadThumbnail(uri, mSize, cancellationSignal)
    }
    return bitmap
}

fun Context.navigateToAppSettings() {
    this.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", this.packageName, null)
        )
    )
}

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun isLocationEnabled(context: Context): Boolean {
    val locationMode: Int = try {
        Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
    } catch (e: SettingNotFoundException) {
        e.printStackTrace()
        return false
    }
    return locationMode != Settings.Secure.LOCATION_MODE_OFF
}

fun getGrantedPermissions(context: Context): List<String> {
    val granted: MutableList<String> = ArrayList()
    try {
        val pi: PackageInfo =
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        for (i in pi.requestedPermissions.indices) {
            if (pi.requestedPermissionsFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                granted.add(pi.requestedPermissions[i])
            }
        }
    } catch (_: Exception) {
    }
    return granted
}

fun MediaPlayer.getDurationInMmSs(): String {
    return convertSecondsToMmSs(Math.round((this.duration / 1000).toDouble()))
}

fun MediaPlayer.getCurrentPositionInMmSs(): String {
    return convertSecondsToMmSs(Math.round(((this.currentPosition / 1000)).toDouble()))
}

fun convertSecondsToMmSs(seconds: Long): String {
    val s = seconds % 60
    val m = seconds / 60 % 60
    return String.format("%02d:%02d", m, s)
}