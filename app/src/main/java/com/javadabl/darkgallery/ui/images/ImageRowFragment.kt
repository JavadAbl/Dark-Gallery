package com.javadabl.darkgallery.ui.images

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.javadabl.darkgallery.R
import com.javadabl.darkgallery.databinding.FragmentImagesBinding
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.MainActivity
import com.javadabl.darkgallery.ui.album.AlbumViewModel
import com.javadabl.darkgallery.ui.images.adapter.ImagesRecyclerAdapter
import com.javadabl.darkgallery.util.AppUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ImageRowFragment : Fragment() {


    @Inject
    lateinit var mAdapter: ImagesRecyclerAdapter
    private var mBinding: FragmentImagesBinding? = null
    //   private val mArgs: ImageRowFragmentArgs by navArgs()


    //ViewModel
    private lateinit var mViewModel: AlbumViewModel
    val viewModel: AlbumViewModel get() = mViewModel

    //DeleteModeUtil
    lateinit var deleteModeUtil: ImagesDeleteModeUtil

    //MainScope
    private val mainScope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity())[AlbumViewModel::class.java]
        //   mAlbum = mArgs.album
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding!!.imagesRecycleView.adapter = null
        mBinding!!.unbind()
        mBinding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentImagesBinding.inflate(inflater, container, false)

        val activityBinding = (requireActivity() as MainActivity).mainActivityDataBinding
        activityBinding.topAppBar.visibility = View.VISIBLE

        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.imagesFragment || destination.id == R.id.imageFragment)
                activityBinding.topAppBar.title = mViewModel.currentViewingAlbum.name

        }

        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainScope.launch {
            setupRecyclerView()

            mMenuHost = requireActivity()

            //DeleteModeUtil Initialize
            deleteModeUtil =
                ImagesDeleteModeUtil(
                    mAdapter,
                    mViewModel,
                    menuHost,
                    menuProvider,
                )


            //Nav BackStack
            requireActivity()
                .onBackPressedDispatcher
                .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    @Suppress("DEPRECATION")
                    override fun handleOnBackPressed() {
                        if (deleteModeUtil.getSelectMode()) {
                            deleteModeUtil.exitSelectMode()
                        } else
                            findNavController().popBackStack()
                    }
                }
                )


            Glide.with(requireContext())
                .load(R.drawable.background_images)
                .centerCrop().into(mBinding!!.imagesBackground1)

            Glide.with(requireContext())
                .load(R.drawable.background_images)
                .centerCrop().into(mBinding!!.imagesBackground2)


            AnimationBuilder.backgroundAnimation(
                mBinding!!.imagesBackground1, mBinding!!.imagesBackground2,
                3700
            )
        }

    }

    private fun setupRecyclerView() {

        mBinding!!.imagesRecycleView.post {
            mBinding!!.imagesRecycleView.layoutManager = GridLayoutManager(
                requireContext(),
                4,
                GridLayoutManager.HORIZONTAL,
                false
            )
            mBinding!!.imagesRecycleView.setHasFixedSize(true)
            mAdapter.setHasStableIds(true)
            mBinding!!.imagesRecycleView.adapter = mAdapter
            mAdapter.setData(mViewModel.currentViewingAlbum, this)
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////
    //Delete Mode

    private lateinit var mMenuHost: MenuHost
    private val menuHost get() = mMenuHost

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MainActivity.REQUEST_DELETE_SENDER -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    requireContext(),
                    "${deleteModeUtil.getItemsSize()} items deleted..", Toast.LENGTH_LONG
                ).show()
                mViewModel.currentViewingAlbum.media.removeAll(deleteModeUtil.selectedMedia)
                mAdapter.setData(mViewModel.currentViewingAlbum, this)
                mViewModel.updateAlbums()
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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                deleteModeUtil.deleteRequestAPIBelow29().observe(viewLifecycleOwner) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG)
                        .show()
                    mViewModel.currentViewingAlbum.media.removeAll(deleteModeUtil.selectedMedia)
                    mAdapter.setData(mViewModel.currentViewingAlbum, this@ImageRowFragment)
                    mViewModel.updateAlbums()
                }
            else deleteModeUtil.exitSelectMode()
    }

/////////////////////////////////////////////////////////////////////////////////////////

    private val menuProvider = object : MenuProvider {
        override fun onPrepareMenu(menu: Menu) {
            // Handle for example visibility of menu items
        }

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.add("Delete")
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setIcon(R.drawable.icon_delete)

        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.title == "Delete") {
                deleteModeUtil.exitSelectMode()
                val dialogBuilder =
                    AlertDialog.Builder(requireContext())
                dialogBuilder.setTitle("Delete Confirmation")
                    .setMessage("Are you sure, you want to continue ?")
                    .setCancelable(true)
                    .setPositiveButton(
                        "Delete"
                    ) { _, _ ->

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            deleteModeUtil.deleteRequestAPIAbove30().observe(viewLifecycleOwner) {
                                if (it != null)
                                    startIntentSenderForResult(
                                        it,
                                        MainActivity.REQUEST_DELETE_SENDER,
                                        null,
                                        0,
                                        0,
                                        0,
                                        null
                                    )
                            }
                        } else {
                            val result =
                                AppUtil.checkWriteExternalStoragePermission(requireContext())
                            if (result)
                                deleteModeUtil.deleteRequestAPIBelow29()
                                    .observe(viewLifecycleOwner) {
                                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG)
                                            .show()
                                        mViewModel.currentViewingAlbum.media.removeAll(
                                            deleteModeUtil.selectedMedia
                                        )
                                        mAdapter.setData(
                                            mViewModel.currentViewingAlbum,
                                            this@ImageRowFragment
                                        )
                                        mViewModel.updateAlbums()
                                    }
                            else
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    requestPermissions(
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        MainActivity.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                                    )
                                else
                                    Toast.makeText(
                                        context,
                                        "Need storage permission for deleting..",
                                        Toast.LENGTH_LONG
                                    ).show()
                        }
                        deleteModeUtil.exitSelectMode()
                    }
                    .setNegativeButton(
                        "Cancel"
                    ) { dialog, _ ->
                        dialog.dismiss()
                        deleteModeUtil.exitSelectMode()
                    }.show()
            }
            return true
        }
    }


}