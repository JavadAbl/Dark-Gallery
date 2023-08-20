package com.javadabl.darkgallery.domain.model

import android.net.Uri
import android.os.Parcelable
import com.javadabl.darkgallery.data.local.file.model.MediaType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Media(

    val uri: Uri,
    val name: String,
    val path: String,
    val albumName: String,
    var dateModified: Date,
    var dateTaken: Date,
    val size: Long,
    val resolution: String,
    val type: MediaType,


    ) : Parcelable {
    val ID: Int get() = this.hashCode()

    @IgnoredOnParcel
    var selected: Boolean = false
}
