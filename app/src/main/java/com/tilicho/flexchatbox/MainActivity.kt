package com.tilicho.flexchatbox

import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
                mutableStateOf<MutableList<Uri>?>(null)
            }
            val galleryImagesUriList by remember {
                mutableStateOf<MutableList<Uri>?>(null)
            }

            var galleryVideoUri by remember {
                mutableStateOf<Uri?>(null)
            }

            var mediaPlayer by remember {
                mutableStateOf(MediaPlayer())
            }

            FlexChatBoxTheme {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    ChatBox(
                        context = this@MainActivity,
                        source = Sources.VOICE,
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
                        }
                    )

                }
            }
        }
    }
}

