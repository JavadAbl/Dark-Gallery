package com.javadabl.darkgallery.ui.images.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.javadabl.darkgallery.data.local.file.model.MediaType
import com.javadabl.darkgallery.databinding.ItemImagesBinding
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.ui.images.ImageRowFragment

class ImagesRecyclerAdapter : RecyclerView.Adapter<ImagesRecyclerAdapter.ViewHolder>() {


    private lateinit var mFragment: ImageRowFragment
    private lateinit var data: Album


    class ViewHolder(private val binding: ItemImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(media: Media, position: Int, fragment: ImageRowFragment) {


            binding.media = media
            binding.position = position
            binding.fragment = fragment
            if (media.type == MediaType.Video)
                binding.imagesBotCard.visibility = View.VISIBLE
            else
                binding.imagesBotCard.visibility = View.GONE

            if (media.selected)
                binding.iconChecked.visibility = View.VISIBLE
            else
                binding.iconChecked.visibility = View.GONE

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImagesBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = data.media[position]
        holder.bind(image, position, mFragment)
    }

    override fun getItemCount(): Int {
        return if (this::data.isInitialized) data.imagesCount else 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: Album, imageRowFragment: ImageRowFragment) {
        this.data = data
        mFragment = imageRowFragment
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return data.media[position].ID.toLong()
    }


    fun setChecked(position: Int, media: Media) {
        data.media[position] = media
        notifyItemChanged(position)
    }

}