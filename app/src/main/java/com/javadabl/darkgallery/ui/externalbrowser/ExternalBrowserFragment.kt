package com.javadabl.darkgallery.ui.externalbrowser

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
import com.javadabl.darkgallery.databinding.ActivityExternalBrowserBinding
import com.javadabl.darkgallery.databinding.FragmentExternalBrowserBinding
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.browser.BrowserViewModel
import kotlinx.coroutines.MainScope
import kotlin.concurrent.thread


private const val MEDIA_PARAM1 = "mMedia"


class ExternalBrowserFragment : Fragment() {

    //ViewModel
    private lateinit var mViewModel: BrowserViewModel

    //Data
    private var mMedia: Media? = null

    //DataBinding
    private var mBinding: FragmentExternalBrowserBinding? = null
    private lateinit var parentBinding: ActivityExternalBrowserBinding

    //Booleans
    private var isViewAnimating: Boolean = false

    private lateinit var mVideoView: StyledPlayerView

    private val mainScope = MainScope()

    override fun onDestroyView() {
        super.onDestroyView()
        if (mBinding != null) {
            mBinding!!.unbind()
            mBinding = null
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mMedia = it.getParcelable(MEDIA_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentExternalBrowserBinding.inflate(inflater, container, false)
        parentBinding = (requireActivity() as ExternalBrowserActivity).binding


        val cardView = parentBinding.externalCardView

        cardView.translationX = 0f
        cardView.translationY = 0f

        val clickListener = View.OnClickListener {

            when (sMenuVisibility) {
                false -> {
                    if (!isViewAnimating)
                        showParentTools(cardView)
                }
                true -> {
                    if (!isViewAnimating)
                        hideParentTools(cardView)
                }
            }
        }

        when (mMedia!!.type) {
            MediaType.Image -> {
                //  If media is an image
                val imageView = mBinding!!.externalImageView
                imageView.visibility = View.VISIBLE

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
                    ViewModelProvider(this)[mMedia!!.name, BrowserViewModel::class.java]

                mVideoView = mBinding!!.externalVideoView
                mVideoView.visibility = View.VISIBLE
                val player = ExoPlayer.Builder(requireContext()).build()
                player.addMediaItem(MediaItem.fromUri(mMedia!!.uri))
                mVideoView.setShowNextButton(false)
                mVideoView.setShowPreviousButton(false)
                mVideoView.player = player
                mVideoView.setShowBuffering(StyledPlayerView.SHOW_BUFFERING_NEVER)
                mVideoView.controllerShowTimeoutMs = 2500
                player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

                mVideoView.post {
                    if (mViewModel.isVideoPlaying && mViewModel.mTempPlayingState) {
                        player.play()
                        player.seekTo(mViewModel.currentVideoPosition)
                    }
                }


                mVideoView.setControllerVisibilityListener(StyledPlayerView.ControllerVisibilityListener {
                    if (!isViewAnimating) {
                        val view2 = parentBinding.externalCardView
                        if (it == View.VISIBLE)
                            showParentTools(view2)
                        else
                            hideParentTools(view2)
                    }
                })


                player.prepare()


            }
        }

        return mBinding!!.root
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


    companion object {

        private var sMenuVisibility = true

        @JvmStatic
        fun newInstance(media: Media) =
            ExternalBrowserFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(MEDIA_PARAM1, media)
                }
            }
    }
}