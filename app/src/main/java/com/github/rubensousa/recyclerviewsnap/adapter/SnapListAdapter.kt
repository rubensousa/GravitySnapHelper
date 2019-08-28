package com.github.rubensousa.recyclerviewsnap.adapter

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.github.rubensousa.recyclerviewsnap.R
import com.github.rubensousa.recyclerviewsnap.model.SnapList

class SnapListAdapter : RecyclerView.Adapter<SnapListAdapter.VH>() {

    private var items = listOf<SnapList>()

    fun setItems(list: List<SnapList>) {
        this.items = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(
                parent.context
            ).inflate(viewType, parent, false)
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view), GravitySnapHelper.SnapListener {

        private val titleView: TextView = view.findViewById(R.id.snapTextView)
        private val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        private val layoutManager = LinearLayoutManager(
            view.context,
            LinearLayoutManager.HORIZONTAL, false
        )
        private val snapButton: View = view.findViewById(R.id.scrollButton)
        private var snapHelper: SnapHelper? = null
        private var item: SnapList? = null
        private val adapter = AppAdapter()

        init {
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            snapButton.setOnClickListener { scrollToNext() }
        }

        fun bind(snapList: SnapList) {
            item = snapList
            snapButton.isVisible = snapList.showScrollButton
            titleView.text = snapList.title
            adapter.setItems(snapList.apps)
            snapHelper?.attachToRecyclerView(null)
            snapHelper = getSnapHelper(snapList)
            snapHelper?.attachToRecyclerView(recyclerView)
        }

        override fun onSnap(position: Int) {
            Log.d("Snapped", position.toString())
        }

        private fun getSnapHelper(snapList: SnapList): SnapHelper {
            if (snapList.gravity == Gravity.START || snapList.gravity == Gravity.END) {
                val gravitySnapHelper = GravitySnapHelper(snapList.gravity, this)
                gravitySnapHelper.setSnapToPadding(snapList.snapToPadding)
                gravitySnapHelper.setScrollMsPerInch(50f)
                // Max scroll distance = recyclerview width
                gravitySnapHelper.setMaxScrollDistanceFromSize(1.0f)
                return gravitySnapHelper
            } else {
                return LinearSnapHelper()
            }
        }

        private fun scrollToNext() {
            if (item == null) {
                return
            }
            val currentSnapHelper = snapHelper
            if (currentSnapHelper is GravitySnapHelper) {
                val referencePosition = if (item!!.gravity == Gravity.START)
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                else
                    layoutManager.findLastCompletelyVisibleItemPosition()
                if (referencePosition != RecyclerView.NO_POSITION) {
                    currentSnapHelper.smoothScrollToPosition(referencePosition + 1)
                }
            }
        }
    }
}
