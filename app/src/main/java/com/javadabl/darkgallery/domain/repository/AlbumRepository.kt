package com.javadabl.darkgallery.domain.repository

import android.content.IntentSender
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.javadabl.darkgallery.data.local.file.FileProvider
import com.javadabl.darkgallery.data.local.file.model.MediaFile
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.util.AppUtil
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ViewModelScoped
class AlbumRepository @Inject constructor(
    private val fileProvider: FileProvider
) {


    private lateinit var mListFiles: ArrayList<MediaFile>
    private lateinit var mListAlbums: ArrayList<Album>


    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteImagesAPIAbove30(media: List<Media>) = flow<IntentSender> {
        fileProvider.deleteUrisAPIAbove30(imageListToUriList(media)).collect {
            emit(it)
        }
    }

    fun deleteImagesAPIBelow30(media: List<Media>): String {
        return fileProvider.deleteUrisAPIBelow30(imageListToUriList(media))
    }


    private fun imageListToUriList(media: List<Media>): List<Uri> {
        return media.map {
            it.uri
        }
    }


    suspend fun requestAlbumsFlow(): Flow<ArrayList<Album>> = flow {
        fileProvider.queryImages().collect {

            mListFiles = ArrayList()
            mListAlbums = ArrayList()

            mListFiles.addAll(it)
            extractImagesFromFiles()


            mListAlbums.forEach {
                it.media.sortWith { o1, o2 ->
                    o2.dateModified.time.compareTo(o1.dateModified.time)
                }
            }

            mListAlbums.sortWith { o1, o2 ->
                o2.media[0].dateModified.time.compareTo(o1.media[0].dateModified.time)
            }

        }

        emit(mListAlbums)
    }


    private fun extractImagesFromFiles() {
        for (it in mListFiles) {
            val media = Media(
                uri = it.uri,
                name = it.name,
                path = it.path,
                albumName = it.directory,
                dateModified = it.dateAdded,
                dateTaken = it.dateTaken,
                size = it.size,
                it.resolution,
                type = it.type
            )
            extractAlbumFromImage(media)
        }
    }

    @Synchronized
    private fun extractAlbumFromImage(media: Media) {
        val addNewAlbum = {
            mListAlbums.add(
                Album(
                    arrayListOf(media),
                    media.albumName,
                )
            )
        }

        if (mListAlbums.size == 0) {
            addNewAlbum()
            return
        }

        var changeIndex = -1

        mListAlbums.forEachIndexed { index, album ->
            if (album.name == media.albumName)
                changeIndex = index
        }

        if (changeIndex == -1) {
            addNewAlbum()
            return
        }

        mListAlbums[changeIndex].media.add(media)
    }



}