package com.streamfree.app.util

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

fun View.show() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.hide() { visibility = View.INVISIBLE }

fun Context.toast(msg: String, long: Boolean = false) =
    Toast.makeText(this, msg, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

fun Fragment.toast(msg: String, long: Boolean = false) =
    requireContext().toast(msg, long)
