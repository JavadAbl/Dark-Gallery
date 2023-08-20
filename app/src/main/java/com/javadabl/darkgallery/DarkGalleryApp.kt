package com.javadabl.darkgallery

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


private const val TAG = "MyTag"

@HiltAndroidApp
class DarkGalleryApp : Application() {


    override fun onCreate() {
        super.onCreate()

        /*ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/WendyOne-Regular.ttf")
                         //   .setFontAttrId(R.attr.font)
                            .build()
                    )
                )
                .build()
        )*/
        //  Glide.get(this).setMemoryCategory(MemoryCategory.NORMAL)
        //   Glide.get(this).


    }


}
