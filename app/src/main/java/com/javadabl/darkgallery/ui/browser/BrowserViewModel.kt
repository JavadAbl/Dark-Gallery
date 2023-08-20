package com.javadabl.darkgallery.ui.browser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(private val application: Application) :
    AndroidViewModel(application) {


    var currentVideoPosition = 0L
    var isVideoPlaying = false
    var mTempPlayingState = false


}