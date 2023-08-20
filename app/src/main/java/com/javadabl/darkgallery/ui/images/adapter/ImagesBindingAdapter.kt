package com.javadabl.darkgallery.ui.images.adapter

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.javadabl.darkgallery.R
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.ui.images.ImageRowFragment
import com.javadabl.darkgallery.ui.images.ImageRowFragmentDirections


object ImagesBindingAdapter {


    @SuppressLint("ResourceAsColor")
    @BindingAdapter(
        "ImagesOnClickData",
        "ImagesOnClickIndex",
        "ImagesOnClickFragment",
        requireAll = true
    )
    @JvmStatic
    fun albumTableOnClick(
        container: ConstraintLayout,
        media: Media,
        index: Int,
        fragment: ImageRowFragment
    ) {
        container.setOnClickListener {
            when (fragment.deleteModeUtil.getSelectMode()) {
                false -> {
                    fragment.viewModel.currentViewingPosition = index
                    val action =
                        ImageRowFragmentDirections.actionImagesFragmentToImageFragment()
                    container.findNavController().navigate(action)
                }
                true -> {
                    if (fragment.deleteModeUtil.contain(media)) {
                        fragment.deleteModeUtil.removePending(
                            media, index
                        )
                    } else {
                        fragment.deleteModeUtil.addSelectedItem(
                            media, index
                        )
                    }
                }
            }
        }
    }


    @SuppressLint("ResourceAsColor")
    @BindingAdapter("ImagesOnLongClickData","ImagesOnLongClickIndex", "ImagesOnLongClickFragment", requireAll = true)
    @JvmStatic
    fun albumOnLongClick(
        container: ConstraintLayout,
        media: Media,
        index:Int,
        fragment: ImageRowFragment
    ) {
        container.setOnLongClickListener {
            when (fragment.deleteModeUtil.getSelectMode()) {
                false -> {
                    fragment.deleteModeUtil.enterSelectMode()
                    fragment.deleteModeUtil.addSelectedItem(media, index)
                    return@setOnLongClickListener true
                }
                true -> {
                    if (fragment.deleteModeUtil.contain(media)) {
                        fragment.deleteModeUtil.removePending(media, index)
                    } else {
                        fragment.deleteModeUtil.addSelectedItem(media, index)
                    }
                    return@setOnLongClickListener true
                }
            }
        }
    }

}