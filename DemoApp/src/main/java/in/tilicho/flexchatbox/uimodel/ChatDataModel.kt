package `in`.tilicho.flexchatbox.uimodel

import android.net.Uri
import `in`.tilicho.flexchatbox.Location
import `in`.tilicho.flexchatbox.enums.Sources
import `in`.tilicho.flexchatbox.utils.ContactData
import java.io.File


data class ChatDataModel(
    val camera: Camera? = null,
    val video: Video? = null,
    val voice: Voice? = null,
    val location: LocationItem? = null,
    val galleryItems: GalleryItems? = null,
    val file: FileItems? = null,
    val contacts: Contacts? = null,
    val textFieldValue: String? = null
)

data class Camera(
    val uri: Uri? = null,
    val sourceType: Sources = Sources.CAMERA
)

data class Video(
    val uri: Uri? = null,
    val sourceType: Sources = Sources.VIDEO
)

data class Voice(
    val file: File? = null,
    val sourceType: Sources = Sources.VOICE
)

data class LocationItem(
    val location: Location? = null,
    val sourceType: Sources = Sources.LOCATION
)

data class GalleryItems(
    val uris: List<Uri>? = null,
    val sourceType: Sources = Sources.GALLERY
)

data class FileItems(
    val files: List<Uri>? = null,
    val sourceType: Sources = Sources.FILES
)

data class Contacts(
    val contacts: List<ContactData>? = null,
    val sourceType: Sources = Sources.CONTACTS
)


