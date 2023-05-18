package `in`.tilicho.flexchatbox.v2

import android.net.Uri
import androidx.compose.runtime.Composable
import java.io.File


@Composable
fun SampleComposable() {

}

// Create views for each individual flexType
object InvocationViewFactory {

    /*
    // Below code is conversion from iOS
    @Composable
    fun cameraInvocationCallback(successCallback: CameraSuccessCallback): AnyView {
        AndroidView(factory = { context ->
            CameraView(context).apply {
                setCameraSuccessCallback(successCallback)
            }
        })
    }
    * */


    @Composable
    fun cameraInvocationCallback(successCallback: CameraSuccessCallback): Uri {
        /*return CameraView(cameraSuccessCallback = successCallback)
            .eraseToAnyView()*/
        return Uri.EMPTY
    }

    @Composable
    fun galleryInvocationCallback(successCallback: GallerySuccessCallback): List<Uri> {
        /*return GalleryView(cameraSuccessCallback = successCallback)
            .eraseToAnyView()*/
        return emptyList()
    }

    @Composable
    fun microphoneInvocationCallback(): File/*Have to change further*/ {
        // Check for permissions
        // Launch microphone
        // Prepare output
        // Send output
        /*return PlaceholderView()
            .eraseToAnyView()*/
        return File("");
    }
}
