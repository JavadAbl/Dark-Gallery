package com.javadabl.darkgallery.data.local.file.model

import android.net.Uri
import java.util.Date

data class MediaFile(

    val uri: Uri,
    val name:String,
    val path: String,
    val directory:String,
    val dateAdded:Date,
    val dateTaken:Date,
    val size:Long,
    val resolution:String,
    val type : MediaType
)
