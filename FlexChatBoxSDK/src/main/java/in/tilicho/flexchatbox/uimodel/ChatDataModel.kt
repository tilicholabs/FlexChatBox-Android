package `in`.tilicho.flexchatbox.uimodel

import android.net.Uri
import `in`.tilicho.flexchatbox.Location
import `in`.tilicho.flexchatbox.enums.FlexType
import `in`.tilicho.flexchatbox.utils.ContactData
import java.io.File


data class ChatDataModel(
    val camera: Camera? = null,
    val voice: Voice? = null,
    val location: LocationItem? = null,
    val galleryItems: GalleryItems? = null,
    val file: FileItems? = null,
    val contacts: Contacts? = null,
    val textFieldValue: String? = null
)

data class Camera(
    val uri: Uri? = null,
    val flexType: FlexType = FlexType.CAMERA
)

data class Voice(
    val file: File? = null,
    val flexType: FlexType = FlexType.VOICE
)

data class LocationItem(
    val location: Location? = null,
    val flexType: FlexType = FlexType.LOCATION
)

data class GalleryItems(
    val uris: List<Uri>? = null,
    val flexType: FlexType = FlexType.GALLERY
)

data class FileItems(
    val files: List<Uri>? = null,
    val flexType: FlexType = FlexType.FILES
)

data class Contacts(
    val contacts: List<ContactData>? = null,
    val flexType: FlexType = FlexType.CONTACTS
)


