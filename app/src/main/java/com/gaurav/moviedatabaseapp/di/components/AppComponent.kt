package com.gaurav.moviedatabaseapp.di.components

import com.gaurav.moviedatabaseapp.di.modules.AppModule
import com.gaurav.moviedatabaseapp.ui.activities.MainActivity
import com.gaurav.moviedatabaseapp.ui.activities.MovieDetailsActivity
import com.gaurav.moviedatabaseapp.ui.fragments.BookmarksFragment
import com.gaurav.moviedatabaseapp.ui.fragments.BookmarkedMoviesFragment
import com.gaurav.moviedatabaseapp.ui.fragments.HomeFragment
import com.gaurav.moviedatabaseapp.ui.fragments.NowPlayingMoviesFragment
import com.gaurav.moviedatabaseapp.ui.fragments.TrendingMoviesFragment
import com.gaurav.moviedatabaseapp.ui.viewmodels.MovieViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: MovieDetailsActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: BookmarksFragment)
    fun inject(fragment: TrendingMoviesFragment)
    fun inject(fragment: NowPlayingMoviesFragment)
    fun inject(fragment: BookmarkedMoviesFragment)
}
