package com.privo.sdk.components

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout

import android.view.WindowManager

import android.widget.TextView

import android.view.Gravity

import android.view.ViewGroup

import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog


internal class LoadingDialog (context: Context) {
    private val dialog : AlertDialog;

    init {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = "Loading ..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 16F
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        dialog = builder.create()
        dialog.window?.let { window ->
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            window.attributes = layoutParams
        }
    }
    private fun runOnMainThread(completion: () -> Unit) = Handler(Looper.getMainLooper()).post(completion)
    fun show() = runOnMainThread { dialog.show() }
    fun hide() = runOnMainThread { dialog.dismiss() }
}