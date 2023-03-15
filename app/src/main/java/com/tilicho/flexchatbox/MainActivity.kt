package com.tilicho.flexchatbox

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Patterns
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.core.text.util.LinkifyCompat
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.tilicho.flexchatbox.enums.Sources
import com.tilicho.flexchatbox.ui.theme.FlexChatBoxTheme
import com.tilicho.flexchatbox.utils.ContactData
import com.tilicho.flexchatbox.utils.getCurrentPositionInMmSs
import com.tilicho.flexchatbox.utils.getDurationInMmSs
import com.tilicho.flexchatbox.utils.getThumbnail
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = this@MainActivity
            var cameraImages by remember {
                mutableStateOf(Uri.EMPTY)
            }

            var cameraVideos by remember {
                mutableStateOf(Uri.EMPTY)
            }

            var source by remember {
                mutableStateOf(Sources.CAMERA)
            }

            var textFieldValue by remember {
                mutableStateOf("")
            }

            var listOfLocations by remember {
                mutableStateOf(mutableListOf<Location>())
            }

            var location by remember {
                mutableStateOf<com.tilicho.flexchatbox.Location?>(null)
            }

            var galleryUriList by remember {
                mutableStateOf<MutableList<Uri>?>(null)
            }
            var galleryItemsUriList by remember {
                mutableStateOf<MutableList<Uri>?>(null)
            }

            var galleryVideoUri by remember {
                mutableStateOf<Uri?>(null)
            }

            var mediaPlayers by remember {
                mutableStateOf(mutableListOf<MediaPlayer>())
            }
            var flexItemsDialog by remember {
                mutableStateOf(false)
            }
            var selectedFlex by remember {
                mutableStateOf(Sources.CAMERA)
            }
            var contacts: MutableList<ContactData> by remember { mutableStateOf(mutableListOf()) }

            var setAudioPlayerState by remember {
                mutableStateOf(false)
            }

            var displayText by remember {
                mutableStateOf(false)
            }
            FlexChatBoxTheme {

                /*Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { flexItemsDialog = true }) {
                        Text(text = "Select Flex")
                    }
                    if (flexItemsDialog) {
                        DisplayFlexItems(selectedFlex = {
                            selectedFlex = it
                        }, setFlexItemDialog = {
                            flexItemsDialog = it
                        })
                    }

                    var displayState by remember {
                        mutableStateOf(false)
                    }
                    if (displayState) {
                        DisplayContacts(contacts = contacts)
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    if (selectedFlex == Sources.LOCATION) {
                        DisplayLocation(location = location)
                    } else if (selectedFlex == Sources.VOICE) {
                        AudioPlayer(mediaPlayer = mediaPlayer)
                    } else if (selectedFlex == Sources.CAMERA) {
                        DisplayImage(image = imageUri)
                    } else if (selectedFlex == Sources.CONTACTS) {
                        Column(modifier = Modifier.padding(bottom = 300.dp)) {
                            Button(onClick = {
                                displayState = true
                            }) {
                                Text(text = "Display contacts")
                            }
                        }


                    }



                    ChatBox(
                        context = this@MainActivity,
                        source = selectedFlex,
                        cameraImage = { uri ->
                            imageUri = uri
                        },
                        onClickSend = { inputValue, _location ->
                            textFieldValue = inputValue
                            location = _location
                        },
                        selectedPhotosOrVideos = { uriList ->
                            galleryUriList = uriList.toMutableStateList()
                        },
                        recordedAudio = {
                            MediaPlayer.create(this@MainActivity, it.toUri()).apply {
                                mediaPlayer = this
                            }
                        },
                        selectedContactsCallBack = {
                            contacts = it
                        }
                    )

                }*/

                /*val singapore = LatLng(1.35, 103.87)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(singapore, 10f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = singapore),
                        title = "Singapore",
                        snippet = "Marker in Singapore"
                    )
                }*/


                Scaffold(topBar = {
                    DisplayFlexItems(selectedFlex = {
                        selectedFlex = it
                    }, setFlexItemDialog = {
                        true
                    })
                }, bottomBar = {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .padding(start = 5.dp)
                    ) {
                        ChatBox(
                            context = context,
                            source = selectedFlex,
                            selectedPhotosOrVideos = {
                                galleryItemsUriList = it.toMutableStateList()
                            },
                            recordedAudio = {
                                MediaPlayer.create(this@MainActivity, it.toUri()).apply {
                                    val updatedMediaPlayers = mediaPlayers.toMutableList()
                                    updatedMediaPlayers.add(this)
                                    mediaPlayers = updatedMediaPlayers
                                    setAudioPlayerState = true
                                }
                            },
                            onClickSend = { it1, it2 ->
                                textFieldValue = it1
                                displayText = true
                                it2.let {
                                    if (it != null) {
                                        location = it
                                    }
                                }
                            },
                            selectedContactsCallBack = {
                                val currContactList = it.toMutableList()
                                currContactList.addAll(currContactList.size - 1, contacts)

                                contacts = currContactList
                            },
                            selectedFiles = {
                                galleryItemsUriList = it.toMutableList()
                            },
                            camera = { _source, uri ->
                                source = _source
                                if (_source == Sources.CAMERA) {
                                    cameraImages = uri
                                } else {
                                    cameraVideos = uri
                                }
                            }
                        )
                    }
                }) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .padding(it)
                    ) {
                        when (selectedFlex) {
                            Sources.CONTACTS -> {
                                DisplayContacts(contacts = contacts, chatText = textFieldValue)
                            }
                            Sources.CAMERA -> {
                                if (source == Sources.CAMERA) {
                                    Image(
                                        painter = rememberImagePainter(data = cameraImages),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(150.dp)
                                    )
                                } else {
                                    var setPreviewDialog by remember {
                                        mutableStateOf(false)
                                    }

                                    if (setPreviewDialog) {
                                        Dialog(onDismissRequest = { setPreviewDialog = false }) {
                                            Column(
                                                modifier = Modifier
                                                    .size(300.dp)
                                                    .clickable(onClick = {
                                                        setPreviewDialog = false
                                                    })
                                            ) {
                                                VideoView(videoUri = cameraVideos.toString())
                                            }
                                        }
                                    }

                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = rememberImagePainter(data = getThumbnail(context,
                                                cameraVideos)),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(150.dp)
                                                .clickable(onClick = {
                                                    setPreviewDialog = true
                                                })
                                        )
                                        Image(
                                            painter = rememberImagePainter(data = R.drawable.ic_play),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                        )
                                    }
                                }
                            }
                            Sources.VOICE -> {
                                if (setAudioPlayerState) {
                                    AudioPlayer(mediaPlayers = mediaPlayers)
                                }
                            }
                            Sources.LOCATION -> {
                                location?.let { it1 ->
                                    DisplayLocation(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                        it1
                                    )
                                }
                            }
                            Sources.GALLERY -> {
                                DisplayGalleryItems(context, galleryItemsUriList)
                            }
                            Sources.FILES -> {
                                DisplayFileItems(context = context,
                                    galleryItemsUriList = galleryItemsUriList)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayChatText(text: String) {
    Card(modifier = Modifier.width(200.dp)) {
        Text(text = text, fontSize = 16.sp, modifier = Modifier.padding(10.dp))
    }
}

@Composable
fun DisplayGalleryItems(context: Context, galleryItemsUriList: MutableList<Uri>?) {
    var setPreviewDialog by remember {
        mutableStateOf(false)
    }
    var selectedGalleryItem by remember {
        mutableStateOf<Uri?>(null)
    }
    var mediaType by remember {
        mutableStateOf<MediaType?>(null)
    }

    if (setPreviewDialog) {
        selectedGalleryItem?.let {
            GalleryItemPreview(mediaType, mediaItem = it) {
                setPreviewDialog = it
            }
        }
    }
    LazyColumn {
        if (galleryItemsUriList != null) {
            items(galleryItemsUriList.size) {
                val galleryItem = galleryItemsUriList[it]
                val mediaTypeRaw = galleryItem.let { context.contentResolver.getType(it) }
                if (mediaTypeRaw?.startsWith("video") == true) {
                    mediaType = MediaType.MediaTypeVideo
                    val videoThumbnail = getThumbnail(context = context, galleryItem)
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = rememberImagePainter(data = videoThumbnail),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clickable(onClick = {
                                    selectedGalleryItem = galleryItem
                                    setPreviewDialog = true
                                })
                        )
                        Image(
                            painter = rememberImagePainter(data = R.drawable.ic_play),
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                        )
                    }

                } else {
                    mediaType = MediaType.MediaTypeImage
                    Image(
                        painter = rememberImagePainter(data = galleryItem),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clickable(onClick = {
                                selectedGalleryItem = galleryItem
                                setPreviewDialog = true
                            })
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun GalleryItemPreview(
    mediaType: MediaType?,
    mediaItem: Uri,
    setGalleryPreview: (Boolean) -> Unit,
) {
    Dialog(onDismissRequest = { setGalleryPreview(false) }) {
        if (mediaType == MediaType.MediaTypeImage) {
            Column(modifier = Modifier.size(300.dp)) {
                Image(
                    painter = rememberImagePainter(data = mediaItem),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clickable(onClick = {
                            setGalleryPreview.invoke(false)
                        })
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .size(300.dp)
                    .clickable(onClick = {
                        setGalleryPreview.invoke(false)
                    })
            ) {
                VideoView(videoUri = mediaItem.toString())
            }
        }
    }
}

@Composable
fun VideoView(videoUri: String) {
    val context = LocalContext.current

    val exoPlayer = ExoPlayer.Builder(LocalContext.current)
        .build()
        .also { exoPlayer ->
            val mediaItem = MediaItem.Builder()
                .setUri(videoUri)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }

    DisposableEffect(
        AndroidView(factory = {
            StyledPlayerView(context).apply {
                player = exoPlayer
            }
        })
    ) {
        onDispose { exoPlayer.release() }
    }
}


@Composable
fun AudioPlayer(mediaPlayers: MutableList<MediaPlayer>) {
    LazyColumn {
        items(mediaPlayers.size) { index ->
            var durationScale by remember {
                mutableStateOf(mediaPlayers[index].getDurationInMmSs())
            }
            var isPlaying by remember {
                mutableStateOf(false)
            }
            val handler = Handler()

            Card(
                border = BorderStroke(width = Dp.Hairline, color = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_recorder),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                    )

                    mediaPlayers[index].setOnCompletionListener {
                        isPlaying = false
                        durationScale = mediaPlayers[index].getDurationInMmSs()
                    }

                    Text(text = "Audio $durationScale")

                    if (!isPlaying) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable(onClick = {
                                    mediaPlayers[index].start()
                                    object : Runnable {
                                        override fun run() {
                                            durationScale =
                                                mediaPlayers[index].getCurrentPositionInMmSs()
                                            handler.postDelayed(this, 1000)
                                        }
                                    }.run()
                                    isPlaying = true
                                })
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_pause),
                            contentDescription = "",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable(onClick = {
                                    mediaPlayers[index].pause()
                                    isPlaying = false
                                })
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

    }
}

@Composable
fun DisplayImage(images: MutableList<Uri>?) {
    LazyColumn() {
        images?.size?.let { it ->
            items(it) {
                Image(
                    painter = rememberImagePainter(data = images[it]),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun DisplayContacts(contacts: List<ContactData>, chatText: String) {
    LazyColumn(horizontalAlignment = Alignment.End) {
        items(contacts.size) {
            Card(elevation = 2.dp) {
                Column(modifier = Modifier.padding(10.dp)) {
                    contacts[it].name?.let { it1 -> Text(text = it1) }
                    contacts[it].mobileNumber?.let { it1 -> Text(text = it1) }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

        }
        if (chatText.isNotEmpty()) {
            item {
                Card(elevation = 2.dp) {
                    Text(text = chatText, modifier = Modifier.padding(10.dp))
                }

            }
        }
    }
}

@Composable
fun DisplayFlexItems(
    selectedFlex: (Sources) -> Unit,
    setFlexItemDialog: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(top = 10.dp, start = 10.dp)
            .background(color = Color.White, shape = RoundedCornerShape(10.dp))
    ) {
        Image(imageVector = ImageVector.vectorResource(R.drawable.ic_camera),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .clickable(onClick = {
                    selectedFlex.invoke(Sources.CAMERA)
                    setFlexItemDialog.invoke(false)
                }
                ))
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_mic),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .clickable(onClick = {
                    selectedFlex.invoke(Sources.VOICE)
                    setFlexItemDialog.invoke(false)
                }
                )
        )

        Image(imageVector = ImageVector.vectorResource(R.drawable.ic_location),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .clickable(onClick = {
                    selectedFlex.invoke(Sources.LOCATION)
                    setFlexItemDialog.invoke(false)
                }
                ))
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_gallery),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .clickable(onClick = {
                    selectedFlex.invoke(Sources.GALLERY)
                    setFlexItemDialog.invoke(false)
                })
        )
        Image(
            imageVector = (Icons.Filled.Person),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .clickable(onClick = {
                    selectedFlex.invoke(Sources.CONTACTS)
                    setFlexItemDialog.invoke(false)
                })
        )
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_file),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .clickable(onClick = {
                    selectedFlex.invoke(Sources.FILES)
                    setFlexItemDialog.invoke(false)
                })
        )
    }

}

@Composable
fun DisplayFileItems(context: Context, galleryItemsUriList: MutableList<Uri>?) {
    LazyColumn {
        galleryItemsUriList?.let {
            itemsIndexed(galleryItemsUriList) { _, item ->
                Box(contentAlignment = Alignment.BottomStart,
                    modifier = Modifier
                        .border(shape = RoundedCornerShape(10.dp),
                            width = 1.dp,
                            color = Color.Black)
                        .wrapContentWidth().wrapContentHeight()) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)) {
                        Image(imageVector = ImageVector.vectorResource(id = R.drawable.ic_uploaded_file),
                            contentDescription = null)

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            val file = File(item.toString())
                            var type = ""
                            var fileSize = 0L
                            context.let {
                                val cR: ContentResolver = it.contentResolver
                                val mime = MimeTypeMap.getSingleton()
                                type = mime.getExtensionFromMimeType(cR.getType(item)).toString()
                                val fileDescriptor: AssetFileDescriptor? =
                                    it.contentResolver.openAssetFileDescriptor(item, "r")
                                fileSize = fileDescriptor?.length!!
                            }
                            val fileName = file.name + "." + type
                            Text(text = fileName)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = fileSize.toString() + "kb")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun DisplayLocation(modifier: Modifier = Modifier, location: com.tilicho.flexchatbox.Location) {
    val context = LocalContext.current
    val customLinkifyTextView = remember {
        TextView(context)
    }
    Card(modifier = Modifier.border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(10.dp)).width(300.dp)) {
        Column {
            Row(modifier = Modifier
                .fillMaxWidth()
                .width(300.dp), horizontalArrangement = Arrangement.End) {
                AndroidView(modifier = modifier, factory = { customLinkifyTextView }) { textView ->
                    textView.text = location.url
                    LinkifyCompat.addLinks(textView, Linkify.ALL)
                    Linkify.addLinks(textView, Patterns.PHONE, "tel:",
                        Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter)
                    textView.movementMethod = LinkMovementMethod.getInstance()
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(text = location.location.toString(), textAlign = TextAlign.Center)
            }
        }
    }
}
