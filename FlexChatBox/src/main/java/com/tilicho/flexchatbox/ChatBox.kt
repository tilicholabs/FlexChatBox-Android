package com.tilicho.flexchatbox

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
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
import com.tilicho.flexchatbox.utils.getImageUri
import com.tilicho.flexchatbox.utils.getLocation
import com.tilicho.flexchatbox.utils.getMediaType
import com.tilicho.flexchatbox.utils.getThumbnail
import com.tilicho.flexchatbox.utils.isLocationEnabled
import com.tilicho.flexchatbox.utils.navigateToAppSettings
import com.tilicho.flexchatbox.utils.openFiles
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalPermissionsApi::class)
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

    var audioFilePath by remember {
        mutableStateOf<String?>(String.empty())
    }

    var isRecording by remember {
        mutableStateOf(false)
    }

    var sendIconState by remember {
        mutableStateOf(R.drawable.ic_send)
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

    var isPermissionPermanentlyDenied by remember { mutableStateOf(false) }

    if (isPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
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
                    galleryList = galleryList!!,
                    galleryLauncher = galleryLauncher,
                    onDismissCallback = { dismissDialog, setImages ->
                        showDialog = dismissDialog
                        if (setImages) {
                            selectedPhotosOrVideos(galleryList!!)
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
                    val count = activityResult.data?.clipData?.itemCount
                    var currentItem = 0
                    while (currentItem < count!!) {
                        activityResult.data!!.clipData?.getItemAt(currentItem)?.uri
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
                isPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                galleryLauncher.launch(GALLERY_INPUT_TYPE)
                isPermissionPermanentlyDenied = false
                showDialog = false
            }
        }

    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.CAMERA
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                cameraIntent(cameraLauncher)
                isPermissionPermanentlyDenied = false
                showDialog = false
            }
        }

    val locationPermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                if (!isLocationEnabled(context)) {
                    Toast.makeText(context,  R.string.enable_location, Toast.LENGTH_LONG).show()
                } else {
                    val currLocation = getLocation(context)
                    val latLong =
                        (currLocation?.latitude).toString() + "," + (currLocation?.longitude).toString()
                    location = Location(
                        currLocation,
                        LOCATION_URL + latLong
                    )
                    currentLocation(location)
                    isPermissionPermanentlyDenied = false
                    showDialog = false
                }
            }
        }

    val contactsPermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.READ_CONTACTS
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                val data = getContacts(context)
                contacts = data
                displayContacts = true
                isPermissionPermanentlyDenied = false
                showDialog = false
            }
        }

    val filesPermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                openFiles(context, fileLauncher)
                isPermissionPermanentlyDenied = false
                showDialog = false
            }
        }

    val recordAudioPermissionState =
        rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.RECORD_AUDIO
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isPermissionPermanentlyDenied = false
                showDialog = false
            }
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isRecording) {
            sendIconState = R.drawable.ic_recorder
            AudioRecordingUi()
        } else {
            sendIconState = R.drawable.ic_send
            TextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.hint),
                        color = colorResource(id = R.color.c_placeholder),
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.opensans_regular))
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .weight(4f)
                    .border(
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacing_20)),
                        border = BorderStroke(1.dp, color = Color.Black)
                    ),
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        }

        Row(
            modifier = Modifier
                .weight(2f)
                .padding(dimensionResource(id = R.dimen.spacing_10)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            if (true) {
                when (source) {
                    Sources.GALLERY -> {
                        SourceImage(icon = R.drawable.ic_gallery,
                            isDenied = isPermissionPermanentlyDenied,
                            onClickIcon = {
                                galleryPermissionState.launchPermissionRequest()
                            })
                    }

                    Sources.LOCATION -> {
                        SourceImage(icon = R.drawable.ic_location,
                            isDenied = isPermissionPermanentlyDenied,
                            onClickIcon = {
                                locationPermissionState.launchPermissionRequest()
                            })
                    }

                    Sources.VOICE -> {
                        var iconState by remember {
                            mutableStateOf(Color.Transparent)
                        }
                        if (isPermissionPermanentlyDenied) {
                            iconState = Color(0xffEBEEF1)
                        }

                        var pressedX = 0F
                        var pressedY = 0F
                        var fileName by remember {
                            mutableStateOf("")
                        }
                        var isPressed by remember {
                            mutableStateOf(false)
                        }

                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_mic),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(dimensionResource(id = R.dimen.spacing_20))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            if (checkPermission(context,
                                                    Manifest.permission.RECORD_AUDIO)
                                            ) {
                                                try {
                                                    context.cacheDir.deleteRecursively()
                                                    File(context.cacheDir, System.currentTimeMillis().toString() + "_audio.mp3").also {
                                                        recorder.start(it)
                                                        isRecording = true
                                                        audioFile = it
                                                    }
                                                    awaitRelease()
                                                } finally {
                                                    try {
                                                        recorder.stop()
                                                        isRecording = false
                                                        audioFile?.let { it1 ->
                                                            recordedAudio.invoke(it1)
                                                        }
                                                    } catch (_: Exception) {
                                                        // do nothing
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

                    Sources.CONTACTS -> {
                        SourceImage(
                            icon = R.drawable.ic_person,
                            isDenied = isPermissionPermanentlyDenied,
                            onClickIcon = {
                                contactsPermissionState.launchPermissionRequest()
                            })
                    }
                    Sources.FILES -> {
                        SourceImage(icon = R.drawable.ic_file,
                            isDenied = isPermissionPermanentlyDenied,
                            onClickIcon = {
                                filesPermissionState.launchPermissionRequest()
                            })
                    }
                    Sources.CAMERA -> {
                        SourceImage(icon = R.drawable.ic_camera,
                            isDenied = isPermissionPermanentlyDenied) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
                    else -> {}
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clickable(onClick = {
                    onClickSend.invoke(textFieldValue)
                    textFieldValue = String.empty()
                })
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(sendIconState),
                    contentDescription = null,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_20))
                )
            }
        }
    }
}

@Composable
fun SourceImage(icon: Int, isDenied: Boolean, onClickIcon: () -> Unit) {
    var iconState by remember {
        mutableStateOf(Color.Transparent)
    }
    if (isDenied) {
        iconState = colorResource(id = R.color.c_ebeef1)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(onClick = {
                onClickIcon.invoke()
            })
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            modifier = Modifier
                .background(color = iconState)
                .padding(dimensionResource(id = R.dimen.spacing_20)),
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
                    .padding(vertical = dimensionResource(id = R.dimen.spacing_10dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.spacing_10dp))
                        .padding(start = dimensionResource(id = R.dimen.spacing_10dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.contacts),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.opensans_regular)))
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        selectedContactsCallBack.invoke(selectedContacts)
                    }) {
                        Text(text = stringResource(id = R.string.send),
                            fontFamily = FontFamily(Font(R.font.opensans_regular)))
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_50)))

                LazyColumn(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.spacing_10dp))) {
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
                                    Text(text = it,
                                        fontFamily = FontFamily(Font(R.font.opensans_regular)))
                                }
                                contact.mobileNumber?.let {
                                    Text(text = it,
                                        fontFamily = FontFamily(Font(R.font.opensans_regular)))
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                            )
                        }
                        Divider()
                    }
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
            .wrapContentWidth()
            .width(230.dp)
            .height(dimensionResource(id = R.dimen.spacing_90))
            .padding(horizontal = dimensionResource(id = R.dimen.spacing_10dp))
            .padding(top = dimensionResource(id = R.dimen.spacing_10dp))
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete_new),
            contentDescription = null,
            tint = Color.Red
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10dp)))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.recording_audio),
                color = Color.Red,
                fontFamily = FontFamily(Font(R.font.opensans_regular)))
            Text(text = stringResource(id = R.string.swipe_to_cancel),
                fontSize = 12.sp,
                color = Color.Red,
                fontFamily = FontFamily(Font(R.font.opensans_regular)))
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10dp)))

        if (seconds < 10) {
            Text(text = "0$minutes:0$seconds",
                color = Color.Red,
                fontFamily = FontFamily(Font(R.font.opensans_regular)))
        } else {
            Text(text = "0$minutes:$seconds",
                color = Color.Red,
                fontFamily = FontFamily(Font(R.font.opensans_regular)))
        }
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

            if (previewUriList?.size!! > 1) {
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
                                color = if (previewUri == item) colorResource(id = R.color.c_0096ff) else Color.Transparent
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
                                        color = if (previewUri == item) colorResource(id = R.color.c_0096ff) else Color.Transparent
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
                    )
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
        AndroidView(modifier = Modifier.height(dimensionResource(id = R.dimen.preview_image_height)),
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
        Card(shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacing_40)),
            border = BorderStroke(
                dimensionResource(id = R.dimen.spacing_00), color = colorResource(
                    id = R.color.c_c0e5ff))) {
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_30))) {
                Text(text = stringResource(id = R.string.permission_denied),
                    fontFamily = FontFamily(Font(R.font.opensans_regular)))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_20)))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                    Text(text = stringResource(id = R.string.cancel),
                        color = Color.Blue,
                        fontFamily = FontFamily(Font(R.font.opensans_regular)),
                        modifier = Modifier.clickable {
                            onDismissCallback(false)
                        })

                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_40)))

                    Text(text = stringResource(id = R.string.settings),
                        fontFamily = FontFamily(Font(R.font.opensans_regular)),
                        color = Color.Blue,
                        modifier = Modifier.clickable {
                            onDismissCallback(false)
                            context.navigateToAppSettings()
                        })
                }
            }
        }
    }
}