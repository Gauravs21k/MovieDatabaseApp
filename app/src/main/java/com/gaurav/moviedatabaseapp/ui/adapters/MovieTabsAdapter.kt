package com.gaurav.moviedatabaseapp.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gaurav.moviedatabaseapp.ui.fragments.TrendingMoviesFragment
import com.gaurav.moviedatabaseapp.ui.fragments.NowPlayingMoviesFragment
import com.gaurav.moviedatabaseapp.ui.fragments.BookmarkedMoviesFragment

class MovieTabsAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TrendingMoviesFragment()
            1 -> NowPlayingMoviesFragment()
            2 -> BookmarkedMoviesFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    fun getTabTitle(position: Int): String {
        return when (position) {
            0 -> "Trending"
            1 -> "Now Playing"
            2 -> "Saved"
            else -> ""
        }
    }
}