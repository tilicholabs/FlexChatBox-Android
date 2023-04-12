package com.tilicho.flexchatbox

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.tilicho.flexchatbox.audiorecorder.AndroidAudioRecorder
import com.tilicho.flexchatbox.enums.MediaType
import com.tilicho.flexchatbox.enums.Sources
import com.tilicho.flexchatbox.utils.ContactData
import com.tilicho.flexchatbox.utils.GALLERY_INPUT_TYPE
import com.tilicho.flexchatbox.utils.GetMediaActivityResultContract
import com.tilicho.flexchatbox.utils.LOCATION_URL
import com.tilicho.flexchatbox.utils.cameraIntent
import com.tilicho.flexchatbox.utils.checkPermission
import com.tilicho.flexchatbox.utils.findActivity
import com.tilicho.flexchatbox.utils.getContacts
import com.tilicho.flexchatbox.utils.getCurrentPositionInMmSs
import com.tilicho.flexchatbox.utils.getDurationInMmSs
import com.tilicho.flexchatbox.utils.getGrantedPermissions
import com.tilicho.flexchatbox.utils.getImageUri
import com.tilicho.flexchatbox.utils.getLocation
import com.tilicho.flexchatbox.utils.getMediaType
import com.tilicho.flexchatbox.utils.getThumbnail
import com.tilicho.flexchatbox.utils.isLocationEnabled
import com.tilicho.flexchatbox.utils.navigateToAppSettings
import com.tilicho.flexchatbox.utils.openFiles
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ChatBox(
    context: Context,
    source: Sources,
    selectedPhotosOrVideos: (List<Uri>) -> Unit,
    recordedAudio: (File) -> Unit,
    currentLocation: (Location?) -> Unit,
    onClickSend: (String) -> Unit,
    selectedContactsCallBack: (List<ContactData>) -> Unit,
    selectedFiles: (List<Uri>) -> Unit,
    camera: (Sources, Uri) -> Unit,
) {
    var textFieldValue by rememberSaveable { mutableStateOf(String.empty()) }

    var location by remember {
        mutableStateOf<Location?>(null)
    }

    var galleryList by remember {
        mutableStateOf<List<Uri>?>(null)
    }

    val recorder by lazy {
        AndroidAudioRecorder(context)
    }

    var audioFile by remember {
        mutableStateOf<File?>(null)
    }

    var isPressed by remember {
        mutableStateOf(false)
    }

    var isRecording by remember {
        mutableStateOf(false)
    }

    var showAudioPreview by remember {
        mutableStateOf(false)
    }

    var contacts: List<ContactData> by remember { mutableStateOf(listOf()) }

    var displayContacts by remember {
        mutableStateOf(false)
    }

    if (displayContacts) {
        DisplayContacts(contacts = contacts, selectedContactsCallBack = {
            displayContacts = false
            if (it.isNotEmpty()) {
                selectedContactsCallBack.invoke(it)
            }
        })

    }

    var galleryState by remember {
        mutableStateOf(false)
    }
    var showDialog by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    var isCameraPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var isGalleryPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var isLocationPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var isContactsPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var isFilesPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var isRecordAudioPermissionPermanentlyDenied by remember { mutableStateOf(false) }

    if (isCameraPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.CAMERA)) {
                        isCameraPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    if (isGalleryPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        isGalleryPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    if (isLocationPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        isLocationPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    if (isContactsPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.READ_CONTACTS)) {
                        isContactsPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    if (isFilesPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        isFilesPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    if (isRecordAudioPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.RECORD_AUDIO)) {
                        isRecordAudioPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(GetMediaActivityResultContract()) {
        galleryList = it
        galleryState = true
        showDialog = true
    }
    if (galleryState) {
        if (galleryList?.isNotEmpty() == true) {
            if (showDialog) {
                GalleryPreviewDialog(
                    context,
                    galleryList = galleryList ?: listOf(),
                    galleryLauncher = galleryLauncher,
                    onDismissCallback = { dismissDialog, setImages ->
                        showDialog = dismissDialog
                        if (setImages) {
                            selectedPhotosOrVideos(galleryList ?: listOf())
                        }
                    })
            }
        }
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {

                if (activityResult.data?.data != null) {
                    activityResult.data?.data?.let {
                        camera(Sources.VIDEO, it)
                    }
                } else {
                    getImageUri(context, activityResult.data?.extras?.get("data") as Bitmap)?.let {
                        camera(Sources.CAMERA, it)
                    }
                }
            }
        }

    val fileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val filesUriList: MutableList<Uri> = mutableListOf()
            if (activityResult.resultCode == Activity.RESULT_OK) {
                if (activityResult.data?.clipData != null) {
                    val count = activityResult.data?.clipData?.itemCount ?: 0
                    var currentItem = 0
                    while (currentItem < count) {
                        activityResult.data?.clipData?.getItemAt(currentItem)?.uri
                            ?.let { (filesUriList.add(it)) }
                        currentItem += 1
                    }
                    selectedFiles(filesUriList)
                } else {
                    activityResult.data?.data.let {
                        if (it != null) {
                            filesUriList.add(it)
                        }
                    }
                    selectedFiles(filesUriList)
                }
            }
        }

    val galleryPermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isGalleryPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isGalleryPermissionPermanentlyDenied = false
                showDialog = false
                galleryLauncher.launch(GALLERY_INPUT_TYPE)
            }
        }

    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.CAMERA
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isCameraPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isCameraPermissionPermanentlyDenied = false
                showDialog = false
                cameraIntent(cameraLauncher)
            }
        }

    val locationPermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isLocationPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isLocationPermissionPermanentlyDenied = false
                showDialog = false
                if (!isLocationEnabled(context)) {
                    Toast.makeText(context, R.string.enable_location, Toast.LENGTH_LONG).show()
                } else {
                    val currLocation = getLocation(context)
                    val latLong =
                        (currLocation?.latitude).toString() + "," + (currLocation?.longitude).toString()
                    location = Location(
                        currLocation,
                        LOCATION_URL + latLong
                    )
                    currentLocation(location)
                }
            }
        }

    val contactsPermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.READ_CONTACTS
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isContactsPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                val data = getContacts(context)
                isContactsPermissionPermanentlyDenied = false
                showDialog = false
                contacts = data
                displayContacts = true
            }
        }

    val filesPermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isFilesPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isFilesPermissionPermanentlyDenied = false
                showDialog = false
                openFiles(context, fileLauncher)
            }
        }

    val recordAudioPermissionState =
        rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.RECORD_AUDIO
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isRecordAudioPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isRecordAudioPermissionPermanentlyDenied = false
                showDialog = false
            }
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (showAudioPreview) {
            AudioPreviewUI(context = context, audioFile = audioFile,
                onClickDelete = {
                    showAudioPreview = false
                }, onClickSend = {
                    showAudioPreview = false
                    audioFile?.let { recordedAudio(it) }
                })
        } else {
            if (isRecording) {
                AudioRecordingUi()
            } else {
                var sendIconState by remember {
                    mutableStateOf(Color(0xFF808080))
                }
                sendIconState = if (textFieldValue.isNotEmpty()) {
                    colorResource(R.color.c_2ba6ff)
                } else {
                    Color(0xFF808080)
                }
                Row(
                    verticalAlignment = Alignment.Bottom, modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f)
                        .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacing_60)))
                        .background(
                            color = if (isSystemInDarkTheme()) Color.Black else colorResource(
                                id = R.color.c_edf0ee
                            )
                        )
                ) {
                    TextField(
                        value = textFieldValue,
                        onValueChange = {
                            textFieldValue = it
                        },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint),
                                color = colorResource(id = R.color.c_placeholder),
                                fontSize = 18.sp
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier.weight(6f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    IconButton(modifier = Modifier.weight(1.5f),
                        onClick = {
                            onClickSend.invoke(textFieldValue)
                            textFieldValue = String.empty()
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_send),
                            contentDescription = null,
                            tint = sendIconState
                        )
                    }
                    colorResource(R.color.grey)
                }
            }

            Row(
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.spacing_20), bottom = dimensionResource(
                        id = R.dimen.spacing_00
                    )),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                if (true) {
                    when (source) {
                        Sources.GALLERY -> {
                            SourceImage(context = context,
                                icon = R.drawable.ic_gallery,
                                isDenied = isGalleryPermissionPermanentlyDenied,
                                permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                                onClickIcon = {
                                    galleryPermissionState.launchPermissionRequest()
                                })
                        }

                        Sources.LOCATION -> {
                            SourceImage(context = context,
                                icon = R.drawable.ic_location,
                                isDenied = isLocationPermissionPermanentlyDenied,
                                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                                onClickIcon = {
                                    locationPermissionState.launchPermissionRequest()
                                })
                        }

                        Sources.VOICE -> {
                            var iconState by remember {
                                mutableStateOf(R.color.c_2ba6ff)
                            }
                            if (!checkPermission(context, Manifest.permission.RECORD_AUDIO)) {
                                if (isRecordAudioPermissionPermanentlyDenied) {
                                    iconState = R.color.c_ebeef1
                                }
                            } else {
                                iconState = R.color.c_2ba6ff
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(shape = CircleShape)
                                    .background(color = colorResource(id = iconState))
                            ) {
                                Image(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_mic),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(dimensionResource(id = R.dimen.spacing_30))
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    isPressed = true
                                                    if (checkPermission(
                                                            context,
                                                            Manifest.permission.RECORD_AUDIO
                                                        )
                                                    ) {
                                                        isRecordAudioPermissionPermanentlyDenied =
                                                            false
                                                        try {
                                                            File(
                                                                context.cacheDir,
                                                                "audio${System.currentTimeMillis()}.mp3"
                                                            ).also {
                                                                Handler(Looper.getMainLooper()).postDelayed(
                                                                    {
                                                                        if (isPressed) {
                                                                            isRecording = true
                                                                            recorder.start(it)
                                                                            audioFile = it
                                                                        }
                                                                    }, 200
                                                                )
                                                            }
                                                            awaitRelease()
                                                        } finally {
                                                            try {
                                                                isPressed = false
                                                                if (isRecording) {
                                                                    recorder.stop()
                                                                    isRecording = false
                                                                    showAudioPreview = true
                                                                }
                                                            } catch (_: Exception) {
                                                            }
                                                        }
                                                    } else {
                                                        recordAudioPermissionState.launchPermissionRequest()
                                                    }
                                                }
                                            )
                                        }
                                )
                            }
                        }

                        Sources.CONTACTS -> {
                            SourceImage(context = context,
                                icon = R.drawable.ic_person,
                                isDenied = isContactsPermissionPermanentlyDenied,
                                Manifest.permission.READ_CONTACTS,
                                onClickIcon = {
                                    contactsPermissionState.launchPermissionRequest()
                                })
                        }
                        Sources.FILES -> {
                            SourceImage(context = context,
                                icon = R.drawable.ic_file,
                                isDenied = isFilesPermissionPermanentlyDenied,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                onClickIcon = {
                                    filesPermissionState.launchPermissionRequest()
                                })
                        }
                        Sources.CAMERA -> {
                            SourceImage(context = context,
                                icon = R.drawable.ic_camera,
                                isDenied = isCameraPermissionPermanentlyDenied,
                                Manifest.permission.CAMERA, onClickIcon = {
                                    cameraPermissionState.launchPermissionRequest()
                                })
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}


