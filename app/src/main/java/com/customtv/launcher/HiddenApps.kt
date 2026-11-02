package com.customtv.launcher

import android.content.Context

/** Persists which packages the user chose to hide from the home screen. */
class HiddenApps(context: Context) {
    private val prefs = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    fun all(): Set<String> = prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    fun isHidden(pkg: String) = pkg in all()

    fun toggle(pkg: String) {
        val next = all().toMutableSet()
        if (!next.remove(pkg)) next.add(pkg)
        prefs.edit().putStringSet(KEY, next).apply()
    }

    private companion object {
        const val KEY = "hidden_packages"
    }
}
