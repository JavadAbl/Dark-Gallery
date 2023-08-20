package com.javadabl.darkgallery.ui.image

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.bumptech.glide.Glide
import com.javadabl.darkgallery.R
import com.javadabl.darkgallery.data.local.file.model.MediaType
import com.javadabl.darkgallery.databinding.FragmentImageBinding
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.MainActivity
import com.javadabl.darkgallery.ui.album.AlbumViewModel
import com.javadabl.darkgallery.ui.browser.BrowserFragment
import com.javadabl.darkgallery.ui.image.adapter.ImagePagerAdapter
import com.javadabl.darkgallery.ui.images.ImagesDeleteModeUtil
import com.javadabl.darkgallery.util.AppUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


class ImageFragment : Fragment() {


    //DataBinding
    private var mBinding: FragmentImageBinding? = null
    val binding: FragmentImageBinding get() = mBinding!!
    //  private val mArgs by navArgs<ImageFragmentArgs>()

    //Data
    private var mDeletePosition = -1

    //ViewPager
    private lateinit var mPager: ViewPager2
    private lateinit var mPagerAdapter: ImagePagerAdapter

    //ViewModel
    private lateinit var mViewModel: AlbumViewModel
    private lateinit var mImageViewModel: ImageViewModel

    //General
    private var mPagerFirstInit = true

