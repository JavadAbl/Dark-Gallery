package com.javadabl.darkgallery.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat

class AppUtil {

    companion object{

        @SuppressLint("SimpleDateFormat")
        fun getSimpleDateFormat() = SimpleDateFormat("yyyy-MM-dd, HH:mm")


        fun getFormatSize(size: Long): String {
            val kilo: Long = 1024
            val mega: Long = kilo * kilo
            val giga: Long = mega * kilo
            val tera: Long = giga * kilo

            val s: String
            val kb: Double = size.toDouble() / kilo
            val mb: Double = kb / kilo
            val gb: Double = mb / kilo
            val tb: Double = gb / kilo
            s = if (size < kilo) {
                "$size Bytes"
            } else if (size in kilo until mega) {
                String.format("%.2f", kb) + " KB"
            } else if (size in mega until giga) {
                String.format("%.2f", mb) + " MB"
            } else if (size in giga until tera) {
                String.format("%.2f", gb) + " GB"
            } else {
                String.format("%.2f", tb) + " TB"
            }
            return s
        }

        fun checkReadExternalStoragePermission(context: Context): Boolean {
            val result =
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }

        fun checkWriteExternalStoragePermission(context: Context): Boolean {
            val result =
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }

    }


}