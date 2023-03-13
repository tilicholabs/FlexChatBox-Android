package com.tilicho.flexchatbox

import android.Manifest
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tilicho.flexchatbox.audiorecorder.AndroidAudioRecorder
import com.tilicho.flexchatbox.enums.Sources
import com.tilicho.flexchatbox.utils.ContactData
import com.tilicho.flexchatbox.utils.GALLERY_INPUT_TYPE
import com.tilicho.flexchatbox.utils.GetMediaActivityResultContract
import com.tilicho.flexchatbox.utils.checkPermission
import com.tilicho.flexchatbox.utils.generateUri
import com.tilicho.flexchatbox.utils.getContacts
import com.tilicho.flexchatbox.utils.getLocation
import com.tilicho.flexchatbox.utils.requestPermission
import com.tilicho.flexchatbox.utils.showToast
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatBox(
    context: Context,
    source: Sources,
    cameraImage: (Uri) -> Unit,
    selectedPhotosOrVideos: (List<Uri>) -> Unit,
    recordedAudio: (File) -> Unit,
    onClickSend: (String, Location?) -> Unit,
    selectedContactsCallBack: (List<ContactData>) -> Unit,
) {
    var textFieldValue by rememberSaveable { mutableStateOf("") }
    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    var location by remember {
        mutableStateOf<Location?>(null)
    }

    val recorder by lazy {
        AndroidAudioRecorder(context)
    }
    var audioFile: File? = null

    var isPressed by remember {
        mutableStateOf(false)
    }

    var isRecording by remember {
        mutableStateOf(false)
    }

    var isDeniedPermission by remember {
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

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        cameraImage.invoke(capturedImageUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(GetMediaActivityResultContract()) {
        selectedPhotosOrVideos.invoke(it)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isDeniedPermission = false
            when (source) {
                Sources.CAMERA -> {
                    capturedImageUri = generateUri(context)
                    cameraLauncher.launch(capturedImageUri)
                }
                Sources.GALLERY -> {
                    galleryLauncher.launch(GALLERY_INPUT_TYPE)
                }
                Sources.LOCATION -> {
                    location = getLocation(context)
                }
                Sources.VOICE -> {
                }
                Sources.CONTACTS -> {
                    contacts = getContacts(context)
                    displayContacts = true
                }
            }
        } else {
            isDeniedPermission = true
            showToast(context.applicationContext, context.getString(R.string.permission_denied))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isRecording) {
            AudioRecordingUi()
        } else {
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
                modifier = Modifier
                    .weight(4f)
                    .border(
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, color = Color.Black)
                    ),
                singleLine = false,
                maxLines = 5,
            )
        }

        Row(
            modifier = Modifier
                .weight(2f)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            if (true) {
                when (source) {
                    Sources.CAMERA -> {
                        SourceImage(icon = R.drawable.ic_camera, isDeniedPermission, onClickIcon = {
                            // Check whether permission is granted or not, if not request permission
                            if (checkPermission(context, Manifest.permission.CAMERA)) {
                                capturedImageUri = generateUri(context)
                                cameraLauncher.launch(capturedImageUri)
                            } else {
                                requestPermission(permissionLauncher, Manifest.permission.CAMERA)
                            }
                        })
                    }

                    Sources.GALLERY -> {
                        SourceImage(icon = R.drawable.ic_gallery,
                            isDenied = isDeniedPermission,
                            onClickIcon = {
                                // Check whether permission is granted or not, if not request permission
                                if (checkPermission(
                                        context, Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                ) {
                                    galleryLauncher.launch(GALLERY_INPUT_TYPE)
                                } else {
                                    requestPermission(
                                        permissionLauncher,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                }
                            })
                    }

                    Sources.LOCATION -> {
                        SourceImage(icon = R.drawable.ic_location,
                            isDenied = isDeniedPermission,
                            onClickIcon = {
                                if (checkPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                ) {
                                    location = getLocation(context)
                                    onClickSend.invoke("", location)
                                } else {
                                    requestPermission(
                                        permissionLauncher, Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                }
                            })
                    }

                    Sources.VOICE -> {
                        var iconState by remember {
                            mutableStateOf(Color.Transparent)
                        }
                        if (isDeniedPermission) {
                            iconState = Color(0xffEBEEF1)
                        }

                        var offsetX by remember { mutableStateOf(0f) }
                        var offsetY by remember { mutableStateOf(0f) }
                        Box(
                            contentAlignment = Alignment.Center,
                            /*modifier = Modifier
                                .offset {
                                    IntOffset(
                                        offsetX.roundToInt(),
                                        offsetY.roundToInt()
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures() { change, dragAmount ->
                                        change.consume()
                                        val (x, y) = dragAmount
                                        when {
                                            x < 0 -> {
                                                recorder.stop()
                                            }
                                        }
                                    }
                                }*/
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_mic),
                                contentDescription = null,
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            offsetX.roundToInt(),
                                            offsetY.roundToInt()
                                        )
                                    }
                                    .padding(dimensionResource(id = R.dimen.spacing_20))
                                    .pointerInput(Unit) {
                                        detectTapGestures(onLongPress = {
                                            when {
                                                offsetX < it.x -> {
                                                    Log.d("x_1", it.x.toString())
                                                    Log.d("x_2", it.y.toString())
                                                }
                                                offsetX > it.y -> {

                                                }
                                            }

                                        },
                                            onPress = {
                                                isPressed = true
                                                if (checkPermission(
                                                        context, Manifest.permission.RECORD_AUDIO
                                                    )
                                                ) {
                                                    try {
                                                        File(context.cacheDir, "audio.mp3").also {
                                                            Handler(Looper.getMainLooper()).postDelayed(
                                                                {
                                                                    if (isPressed) {
                                                                        recorder.start(it)
                                                                        isRecording = true
                                                                    }
                                                                },
                                                                200
                                                            )
                                                            audioFile = it
                                                        }
                                                        awaitRelease()
                                                    } finally {
                                                        try {
                                                            isPressed = false
                                                            if (isRecording) {
                                                                recorder.stop()

                                                                audioFile?.let { it1 ->
                                                                    recordedAudio.invoke(
                                                                        it1
                                                                    )
                                                                }
                                                            }
                                                            isRecording = false
                                                        } catch (_: Exception) {
                                                            // do nothing
                                                        }
                                                    }
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            })
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val (x, y) = dragAmount
                                            when {
                                                x < 0 -> {
                                                    recorder.stop()
                                                }
                                                y > 0 -> {
                                                    recorder.stop()
                                                }
                                            }
                                            offsetX += dragAmount.x
                                            offsetY += dragAmount.y
                                        }
                                    }
                            )
                        }


                    }
                    Sources.CONTACTS -> {
                        SourceImage(
                            icon = R.drawable.baseline_person_outline_24,
                            isDenied = isDeniedPermission
                        ) {
                            if (checkPermission(context, Manifest.permission.READ_CONTACTS)) {
                                val data = getContacts(context)
                                contacts = data
                                displayContacts = true
                            } else {
                                requestPermission(
                                    permissionLauncher, Manifest.permission.READ_CONTACTS
                                )
                            }
                        }
                    }
                }
            }
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = {
                    onClickSend.invoke(textFieldValue, null)
                    textFieldValue = String.empty()
                })
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_send),
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
        iconState = Color(0xffEBEEF1)
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


private fun String.Companion.empty() = ""


@Composable
fun DisplayContacts(
    contacts: List<ContactData>, selectedContactsCallBack: (List<ContactData>) -> Unit
) {

    val selectedContacts: MutableList<ContactData> = mutableListOf()
    Dialog(
        onDismissRequest = {
            selectedContactsCallBack.invoke(emptyList())
        }, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            color = Color(0xffffffff),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Contacts", textAlign = TextAlign.Center, fontSize = 20.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        selectedContactsCallBack.invoke(selectedContacts)
                    }) {
                        Text(text = "Send")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                    items(contacts.size) { index ->
                        Spacer(modifier = Modifier.height(2.dp))
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
                                .padding(10.dp)
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
                            Column() {
                                contact.name?.let { Text(text = it) }
                                contact.mobileNumber?.let { Text(text = it) }
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
            .height(50.dp)
            .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_recorder),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Recording audio")
        Spacer(modifier = Modifier.width(10.dp))

        if (seconds < 10) {
            Text(text = "0$minutes:0$seconds")
        } else {
            Text(text = "0$minutes:$seconds")
        }
    }
}