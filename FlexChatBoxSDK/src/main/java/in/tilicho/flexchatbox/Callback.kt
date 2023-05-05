package `in`.tilicho.flexchatbox

import android.net.Uri
import `in`.tilicho.flexchatbox.utils.ContactData
import java.io.File

sealed interface Callback {
    data class Camera(val uri: Uri) : Callback
    data class Voice(val file: File) : Callback
    data class Location(val location: `in`.tilicho.flexchatbox.Location) : Callback
    data class Files(val uris: List<Uri>) : Callback
    data class Gallery(val uris: List<Uri>) : Callback
    data class Contacts(val contacts: List<ContactData>) : Callback
}