package com.bloombook.common.composables

import android.content.Context
import android.widget.Toast

fun customToast(context: Context, message: String, duration: Int) {
    var toast: Toast? = null

    toast?.cancel()

    toast = Toast.makeText(context, message, duration)
    toast.show()
}