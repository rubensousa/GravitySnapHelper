package com.github.rubensousa.recyclerviewsnap

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper


class GridActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val adapter = AppAdapter(R.layout.adapter_vertical)
        adapter.setItems(MainActivity.getApps())

        recyclerView.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, true)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        GravitySnapHelper(Gravity.TOP).attachToRecyclerView(recyclerView)
    }
}
