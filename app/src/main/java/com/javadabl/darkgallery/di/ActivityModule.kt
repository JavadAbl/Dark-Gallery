package com.javadabl.darkgallery.di


import com.javadabl.darkgallery.ui.album.adapter.AlbumTableRecyclerAdapter
import com.javadabl.darkgallery.ui.images.adapter.ImagesRecyclerAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped

@Module
//@InstallIn(FragmentComponent::class)
@InstallIn(ActivityComponent::class)
object ActivityModule {

   //  @FragmentScoped
    @ActivityScoped
    @Provides
    fun provideAlbumRecyclerAdapter(): AlbumTableRecyclerAdapter = AlbumTableRecyclerAdapter()



 //   @FragmentScoped
    @ActivityScoped
    @Provides
    fun provideImagesRecyclerAdapter(): ImagesRecyclerAdapter = ImagesRecyclerAdapter()
}