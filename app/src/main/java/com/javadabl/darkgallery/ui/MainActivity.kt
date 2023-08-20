package com.javadabl.darkgallery.ui


import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.javadabl.darkgallery.R
import com.javadabl.darkgallery.StartupActivity
import com.javadabl.darkgallery.databinding.ActivityMainBinding
import com.javadabl.darkgallery.util.AppUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var mBinding: ActivityMainBinding
    val mainActivityDataBinding: ActivityMainBinding get() = mBinding
    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window: Window = this.window
        window.setBackgroundDrawable(ColorDrawable(Color.BLACK))

        if (!AppUtil.checkReadExternalStoragePermission(this)) {
            startActivity(Intent(applicationContext, StartupActivity::class.java))
            finish()
        }


        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(mBinding.topAppBar)
        navSetup()
        menuSetup()
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun navSetup() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.browse_NavHost) as NavHostFragment
        navController = navHostFragment.navController

        val bar = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, bar)


    }

    private fun menuSetup() {

        addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_About)
                    AboutDialog(this@MainActivity).show()
                return false
            }
        })
    }



    companion object {
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0x1
        const val REQUEST_DELETE_SENDER = 0x2
    }

}