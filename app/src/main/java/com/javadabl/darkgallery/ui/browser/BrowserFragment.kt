package com.javadabl.darkgallery.ui.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.javadabl.darkgallery.data.local.file.model.MediaType
import com.javadabl.darkgallery.databinding.FragmentBrowserBinding
import com.javadabl.darkgallery.databinding.FragmentImageBinding
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.image.ImageFragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

private const val MEDIA_PARAM = "BROWSER_MEDIA"

class BrowserFragment : Fragment() {

    companion object {
        var sMenuVisibility = true

        @JvmStatic
        fun newInstance(media: Media) =
            BrowserFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(MEDIA_PARAM, media)
                }
            }
    }

    //ViewModel
    private lateinit var mViewModel: BrowserViewModel

    private var mMedia: Media? = null
    private var parentBinding: FragmentImageBinding? = null

    //DataBinding
    private var mBinding: FragmentBrowserBinding? = null

    private lateinit var mVideoView: StyledPlayerView

    private var isViewAnimating = false

    private val mainScope = MainScope()

    override fun onDestroyView() {
        super.onDestroyView()
        if (mBinding != null) {
            mBinding!!.unbind()
            mBinding = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentBrowserBinding.inflate(inflater, container, false)
        @Suppress("DEPRECATION")
        mMedia = arguments?.getParcelable(MEDIA_PARAM)
        return mBinding!!.root
    }


    @SuppressLint("ClickableViewAccessibility", "SwitchIntDef")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentBinding = (parentFragment as ImageFragment).binding

        mainScope.launch {
            if (mMedia != null && parentBinding != null) {

                val cardView = parentBinding!!.imageCardView
                cardView.translationX = 0f
                cardView.translationY = 0f

                if (sMenuVisibility)
                    cardView.visibility = View.VISIBLE
                else cardView.visibility = View.INVISIBLE

                val clickListener = View.OnClickListener {

                    when (sMenuVisibility) {
                        false -> {
                            if (!isViewAnimating)
                            //   CoroutineScope(Main).launch {
                                showParentTools(cardView)
                            //   }
                        }
                        true -> {
                            if (!isViewAnimating)
                            //       CoroutineScope(Main).launch {
                                hideParentTools(cardView)
                            //      }

                        }
                    }
                }

                when (mMedia!!.type) {
                    MediaType.Image -> {
                        //  If media is an image
                        mBinding!!.imageImageView.visibility = View.VISIBLE
                        val imageView = mBinding!!.imageImageView

                        Glide.with(imageView)
                            .load(mMedia!!.uri)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .transition(DrawableTransitionOptions().crossFade())
                            .centerInside()
                            .into(imageView)

                        imageView.setOnClickListener(clickListener)
                    }

                    MediaType.Video -> {
                        //If media is a video
                        mViewModel =
                            ViewModelProvider(requireParentFragment())[mMedia!!.name, BrowserViewModel::class.java]

                        mVideoView = mBinding!!.videoVideoView
                        val videoView = mBinding!!.videoVideoView
                        mVideoView.visibility = View.VISIBLE
                        val player = ExoPlayer.Builder(requireContext()).build()
                        player.addMediaItem(MediaItem.fromUri(mMedia!!.uri))
                        videoView.setShowNextButton(false)
                        videoView.setShowPreviousButton(false)
                        videoView.player = player
                        videoView.setShowBuffering(StyledPlayerView.SHOW_BUFFERING_NEVER)
                        videoView.controllerShowTimeoutMs = 2500
                        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT


                        videoView.post {
                            if (mViewModel.isVideoPlaying && mViewModel.mTempPlayingState) {
                                player.play()
                                player.seekTo(mViewModel.currentVideoPosition)
                            }
                        }

                        videoView.setControllerVisibilityListener(StyledPlayerView.ControllerVisibilityListener {
                            if (!isViewAnimating) {
                                val view2 = parentBinding!!.imageCardView
                                if (it == View.VISIBLE)
                                //        CoroutineScope(Main).launch {
                                    showParentTools(view2)
                                //      }
                                else
                                //       CoroutineScope(Main).launch {
                                    hideParentTools(view2)
                                //      }
                            }
                        })



                        delay(150)
                        player.prepare()


                    }
                }
            }

        }
    }

    @Synchronized
    private fun showParentTools(view: View) {
        isViewAnimating = true
        sMenuVisibility = true
        view.visibility = View.VISIBLE
        view.post {
            AnimationBuilder(view)
                .rotateBy(30f).ms(80)
                .then().rotateBy(-15f).ms(80)
                .then().reset().ms(15)
                .execute()
            thread {
                Thread.sleep(200)
                isViewAnimating = false
            }
        }
    }

    @Synchronized
    private fun hideParentTools(view: View) {
        isViewAnimating = true
        sMenuVisibility = false
        AnimationBuilder(view)
            .rotateBy(180f).ms(70)
            .then().rotateBy(180f).ms(80)
            .then().reset().ms(40)
            .execute()
        view.postDelayed(200) {
            view.visibility = View.INVISIBLE
            isViewAnimating = false
        }
    }



    override fun onPause() {
        super.onPause()
        if (mMedia != null && this::mVideoView.isInitialized && this::mViewModel.isInitialized)
            if (mMedia!!.type == MediaType.Video) {
                mViewModel.mTempPlayingState = false
                mViewModel.currentVideoPosition = mVideoView.player!!.currentPosition
                mViewModel.isVideoPlaying = mVideoView.player!!.isPlaying
                mVideoView.player?.pause()
            }
    }

    override fun onStop() {
        super.onStop()
        if (mMedia != null && this::mViewModel.isInitialized)
            if (mMedia!!.type == MediaType.Video) {
                mViewModel.mTempPlayingState = mViewModel.isVideoPlaying
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mVideoView.isInitialized)
            mVideoView.player?.release()
    }


}


