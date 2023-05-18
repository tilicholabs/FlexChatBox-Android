package `in`.tilicho.flexchatbox.v2

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import java.net.URI
import java.net.URL

// Success callback types
typealias CameraSuccessCallback = (image: ImageBitmap?, videoURL: URL?) -> Unit
typealias GallerySuccessCallback = (uri: URI) -> Unit
typealias MicSuccessCallback = (image: ImageBitmap, videoURL: String) -> Unit
typealias LocationSuccessCallback = (image: ImageBitmap, videoURL: String) -> Unit
