package `in`.tilicho.flexchatbox

import android.annotation.SuppressLint
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
import android.util.Patterns
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.core.text.util.LinkifyCompat
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import `in`.tilicho.flexchatbox.enums.FlexType
import `in`.tilicho.flexchatbox.enums.MediaType
import `in`.tilicho.flexchatbox.ui.theme.FlexChatBoxTheme
import `in`.tilicho.flexchatbox.ui.theme.ItemsBackground
import `in`.tilicho.flexchatbox.uimodel.Camera
import `in`.tilicho.flexchatbox.uimodel.ChatDataModel
import `in`.tilicho.flexchatbox.uimodel.Contacts
import `in`.tilicho.flexchatbox.uimodel.FileItems
import `in`.tilicho.flexchatbox.uimodel.GalleryItems
import `in`.tilicho.flexchatbox.uimodel.LocationItem
import `in`.tilicho.flexchatbox.uimodel.Voice
import `in`.tilicho.flexchatbox.utils.ContactData
import `in`.tilicho.flexchatbox.utils.getCurrentPositionInMmSs
import `in`.tilicho.flexchatbox.utils.getDurationInMmSs
import `in`.tilicho.flexchatbox.utils.getMediaType
import `in`.tilicho.flexchatbox.utils.getThumbnail
import java.io.File
import java.util.*