    private val mainScope = MainScope()


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding!!.imagePagerViewPager.adapter = null
        mBinding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity())[AlbumViewModel::class.java]
        mImageViewModel = ViewModelProvider(this)[ImageViewModel::class.java]
        if (mImageViewModel.currentPosition == -1)
            mImageViewModel.currentPosition = mViewModel.currentViewingPosition
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_image, container, false)
        mPager = mBinding!!.imagePagerViewPager
        mPager.offscreenPageLimit = 1

        val activity = (requireActivity() as MainActivity)
        activity.mainActivityDataBinding.topAppBar.visibility = View.GONE

        mainScope.launch {

            Glide.with(mBinding!!.imageDeleteButton)
                .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_delete, null))
                .into(mBinding!!.imageDeleteButton)

            Glide.with(mBinding!!.imageDetailsButton)
                .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_details, null))
                .into(mBinding!!.imageDetailsButton)

            Glide.with(mBinding!!.imageShareButton)
                .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_share, null))
                .into(mBinding!!.imageShareButton)

            Glide.with(mBinding!!.imageEditButton)
                .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_edit, null))
                .into(mBinding!!.imageEditButton)

            Glide.with(mBinding!!.imageBackButton)
                .load(ResourcesCompat.getDrawable(resources, R.drawable.icon_left_arrow, null))
                .into(mBinding!!.imageBackButton)

            BrowserFragment.sMenuVisibility = true


          withContext(IO) {
                mPagerAdapter = ImagePagerAdapter(
                    this@ImageFragment,
                    mViewModel.currentViewingAlbum.media
                )
            }

            mPager.adapter = mPagerAdapter


            //Click Listeners
            backBtnClick()
            shareBtnClick()
            editBtnClick()
            detailBtnClick()
            deleteBtnClick()


            Glide.with(mBinding!!.imageBackground1.context)
                .load(R.drawable.background_image_bar)
                .centerCrop().into(mBinding!!.imageBackground1)

            Glide.with(mBinding!!.imageBackground2.context)
                .load(R.drawable.background_image_bar)
                .centerCrop().into(mBinding!!.imageBackground2)

            mPager.postOnAnimation {
                mPager.setCurrentItem(mImageViewModel.currentPosition, false)
                mPagerFirstInit = false


                mPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        if (!mPagerFirstInit) {
                            mViewModel.currentViewingPosition = position
                            mImageViewModel.currentPosition = position
                        }
                    }
                })


                AnimationBuilder.backgroundAnimation(
                    mBinding!!.imageBackground1,
                    mBinding!!.imageBackground2, 3700
                )
            }
        }

        return mBinding!!.root
    }


    private fun backBtnClick() {
        mBinding!!.imageBackButton.setOnClickListener {
            btnAnimation(it)
            findNavController().navigateUp()
        }
    }

    private fun editBtnClick() {
        mBinding!!.imageEditButton.setOnClickListener {

            btnAnimation(it)

            val index = mBinding!!.imagePagerViewPager.currentItem
            /* val uri = FileProvider.getUriForFile(
                 requireContext(),
                 "com.javadabl.darkgallery.provider",
                 File(mAlbum.media[index].uri.path!!)
             )*/

            val type = mViewModel.currentViewingAlbum.media[index].type

            val editIntent = Intent(Intent.ACTION_EDIT)
            if (type == MediaType.Image)
                editIntent.type = "image/*"
            if (type == MediaType.Video)
                editIntent.type = "video/*"
            editIntent.setData(mViewModel.currentViewingAlbum.media[index].uri)
            editIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            // startActivity(Intent.createChooser(editIntent, null))

            requireActivity().startActivities(
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
        mBinding!!.imageShareButton.setOnClickListener {

            btnAnimation(it)

            val index = mBinding!!.imagePagerViewPager.currentItem
            /*val imageUri = FileProvider.getUriForFile(
                requireContext(),
                "com.javadabl.darkgallery.provider",
                File(mAlbum.media[index].uri.path!!)
            )*/

            val sharingIntent = Intent(Intent.ACTION_SEND)
            if (mViewModel.currentViewingAlbum.media[mViewModel.currentViewingPosition].type == MediaType.Image)
                sharingIntent.type = "image/*"
            if (mViewModel.currentViewingAlbum.media[mViewModel.currentViewingPosition].type == MediaType.Video)
                sharingIntent.type = "video/*"
            sharingIntent.putExtra(
                Intent.EXTRA_STREAM,
                mViewModel.currentViewingAlbum.media[index].uri
            )

            requireActivity().startActivities(
                arrayOf(
                    Intent.createChooser(
                        sharingIntent,
                        "Share with"
                    )
                )
            )
        }
    }


    private fun detailBtnClick() {
        mBinding!!.imageDetailsButton.setOnClickListener {

            btnAnimation(it)
            val dialog =
                DetailsDialog(
                    requireActivity(),
                    mViewModel.currentViewingAlbum.media[mImageViewModel.currentPosition]
                )
            dialog.show()
        }
    }


    private fun deleteBtnClick() {
        if ((mViewModel.currentViewingAlbum.media.size <= mImageViewModel.currentPosition))
            return
        mBinding!!.imageDeleteButton.setOnClickListener {

            btnAnimation(it)

            AlertDialog.Builder(requireContext())
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure, you want to continue ?")
                .setPositiveButton(
                    "Delete"
                ) { dialog, which ->
                    mDeletePosition = mImageViewModel.currentPosition
                    when {
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) -> {
                            ImagesDeleteModeUtil.singleDeleteRequestAPIAbove30(
                                mViewModel.currentViewingAlbum.media[mImageViewModel.currentPosition],
                                mViewModel
                            ).observe(viewLifecycleOwner) {
                                startIntentSenderForResult(
                                    it, MainActivity.REQUEST_DELETE_SENDER, null, 0, 0, 0, null
                                )
                            }
                        }
                        (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) -> {
                            if (AppUtil.checkWriteExternalStoragePermission(requireContext())) {
                                ImagesDeleteModeUtil.singleDeleteRequestAPIBelow29(
                                    mViewModel.currentViewingAlbum.media[mImageViewModel.currentPosition],
                                    mViewModel
                                )
                                afterDeleteOps()
                            } else
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    requestPermissions(
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        MainActivity.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                                    )
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, which ->
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
        when (requestCode) {
            MainActivity.REQUEST_DELETE_SENDER -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(requireContext(), "Item deleted..", Toast.LENGTH_LONG).show()
                afterDeleteOps()
            } else
                Toast.makeText(requireContext(), "An error happens..", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MainActivity.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagesDeleteModeUtil.singleDeleteRequestAPIBelow29(
                    media = mViewModel.currentViewingAlbum.media[mImageViewModel.currentPosition],
                    mViewModel
                )
                afterDeleteOps()
            }
    }

    private fun afterDeleteOps() {
        if (mDeletePosition != -1) {
            mViewModel.currentViewingAlbum.media.removeAt(mDeletePosition)
            mPagerAdapter.setData(mDeletePosition)
            mDeletePosition = -1
        }
        mViewModel.updateAlbums()
    }

}