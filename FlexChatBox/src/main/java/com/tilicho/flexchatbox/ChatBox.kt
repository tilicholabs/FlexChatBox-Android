package com.tilicho.flexchatbox

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tilicho.flexchatbox.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

@Composable
fun ChatBox(
    capturedImage: (Uri) -> Unit,
    onClickSend: (String) -> Unit,
) {
    val context = LocalContext.current
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        val cameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
                capturedImage.invoke(capturedImageUri)
            }
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                capturedImageUri = generateUri(context)
                cameraLauncher.launch(capturedImageUri)
            }
        }

        OutlinedTextField(value = textFieldValue, onValueChange = {
            textFieldValue = it
        }, placeholder = {
            Text(
                text = stringResource(id = R.string.hint),
                color = colorResource(id = R.color.c_placeholder),
                fontSize = 16.sp
            )
        }, modifier = Modifier.weight(5f), singleLine = false, maxLines = 5
        )
        Image(imageVector = ImageVector.vectorResource(R.drawable.baseline_camera_alt_24),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = {
                    // Check permission
                    when (PackageManager.PERMISSION_GRANTED) {

                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                            capturedImageUri = generateUri(context)
                            cameraLauncher.launch(capturedImageUri)
                        }
                        else -> {
                            // Ask for permission
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
                ))
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_send_24),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = {
                    onClickSend.invoke(textFieldValue.text)
                })
        )
    }
}

fun generateUri(context: Context): Uri {
    val file = context.createImageFile()
    return FileProvider.getUriForFile(Objects.requireNonNull(context),"${context.packageName}.provider", file)
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val image = File.createTempFile(imageFileName, ".jpg", externalCacheDir)
    return image
}