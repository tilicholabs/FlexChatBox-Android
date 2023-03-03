package com.tilicho.flexchatbox

import android.Manifest
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.tilicho.flexchatbox.enums.Sources
import com.tilicho.flexchatbox.utils.*

@Composable
fun ChatBox(
    source: Sources,
    cameraImage: (Uri) -> Unit,
    gallery: (List<Uri>) -> Unit,
    onClickSend: (String, Location?) -> Unit,
) {
    val context = LocalContext.current
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }

    var location by remember {
        mutableStateOf<Location?>(null)
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            cameraImage.invoke(capturedImageUri)
        }

    val galleryLauncher = rememberLauncherForActivityResult(GetMediaActivityResultContract()) {
        gallery.invoke(it)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (source) {
                Sources.CAMERA -> {
                    capturedImageUri = generateUri(context)
                    cameraLauncher.launch(capturedImageUri)
                }
                Sources.GALLERY -> {
                    galleryLauncher.launch("*/*")
                }
                else -> {}
            }

        } else {
            showToast(context.applicationContext, context.getString(R.string.permission_denied))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        OutlinedTextField(value = textFieldValue, onValueChange = {
            textFieldValue = it
        }, placeholder = {
            Text(
                text = stringResource(id = R.string.hint),
                color = colorResource(id = R.color.c_placeholder),
                fontSize = 18.sp
            )
        }, modifier = Modifier.weight(1f),
            singleLine = false, maxLines = 5
        )


        Row(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.spacing_30))) {
            when (source) {
                Sources.CAMERA -> {
                    SourceImage(icon = R.drawable.ic_camera, onClickIcon = {
                        // Check whether permission is granted or not, if not request permission
                        if (checkPermission(context, Manifest.permission.CAMERA)) {
                            galleryLauncher.launch("image/*")
                        } else {
                            requestPermission(permissionLauncher, Manifest.permission.CAMERA)
                        }
                    })
                }

                Sources.GALLERY -> {
                    SourceImage(icon = R.drawable.ic_gallery, onClickIcon = {
                        // Check whether permission is granted or not, if not request permission
                        if (checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            galleryLauncher.launch("*/*")
                        } else {
                            requestPermission(permissionLauncher,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    })
                }

                Sources.LOCATION -> {
                    SourceImage(icon = R.drawable.ic_location, onClickIcon = {
                        location = if (!checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            requestPermission(permissionLauncher,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                            getLocation(context)
                        } else {
                            getLocation(context)
                        }

                    })
                }
                else -> {}
            }

            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_send),
                contentDescription = null,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.spacing_10))
                    .padding(start = dimensionResource(id = R.dimen.spacing_30))
                    .clickable(onClick = {
                        onClickSend.invoke(textFieldValue.text, location)
                    })
            )
        }
    }
}

@Composable
fun SourceImage(icon: Int, onClickIcon: () -> Unit) {
    Image(
        imageVector = ImageVector.vectorResource(icon),
        contentDescription = null,
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.spacing_20))
            .clickable(onClick = {
                onClickIcon.invoke()
            }
            )
    )
}