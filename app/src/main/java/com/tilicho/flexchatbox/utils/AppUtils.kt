package com.tilicho.flexchatbox.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.util.Size
import com.tilicho.flexchatbox.enums.MediaType

fun convertSecondsToMmSs(seconds: Long): String {
    val s = seconds % 60
    val m = seconds / 60 % 60
    return String.format("%02d:%02d", m, s)
}

fun MediaPlayer.getDurationInMmSs(): String {
    return convertSecondsToMmSs(Math.round((this.duration / 1000).toDouble()))
}

fun MediaPlayer.getCurrentPositionInMmSs(): String {
    return convertSecondsToMmSs(Math.round(((this.currentPosition / 1000)).toDouble()))
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