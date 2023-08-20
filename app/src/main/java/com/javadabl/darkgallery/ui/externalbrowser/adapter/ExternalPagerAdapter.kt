package com.javadabl.darkgallery.ui.externalbrowser.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.ui.externalbrowser.ExternalBrowserFragment

class ExternalPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val album: Album
) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments = ArrayList<ExternalBrowserFragment>()

    init {
        album.media.forEach {
            val fragment = ExternalBrowserFragment.newInstance(it)
            fragments.add(fragment)
        }
    }

    fun setData(position: Int){
        fragments.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun containsItem(itemId: Long): Boolean {
        var contains = false
        for (it in album.media)
            if (it.hashCode().toLong() == itemId) {
                contains = true
                break
            }
        return contains
    }

    override fun getItemId(position: Int): Long {
        return album.media[position].hashCode().toLong()
    }

    override fun getItemCount() = album.imagesCount

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }


}