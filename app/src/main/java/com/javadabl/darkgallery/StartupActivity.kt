package com.javadabl.darkgallery


import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.javadabl.darkgallery.ui.MainActivity


class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        val btn = findViewById<Button>(R.id.startup_btn)



        btn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(arrayOf(permission.READ_EXTERNAL_STORAGE), 0)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        } else
            Toast.makeText(this, "Why You Don't Trust Me...", Toast.LENGTH_LONG).show()
    }

}