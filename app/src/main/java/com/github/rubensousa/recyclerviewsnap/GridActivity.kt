package com.github.rubensousa.recyclerviewsnap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class GridActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val adapter = AppAdapter(R.layout.adapter_vertical)

        val apps = arrayListOf<App>()
        repeat(5) {
            apps.addAll(MainActivity.getApps())
        }

        adapter.setItems(apps)
        recyclerView.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }
}
