package com.xenderx.mediaplayer.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.xenderx.mediaplayer.ui.fragments.AudioFragment
import com.xenderx.mediaplayer.ui.fragments.VideoFragment

class TabPagerAdapter(
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val mPageTitles = listOf("Audio")

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AudioFragment()
            else -> VideoFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? = mPageTitles[position]

    override fun getCount(): Int = mPageTitles.size
}