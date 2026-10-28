package com.flipos.launcher

import android.os.Bundle
import com.flipos.launcher.data.AppInfo
import com.flipos.launcher.data.AppRepository
import com.flipos.launcher.data.LauncherPrefs
import com.flipos.launcher.ui.ListRowAdapter
import com.flipos.launcher.ui.Row
import com.flipos.launcher.util.launchAppByKey

/** Provides a shortcut-only list of apps hidden from the regular drawer. */
class HiddenAppsActivity : BaseListActivity() {

    private lateinit var adapter: ListRowAdapter
    private var apps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleView.text = getString(R.string.title_hidden_apps)
        adapter = ListRowAdapter(onClick = { position ->
            apps.getOrNull(position)?.let { launchAppByKey(it.key) }
        })
        listView.adapter = adapter
        softKeys.setLabels(getString(R.string.softkey_back), getString(R.string.softkey_open), null)
        softKeys.setOnLeftClick { finish() }
        softKeys.setOnCenterClick {
            apps.getOrNull(focusedPosition())?.let { launchAppByKey(it.key) }
        }
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun loadApps() {
        Thread {
            val prefs = LauncherPrefs(this)
            val loaded = AppRepository.getAllApps(this).filter { prefs.isHidden(it.key) }
            if (isDestroyed) return@Thread
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                apps = loaded
                adapter.submit(
                    if (loaded.isEmpty()) {
                        listOf(Row(title = getString(R.string.hidden_apps_empty)))
                    } else {
                        loaded.map { Row(title = it.label, icon = it.icon) }
                    },
                )
                focusFirst()
            }
        }.start()
    }
}
