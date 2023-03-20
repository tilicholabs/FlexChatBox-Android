package com.tilicho.flexchatbox

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.util.Patterns
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
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
import com.tilicho.flexchatbox.audioPlayer.AndroidAudioPlayer
import com.tilicho.flexchatbox.enums.Sources
import com.tilicho.flexchatbox.ui.theme.FlexChatBoxTheme
import com.tilicho.flexchatbox.uimodel.Camera
import com.tilicho.flexchatbox.uimodel.ChatDataModel
import com.tilicho.flexchatbox.uimodel.Contacts
import com.tilicho.flexchatbox.uimodel.FileItems
import com.tilicho.flexchatbox.uimodel.GalleryItems
import com.tilicho.flexchatbox.uimodel.LocationItem
import com.tilicho.flexchatbox.uimodel.Video
import com.tilicho.flexchatbox.uimodel.Voice
import com.tilicho.flexchatbox.utils.ContactData
import com.tilicho.flexchatbox.utils.getCurrentPositionInMmSs
import com.tilicho.flexchatbox.utils.getDurationInMmSs
import com.tilicho.flexchatbox.utils.getThumbnail
import java.io.File
import java.util.*

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = this@MainActivity

            var source by remember {
                mutableStateOf(Sources.CAMERA)
            }

            var chatData by remember {
                mutableStateOf<MutableList<ChatDataModel>?>(mutableListOf())
            }

            var galleryItemsUriList by remember {
                mutableStateOf<MutableList<Uri>?>(null)
            }

            var mediaPlayer by remember {
                mutableStateOf(MediaPlayer())
            }

            var file by remember {
                mutableStateOf<File?>(null)
            }

            var selectedFlex by remember {
                mutableStateOf(Sources.CAMERA)
            }

            FlexChatBoxTheme {
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
                                val currData =
                                    mutableListOf(ChatDataModel(galleryItems = GalleryItems(uris = it.toMutableList())))
                                chatData?.let { it1 -> currData.addAll(currData.size - 1, it1) }
                                chatData = currData
                            },
                            recordedAudio = {
                                val currData =
                                    mutableListOf(ChatDataModel(voice = Voice(file = it))).toMutableList()
                                chatData?.let { it1 -> currData.addAll(currData.size - 1, it1) }
                                chatData = currData
                                /*MediaPlayer.create(this@MainActivity, it.toUri()).apply {
                                    file = it
                                    mediaPlayer = this
                                }*/
                            },
                            onClickSend = { it ->
                                it.let {
                                    if (it != "") {
                                        val currData =
                                            mutableListOf(ChatDataModel(textFieldValue = it)).toMutableList()
                                        chatData?.let { it1 ->
                                            currData.addAll(
                                                currData.size - 1,
                                                it1
                                            )
                                        }
                                        chatData = currData

                                    }
                                }
                            },
                            currentLocation = {
                                it.let {
                                    val currData =
                                        mutableListOf(
                                            ChatDataModel(
                                                location = LocationItem(location = it)
                                            )
                                        ).toMutableList()
                                    chatData?.let { it3 -> currData.addAll(currData.size - 1, it3) }
                                    chatData = currData
                                }
                            },
                            selectedContactsCallBack = {
                                val currData =
                                    mutableListOf(ChatDataModel(contacts = Contacts(contacts = it.toMutableList())))
                                chatData?.let { it1 -> currData.addAll(currData.size - 1, it1) }
                                chatData = currData
                            },
                            selectedFiles = {
                                val currData =
                                    mutableListOf(ChatDataModel(file = FileItems(files = it.toMutableList())))
                                chatData?.let { it1 -> currData.addAll(currData.size - 1, it1) }
                                chatData = currData
                            },
                            camera = { _source, uri ->
                                source = _source
                                if (_source == Sources.CAMERA) {
                                    val currData =
                                        mutableListOf(ChatDataModel(camera = Camera(uri = uri))).toMutableList()
                                    chatData?.let { currData.addAll(currData.size - 1, it) }
                                    chatData = currData
                                } else {
                                    val currData =
                                        mutableListOf(ChatDataModel(video = Video(uri = uri))).toMutableList()
                                    chatData?.let { currData.addAll(currData.size - 1, it) }
                                    chatData = currData

                                }
                            }
                        )
                    }
                }) {
                    Column(modifier = Modifier.padding(it)) {
                        chatData?.let { it1 -> ChatUI(context = context, chatData = it1) }
                    }
                }
            }
        }
    }

    @Composable
    fun ChatUI(context: Context, chatData: List<ChatDataModel>) {
        LazyColumn(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            for (chatItem in chatData) {
                if (chatItem.contacts?.sourceType == Sources.CONTACTS) {
                    item {
                        val contacts = chatItem.contacts.contacts
                        SetContactItemCell(contacts)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else if (chatItem.galleryItems?.sourceType == Sources.GALLERY) {
                    val galleryItemsUriList = chatItem.galleryItems.uris
                    if (galleryItemsUriList != null) {
                        items(galleryItemsUriList.size) {
                            val galleryItem = galleryItemsUriList[it]
                            SetGalleryItemCell(context = context, galleryItem = galleryItem)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                } else if (chatItem.location?.sourceType == Sources.LOCATION) {
                    item {
                        val location = chatItem.location.location
                        SetLocationItemCell(context = context, location = location)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else if (chatItem.voice?.sourceType == Sources.VOICE) {
                    item {
                        val audioFile = chatItem.voice.file
                        SetVoiceItemCell(context = context, audioFile = audioFile)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else if (chatItem.file?.sourceType == Sources.FILES) {
                    val fileItemsUriList = chatItem.file.files
                    fileItemsUriList?.let {
                        items(fileItemsUriList.size) { index ->
                            val fileItem = fileItemsUriList[index]
                            SetFileItemCell(context = context, fileItem = fileItem)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                } else if (chatItem.camera?.sourceType == Sources.CAMERA) {
                    val cameraImage = chatItem.camera.uri
                    item {
                        SetCameraPictureItemCell(cameraImage)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else if (chatItem.video?.sourceType == Sources.VIDEO) {
                    val video = chatItem.video.uri
                    item {
                        SetCameraVideoItemCell(context = context, video = video)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else {
                    val text = chatItem.textFieldValue
                    item {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        )
                        {
                            text?.let {
                                Text(
                                    text = it,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SetContactItemCell(contacts: List<ContactData>?) {
        Log.d("contacts", contacts.toString())
        if (contacts?.isNotEmpty() == true && contacts.size <= 1) {
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, color = Color.Black)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    )
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_person),
                        contentDescription = "",
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column() {
                        contacts[0].name?.let { it1 -> Text(text = it1) }
                        contacts[0].mobileNumber?.let { it1 -> Text(text = it1) }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

            }
        } else if (contacts?.isNotEmpty() == true) {
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, color = Color.Black)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 5.dp
                    )
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_group),
                        contentDescription = "",
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Contact 1 and ${contacts.size - 1} \n other contacts")
                }
            }
        }
    }

    @Composable
    fun SetGalleryItemCell(context: Context, galleryItem: Uri) {
        val mediaTypeRaw = galleryItem.let { context.contentResolver.getType(it) }
        if (mediaTypeRaw?.startsWith("video") == true) {
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
                        VideoView(
                            context = context,
                            videoUri = galleryItem.toString()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            val videoThumbnail = getThumbnail(context = context, galleryItem)
            Box(contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, color = Color.Black)
                ) {
                    Image(
                        painter = rememberImagePainter(data = videoThumbnail),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(10.dp)
                            .clickable(onClick = {
                                setPreviewDialog = true
                            })
                    )
                }
                Image(
                    painter = rememberImagePainter(data = R.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                )
            }

        } else {
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, color = Color.Black)
            ) {
                Image(
                    painter = rememberImagePainter(data = galleryItem),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(150.dp)
                        .clickable(onClick = {

                        })
                )
            }
        }
    }

    @Composable
    fun SetLocationItemCell(context: Context, location: Location?) {
        val customLinkifyTextView = remember {
            TextView(context)
        }
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                )
                .width(300.dp)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(painterResource(id = R.drawable.image_map), contentDescription = null)
                Spacer(modifier = Modifier.width(20.dp))
                Text(text = "${location?.location?.longitude},${location?.location?.latitude}")
            }
            Spacer(modifier = Modifier.height(5.dp))
            Divider(modifier = Modifier.background(color = Color.Black))
            AndroidView(modifier = Modifier,
                factory = { customLinkifyTextView }) { textView ->
                textView.text = location?.url
                LinkifyCompat.addLinks(textView, Linkify.ALL)
                Linkify.addLinks(
                    textView,
                    Patterns.PHONE,
                    "tel:",
                    Linkify.sPhoneNumberMatchFilter,
                    Linkify.sPhoneNumberTransformFilter
                )
                textView.movementMethod = LinkMovementMethod.getInstance()
            }

        }
    }

    @Composable
    fun SetVoiceItemCell(context: Context, audioFile: File?) {
        val audioPlayer by lazy {
            AndroidAudioPlayer(context)
        }
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
                        .clickable(onClick = {

                        })
                )

                Text(text = "Audio 00:00")

                if (true) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = "",
                        modifier = Modifier
                            .clickable(onClick = {
                                if (audioFile != null) {
                                    audioPlayer.playFile(audioFile)
                                }
                            })
                            .size(30.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = "",
                        modifier = Modifier
                            .clickable(onClick = {
                                if (audioFile != null) {
                                    audioPlayer.stop()
                                }
                            })
                            .size(30.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun SetFileItemCell(context: Context, fileItem: Uri) {
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .border(
                    shape = RoundedCornerShape(10.dp),
                    width = 1.dp,
                    color = Color.Black
                )
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_uploaded_file),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    val file = File(fileItem.toString())
                    var type = ""
                    var fileSize = 0L
                    context.let {
                        val cR: ContentResolver = it.contentResolver
                        val mime = MimeTypeMap.getSingleton()
                        type = mime.getExtensionFromMimeType(cR.getType(fileItem))
                            .toString()
                        val fileDescriptor: AssetFileDescriptor? =
                            it.contentResolver.openAssetFileDescriptor(fileItem, "r")
                        fileSize = fileDescriptor?.length!!
                    }
                    val fileName = file.name + "." + type
                    Text(text = fileName)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = (fileSize / 1024).toString() + " mb")
                }
            }
        }
    }


    @Composable
    fun SetCameraPictureItemCell(cameraImage: Uri?) {
        cameraImage?.let {
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, color = Color.Black)
            ) {
                Image(
                    painter = rememberImagePainter(data = cameraImage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .padding(10.dp)
                )
            }
        }
    }

    @Composable
    fun SetCameraVideoItemCell(context: Context, video: Uri?) {
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
                    VideoView(
                        context = context,
                        videoUri = video.toString()
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Box(contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, color = Color.Black)
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = video?.let {
                            getThumbnail(
                                context,
                                it
                            )
                        }
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(150.dp)
                        .clickable(onClick = {
                            setPreviewDialog = true
                        })
                )
            }
            Image(
                painter = rememberImagePainter(data = R.drawable.ic_play_grey),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
            )
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
    fun AudioPlayer(context: Context, file: File, mediaPlayer: MediaPlayer?) {
        var durationScale by remember {
            mutableStateOf(mediaPlayer?.getDurationInMmSs())
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
                mediaPlayer?.setOnCompletionListener {
                    isPlaying = false
                    durationScale = mediaPlayer.getDurationInMmSs()
                }

                Text(text = "Audio $durationScale")

                if (!isPlaying) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = "",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable(onClick = {
                                mediaPlayer?.start()
                                object : Runnable {
                                    override fun run() {
                                        try {
                                            durationScale =
                                                mediaPlayer
                                                    ?.getCurrentPositionInMmSs()
                                                    .toString()
                                        } catch (e: Exception) {
                                        }
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
                                mediaPlayer?.pause()
                                isPlaying = false
                            })
                    )
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
    fun DisplayLocation(modifier: Modifier = Modifier, location: com.tilicho.flexchatbox.Location) {
        val context = LocalContext.current
        val customLinkifyTextView = remember {
            TextView(context)
        }

        Column(modifier = Modifier
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(12.dp))
            .width(300.dp)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
                horizontalArrangement = Arrangement.Start) {
                Text(text = "${location.location?.longitude},${location.location?.latitude}")
            }

            Row(modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.End) {

                Column(
                    modifier = Modifier
                        .border(width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(12.dp))
                        .width(300.dp)
                        .padding(6.dp)
                        .wrapContentHeight()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Latitude: ${location.location?.latitude}", fontSize = 16.sp)
                        Text(text = "Longitude: ${location.location?.longitude}", fontSize = 16.sp)
                        Text(text = "Altitude: ${location.location?.altitude}", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        AndroidView(modifier = modifier,
                            factory = { customLinkifyTextView }) { textView ->
                            textView.text = location.url
                            LinkifyCompat.addLinks(textView, Linkify.ALL)
                            Linkify.addLinks(
                                textView, Patterns.PHONE, "tel:",
                                Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter
                            )
                            textView.movementMethod = LinkMovementMethod.getInstance()
                        }
                    }

                }
            }
        }
    }
}