@Composable
fun SourceImage(
    context: Context,
    icon: Int,
    isDenied: Boolean,
    permission: String,
    onClickIcon: () -> Unit,
) {
    var iconState by remember {
        mutableStateOf(R.color.c_2ba6ff)
    }

    if (!checkPermission(context = context, permission)) {
        if (isDenied) {
            iconState = R.color.c_ebeef1
        }
    } else {
        iconState = R.color.c_2ba6ff
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape = CircleShape)
            .background(color = colorResource(id = iconState))
            .clickable(onClick = {
                onClickIcon.invoke()
            })
    ) {
        Image(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_30)),
        )
    }

}

fun String.Companion.empty() = ""

@Composable
fun DisplayContacts(
    contacts: List<ContactData>, selectedContactsCallBack: (List<ContactData>) -> Unit,
) {

    val selectedContacts: MutableList<ContactData> = mutableListOf()
    Dialog(
        onDismissRequest = {
            selectedContactsCallBack.invoke(emptyList())
        }, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacing_10dp)),
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(id = R.dimen.spacing_10dp))
                    .padding(vertical = dimensionResource(id = R.dimen.spacing_10dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.spacing_10dp))
                        .padding(start = dimensionResource(id = R.dimen.spacing_10dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        color = colorResource(id = R.color.c_2ba6ff),
                        text = stringResource(id = R.string.contacts),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.opensans_regular))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.c_2ba6ff)),
                        onClick = {
                            selectedContactsCallBack.invoke(selectedContacts)
                        }) {
                        Text(
                            text = stringResource(id = R.string.send),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.opensans_regular))
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_20)))

                LazyColumn(modifier = Modifier) {
                    items(contacts.size) { index ->
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_00)))
                        var isSelected by remember {
                            mutableStateOf(selectedContacts.contains(contacts[index]))
                        }
                        val contact = contacts[index]
                        var icon by remember {
                            mutableStateOf(Icons.Filled.Person)
                        }
                        icon = if (isSelected) {
                            Icons.Filled.Done
                        } else {
                            Icons.Filled.Person
                        }
                        Row(
                            modifier = Modifier
                                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                                .clickable(onClick = {
                                    isSelected = if (selectedContacts.contains(contact)) {
                                        selectedContacts.remove(contact)
                                        false
                                    } else {
                                        selectedContacts.add(contact)
                                        true
                                    }
                                }), verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                contact.name?.let {
                                    Text(
                                        text = it,
                                        fontFamily = FontFamily(Font(R.font.opensans_regular))
                                    )
                                }
                                contact.mobileNumber?.let {
                                    Text(
                                        text = it,
                                        fontFamily = FontFamily(Font(R.font.opensans_regular))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = colorResource(
                                    id = R.color.c_2ba6ff
                                )
                            )
                        }
                        Divider()
                    }
                }

                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Click me!")
                }
            }
        }
    }
}

