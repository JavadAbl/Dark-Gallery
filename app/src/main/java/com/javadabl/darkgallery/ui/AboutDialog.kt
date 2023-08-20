package com.javadabl.darkgallery.ui

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.window.layout.WindowMetricsCalculator
import com.javadabl.darkgallery.R

class AboutDialog(private val activity: Activity) : Dialog(activity) {


    override fun onCreate(savedInstanceState: Bundle?) {

        window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_about)
        val root = findViewById<View>(R.id.about_Root)
        val textLicences = findViewById<TextView>(R.id.about_licences)

        val height = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity).bounds.height() / 2

        val width = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity).bounds.width() - ((WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity).bounds.width() * 15) / 100)

        window?.setLayout(width, height)

        root.requestLayout()
        textLicences.text = activity.getString(R.string.licences)


    }
}