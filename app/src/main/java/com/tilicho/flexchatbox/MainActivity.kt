package com.tilicho.flexchatbox

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.tilicho.flexchatbox.ui.theme.FlexChatBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var capturedImageUri by remember {
                mutableStateOf<Uri>(Uri.EMPTY)
            }
            var textFieldValue by remember {
                mutableStateOf("")
            }
            FlexChatBoxTheme {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ChatBox(
                        { uri ->
                            capturedImageUri = uri
                        },
                        onClickSend = {
                            textFieldValue = it
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Image(
                        painter = rememberImagePainter(data = capturedImageUri),
                        contentDescription = "avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = textFieldValue, color = Color(0xFF535353), fontSize = 16.sp
                    )
                }
            }
        }
    }
}