@Composable
fun AudioRecordingUi() {
    var seconds by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(0) }
    if (seconds >= 60) {
        minutes++
        seconds = 0
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)
            seconds++
        }

    }
    Row(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.spacing_90))
            .padding(
                top = dimensionResource(id = R.dimen.spacing_10dp), start = dimensionResource(
                    id = R.dimen.spacing_40
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.recording_audio),
            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
            fontFamily = FontFamily(Font(R.font.opensans_regular)),
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10dp)))

        if (seconds < 10) {
            Text(
                text = "0$minutes:0$seconds",
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
                fontSize = 18.sp
            )
        } else {
            Text(
                text = "0$minutes:$seconds",
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10dp)))
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_recorder),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_80))
        )
    }
}

@SuppressLint("MutableCollectionMutableState")
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun GalleryPreviewUI(
    context: Context,
    items: List<Uri>,
    galleryLauncher: ManagedActivityResultLauncher<String, List<@JvmSuppressWildcards Uri>>,
    onDismissClickCallBack: (Boolean, Boolean) -> Unit,
) {

    var previewUri by remember {
        mutableStateOf<Uri?>(items[0])
    }

    val previewUriList by remember {
        mutableStateOf<MutableList<Uri>?>(items as MutableList<Uri>)
    }

    Column(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.spacing_20))
        ) {
            Image(imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        onDismissClickCallBack.invoke(false, false)
                    }
                    .padding(dimensionResource(id = R.dimen.spacing_20))
                    .size(dimensionResource(id = R.dimen.spacing_70))
            )

            Spacer(modifier = Modifier.weight(1f))

            if ((previewUriList?.size ?: 0) > 1) {
                Image(ImageVector.vectorResource(R.drawable.ic_delete),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.spacing_20))
                        .size(dimensionResource(id = R.dimen.spacing_70))
                        .clickable {
                            previewUri?.let { previewUriList?.remove(it) }
                            previewUri = previewUriList?.get(0)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_70)))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(items) { _, item ->
                if (getMediaType(context, item) == MediaType.MediaTypeImage) {
                    Image(
                        painter = rememberAsyncImagePainter(model = item),
                        contentDescription = stringResource(R.string.avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(
                                size = if (previewUri == item) dimensionResource(id = R.dimen.spacing_100) else dimensionResource(
                                    id = R.dimen.spacing_90
                                )
                            )
                            .clickable {
                                previewUri = item
                            }
                            .border(
                                width = dimensionResource(id = R.dimen.spacing_00),
                                color = if (previewUri == item) colorResource(id = R.color.c_ebeef1) else Color.Transparent
                            )
                    )
                } else if (getMediaType(context, item) == MediaType.MediaTypeVideo) {
                    Box(contentAlignment = Alignment.Center) {

                        getThumbnail(context, item)?.asImageBitmap()?.let {
                            Image(
                                bitmap = it,
                                contentDescription = stringResource(R.string.avatar),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(
                                        size = if (previewUri == item) dimensionResource(id = R.dimen.spacing_100) else dimensionResource(
                                            id = R.dimen.spacing_90
                                        )
                                    )
                                    .clickable {
                                        previewUri = item
                                    }
                                    .border(
                                        width = dimensionResource(id = R.dimen.spacing_00),
                                        color = if (previewUri == item) colorResource(id = R.color.c_ebeef1) else Color.Transparent
                                    )
                            )
                        }

                        Image(
                            painter = rememberAsyncImagePainter(model = R.drawable.ic_play_grey),
                            contentDescription = null,
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.spacing_80))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_20)))
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_40)))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            if (getMediaType(context, previewUri) == MediaType.MediaTypeImage) {
                Image(
                    painter = rememberAsyncImagePainter(model = previewUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.preview_image_height))
                        .clip(RectangleShape)
                )
            } else if (getMediaType(context, previewUri) == MediaType.MediaTypeVideo) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_100)))
                VideoView(context, videoUri = previewUri.toString())
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_100)))
            }

        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_40)))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Card(shape = RectangleShape,
                backgroundColor = Color.White,
                modifier = Modifier.clickable {
                    galleryLauncher.launch(GALLERY_INPUT_TYPE)
                }) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_gallery),
                    contentDescription = null, modifier = Modifier.padding(
                        dimensionResource(id = R.dimen.spacing_30)
                    ),
                    colorFilter = ColorFilter.tint(Color.Black)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.End, modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.spacing_10))
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_ok),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.spacing_80))
                    .clickable {
                        onDismissClickCallBack.invoke(false, true)
                    }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun GalleryPreviewDialog(
    context: Context,
    galleryList: List<Uri>,
    galleryLauncher: ManagedActivityResultLauncher<String, List<@JvmSuppressWildcards Uri>>,
    onDismissCallback: (Boolean, Boolean) -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onDismissCallback(false, false)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
    {
        GalleryPreviewUI(
            context,
            items = galleryList,
            galleryLauncher = galleryLauncher,
            onDismissClickCallBack = { dismissDialog, setImages ->
                onDismissCallback(dismissDialog, setImages)
            })
    }
}

