package com.javadabl.darkgallery.ui.externalbrowser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.javadabl.darkgallery.R
import com.javadabl.darkgallery.data.local.file.model.MediaType
import com.javadabl.darkgallery.databinding.ActivityExternalBrowserBinding
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.MainActivity
import com.javadabl.darkgallery.ui.externalbrowser.adapter.ExternalPagerAdapter
import com.javadabl.darkgallery.ui.image.DetailsDialog
import com.javadabl.darkgallery.util.AppUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

private const val TAG = "MyTag"

class ExternalBrowserActivity : AppCompatActivity() {


    //  private val mViewModel by viewModels<BrowserViewModel>()
    private lateinit var mViewModel: ExternalBrowserViewModel

    //Data
    private lateinit var mAlbum: Album

    //DataBinding
    private lateinit var mBinding: ActivityExternalBrowserBinding
    val binding: ActivityExternalBrowserBinding get() = mBinding

    private lateinit var mPager: ViewPager2

    private var mPagerFirstInit = true

    private val mainScope = MainScope()


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {

        val window: Window = this.window
        window.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)


        mViewModel = ViewModelProvider(this)[ExternalBrowserViewModel::class.java]


        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_external_browser
        )


        mPager = mBinding.externalPager
        mPager.offscreenPageLimit = 1


        val type = intent.type?.split('/', ignoreCase = true)?.get(0)

        var checkUri: MediaType? = null
        if ("image" == type)
            checkUri = MediaType.Image
        if ("video" == type)
            checkUri = MediaType.Video

        mainScope.launch {
            if (intent.data != null && checkUri != null) {
                mViewModel.getAlbumFromUri(intent.data!!, checkUri)
                setupPager()
            }
        }

        backBtnClick()
        setupMenuBar()

    }


    private fun setupPager() {
        mViewModel.album.observe(this) {
            if (it != null) {
                mAlbum = it
                findCurrentPosition()

                val adapter = ExternalPagerAdapter(this@ExternalBrowserActivity, mAlbum)
                mPager.adapter = adapter

                shareBtnClick()
                editBtnClick()
                detailBtnClick()
                deleteBtnClick()

                mPager.post {
                    mPager.setCurrentItem(mViewModel.currentViewingPosition, false)
                    mPagerFirstInit = false
                }

            }
        }



        mPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!mPagerFirstInit)
                    mViewModel.currentViewingPosition = position
            }
        })
    }

    private fun findCurrentPosition() {
        if (mViewModel.currentViewingPosition == -1) {
            val name = File(intent.data!!.path!!).name
            var i = 0
            for (it2 in mAlbum.media) {
                if (it2.name == name) {
                    mViewModel.currentViewingPosition = i
                    break
                }
                i++
            }
        }
        if (mViewModel.currentViewingPosition == -1)
            mViewModel.currentViewingPosition = 0
    }


    private fun setupMenuBar() {
        Glide.with(mBinding.externalDeleteButton)
            .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_delete, null))
            .into(mBinding.externalDeleteButton)

        Glide.with(mBinding.externalDetailsButton)
            .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_details, null))
            .into(mBinding.externalDetailsButton)

        Glide.with(mBinding.externalShareButton)
            .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_share, null))
            .into(mBinding.externalShareButton)

        Glide.with(mBinding.externalEditButton)
            .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_edit, null))
            .into(mBinding.externalEditButton)

        Glide.with(mBinding.externalBackButton)
            .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_left_arrow, null))
            .into(mBinding.externalBackButton)

        Glide.with(mBinding.externalBackground1.context)
            .load(R.drawable.background_image_bar)
            .centerCrop().into(mBinding.externalBackground1)

        Glide.with(mBinding.externalBackground2.context)
            .load(R.drawable.background_image_bar)
            .centerCrop().into(mBinding.externalBackground2)


        AnimationBuilder.backgroundAnimation(
            mBinding.externalBackground1,
            mBinding.externalBackground2, 3700
        )
    }


    private fun backBtnClick() {
        mBinding.externalBackButton.setOnClickListener {
            finish()
        }
    }


    private fun editBtnClick() {
        mBinding.externalEditButton.setOnClickListener {

            btnAnimation(it)

            val editIntent = Intent(Intent.ACTION_EDIT)
            if (mAlbum.media[mViewModel.currentViewingPosition].type == MediaType.Image)
                editIntent.type = "image/*"
            if (mAlbum.media[mViewModel.currentViewingPosition].type == MediaType.Video)
                editIntent.type = "video/*"
            editIntent.setData(mAlbum.media[mViewModel.currentViewingPosition].uri)
            editIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            startActivities(
                arrayOf(
                    Intent.createChooser(
                        editIntent,
                        "Edit with"
                    )
                )
            )
        }
    }


    private fun shareBtnClick() {
        mBinding.externalShareButton.setOnClickListener {

            btnAnimation(it)

            val sharingIntent = Intent(Intent.ACTION_SEND)
            if (mAlbum.media[mViewModel.currentViewingPosition].type == MediaType.Image)
                sharingIntent.type = "image/*"
            if (mAlbum.media[mViewModel.currentViewingPosition].type == MediaType.Video)
                sharingIntent.type = "video/*"
            sharingIntent.putExtra(
                Intent.EXTRA_STREAM,
                mAlbum.media[mViewModel.currentViewingPosition].uri
            )
            startActivity(Intent.createChooser(sharingIntent, "Share with"))
            /*startActivities(
                arrayOf(
                    Intent.createChooser(
                        sharingIntent,
                        "Share with"
                    )
                )
            )*/

        }
    }


    private fun detailBtnClick() {
        mBinding.externalDetailsButton.setOnClickListener {

            btnAnimation(it)
            val dialog =
                DetailsDialog(
                    this,
                    mAlbum.media[mPager.currentItem]
                )
            dialog.show()
        }
    }


    private fun deleteBtnClick() {

        mBinding.externalDeleteButton.setOnClickListener {

            btnAnimation(it)

            AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure, you want to continue ?")
                .setPositiveButton(
                    "Delete"
                ) { _, _ ->

                    when {
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) -> {
                            try {
                                val intentSender =
                                    MediaStore.createDeleteRequest(
                                        contentResolver,
                                        listOf(mAlbum.media[mPager.currentItem].uri)
                                    ).intentSender


                                @Suppress("DEPRECATION")
                                startIntentSenderForResult(
                                    intentSender,
                                    MainActivity.REQUEST_DELETE_SENDER,
                                    null,
                                    0,
                                    0,
                                    0
                                )
                            } catch (e: Exception) {
                                Toast.makeText(this, "A error happens..", Toast.LENGTH_LONG).show()
                            }
                        }
                        (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) -> {
                            if (AppUtil.checkWriteExternalStoragePermission(this)) {
                                if (File(mAlbum.media[mPager.currentItem].path).delete()) {
                                    Toast.makeText(this, "Image deleted.", Toast.LENGTH_LONG).show()
                                    afterDeleteOps()
                                } else
                                    Toast.makeText(
                                        this,
                                        "Image do not deleted...",
                                        Toast.LENGTH_LONG
                                    ).show()
                            } else
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    requestPermissions(
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        MainActivity.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                                    )
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    @Synchronized
    private fun btnAnimation(view: View) {
        AnimationBuilder(view)
            .scaleX(2.0f).scaleY(2.0f).ms(170)
            .then()
            .reset().ms(80)
            .execute()
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MainActivity.REQUEST_DELETE_SENDER -> if (resultCode == RESULT_OK) {
                Toast.makeText(this, "image deleted..", Toast.LENGTH_LONG).show()
                afterDeleteOps()
            } else
                Toast.makeText(this, "An error happens..", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (File(mAlbum.media[mPager.currentItem].path).delete()) {
                    Toast.makeText(this, "Image deleted.", Toast.LENGTH_LONG).show()
                    afterDeleteOps()
                } else
                    Toast.makeText(
                        this,
                        "Image do not deleted...",
                        Toast.LENGTH_LONG
                    ).show()
            }
    }

    private fun afterDeleteOps() {
        mAlbum.media.removeAt(mViewModel.currentViewingPosition)
        if (mAlbum.imagesCount == 0)
            finish()
        (mPager.adapter as ExternalPagerAdapter).setData(mViewModel.currentViewingPosition)
    }

}