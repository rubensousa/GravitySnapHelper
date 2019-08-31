package com.github.rubensousa.recyclerviewsnap.adapter

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

/**
 * An item decoration that applies startPadding to the edges only.
 *
 * If the orientation passed is [RecyclerView.HORIZONTAL], left startPadding is set at position 0
 * and right endPadding is set at the last position.
 *
 * If the orientation passed is [RecyclerView.VERTICAL], top startPadding is set at position 0
 * and bottom endPadding is set at the last position.
 */
class LinearEdgeDecoration(
    @Px private val startPadding: Int,
    @Px private val endPadding: Int = startPadding,
    private val orientation: Int = RecyclerView.VERTICAL,
    private val inverted: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val layoutManager: RecyclerView.LayoutManager = parent.layoutManager!!
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        val position = layoutParams.viewAdapterPosition
        val itemCount = layoutManager.itemCount

        if (position == RecyclerView.NO_POSITION || itemCount == 0
            || (position > 0 && position < itemCount - 1)
        ) {
            return
        }

        if (orientation == RecyclerView.HORIZONTAL) {
            if (position == 0) {
                if (!inverted) {
                    outRect.left = startPadding
                } else {
                    outRect.right = startPadding
                }
            } else if (position == itemCount - 1) {
                if (!inverted) {
                    outRect.right = endPadding
                } else {
                    outRect.left = endPadding
                }
            }
        } else {
            if (position == 0) {
                if (!inverted) {
                    outRect.top = startPadding
                } else {
                    outRect.bottom = startPadding
                }
            } else if (position == itemCount - 1) {
                if (!inverted) {
                    outRect.bottom = endPadding
                } else {
                    outRect.top = endPadding
                }
            }
        }
    }
}