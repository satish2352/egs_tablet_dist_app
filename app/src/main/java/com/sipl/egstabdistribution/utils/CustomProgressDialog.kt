package com.sipl.egstabdistribution.utils

import android.app.Activity
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import com.sipl.egstabdistribution.R


class CustomProgressDialog(private val context: Context) {
    private val dialog: CustomDialog

    init {
        val inflater = (context as Activity).layoutInflater
        val view = inflater.inflate(R.layout.layout_custom_dialog, null)
        // cpCardView.setCardBackgroundColor(Color.parseColor("#70000000"))
        // Progress Bar Color
        // Text Color
        // cpTitle.setTextColor(Color.WHITE)
        // Custom Dialog initialization
        dialog = CustomDialog(context)
        dialog.setContentView(view)
    }

    public fun show() {
        dialog.show()
    }

    public fun dismiss() {
        dialog.dismiss()
    }
    public fun setCancelable(isCancellable: Boolean){
        dialog.setCancelable(isCancellable)
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}