class MainActivity : ComponentActivity() {
    @SuppressLint("MutableCollectionMutableState")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContent {
            val context = this@MainActivity

            var flexType by remember {
                mutableStateOf(FlexType.CAMERA)
            }

            var chatData by remember {
                mutableStateOf<MutableList<ChatDataModel>?>(mutableListOf())
            }

            var showFlexItems by remember {
                mutableStateOf(false)
            }

            var selectedFlex by remember {
                mutableStateOf(FlexType.CAMERA)
            }

            fun performChatUIOperations(uris: List<Uri>?) {
                val currData =
                    mutableListOf(ChatDataModel(galleryItems = GalleryItems(
                        uris = uris?.toMutableList())))
                chatData?.let { it1 -> currData.addAll(0, it1) }
                chatData = currData
            }

            FlexChatBoxTheme {
                SetStatusBarColour()
                Scaffold(topBar = {
                    Column(
                        modifier = Modifier
                            .padding(top = dimensionResource(id = R.dimen.spacing_10dp))
                            .padding(horizontal = dimensionResource(id = R.dimen.spacing_10dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painterResource(id = R.drawable.app_chat_profile),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = dimensionResource(id = R.dimen.spacing_10))
                                    .size(dimensionResource(id = R.dimen.spacing_80))
                                    .clip(
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10dp)))
                            Text(text = stringResource(id = R.string.tony_stark), fontSize = 16.sp)
                            Spacer(modifier = Modifier.weight(1f))

                            var mExpanded by remember { mutableStateOf(false) }

                            val sources = listOf(FlexType.CAMERA, FlexType.FILES, FlexType.GALLERY, FlexType.LOCATION, FlexType.VOICE, FlexType.CONTACTS)
                            var mSelectedText by remember { mutableStateOf(FlexType.CAMERA.toString()) }
                            val icon = if (mExpanded)
                                Icons.Filled.KeyboardArrowUp
                            else
                                Icons.Filled.KeyboardArrowDown

                            Column {
                                Box(contentAlignment = Alignment.CenterEnd) {
                                    Row {
                                        Text(text = mSelectedText,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis)
                                        Icon(icon, null,
                                            Modifier.clickable { mExpanded = !mExpanded })
                                    }
                                }

                                DropdownMenu(
                                    expanded = mExpanded,
                                    onDismissRequest = { mExpanded = false },
                                    modifier = Modifier
                                        .width(dimensionResource(id = R.dimen.drop_down_width))
                                ) {
                                    sources.forEach { label ->
                                        DropdownMenuItem(onClick = {
                                            selectedFlex = label
                                            mSelectedText = label.toString().lowercase()
                                            mExpanded = false
                                        }) {
                                            Text(text = label.toString())
                                        }
                                    }
                                }
                            }
                        }
                        Divider(
                            color = Color.Black,
                            thickness = Dp.Hairline,
                            modifier = Modifier.padding(
                                vertical =
                                dimensionResource(id = R.dimen.spacing_20)
                            )
                        )

                        if (showFlexItems) {
                            DisplayFlexItems(selectedFlex = {
                                selectedFlex = it
                            }, setFlexItemDialog = {
                                showFlexItems = it
                            })
                        }
                    }
                }, bottomBar = {
                    Column(
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.spacing_10dp))
                            .padding(start = dimensionResource(id = R.dimen.spacing_10))
                    ) {
                        FlexChatBox(
                            context = context,
                            flexType = Pair(selectedFlex) { callback ->
                                when (callback) {
                                    is Callback.Camera -> {
                                        val currData =
                                            mutableListOf(ChatDataModel(camera = Camera(uri = callback.uri))).toMutableList()
                                        chatData?.let { currData.addAll(currData.size - 1, it) }
                                        chatData = currData
                                    }
                                    is Callback.Voice -> {
                                        val currData =
                                            mutableListOf(ChatDataModel(voice = Voice(callback.file)))
                                        chatData?.let { it1 -> currData.addAll(0, it1) }
                                        chatData = currData
                                    }
                                    is Callback.Files -> {
                                        val currData =
                                            mutableListOf(ChatDataModel(file = FileItems(files = callback.uris.toMutableList())))
                                        chatData?.let { it1 -> currData.addAll(0, it1) }
                                        chatData = currData
                                    }
                                    is Callback.Location -> {
                                        callback.let {
                                            val currData =
                                                mutableListOf(
                                                    ChatDataModel(
                                                        location = LocationItem(location = it.location)
                                                    )
                                                ).toMutableList()
                                            chatData?.let { it3 ->
                                                currData.addAll(currData.size - 1,
                                                    it3)
                                            }
                                            chatData = currData
                                        }
                                    }
                                    is Callback.Contacts -> {
                                        val currData =
                                            mutableListOf(ChatDataModel(contacts = Contacts(contacts = callback.contacts.toMutableList())))
                                        chatData?.let { it1 -> currData.addAll(0, it1) }
                                        chatData = currData
                                    }
                                    is Callback.Gallery -> {
                                        val currData =
                                            mutableListOf(ChatDataModel(galleryItems = GalleryItems(
                                                uris = callback.uris.toMutableList())))
                                        chatData?.let { it1 -> currData.addAll(0, it1) }
                                        chatData = currData
                                    }
                                    else -> {}
                                }
                            },
                            textFieldPlaceHolder = stringResource(id = R.string.hint),
                        ) { it ->
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
                        }
                    }
                }) {
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .padding(
                                horizontal = dimensionResource(
                                    id = R.dimen.spacing_20
                                )
                            )
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        chatData?.let { it1 -> ChatUI(context = context, chatData = it1) }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatUI(context: Context, chatData: List<ChatDataModel>) {
    val lazyListState = rememberLazyListState()
    LaunchedEffect(chatData.size) {
        if (chatData.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatData.size - 1)
        }
    }
    LazyColumn(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier, state = lazyListState
    ) {
        for (chatItem in chatData) {
            if (chatItem.contacts?.flexType == FlexType.CONTACTS) {
                item {
                    val contacts = chatItem.contacts!!.contacts
                    SetContactItemCell(contacts)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
                }
            } else if (chatItem.galleryItems?.flexType == FlexType.GALLERY) {
                val galleryItemsUriList = chatItem.galleryItems!!.uris
                if (galleryItemsUriList != null) {
                    items(galleryItemsUriList.size) {
                        val galleryItem = galleryItemsUriList[it]
                        SetGalleryItemCell(context = context, galleryItem = galleryItem)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
                    }
                }
            } else if (chatItem.location?.flexType == FlexType.LOCATION) {
                item {
                    val location = chatItem.location!!.location
                    SetLocationItemCell(context = context, location = location)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
                }
            } else if (chatItem.voice?.flexType == FlexType.VOICE) {
                item {
                    val audioFile = chatItem.voice!!.file
                    SetVoiceItemCell(context = context, audioFile = audioFile)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
                }
            } else if (chatItem.file?.flexType == FlexType.FILES) {
                val fileItemsUriList = chatItem.file!!.files
                fileItemsUriList?.let {
                    items(fileItemsUriList.size) { index ->
                        val fileItem = fileItemsUriList[index]
                        SetFileItemCell(context = context, fileItem = fileItem)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
                    }
                }
            } else if (chatItem.camera?.flexType == FlexType.CAMERA) {
                val cameraItem = chatItem.camera!!.uri
                val mediaType = getMediaType(context, cameraItem)
                if (mediaType == MediaType.MediaTypeVideo) {
                    item {
                        SetCameraVideoItemCell(context = context, video = cameraItem)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
                    }
                } else if (mediaType == MediaType.MediaTypeImage) {
                    item {
                        SetCameraPictureItemCell(cameraItem)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
                    }
                }
            } else {
                val text = chatItem.textFieldValue
                item {
                    if (text != null) {
                        SetChatTextCell(text = text)
                    }
                }
            }
        }
    }
}

