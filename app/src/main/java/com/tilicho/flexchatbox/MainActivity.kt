package com.tilicho.flexchatbox

import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.tilicho.flexchatbox.enums.Sources
import com.tilicho.flexchatbox.ui.theme.FlexChatBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var imageUri by remember {
                mutableStateOf<Uri?>(null)
            }
            var textFieldValue by remember {
                mutableStateOf("")
            }

            var location by remember {
                mutableStateOf<Location?>(null)
            }

            var galleryUriList by remember {
                mutableStateOf<List<Uri>?>(null)
            }
            val galleryImagesUriList by remember {
                mutableStateOf<List<Uri>?>(null)
            }

            val galleryVideosUriList by remember {
                mutableStateOf<List<Uri>?>(null)
            }

            FlexChatBoxTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.End
                ) {

                    imageUri?.let { DisplayImage(imageUri = it) }

                    galleryUriList?.let {
                        for (uri in it) {
                            if (getMediaType(this@MainActivity,
                                    uri) == MediaType.MediaTypeImage
                            ) {
                                DisplayImage(imageUri = uri)
//                                galleryImagesUriList?.plus(uri)
                            } else if (getMediaType(this@MainActivity,
                                    uri) == MediaType.MediaTypeVideo
                            ) {
                                VideoView(videoUri = uri.toString())
//                                galleryVideosUriList?.plus(uri)
                            }
                        }
                    }

                    /*Spacer(modifier = Modifier.height(40.dp))

                    galleryImagesUriList?.let {
                        if (it.size == 1) {
                            imageUri = it[0]
                            DisplayImage(imageUri = imageUri!!)
                        }
                        else {
                            DisplayImageGrid(imageUriList = it)
                        }
                    }*/
                    Spacer(modifier = Modifier.height(40.dp))

                    /*galleryVideosUriList?.let {
                        VideoView(videoUri = it[0].toString())
                    }*/

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = location.toString(),
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    ChatBox(
                        Sources.LOCATION,
                        cameraImage = { uri ->
                            imageUri = uri
                        },
                        onClickSend = { inputValue, _location ->
                            textFieldValue = inputValue
                            location = _location
                        },
                        gallery = { uriList ->
                            galleryUriList = uriList
                        }
                    )
                }
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
fun DisplayImage(imageUri: Uri) {
    Row(horizontalArrangement = Arrangement.End) {
        Image(
            painter = rememberImagePainter(data = imageUri),
            contentDescription = "avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
fun DisplayImageGrid(imageUriList: List<Uri>) {
    Row {
        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            items(imageUriList) {
                Image(
                    painter = rememberImagePainter(data = it),
                    contentDescription = "avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}

private fun getMediaType(context: Context, source: Uri?): MediaType {
    val mediaTypeRaw = source?.let { context.contentResolver.getType(it) }
    if (mediaTypeRaw?.startsWith("image") == true)
        return MediaType.MediaTypeImage
    if (mediaTypeRaw?.startsWith("video") == true)
        return MediaType.MediaTypeVideo
    return MediaType.Unknown
}
