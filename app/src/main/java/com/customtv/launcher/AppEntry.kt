package com.customtv.launcher

import android.content.Intent
import android.graphics.drawable.Drawable

/** One tile on the home screen: either an installed app or the "show hidden" toggle. */
data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: Drawable?,
    val launchIntent: Intent?,
    val isToggle: Boolean = false,
)