@Composable
fun SetContactItemCell(contacts: List<ContactData>?) {
    if (contacts?.isNotEmpty() == true && contacts.size <= 1) {
        Card(
            shape = RoundedCornerShape(
                dimensionResource(id = R.dimen.spacing_40),
                dimensionResource(id = R.dimen.spacing_40),
                0.dp,
                dimensionResource(id = R.dimen.spacing_40)
            ),
            backgroundColor = ItemsBackground
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.spacing_40),
                    vertical = dimensionResource(id = R.dimen.spacing_10dp)
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_person),
                    contentDescription = "",
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10dp)))
                Column {
                    contacts[0].name?.let { it1 ->
                        Text(
                            text = it1,
                            fontFamily = FontFamily(Font(R.font.opensans_regular))
                        )
                    }
                    contacts[0].mobileNumber?.let { it1 ->
                        Text(
                            text = it1,
                            fontFamily = FontFamily(Font(R.font.opensans_regular))
                        )
                    }
                }
            }
        }
    } else if (contacts?.isNotEmpty() == true) {
        Card(
            shape = RoundedCornerShape(
                dimensionResource(id = R.dimen.spacing_40),
                dimensionResource(id = R.dimen.spacing_40),
                0.dp,
                dimensionResource(id = R.dimen.spacing_40)
            ),
            backgroundColor = ItemsBackground
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.spacing_40),
                    vertical = dimensionResource(id = R.dimen.spacing_10)
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_group),
                    contentDescription = "",
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10dp)))
                Text(
                    text = "Contact 1 and ${contacts.size - 1} \n other contacts",
                    fontFamily = FontFamily(Font(R.font.opensans_regular))
                )
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
                        .size(dimensionResource(id = R.dimen.dialog_size))
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
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
        }
        val videoThumbnail = getThumbnail(context = context, galleryItem)
        Box(contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(
                    dimensionResource(id = R.dimen.spacing_40),
                    dimensionResource(id = R.dimen.spacing_40),
                    0.dp,
                    dimensionResource(id = R.dimen.spacing_40)
                ),
                border = BorderStroke(width = 1.dp, color = ItemsBackground)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = videoThumbnail),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.image_size))
                        .padding(dimensionResource(id = R.dimen.spacing_10dp))
                        .clickable(onClick = {
                            setPreviewDialog = true
                        })
                )
            }
            Image(
                painter = rememberAsyncImagePainter(model = R.drawable.ic_play_circle),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.spacing_90))
            )
        }

    } else {
        Card(
            shape = RoundedCornerShape(
                dimensionResource(id = R.dimen.spacing_40),
                dimensionResource(id = R.dimen.spacing_40),
                0.dp,
                dimensionResource(id = R.dimen.spacing_40)
            ),
            border = BorderStroke(width = 1.dp, color = ItemsBackground)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = galleryItem),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.spacing_10dp))
                    .size(dimensionResource(id = R.dimen.image_size))
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
            .background(
                color = ItemsBackground,
                shape = RoundedCornerShape(
                    dimensionResource(id = R.dimen.spacing_40),
                    dimensionResource(id = R.dimen.spacing_40),
                    0.dp,
                    dimensionResource(id = R.dimen.spacing_40)
                )
            )
            .width(dimensionResource(id = R.dimen.dialog_size))
            .padding(dimensionResource(id = R.dimen.spacing_10dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Image(painterResource(id = R.drawable.image_map), contentDescription = null)
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_50)))
            Text(
                text = "${location?.location?.longitude},${location?.location?.latitude}",
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10)))
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
    var mediaPlayer: MediaPlayer? = null

    if (audioFile != null) {
        MediaPlayer.create(context, audioFile.toUri()).apply {
            mediaPlayer = this
        }
    }

    var durationScale by remember {
        mutableStateOf(mediaPlayer?.getDurationInMmSs())
    }
    var isPlaying by remember {
        mutableStateOf(false)
    }
    val handler = Handler()

    Card(
        backgroundColor = ItemsBackground,
        shape = RoundedCornerShape(
            topStart = dimensionResource(id = R.dimen.spacing_40),
            topEnd = dimensionResource(id = R.dimen.spacing_40),
            bottomEnd = 0.dp,
            bottomStart = dimensionResource(id = R.dimen.spacing_40)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.spacing_10dp))
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_recorder),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.spacing_90))
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_20)))

            mediaPlayer?.setOnCompletionListener {
                isPlaying = false
                durationScale = it?.getDurationInMmSs()
            }

            Text(
                text = "Audio $durationScale",
                fontFamily = FontFamily(Font(R.font.opensans_regular))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_20)))
            if (!isPlaying) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play_circle),
                    contentDescription = "",
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.spacing_70))
                        .clickable(onClick = {
                            mediaPlayer?.start()
                            object : Runnable {
                                override fun run() {
                                    durationScale =
                                        mediaPlayer?.getCurrentPositionInMmSs()
                                    handler.postDelayed(this, 1000)
                                }
                            }.run()
                            isPlaying = true
                        })
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_pause_circle),
                    contentDescription = "",
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.spacing_70))
                        .clickable(onClick = {
                            mediaPlayer?.pause()
                            isPlaying = false
                        })
                )
            }
        }
    }
    if (mediaPlayer?.isPlaying == true) {
        mediaPlayer?.stop()
    }
}

