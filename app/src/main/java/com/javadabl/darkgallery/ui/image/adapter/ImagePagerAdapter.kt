package com.javadabl.darkgallery.ui.image.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.ui.browser.BrowserFragment
import com.javadabl.darkgallery.ui.browser.BrowserFragment.Companion.newInstance

class ImagePagerAdapter(
    fragment: Fragment,
    private val media: ArrayList<Media>
) :
    FragmentStateAdapter(fragment) {

    private val fragments = ArrayList<BrowserFragment>()

    init {
        for (i in media.indices) {
            fragments.add(newInstance(media[i]))
        }
    }

    fun setData(position: Int) {
        fragments.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemId(position: Int): Long {
        return media[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        var contains = false
        for (it in media.indices) if (media[it].hashCode().toLong() == itemId) {
            contains = true
            break
        }
        return contains
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}