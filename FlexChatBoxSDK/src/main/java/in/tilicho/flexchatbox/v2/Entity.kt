package `in`.tilicho.flexchatbox.v2

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

class Entity<Content : @Composable () -> Unit>(val type: FlexInputType, val invocationContentView: Content) {
    @Composable
    fun GetInvocationContentView() {
        invocationContentView()
    }
}

// Invocation callback types
typealias CameraInvocationCallback<T> = () -> T
typealias GalleryInvocationCallback<T> = () -> T
typealias MicInvocationCallback<T> = () -> T
typealias LocationInvocationCallback<T> = () -> T


typealias SnapCamInvocationCallback<T> = () -> T

// SuccessCallback
typealias FlexSuccessCallback = (Any) -> Unit
