package com.javadabl.darkgallery.ui.album


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
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
import com.javadabl.darkgallery.databinding.ActivityMainBinding
import com.javadabl.darkgallery.databinding.FragmentAlbumTableBinding
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.ui.MainActivity
import com.javadabl.darkgallery.ui.MainActivity.Companion.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
import com.javadabl.darkgallery.ui.album.adapter.AlbumTableRecyclerAdapter
import com.javadabl.darkgallery.util.AppUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AlbumTableFragment : Fragment() {


    lateinit var thread: HandlerThread

    //RecyclerAdapter
    @Inject
    lateinit var mAdapter: AlbumTableRecyclerAdapter

    //DataBinding
    private var mBinding: FragmentAlbumTableBinding? = null
    lateinit var activityBinding: ActivityMainBinding

    //ViewModel
    private lateinit var mViewModel: AlbumViewModel
    val viewModel: AlbumViewModel get() = mViewModel

    //DeleteModeUtil
    lateinit var deleteModeUtil: AlbumDeleteModeUtil


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity())[AlbumViewModel::class.java]
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding!!.tableRecycleView.adapter = null
        mBinding!!.unbind()
        mBinding = null
        if (this::thread.isInitialized)
            thread.quitSafely()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAlbumTableBinding.inflate(inflater, container, false)

        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            activityBinding = (requireActivity() as MainActivity).mainActivityDataBinding
            if (destination.id == R.id.albumTableFragment)
                activityBinding.topAppBar.title = "Dark Gallery"
        }

        return mBinding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mMenuHost = requireActivity()

        //DeleteModeUtil Initialize
        deleteModeUtil =
            AlbumDeleteModeUtil(mAdapter, mViewModel, menuHost, menuProvider)

        //Nav BackStack
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                @Suppress("DEPRECATION")
                override fun handleOnBackPressed() {
                    if (deleteModeUtil.getSelectMode()) {
                        deleteModeUtil.exitSelectMode()
                    } else
                        requireActivity().finishAffinity()
                }
            }
            )

        setupRecyclerView()
        requestData()
        mViewModel.updateAlbums()

        Glide.with(requireContext())
            .load(R.drawable.background_album)
            .centerCrop().into(mBinding!!.albumBackground1)

        Glide.with(requireContext())
            .load(R.drawable.background_album)
            .centerCrop().into(mBinding!!.albumBackground2)


        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Log.i("mytag", Thread.currentThread().toString())
            AnimationBuilder.backgroundAnimation(
                mBinding!!.albumBackground1, mBinding!!.albumBackground2,
                3700
            )
        }

    }


    private fun requestData() {
        mViewModel.albums.observe(viewLifecycleOwner) {
            //   mBinding!!.tableRecycleView.hideShimmer()
            mAdapter.setData(it, this)
        }
    }

    private fun setupRecyclerView() {
        mBinding!!.tableRecycleView.layoutManager = GridLayoutManager(
            requireContext(),
            2,
            GridLayoutManager.VERTICAL,
            false
        )
        mBinding!!.tableRecycleView.adapter = mAdapter

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SelectMode UI Logic
    private lateinit var mMenuHost: MenuHost
    val menuHost get() = mMenuHost

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MainActivity.REQUEST_DELETE_SENDER -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    requireContext(),
                    "${deleteModeUtil.getItemsSize()} items deleted..", Toast.LENGTH_LONG
                ).show()
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
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                deleteModeUtil.deleteRequestAPIBelow29().observe(viewLifecycleOwner) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    mViewModel.updateAlbums()
                }
            else deleteModeUtil.exitSelectMode()
    }


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
                                        mViewModel.updateAlbums()
                                    }
                            else
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    requestPermissions(
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE
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
                    ) { _, _ ->
                        deleteModeUtil.exitSelectMode()
                    }.create().show()
            }
            return true
        }
    }


}