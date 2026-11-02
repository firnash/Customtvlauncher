package com.customtv.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {

    private lateinit var hidden: HiddenApps
    private lateinit var adapter: AppAdapter
    private lateinit var recycler: RecyclerView
    private var showHidden = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hidden = HiddenApps(this)

        adapter = AppAdapter(onClick = ::launch, onLongClick = ::toggleHidden)
        recycler = findViewById(R.id.apps)
        recycler.layoutManager = GridLayoutManager(this, COLUMNS)
        recycler.adapter = adapter

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)
        refresh()
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home button pressed while already on the launcher: return to the top.
        recycler.scrollToPosition(0)
    }

    // A home screen has nowhere to go "back" to.
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = Unit

    private fun launch(entry: AppEntry) {
        if (entry.isToggle) {
            showHidden = !showHidden
            refresh()
            return
        }
        try {
            startActivity(entry.launchIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Can't open ${entry.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleHidden(entry: AppEntry) {
        val nowHidden = !hidden.isHidden(entry.packageName)
        hidden.toggle(entry.packageName)
        Toast.makeText(
            this,
            "${entry.label} ${if (nowHidden) "hidden" else "shown"}",
            Toast.LENGTH_SHORT,
        ).show()
        refresh()
    }

    private fun refresh() {
        val pm = packageManager
        val hiddenSet = hidden.all()

        // Leanback apps first; fall back to normal launcher activities for phone-style apps.
        val infos = LAUNCH_CATEGORIES.flatMap { cat ->
            pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(cat), 0)
        }

        val apps = infos
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                AppEntry(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = ri.loadIcon(pm),
                    launchIntent = pm.getLeanbackLaunchIntentForPackage(pkg)
                        ?: pm.getLaunchIntentForPackage(pkg),
                )
            }
            .filter { it.launchIntent != null }
            .filter { showHidden || it.packageName !in hiddenSet }
            .sortedBy { it.label.lowercase() }

        val toggle = AppEntry(
            label = getString(if (showHidden) R.string.hide_hidden else R.string.show_hidden),
            packageName = "",
            icon = null,
            launchIntent = null,
            isToggle = true,
        )
        adapter.submit(apps + toggle)
    }

    private companion object {
        const val COLUMNS = 6
        val LAUNCH_CATEGORIES = listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
    }
}
