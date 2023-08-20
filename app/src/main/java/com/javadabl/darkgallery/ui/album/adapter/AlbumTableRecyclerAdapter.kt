package com.javadabl.darkgallery.ui.album.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.javadabl.darkgallery.databinding.ItemAlbumTableBinding
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.album.AlbumTableFragment
import com.javadabl.darkgallery.util.C
import kotlin.random.Random


class AlbumTableRecyclerAdapter : RecyclerView.Adapter<AlbumTableRecyclerAdapter.ViewHolder>() {

    private var data = ArrayList<Album>()
    private lateinit var mFragment: AlbumTableFragment


    class ViewHolder(private val binding: ItemAlbumTableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album, fragment: AlbumTableFragment, position: Int) {

            val randomNum = Random.nextInt(0, C.emojiBackgrounds.size)

            Glide.with(fragment.requireContext())
                .load(C.emojiBackgrounds[randomNum])
                .fitCenter().into(binding.albumItemBackground1)

            Glide.with(fragment.requireContext())
                .load(C.emojiBackgrounds[randomNum])
                .fitCenter().into(binding.albumItemBackground2)

            AnimationBuilder.backgroundAnimation(
                binding.albumItemBackground1,
                binding.albumItemBackground2
            )

            binding.album = album
            binding.fragment = fragment
            binding.position = position

            if (album.selected)
                binding.iconChecked.visibility = View.VISIBLE
            else
                binding.iconChecked.visibility = View.GONE

                if (album.media.size > 0)
                    binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAlbumTableBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = data[position]
        holder.bind(album, mFragment, position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: ArrayList<Album>, fragment: AlbumTableFragment) {
        this.data = data
        mFragment = fragment
        notifyDataSetChanged()
    }

    fun setChecked(position: Int, album: Album) {
        data[position] = album
        notifyItemChanged(position)
    }
}