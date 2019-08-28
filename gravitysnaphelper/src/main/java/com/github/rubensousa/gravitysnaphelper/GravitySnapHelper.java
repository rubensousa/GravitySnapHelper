/*
 * Copyright 2018 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rubensousa.gravitysnaphelper;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;


public class GravitySnapHelper extends LinearSnapHelper {

    public static final int SCROLL_DISTANCE_DEFAULT = -1;

    @NonNull
    private final GravityDelegate delegate;

    public GravitySnapHelper(int gravity) {
        this(gravity, false, null);
    }

    public GravitySnapHelper(int gravity, @NonNull SnapListener snapListener) {
        this(gravity, false, snapListener);
    }

    public GravitySnapHelper(int gravity, boolean enableSnapLastItem) {
        this(gravity, enableSnapLastItem, null);
    }

    public GravitySnapHelper(int gravity, boolean enableSnapLastItem,
                             @Nullable SnapListener snapListener) {
        delegate = new GravityDelegate(gravity, enableSnapLastItem, snapListener);
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
            throws IllegalStateException {
        delegate.attachToRecyclerView(recyclerView);
        super.attachToRecyclerView(recyclerView);
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        return delegate.calculateDistanceToFinalSnap(layoutManager, targetView);
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return delegate.findSnapView(layoutManager);
    }

    @Override
    public int[] calculateScrollDistance(int velocityX, int velocityY) {
        final int[] out = delegate.calculateScrollDistance(velocityX, velocityY);
        if (out[0] == 0 && out[1] == 0) {
            return super.calculateScrollDistance(velocityX, velocityY);
        }
        return out;
    }

    @Nullable
    @Override
    protected RecyclerView.SmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
        return delegate.createScroller(layoutManager);
    }

    /**
     * Enable snapping of the last item that's snappable.
     * The default value is false, because you can't see the last item completely
     * if this is enabled.
     *
     * @param snap true if you want to enable snapping of the last snappable item
     */
    public void enableLastItemSnap(boolean snap) {
        delegate.enableLastItemSnap(snap);
    }

    /**
     * If true, GravitySnapHelper will snap to the gravity edge
     * plus any amount of padding that was set in the RecyclerView.
     * <p>
     * The default value is false.
     *
     * @param snapToPadding true if you want to snap to the padding
     */
    public void setSnapToPadding(boolean snapToPadding) {
        delegate.setSnapToPadding(snapToPadding);
    }

    /**
     * Sets the scroll duration in ms per inch.
     * <p>
     * Default value is 100.0f
     * <p>
     * This value will be used in
     * {@link GravitySnapHelper#createScroller(RecyclerView.LayoutManager)}
     *
     * @param ms scroll duration in ms per inch
     */
    public void setScrollMsPerInch(float ms) {
        delegate.setScrollMsPerInch(ms);
    }

    /**
     * Changes the max scroll distance in absolute values.
     *
     * @param distance max scroll distance in pixels
     *                 or {@link GravitySnapHelper#SCROLL_DISTANCE_DEFAULT}
     *                 to reset the max scroll distance
     */
    public void setMaxScrollDistance(@Px int distance) {
        delegate.setMaxScrollDistance(distance);
    }

    /**
     * Changes the max scroll distance depending on the available size of the RecyclerView.
     * <p>
     * Example: if you pass 0.5f and the RecyclerView measures 600dp,
     * the max scroll distance will be 300dp.
     *
     * @param offset size offset to be used for the max scroll distance
     */
    public void setMaxScrollDistanceFromSize(float offset) {
        delegate.setMaxScrollDistanceFromSize(offset);
    }

    public void smoothScrollToPosition(int position) {
        delegate.smoothScrollToPosition(position);
    }

    public void scrollToPosition(int position) {
        delegate.scrollToPosition(position);
    }

    public int getCurrentSnappedPosition() {
        return delegate.getCurrentSnappedPosition();
    }

    public interface SnapListener {
        void onSnap(int position);
    }

}