@SuppressLint("Recycle")
@Composable
fun SetFileItemCell(context: Context, fileItem: Uri) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .background(
                shape = RoundedCornerShape(
                    dimensionResource(id = R.dimen.spacing_40),
                    dimensionResource(id = R.dimen.spacing_40),
                    0.dp,
                    dimensionResource(id = R.dimen.spacing_40)
                ),
                color = ItemsBackground
            )
            .wrapContentWidth()
            .wrapContentHeight()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                .background(color = ItemsBackground)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_uploaded_file),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_10)))

            Column {
                val file = File(fileItem.toString())
                var type: String
                var fileSize: Long
                context.let {
                    val cR: ContentResolver = it.contentResolver
                    val mime = MimeTypeMap.getSingleton()
                    type = mime.getExtensionFromMimeType(cR.getType(fileItem))
                        .toString()
                    val fileDescriptor: AssetFileDescriptor? =
                        it.contentResolver.openAssetFileDescriptor(fileItem, "r")
                    fileSize = fileDescriptor?.length ?: 0L
                }
                val fileName = file.name + "." + type
                Text(text = fileName, fontFamily = FontFamily(Font(R.font.opensans_regular)))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = (fileSize / 1024).toString() + " mb",
                    fontFamily = FontFamily(Font(R.font.opensans_regular))
                )
            }
        }
    }
}


