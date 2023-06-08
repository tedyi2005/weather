package com.android.weather.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import com.android.weather.R
import com.squareup.picasso.Picasso

/**
 *  Common Utility methods
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.show() {
    visibility = View.VISIBLE
}

fun show(vararg views: View) {
    views.forEach { it.show() }
}

fun View.hide() {
    visibility = View.GONE
}

fun hide(vararg views: View) {
    views.forEach { it.hide() }
}

fun ImageView.loadImage(imageUrl: String) {
    Picasso.get()
        .load(imageUrl)
        .transform(RoundedCornersTransform()) // set transformation for rounded image
        .placeholder(R.drawable.ic_error_image)
        .into(this)

}

// set text if not null else hide text
fun TextView.setTextOrHide(newText: String?) {
    if (newText.isNullOrBlank()) {
        this.visibility = View.GONE
    } else {
        this.text = newText
        this.visibility = View.VISIBLE
    }
}

fun String.toIconUrl(): String {
    return "https://openweathermap.org/img/wn/$this@4x.png"
}