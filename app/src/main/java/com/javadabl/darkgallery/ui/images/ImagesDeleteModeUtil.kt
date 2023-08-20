package com.javadabl.darkgallery.ui.images

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.domain.model.MediaDeleteModel
import com.javadabl.darkgallery.ui.album.AlbumViewModel
import com.javadabl.darkgallery.ui.images.adapter.ImagesRecyclerAdapter

class ImagesDeleteModeUtil(
    private val adapter: ImagesRecyclerAdapter,
    private val viewModel: AlbumViewModel,
    private val menuHost: MenuHost,
    private val menuProvider: MenuProvider,
) {


    private var mSelectMode = false
    fun getSelectMode() = mSelectMode

    private var mPending = ArrayList<MediaDeleteModel>()
    val selectedMedia: List<Media> get() = mPending.map { it.media }


    companion object {

        @RequiresApi(Build.VERSION_CODES.R)
        fun singleDeleteRequestAPIAbove30(media: Media, viewModel: AlbumViewModel) =
            viewModel.deleteRequestAPIAbove30(listOf(media))

        fun singleDeleteRequestAPIBelow29(media: Media, viewModel: AlbumViewModel) =
            viewModel.deleteRequestAPIBelow30(listOf(media))
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteRequestAPIAbove30() = viewModel.deleteRequestAPIAbove30(mPending.map {
        it.media
    })


    fun deleteRequestAPIBelow29() = viewModel.deleteRequestAPIBelow30(mPending.map {
        it.media
    })


    fun enterSelectMode() {
        mPending = ArrayList()
        menuHost.addMenuProvider(menuProvider)
        mSelectMode = true
    }


    fun exitSelectMode() {
        mSelectMode = false
        mPending.forEach {
            it.media.selected = false
            adapter.setChecked(it.index,it.media)
        }
        menuHost.removeMenuProvider(menuProvider)
    }


    fun addSelectedItem(media: Media, index: Int) {
        media.selected = true
        val temp = MediaDeleteModel(media, index)
        mPending.add(temp)
        adapter.setChecked(index, media)
    }


    fun removePending(media: Media, index: Int) {
        val temp = MediaDeleteModel(media, index)
        mPending.remove(temp)

        media.selected = false
        adapter.setChecked(index, media)
        if (mPending.size == 0)
            exitSelectMode()
    }


    fun contain(media: Media): Boolean {
        var result = false

        for (x in mPending) {
            if (x.media == media) {
                result = true
                break
            }
        }
        return result
    }

    fun getItemsSize() = mPending.size


}