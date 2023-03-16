package com.tilicho.flexchatbox

import android.content.Context
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
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
import java.io.FileInputStream
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = this@MainActivity
            var images by remember {
                mutableStateOf(mutableListOf<Uri>())
            }
            var textFieldValue by remember {
                mutableStateOf("")
            }

            var listOfLocations by remember {
                mutableStateOf(mutableListOf<Location>())
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

            var mediaPlayer by remember {
                mutableStateOf<MediaPlayer?>(null)
            }

            var file by remember {
                mutableStateOf<File?>(null)
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
                            cameraImage = {
                                val updatedImages = images.toMutableList()
                                updatedImages.add(it)
                                images = updatedImages
                            },
                            selectedPhotosOrVideos = {
                                galleryItemsUriList = it.toMutableStateList()
                            },
                            recordedAudio = {
                                file = it
                                val myUri =  it.toUri()// initialize Uri here
                                if (mediaPlayer != null) {
                                    mediaPlayer?.release()
                                }
                                mediaPlayer = MediaPlayer().apply {
                                    setAudioAttributes(
                                        AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .build()
                                    )
//                                    setDataSource(applicationContext, myUri)
//                                    prepare()
                                }
                                val inputStream = FileInputStream(file)
                                mediaPlayer?.setDataSource(inputStream.getFD())
                                inputStream.close()

                                try {
                                    mediaPlayer!!.prepare()
                                } catch (e: IllegalStateException) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace()
                                }
                                /*MediaPlayer.create(context, it.toUri()).apply {
                                    mediaPlayer = this
                                    setAudioPlayerState = true
                                }*/


                                Log.d("sjknf", "${it.toUri().toString()} ${it.exists()}")

                                /*val mediaPlayer = MediaPlayer()
                                val fis = FileInputStream(it)
                                mediaPlayer.setDataSource(fis.getFD())
                                mediaPlayer.prepareAsync()
                                val updatedMediaPlayers = mediaPlayers.toMutableList()
                                    updatedMediaPlayers.add(mediaPlayer)
                                    mediaPlayers = updatedMediaPlayers
                                    setAudioPlayerState = true*/
//                                MediaPlayer.create(this@MainActivity, it.toUri()).apply {
//                                    val updatedMediaPlayers = mediaPlayers.toMutableList()
//                                    updatedMediaPlayers.add(this)
//                                    mediaPlayers = updatedMediaPlayers
//                                    setAudioPlayerState = true
//                                }
//                                val mediaPlayer = MediaPlayer()//.create(this@MainActivity, Uri.fromFile(it))
//                                    mediaPlayer.setDataSource(it.toUri().toString())
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                        this.setAudioStreamType(AudioManager.STREAM_MUSIC)
//                                    } else {
//                                        this.setAudioStreamType(AudioManager.STREAM_MUSIC)
//                                    }

//                                mediaPlayer.prepare()
//                                val updatedMediaPlayers = mediaPlayers.toMutableList()
//                                updatedMediaPlayers.add(mediaPlayer)
//                                mediaPlayers = updatedMediaPlayers
//                                setAudioPlayerState = true
                               // mediaPlayer.prepareAsync()



//                                mediaPlayerView.prepare()
                                //mediaPlayerView.setDataSource(context, Uri.fromFile(it))

                                //mediaPlayerView.prepare()
//                                    .apply {

//                                    prepare()

                               // val mediaPlayer = MediaPlayer()
                                /*var fis: FileInputStream? = null
                                try {
                                    fis = FileInputStream(it)
                                    mediaPlayer.setDataSource(this@MainActivity, it.toUri())
                                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                                    mediaPlayer.prepare()
                                } finally {
                                    if (fis != null) {
                                        try {
                                            fis.close()
                                        } catch (ignore: IOException) {
                                        }
                                    }
                                }*/
                            },
                            onClickSend = { it1, it2 ->
                                textFieldValue = it1
                                displayText = true
                                val updatedLocations = listOfLocations.toMutableList()
                                if (it2 != null) {
                                    updatedLocations.add(it2)
                                }
                                listOfLocations = updatedLocations
                            },
                            selectedContactsCallBack = {
                                val currContactList = it.toMutableList()
                                currContactList.addAll(currContactList.size - 1, contacts)

                                contacts = currContactList
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
                                DisplayImage(images = images)
                            }
                            Sources.VOICE -> {
                                if (true) {
                                    file?.let { it1 -> AudioPlayer(this@MainActivity, it1, mediaPlayer) }
                                    mediaPlayer?.let { it1 ->

                                    }
                                }
                            }
                            Sources.LOCATION -> {
                                DisplayLocation(locations = listOfLocations)
                            }
                            Sources.GALLERY -> {
                                DisplayGalleryItems(context, galleryItemsUriList)
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
fun DisplayLocation(locations: MutableList<Location>) {
    LazyColumn() {
        items(locations.size) {
            val location = locations[it]
            Card(shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Latitude: ${location.latitude}", fontSize = 16.sp)
                    Text(text = "Longitude: ${location.longitude}", fontSize = 16.sp)
                    Text(text = "Altitude: ${location.altitude}", fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
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
    LazyColumn() {
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
                            painter = rememberImagePainter(data = R.drawable.baseline_play_arrow_24),
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

            //mediaPlayers[index].prepareAsync()
            mediaPlayer?.setOnCompletionListener {
                isPlaying = false
                durationScale = mediaPlayer.getDurationInMmSs()
            }

            Text(text = "Audio $durationScale")

            if (!isPlaying) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                    contentDescription = "",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = {
                            mediaPlayer?.start()
                            object : Runnable {
                                override fun run() {
                                    try {
                                        durationScale = mediaPlayer?.getCurrentPositionInMmSs().toString()
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
                    painter = painterResource(R.drawable.baseline_pause_24),
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
    /*LazyColumn {
        items(mediaPlayer.size) { index ->
            var durationScale by remember {
                mutableStateOf(mediaPlayer[index].getDurationInMmSs())
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
                    verticalAlignment = Alignment.CenterVreertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_recorder),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                    )

                    //mediaPlayers[index].prepareAsync()
                    mediaPlayer[index].setOnCompletionListener {
                        isPlaying = false
                        durationScale = mediaPlayer[index].getDurationInMmSs()
                    }

                    Text(text = "Audio $durationScale")

                    if (!isPlaying) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                            contentDescription = "",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable(onClick = {
                                    mediaPlayer[index].start()
                                    object : Runnable {
                                        override fun run() {
                                            durationScale =
                                                mediaPlayer[index].getCurrentPositionInMmSs()
                                            handler.postDelayed(this, 1000)
                                        }
                                    }.run()
                                    isPlaying = true
                                })
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_pause_24),
                            contentDescription = "",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable(onClick = {
                                    mediaPlayer[index].pause()
                                    isPlaying = false
                                })
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

    }*/
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

        Image(imageVector = ImageVector.vectorResource(com.tilicho.flexchatbox.R.drawable.ic_location),
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
    }

}

