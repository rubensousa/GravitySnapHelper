package com.github.rubensousa.recyclerviewsnap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.github.rubensousa.recyclerviewsnap.adapter.AppAdapter
import com.github.rubensousa.recyclerviewsnap.adapter.SnapListAdapter
import com.github.rubensousa.recyclerviewsnap.model.App
import com.github.rubensousa.recyclerviewsnap.model.SnapList
import java.util.*

class MainActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener {

    companion object {
        const val STATE_ORIENTATION = "orientation"

        fun getApps(): List<App> {
            val apps = ArrayList<App>()
            repeat(2) {
                apps.add(App("Google+", R.drawable.ic_google_48dp, 4.6f))
                apps.add(App("Gmail", R.drawable.ic_gmail_48dp, 4.8f))
                apps.add(App("Inbox", R.drawable.ic_inbox_48dp, 4.5f))
                apps.add(App("Google Keep", R.drawable.ic_keep_48dp, 4.2f))
                apps.add(App("Google Drive", R.drawable.ic_drive_48dp, 4.6f))
                apps.add(App("Hangouts", R.drawable.ic_hangouts_48dp, 3.9f))
                apps.add(App("Google Photos", R.drawable.ic_photos_48dp, 4.6f))
                apps.add(App("Messenger", R.drawable.ic_messenger_48dp, 4.2f))
                apps.add(App("Sheets", R.drawable.ic_sheets_48dp, 4.2f))
                apps.add(App("Slides", R.drawable.ic_slides_48dp, 4.2f))
                apps.add(App("Docs", R.drawable.ic_docs_48dp, 4.2f))
            }
            return apps
        }
    }

    private lateinit var snapHelper: GravitySnapHelper
    private lateinit var recyclerView: RecyclerView
    private var horizontal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        snapHelper = GravitySnapHelper(Gravity.TOP, false,
            GravitySnapHelper.SnapListener { position ->
                Log.d("Snapped", position.toString() + "")
            })

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        horizontal = savedInstanceState?.getBoolean(STATE_ORIENTATION) ?: true

        setupAdapter()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_ORIENTATION, horizontal)
    }

    private fun setupAdapter() {
        val apps = getApps()

        if (horizontal) {
            val adapter = SnapListAdapter()
            val lists = listOf(
                SnapList(
                    gravity = Gravity.CENTER,
                    title = "Center",
                    apps = apps
                ), SnapList(
                    gravity = Gravity.CENTER,
                    title = "Center with faster scroll",
                    scrollMsPerInch = 50f,
                    apps = apps
                ), SnapList(
                    gravity = Gravity.CENTER,
                    title = "Center with slower scroll",
                    scrollMsPerInch = 200f,
                    apps = apps
                ), SnapList(
                    gravity = Gravity.CENTER,
                    title = "Center with fling limited",
                    maxFlingSizeFraction = 0.5f,
                    scrollMsPerInch = 50f,
                    apps = apps
                ), SnapList(
                    gravity = Gravity.START,
                    title = "Start",
                    apps = apps
                ), SnapList(
                    gravity = Gravity.START,
                    title = "Start with fling limited",
                    maxFlingSizeFraction = 0.5f,
                    scrollMsPerInch = 50f,
                    apps = apps
                ), SnapList(
                    layoutId = R.layout.adapter_snap_padding_start,
                    gravity = Gravity.START,
                    snapToPadding = true,
                    title = "Start with padding",
                    apps = apps
                ), SnapList(
                    gravity = Gravity.START,
                    layoutId = R.layout.adapter_snap_extra_padding_start,
                    snapToPadding = false,
                    title = "Start with large padding",
                    apps = apps
                ), SnapList(
                    gravity = Gravity.END,
                    title = "End",
                    apps = apps
                ), SnapList(
                    gravity = Gravity.END,
                    snapToPadding = true,
                    title = "End with padding",
                    apps = apps
                ), SnapList(
                    gravity = Gravity.END,
                    layoutId = R.layout.adapter_snap_extra_padding_end,
                    snapToPadding = false,
                    title = "End with large padding",
                    apps = apps
                )
            )
            adapter.setItems(lists)
            recyclerView.adapter = adapter
            snapHelper.attachToRecyclerView(null)
        } else {
            val adapter =
                AppAdapter(R.layout.adapter_vertical)
            adapter.setItems(apps)
            recyclerView.adapter = adapter
            snapHelper.setScrollMsPerInch(50f)
            snapHelper.setMaxFlingSizeFraction(2f)
            snapHelper.attachToRecyclerView(recyclerView)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.layoutType) {
            horizontal = !horizontal
            setupAdapter()
            item.title = if (horizontal) "Vertical" else "Horizontal"
        } else if (item.itemId == R.id.grid) {
            startActivity(Intent(this, GridActivity::class.java))
        }
        return false
    }

}