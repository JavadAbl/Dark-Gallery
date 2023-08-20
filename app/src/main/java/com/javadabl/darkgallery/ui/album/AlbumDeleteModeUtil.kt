package com.javadabl.darkgallery.ui.album

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.domain.model.AlbumDeleteModel
import com.javadabl.darkgallery.ui.album.adapter.AlbumTableRecyclerAdapter

class AlbumDeleteModeUtil(
    private val adapter: AlbumTableRecyclerAdapter,
    private val viewModel: AlbumViewModel,
    private val menuHost: MenuHost,
    private val menuProvider: MenuProvider,
) {


    private var mSelectMode = false
    fun getSelectMode() = mSelectMode

    private var mPending = ArrayList<AlbumDeleteModel>()


    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteRequestAPIAbove30() = viewModel.deleteRequestAPIAbove30(mPending.map {
        it.album
    })


    fun deleteRequestAPIBelow29() = viewModel.deleteRequestAPIBelow30(mPending.map {
        it.album
    })


    fun enterSelectMode() {
        mPending = ArrayList()
        menuHost.addMenuProvider(menuProvider)
        mSelectMode = true
    }


    fun exitSelectMode() {
        mSelectMode = false
        mPending.forEach {
            it.album.selected = false
            adapter.setChecked(it.index, it.album)
        }
        menuHost.removeMenuProvider(menuProvider)
    }


    fun addSelectedItem(album: Album, index: Int) {
        album.selected = true
        val temp = AlbumDeleteModel(album, index)
        mPending.add(temp)
        adapter.setChecked(index, album)
    }


    fun removePending(album: Album, index: Int) {
        val temp = AlbumDeleteModel(album, index)
        mPending.remove(temp)

        album.selected = false
        adapter.setChecked(index, album)
        if (mPending.size == 0)
            exitSelectMode()
    }


    fun contain(album: Album): Boolean {
        var result = false

        for (x in mPending) {
            if (x.album == album) {
                result = true
                break
            }
        }
        return result
    }

    fun getItemsSize() = mPending.size


}