@Composable
fun VideoView(context: Context, videoUri: String) {
    val exoPlayer = ExoPlayer.Builder(context)
        .build()
        .also { exoPlayer ->
            val mediaItem = MediaItem.Builder()
                .setUri(videoUri)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }

    DisposableEffect(
        AndroidView(modifier = Modifier
            .height(dimensionResource(id = R.dimen.preview_image_height))
            .fillMaxWidth(),
            factory = {
                StyledPlayerView(context).apply {
                    player = exoPlayer
                }
            })
    ) {
        onDispose { exoPlayer.release() }
    }
}

@Composable
fun ShowNavigateToAppSettingsDialog(context: Context, onDismissCallback: (Boolean) -> Unit) {
    Dialog(
        onDismissRequest = {
            onDismissCallback(false)
        }
    ) {
        Card(shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacing_10dp))) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_60)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_settings),
                    contentDescription = null,
                    tint = Color.Blue
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_30)))

                Text(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    text = stringResource(id = R.string.permission_denied)
                )

                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = R.dimen.spacing_40))
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(fontWeight = FontWeight.Bold,
                        text = stringResource(id = R.string.settings),
                        color = Color.Blue,
                        modifier = Modifier.clickable {
                            onDismissCallback(false)
                            context.navigateToAppSettings()
                        })

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_40)))

                    Text(fontWeight = FontWeight.Bold, text = stringResource(id = R.string.cancel),
                        color = Color.Blue,
                        modifier = Modifier.clickable {
                            onDismissCallback(false)
                        })
                }
            }
        }
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun AudioPreviewUI(
    context: Context,
    audioFile: File?,
    onClickDelete: () -> Unit,
    onClickSend: () -> Unit
) {
    var player: MediaPlayer? = null

    if (audioFile != null) {
        MediaPlayer.create(context, audioFile.toUri()).apply {
            player = this
        }
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        ShowAudioSlider(player = player, onClickDelete = {
            onClickDelete()
        }, onClickSend = {
            onClickSend()
        })
    }
}

