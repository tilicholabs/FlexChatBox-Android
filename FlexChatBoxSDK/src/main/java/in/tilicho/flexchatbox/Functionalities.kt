package `in`.tilicho.flexchatbox

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import `in`.tilicho.flexchatbox.utils.LOCATION_URL
import `in`.tilicho.flexchatbox.utils.cameraIntent
import `in`.tilicho.flexchatbox.utils.findActivity
import `in`.tilicho.flexchatbox.utils.getGrantedPermissions
import `in`.tilicho.flexchatbox.utils.getImageUri
import `in`.tilicho.flexchatbox.utils.getLocation
import `in`.tilicho.flexchatbox.utils.isLocationEnabled
import `in`.tilicho.flexchatbox.utils.openFiles

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Camera(
    context: Context,
    textFieldPlaceHolder: String,
    onClickSend: ((String) -> Unit)? = null,
    cameraCallback: ((Uri) -> Unit),
) {
    var isCameraPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (isCameraPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.CAMERA)) {
                        isCameraPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {

                if (activityResult.data?.data != null) {
                    activityResult.data?.data?.let {
                        cameraCallback.invoke(it)
                    }
                } else {
                    getImageUri(context, activityResult.data?.extras?.get("data") as Bitmap)?.let {
                        cameraCallback.invoke(it)
                    }
                }
            }
        }

    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.CAMERA
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isCameraPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isCameraPermissionPermanentlyDenied = false
                cameraIntent(cameraLauncher)
            }
        }

    ChatTextField(context = context,
        textFieldPlaceHolder = textFieldPlaceHolder,
        permissionState = cameraPermissionState,
        drawable = R.drawable.ic_camera,
        permissionPermanentlyDenied = isCameraPermissionPermanentlyDenied,
        permission = Manifest.permission.CAMERA,
        onClickSend = {
            onClickSend?.invoke(it)
        })
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Location(
    context: Context,
    textFieldPlaceHolder: String,
    onClickSend: ((String) -> Unit)? = null,
    locationCallback: ((Location) -> Unit)
) {
    var isLocationPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var location by remember {
        mutableStateOf<Location?>(null)
    }

    if (isLocationPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        isLocationPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    val locationPermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isLocationPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isLocationPermissionPermanentlyDenied = false
                if (!isLocationEnabled(context)) {
                    Toast.makeText(context, R.string.enable_location, Toast.LENGTH_LONG).show()
                } else {
                    val currLocation = getLocation(context)
                    val latLong =
                        (currLocation?.latitude).toString() + "," + (currLocation?.longitude).toString()
                    location = Location(
                        currLocation,
                        LOCATION_URL + latLong
                    )
                    locationCallback.invoke(location!!)
                }
            }
        }

    ChatTextField(context = context,
        textFieldPlaceHolder = textFieldPlaceHolder,
        permissionState = locationPermissionState,
        permissionPermanentlyDenied = isLocationPermissionPermanentlyDenied,
        drawable = R.drawable.ic_location,
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onClickSend = {
            onClickSend?.invoke(it)
        })
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Files(context: Context,
          textFieldPlaceHolder: String,
          onClickSend: ((String) -> Unit)? = null,
          filesCallback: ((List<Uri>) -> Unit)) {
    var isFilesPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (isFilesPermissionPermanentlyDenied) {
        if (showSettingsDialog) {
            ShowNavigateToAppSettingsDialog(context = context, onDismissCallback = {
                showSettingsDialog = it
            })
        }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (getGrantedPermissions(context).contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        isFilesPermissionPermanentlyDenied = false
                    }
                }
                else -> {}
            }
        }
    }

    val fileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val filesUriList: MutableList<Uri> = mutableListOf()
            if (activityResult.resultCode == Activity.RESULT_OK) {
                if (activityResult.data?.clipData != null) {
                    val count = activityResult.data?.clipData?.itemCount ?: 0
                    var currentItem = 0
                    while (currentItem < count) {
                        activityResult.data?.clipData?.getItemAt(currentItem)?.uri
                            ?.let { (filesUriList.add(it)) }
                        currentItem += 1
                    }
                    filesCallback.invoke(filesUriList)
                } else {
                    activityResult.data?.data.let {
                        if (it != null) {
                            filesUriList.add(it)
                        }
                    }
                    filesCallback.invoke(filesUriList)
                }
            }
        }

    val filesPermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { isGranted ->
            val permissionPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) && !isGranted

            if (permissionPermanentlyDenied) {
                isFilesPermissionPermanentlyDenied = true
                showSettingsDialog = true
            }
            if (isGranted) {
                isFilesPermissionPermanentlyDenied = false
                openFiles(context, fileLauncher)
            }
        }

    ChatTextField(context = context,
        textFieldPlaceHolder = textFieldPlaceHolder,
        permissionState = filesPermissionState,
        permissionPermanentlyDenied = isFilesPermissionPermanentlyDenied,
        drawable = R.drawable.ic_file,
        permission = Manifest.permission.READ_EXTERNAL_STORAGE,
        onClickSend = {
            onClickSend?.invoke(it)
        })
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatTextField(
    context: Context,
    textFieldPlaceHolder: String,
    permissionState: PermissionState,
    permissionPermanentlyDenied: Boolean,
    drawable: Int,
    permission: String,
    onClickSend: ((String) -> Unit)?,
) {
    var textFieldValue by rememberSaveable { mutableStateOf(String.empty()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var sendIconState by remember {
            mutableStateOf(Color(0xFF808080))
        }
        sendIconState = if (textFieldValue.isNotEmpty()) {
            colorResource(R.color.c_2ba6ff)
        } else {
            Color(0xFF808080)
        }
        Row(
            verticalAlignment = Alignment.Bottom, modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
                .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacing_60)))
                .background(
                    color = if (isSystemInDarkTheme()) Color.Black else colorResource(
                        id = R.color.c_edf0ee
                    )
                )
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                },
                placeholder = {
                    Text(
                        text = textFieldPlaceHolder,
                        color = colorResource(id = R.color.c_placeholder),
                        fontSize = 18.sp
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.weight(6f),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            IconButton(modifier = Modifier.weight(1.5f),
                onClick = {
                    if (onClickSend == null) {
                        Toast.makeText(
                            context,
                            "onClickSend should be implemented",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        onClickSend.invoke(textFieldValue)
                    }
                    textFieldValue = String.empty()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_send),
                    contentDescription = null,
                    tint = sendIconState
                )
            }
            colorResource(R.color.grey)
        }

        Row(
            modifier = Modifier
                .padding(start = dimensionResource(id = R.dimen.spacing_20),
                    bottom = dimensionResource(
                        id = R.dimen.spacing_00
                    )),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SourceImage(context = context,
                icon = drawable,
                isDenied = permissionPermanentlyDenied,
                permission = permission, onClickIcon = {
                    permissionState.launchPermissionRequest()
                })
        }
    }
}