@Composable
fun SetCameraPictureItemCell(cameraImage: Uri?) {
    cameraImage?.let {
        Card(
            shape = RoundedCornerShape(
                dimensionResource(id = R.dimen.spacing_40),
                dimensionResource(id = R.dimen.spacing_40),
                0.dp,
                dimensionResource(id = R.dimen.spacing_40)
            ),
            border = BorderStroke(width = 1.dp, color = ItemsBackground)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = cameraImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.image_size))
                    .padding(dimensionResource(id = R.dimen.spacing_10dp))
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
                    .size(dimensionResource(id = R.dimen.dialog_size))
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
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
    }

    Box(contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(
                dimensionResource(id = R.dimen.spacing_40),
                dimensionResource(id = R.dimen.spacing_40),
                0.dp,
                dimensionResource(id = R.dimen.spacing_40)
            ),
            border = BorderStroke(width = 1.dp, color = ItemsBackground)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = video?.let {
                    getThumbnail(
                        context,
                        it
                    )
                }
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.spacing_10dp))
                    .size(dimensionResource(id = R.dimen.image_size))
                    .clickable(onClick = {
                        setPreviewDialog = true
                    })
            )
        }
        Image(
            painter = rememberAsyncImagePainter(model = R.drawable.ic_play_circle),
            contentDescription = null,
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.spacing_90))
        )
    }
}

@Composable
fun SetChatTextCell(text: String) {
    if (text.length > 25) {
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.chat_text_cell_size))
                .background(
                    color = ItemsBackground,
                    shape = RoundedCornerShape(
                        dimensionResource(id = R.dimen.spacing_50),
                        dimensionResource(id = R.dimen.spacing_50),
                        0.dp,
                        dimensionResource(id = R.dimen.spacing_50)
                    )
                )
        )
        {
            Text(
                text = text,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.spacing_10dp)),
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
    } else {
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .background(
                    color = ItemsBackground,
                    shape = RoundedCornerShape(
                        dimensionResource(id = R.dimen.spacing_50),
                        dimensionResource(id = R.dimen.spacing_50),
                        0.dp,
                        dimensionResource(id = R.dimen.spacing_50)
                    )
                )
                .wrapContentWidth()
        )
        {
            Text(
                text = text,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.spacing_10dp)),
                fontFamily = FontFamily(Font(R.font.opensans_regular)),
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_10dp)))
    }

}

@Composable
fun DisplayFlexItems(
    selectedFlex: (FlexType) -> Unit,
    setFlexItemDialog: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .padding(
                top = dimensionResource(id = R.dimen.spacing_10dp), start = dimensionResource(
                    id = R.dimen.spacing_10dp
                )
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacing_10dp))
            ), verticalAlignment = Alignment.CenterVertically
    ) {
        Image(imageVector = ImageVector.vectorResource(R.drawable.ic_camera),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                .clickable(onClick = {
                    selectedFlex.invoke(FlexType.CAMERA)
                    setFlexItemDialog.invoke(false)
                }
                ), colorFilter = ColorFilter.tint(Color.Black))
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_mic),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                .clickable(onClick = {
                    selectedFlex.invoke(FlexType.VOICE)
                    setFlexItemDialog.invoke(false)
                }
                ), colorFilter = ColorFilter.tint(Color.Black)
        )

        Image(imageVector = ImageVector.vectorResource(R.drawable.ic_location),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                .clickable(onClick = {
                    selectedFlex.invoke(FlexType.LOCATION)
                    setFlexItemDialog.invoke(false)
                }
                ), colorFilter = ColorFilter.tint(Color.Black))
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_gallery),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                .clickable(onClick = {
                    selectedFlex.invoke(FlexType.GALLERY)
                    setFlexItemDialog.invoke(false)
                }), colorFilter = ColorFilter.tint(Color.Black)
        )
        Image(
            imageVector = (Icons.Filled.Person),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                .clickable(onClick = {
                    selectedFlex.invoke(FlexType.CONTACTS)
                    setFlexItemDialog.invoke(false)
                })
        )
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_file),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.spacing_10dp))
                .clickable(onClick = {
                    selectedFlex.invoke(FlexType.FILES)
                    setFlexItemDialog.invoke(false)
                }),
            colorFilter = ColorFilter.tint(Color.Black)
        )
    }
}

@Composable
fun SetStatusBarColour() {
    var statusBarColour by remember {
        mutableStateOf(Color.Black)
    }
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    if (isSystemInDarkTheme()) {
        statusBarColour = Color.Black
    }

    DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setSystemBarsColor(statusBarColour)
        systemUiController.setNavigationBarColor(statusBarColour)
        onDispose {}
    }
}
