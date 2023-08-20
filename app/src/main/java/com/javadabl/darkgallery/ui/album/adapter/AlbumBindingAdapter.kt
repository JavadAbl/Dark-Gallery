package com.javadabl.darkgallery.ui.album.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.album.AlbumTableFragment
import com.javadabl.darkgallery.ui.album.AlbumTableFragmentDirections

private const val TAG = "mytag"

object AlbumBindingAdapter {


    @SuppressLint("ResourceAsColor")
    @BindingAdapter("albumOnClick", "albumOnClickIndex", "albumOnClickFragment", requireAll = true)
    @JvmStatic
    fun albumTableOnClick(
        container: ConstraintLayout,
        album: Album,
        index: Int,
        fragment: AlbumTableFragment
    ) {
        container.setOnClickListener {
            when (fragment.deleteModeUtil.getSelectMode()) {
                false -> {
                    fragment.viewModel.currentViewingAlbum = album
                    val action =
                        AlbumTableFragmentDirections.actionAlbumTableFragmentToImagesFragment()
                    container.findNavController().navigate(action)
                }
                true -> {
                    if (fragment.deleteModeUtil.contain(album)) {
                        fragment.deleteModeUtil.removePending(album, index)
                    } else {
                        fragment.deleteModeUtil.addSelectedItem(album, index)
                    }
                }
            }
        }
    }


    @BindingAdapter("setImage", "fragment", requireAll = true)
    @JvmStatic
    fun setImage(imageView: ImageView, path: Uri, fragment: Fragment) {

        AnimationBuilder.itemArrivalAnimation(imageView)

        val thumbnailBuilder: RequestBuilder<Drawable> = Glide.with(imageView)
            .asDrawable().sizeMultiplier(0.30f)

        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

        Glide.with(imageView).load(path)
            .load(path)
            .apply(options)
            .transition(DrawableTransitionOptions().crossFade())
            .thumbnail(thumbnailBuilder)
            .centerCrop()
            .into(imageView)
    }


    @SuppressLint("ResourceAsColor")
    @BindingAdapter("albumOnLongClick", "albumOnLongClickIndex", "fragment", requireAll = true)
    @JvmStatic
    fun albumOnLongClick(
        container: ConstraintLayout,
        album: Album,
        index: Int,
        fragment: AlbumTableFragment
    ) {
        container.setOnLongClickListener {
            when (fragment.deleteModeUtil.getSelectMode()) {
                false -> {
                    fragment.deleteModeUtil.enterSelectMode()
                    fragment.deleteModeUtil.addSelectedItem(album, index)
                    return@setOnLongClickListener true
                }
                true -> {
                    if (fragment.deleteModeUtil.contain(album)) {
                        fragment.deleteModeUtil.removePending(album, index)
                    } else {
                        fragment.deleteModeUtil.addSelectedItem(album, index)
                    }
                    return@setOnLongClickListener true
                }
            }
        }
    }


}




