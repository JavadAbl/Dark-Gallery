package com.javadabl.darkgallery.domain.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*


@Parcelize
data class Album(

    var media: @RawValue ArrayList<Media>,
    val name: String,

    ) : Parcelable {

    val ID: Int get() = this.hashCode()
    val lastModified: Date get() = media[0].dateModified

    @IgnoredOnParcel
    var selected = false

    @IgnoredOnParcel
    val imagesCount: Int
        get() = media.size

}
