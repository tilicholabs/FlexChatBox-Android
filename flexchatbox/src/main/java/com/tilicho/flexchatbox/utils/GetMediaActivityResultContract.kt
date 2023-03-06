package com.tilicho.flexchatbox.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class GetMediaActivityResultContract : ActivityResultContracts.GetMultipleContents() {

    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input).apply {
            // To select multiple images and videos
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            // Force only images and videos to be selectable
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
    }
}