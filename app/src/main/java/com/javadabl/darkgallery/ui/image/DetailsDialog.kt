package com.javadabl.darkgallery.ui.image


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.postDelayed
import androidx.window.layout.WindowMetricsCalculator
import com.bumptech.glide.Glide
import com.javadabl.darkgallery.R
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.library.AnimationBuilder
import com.javadabl.darkgallery.util.AppUtil


class DetailsDialog(private val activity: Activity, private val media: Media) : Dialog(activity) {


    private lateinit var nameRow: AppCompatTextView
    private lateinit var resolutionRow: TextView
    private lateinit var pathRow: TextView
    private lateinit var sizeRow: TextView
    private lateinit var dateRow: TextView
    private lateinit var root: ConstraintLayout
    private lateinit var imgView: ImageView

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        window!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_details)

        nameRow = findViewById(R.id.dialog_NameContent)
        resolutionRow = findViewById(R.id.dialog_ResolutionContent)
        pathRow = findViewById(R.id.dialog_PathRow)
        sizeRow = findViewById(R.id.dialog_SizeRow)
        dateRow = findViewById(R.id.dialog_DateRow)
        root = findViewById(R.id.dialog_Root)
        imgView = findViewById(R.id.dialog_Background)


        root.layoutParams.height = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity).bounds.height() / 2
        root.layoutParams.width = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity).bounds.width()


        Glide.with(imgView.context).load(R.drawable.background_dialog).into(imgView)

        rootAnimation(root)

        root.postDelayed(500) {
            contentAnimations(nameRow)
            contentAnimations(resolutionRow)
            contentAnimations(pathRow)
            contentAnimations(sizeRow)
            contentAnimations(dateRow)
            nameRow.text = media.name
            resolutionRow.text = media.resolution
            pathRow.text = media.path
            sizeRow.text = AppUtil.getFormatSize(media.size)
            dateRow.text = AppUtil.getSimpleDateFormat().format(media.dateModified)

            nameRow.isSelected = true
            pathRow.isSelected = true
        }

        root.setOnClickListener {
            this.cancel()
        }

    }


    private fun contentAnimations(view: View) {
        AnimationBuilder(view).translateX(-200f)
            .then()
            .reset().ms(800)
            .execute()
    }

    private fun rootAnimation(view: View) {
        AnimationBuilder(view)
            .rotateBy(360f).ms(700)
            .execute()
    }


}