@Composable
fun ShowAudioSlider(player: MediaPlayer?, onClickDelete: () -> Unit, onClickSend: () -> Unit) {
    val playing = remember {
        mutableStateOf(false)
    }
    val position = remember {
        mutableStateOf(0F)
    }
    var durationScale by remember {
        mutableStateOf("00:00")
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (player != null) {
            Icon(
                imageVector = if (!playing.value) ImageVector.vectorResource(
                    id = R.drawable.ic_play_circle
                ) else ImageVector.vectorResource(
                    id = R.drawable.ic_pause_circle
                ),
                contentDescription = "image",
                modifier = Modifier
                    .weight(1f)
                    .size(dimensionResource(id = R.dimen.spacing_90))
                    .clickable(onClick = {
                        if (player.isPlaying) {
                            player.pause()
                            playing.value = false
                        } else {
                            player.start()
                            playing.value = true
                        }

                        object : CountDownTimer(player.duration.toLong(), 100) {

                            override fun onTick(millisUntilFinished: Long) {
                                durationScale = player.getCurrentPositionInMmSs()
                                position.value = player.currentPosition.toFloat()
                                if ((position.value / 1000).roundToInt() == (player.duration / 1000)
                                        .toDouble()
                                        .roundToInt()
                                ) {
                                    playing.value = false
                                    position.value = 0F
                                    durationScale = "00:00"
                                }
                            }

                            override fun onFinish() {
                            }
                        }.start()
                    })
                )
                Column(modifier = Modifier
                    .weight(3.5f)
                    .padding(top = dimensionResource(id = R.dimen.spacing_30))) {
                    Slider(
                        value = position.value,
                        valueRange = 0F..player.duration.toFloat(),
                        onValueChange = {
                            if ((position.value / 1000).roundToInt() == (player.duration / 1000)
                                    .toDouble()
                                    .roundToInt()
                            ) {
                                playing.value = false
                                position.value = 0F
                                durationScale = "00:00"
                            }
                            position.value = it
                            player.seekTo(it.toInt())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = colorResource(id = R.color.c_2ba6ff),
                            activeTrackColor = if (isSystemInDarkTheme()) colorResource(id = R.color.c_2ba6ff) else Color.Black,
                            inactiveTrackColor = colorResource(
                                id = R.color.c_placeholder
                            )
                        ),
                        modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_60))
                    )
                    Row {
                        Text(
                            text = durationScale,
                            fontFamily = FontFamily(Font(R.font.opensans_regular)),
                            modifier = Modifier.weight(1f),
                            fontSize = 15.sp
                        )
                        Text(
                            text = player.getDurationInMmSs(),
                            fontFamily = FontFamily(Font(R.font.opensans_regular)),
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_20)))
            Row(modifier = Modifier.weight(2f)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.spacing_90))
                        .clip(shape = CircleShape)
                        .background(color = colorResource(R.color.c_ff0404))
                        .clickable(onClick = {
                            player?.stop()
                            onClickDelete.invoke()
                        })
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.spacing_30)),
                    )
                }
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10)))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.spacing_90))
                        .clip(shape = CircleShape)
                        .background(color = colorResource(R.color.c_2ba6ff))
                        .clickable(onClick = {
                            player?.stop()
                            onClickSend.invoke()
                        })
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_send),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.spacing_30)),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.stop()
        }
    }
}