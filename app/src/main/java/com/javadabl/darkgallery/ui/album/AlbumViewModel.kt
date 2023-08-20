package com.javadabl.darkgallery.ui.album

import android.app.Application
import android.content.IntentSender
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.domain.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    application: Application,
) : AndroidViewModel(application) {


    //////////////////////////////////////////////////////////////////////////////////////////////////
    //UI


    private var mAlbums = MutableLiveData<ArrayList<Album>>()
    val albums: MutableLiveData<ArrayList<Album>> get() = mAlbums

    lateinit var currentViewingAlbum: Album
    val currentViewingID: Int get() = currentViewingAlbum.ID

    var currentViewingPosition = 0

    fun updateAlbums() {
        viewModelScope.launch {
            withContext(IO) {
                requestAlbums()
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //Delete Images

    private lateinit var mPendingDeletingMedia: ArrayList<Media>

    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteRequestAPIAbove30(media: List<Media>): LiveData<IntentSender> {
        //Add items to pending
        mPendingDeletingMedia = ArrayList()
        mPendingDeletingMedia.addAll(media)

        //Request delete
        val resultData = MutableLiveData<IntentSender>()
        viewModelScope.launch {
            withContext(IO) {
                try {
                    albumRepository.deleteImagesAPIAbove30(mPendingDeletingMedia).collect {
                        resultData.postValue(it)
                    }
                } catch (e: Exception) {
                    Toast.makeText(getApplication(), e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        return resultData
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @JvmName("deleteRequestAPIAbove30Album")
    fun deleteRequestAPIAbove30(albums: List<Album>): LiveData<IntentSender> {
        //Add items to pending
        mPendingDeletingMedia = ArrayList()
        albums.forEach {
            mPendingDeletingMedia.addAll(it.media)
        }
        //Request delete
        val resultData = MutableLiveData<IntentSender>()
        viewModelScope.launch {
            withContext(IO) {
                try {
                    albumRepository.deleteImagesAPIAbove30(mPendingDeletingMedia).collect {
                        resultData.postValue(it)
                    }
                } catch (e: Exception) {
                    Toast.makeText(getApplication(), e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        return resultData
    }

    fun deleteRequestAPIBelow30(media: List<Media>): LiveData<String> {
        //Add items to pending
        mPendingDeletingMedia = ArrayList()
        mPendingDeletingMedia.addAll(media)

        //Request delete
        val resultData = MutableLiveData<String>()
        viewModelScope.launch {
            withContext(IO) {
                try {
                    resultData.postValue(
                        albumRepository.deleteImagesAPIBelow30(
                            mPendingDeletingMedia
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(getApplication(), e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        return resultData
    }

    @JvmName("deleteRequestAPIBelow30Album")
    fun deleteRequestAPIBelow30(albums: List<Album>): LiveData<String> {
        //Add items to pending
        mPendingDeletingMedia = ArrayList()
        albums.forEach {
            mPendingDeletingMedia.addAll(it.media)
        }

        //Request delete
        val resultData = MutableLiveData<String>()
        viewModelScope.launch {
            withContext(IO) {
                try {
                    resultData.postValue(
                        albumRepository.deleteImagesAPIBelow30(
                            mPendingDeletingMedia
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(getApplication(), e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        return resultData
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //File Engine
    private suspend fun requestAlbums() {
        try {
            albumRepository.requestAlbumsFlow().collect {
                mAlbums.postValue(it)
            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


}