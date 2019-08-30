/*
 * Copyright 2018 The Android Open Source Project
 * Copyright 2019 Rúben Sousa
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

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

/**
 * A {@link LinearSnapHelper} that allows snapping to an edge or to the center.
 * <p>
 * Possible snap positions:
 * {@link Gravity#START}, {@link Gravity#TOP}, {@link Gravity#END}, {@link Gravity#BOTTOM},
 * {@link Gravity#CENTER}.
 * <p>
 * To customize the scroll duration, use {@link GravitySnapHelper#setScrollMsPerInch(float)}.
 * <p>
 * To customize the maximum scroll distance during flings,
 * use {@link GravitySnapHelper#setMaxFlingSizeFraction(float)}
 * or {@link GravitySnapHelper#setMaxFlingDistance(int)}
 */
public class GravitySnapHelper extends LinearSnapHelper {

    public static final int FLING_DISTANCE_DISABLE = -1;
    public static final float FLING_SIZE_FRACTION_DISABLE = -1f;

    /**
     * A listener that's called when the {@link RecyclerView} used by {@link GravitySnapHelper}
     * changes its scroll state to {@link RecyclerView#SCROLL_STATE_IDLE}
     * and there's a valid snap position.
     */
    public interface SnapListener {
        /**
         * @param position last position snapped to
         */
        void onSnap(int position);
    }

    private int gravity;
    private boolean isRtl;
    private boolean snapLastItem;
    private int nextSnapPosition;
    private boolean isScrolling = false;
    private boolean snapToPadding = false;
    private float scrollMsPerInch = 100f;
    private int maxFlingDistance = FLING_DISTANCE_DISABLE;
    private float maxFlingSizeFraction = FLING_SIZE_FRACTION_DISABLE;
    private OrientationHelper verticalHelper;
    private OrientationHelper horizontalHelper;
    private GravitySnapHelper.SnapListener listener;
    private RecyclerView recyclerView;
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE && listener != null) {
                if (nextSnapPosition != RecyclerView.NO_POSITION && isScrolling) {
                    listener.onSnap(nextSnapPosition);
                }
            }
            isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
        }
    };

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
        if (gravity != Gravity.START
                && gravity != Gravity.END
                && gravity != Gravity.BOTTOM
                && gravity != Gravity.TOP
                && gravity != Gravity.CENTER) {
            throw new IllegalArgumentException("Invalid gravity value. Use START " +
                    "| END | BOTTOM | TOP | CENTER constants");
        }
        this.snapLastItem = enableSnapLastItem;
        this.gravity = gravity;
        this.listener = snapListener;
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
            throws IllegalStateException {
        if (this.recyclerView != null) {
            this.recyclerView.removeOnScrollListener(scrollListener);
        }
        if (recyclerView != null) {
            recyclerView.setOnFlingListener(null);
            if (gravity == Gravity.START || gravity == Gravity.END) {
                isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
                        == ViewCompat.LAYOUT_DIRECTION_RTL;
            }
            if (listener != null) {
                recyclerView.addOnScrollListener(scrollListener);
            }
            this.recyclerView = recyclerView;
        } else {
            this.recyclerView = null;
        }
        super.attachToRecyclerView(recyclerView);
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager lm) {
        View snapView = null;

        switch (gravity) {
            case Gravity.START:
                snapView = findEdgeView(lm, getHorizontalHelper(lm), true);
                break;
            case Gravity.END:
                snapView = findEdgeView(lm, getHorizontalHelper(lm), false);
                break;
            case Gravity.TOP:
                snapView = findEdgeView(lm, getVerticalHelper(lm), true);
                break;
            case Gravity.BOTTOM:
                snapView = findEdgeView(lm, getVerticalHelper(lm), false);
                break;
            case Gravity.CENTER:
                // Create the orientation helpers
                if (lm.canScrollHorizontally()) {
                    getHorizontalHelper(lm);
                } else {
                    getVerticalHelper(lm);
                }
                snapView = super.findSnapView(lm);
                break;
        }
        if (snapView != null) {
            nextSnapPosition = recyclerView.getChildAdapterPosition(snapView);
        } else {
            nextSnapPosition = RecyclerView.NO_POSITION;
        }
        return snapView;
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        if (gravity == Gravity.CENTER) {
            return super.calculateDistanceToFinalSnap(layoutManager, targetView);
        }

        int[] out = new int[2];

        if (!(layoutManager instanceof LinearLayoutManager)) {
            return out;
        }

        LinearLayoutManager lm = (LinearLayoutManager) layoutManager;

        if (lm.canScrollHorizontally()) {
            if ((isRtl && gravity == Gravity.END) || (!isRtl && gravity == Gravity.START)) {
                out[0] = getDistanceToStart(targetView, getHorizontalHelper(lm));
            } else {
                out[0] = getDistanceToEnd(targetView, getHorizontalHelper(lm));
            }
        } else if (lm.canScrollVertically()) {
            if (gravity == Gravity.TOP) {
                out[1] = getDistanceToStart(targetView, getVerticalHelper(lm));
            } else {
                out[1] = getDistanceToEnd(targetView, getVerticalHelper(lm));
            }
        }
        return out;
    }

    @Override
    public int[] calculateScrollDistance(int velocityX, int velocityY) {
        if (recyclerView == null
                || (verticalHelper == null && horizontalHelper == null)
                || (maxFlingDistance == FLING_DISTANCE_DISABLE
                && maxFlingSizeFraction == FLING_SIZE_FRACTION_DISABLE)) {
            return super.calculateScrollDistance(velocityX, velocityY);
        }
        final int[] out = new int[2];
        Scroller scroller = new Scroller(recyclerView.getContext(),
                new DecelerateInterpolator());
        int maxDistance = getFlingDistance();
        scroller.fling(0, 0, velocityX, velocityY,
                -maxDistance, maxDistance,
                -maxDistance, maxDistance);
        out[0] = scroller.getFinalX();
        out[1] = scroller.getFinalY();
        return out;
    }

    @Nullable
    @Override
    public RecyclerView.SmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)
                || recyclerView == null) {
            return null;
        }
        return new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected void onTargetFound(View targetView,
                                         RecyclerView.State state,
                                         RecyclerView.SmoothScroller.Action action) {
                if (recyclerView == null || recyclerView.getLayoutManager() == null) {
                    // The associated RecyclerView has been removed so there is no action to take.
                    return;
                }
                int[] snapDistances = calculateDistanceToFinalSnap(recyclerView.getLayoutManager(),
                        targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return scrollMsPerInch / displayMetrics.densityDpi;
            }
        };
    }

    /**
     * Sets a {@link SnapListener} to listen for snap events
     *
     * @param listener a {@link SnapListener} that'll receive snap events or null to clear it
     */
    public void setSnapListener(@Nullable SnapListener listener) {
        this.listener = listener;
    }

    /**
     * Changes the max fling distance in absolute values.
     *
     * @param distance max fling distance in pixels
     *                 or {@link GravitySnapHelper#FLING_DISTANCE_DISABLE}
     *                 to disable fling limits
     */
    public void setMaxFlingDistance(@Px int distance) {
        maxFlingDistance = distance;
        maxFlingSizeFraction = FLING_SIZE_FRACTION_DISABLE;
    }

    /**
     * Changes the max fling distance depending on the available size of the RecyclerView.
     * <p>
     * Example: if you pass 0.5f and the RecyclerView measures 600dp,
     * the max fling distance will be 300dp.
     *
     * @param fraction size fraction to be used for the max fling distance
     *                 or {@link GravitySnapHelper#FLING_SIZE_FRACTION_DISABLE}
     *                 to disable fling limits
     */
    public void setMaxFlingSizeFraction(float fraction) {
        maxFlingDistance = FLING_DISTANCE_DISABLE;
        maxFlingSizeFraction = fraction;
    }

    /**
     * Enable snapping of the last item that's snappable.
     * The default value is false, because you can't see the last item completely
     * if this is enabled.
     *
     * @param snap true if you want to enable snapping of the last snappable item
     */
    public void setSnapLastItem(boolean snap) {
        snapLastItem = snap;
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
        this.snapToPadding = snapToPadding;
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
        scrollMsPerInch = ms;
    }

    /**
     * Changes the gravity of this {@link GravitySnapHelper}
     * and dispatches a smooth scroll for the new snap position.
     *
     * @param newGravity one of the following: {@link Gravity#START}, {@link Gravity#TOP},
     *                   {@link Gravity#END}, {@link Gravity#BOTTOM}, {@link Gravity#CENTER}
     */
    public void setGravity(int newGravity) {
        if (this.gravity != newGravity) {
            this.gravity = newGravity;
            updateSnap();
        }
    }

    /**
     * This method will only work if there's a ViewHolder for the given position.
     *
     * @return true if scroll was successful, false otherwise
     */
    public boolean scrollToPosition(int position) {
        return scrollTo(position, false);
    }

    /**
     * Unlike {@link GravitySnapHelper#scrollToPosition(int)},
     * this method will generally always find a snap view if the position is valid.
     * <p>
     * The smooth scroller from {@link GravitySnapHelper#createScroller(RecyclerView.LayoutManager)}
     * will be used, and so will {@link GravitySnapHelper#scrollMsPerInch} for the scroll velocity
     *
     * @return true if scroll was successful, false otherwise
     */
    public boolean smoothScrollToPosition(int position) {
        return scrollTo(position, true);
    }

    /**
     * Get the current gravity being applied
     *
     * @return one of the following: {@link Gravity#START}, {@link Gravity#TOP}, {@link Gravity#END},
     * {@link Gravity#BOTTOM}, {@link Gravity#CENTER}
     */
    public int getGravity() {
        return this.gravity;
    }

    /**
     * @return true if this SnapHelper should snap to the last item
     */
    public boolean getSnapLastItem() {
        return snapLastItem;
    }

    /**
     * @return last distance set through {@link GravitySnapHelper#setMaxFlingDistance(int)}
     * or {@link GravitySnapHelper#FLING_DISTANCE_DISABLE} if we're not limiting the fling distance
     */
    public int getMaxFlingDistance() {
        return maxFlingDistance;
    }

    /**
     * @return last distance set through {@link GravitySnapHelper#setMaxFlingSizeFraction(float)}
     * or {@link GravitySnapHelper#FLING_SIZE_FRACTION_DISABLE}
     * if we're not limiting the fling distance
     */
    public float getMaxFlingSizeFraction() {
        return maxFlingSizeFraction;
    }

    /**
     * @return last scroll speed set through {@link GravitySnapHelper#setScrollMsPerInch(float)}
     * or 100f
     */
    public float getScrollMsPerInch() {
        return scrollMsPerInch;
    }

    /**
     * @return true if this SnapHelper should snap to the padding. Defaults to false.
     */
    public boolean getSnapToPadding() {
        return snapToPadding;
    }

    /**
     * @return the position of the current view that's snapped
     * or {@link RecyclerView#NO_POSITION} in case there's none.
     */
    public int getCurrentSnappedPosition() {
        if (recyclerView != null && recyclerView.getLayoutManager() != null) {
            View snappedView = findSnapView(recyclerView.getLayoutManager());
            if (snappedView != null) {
                return recyclerView.getChildAdapterPosition(snappedView);
            }
        }
        return RecyclerView.NO_POSITION;
    }

    private int getFlingDistance() {
        if (maxFlingSizeFraction != FLING_SIZE_FRACTION_DISABLE) {
            if (verticalHelper != null) {
                return (int) (recyclerView.getHeight() * maxFlingSizeFraction);
            } else if (horizontalHelper != null) {
                return (int) (recyclerView.getWidth() * maxFlingSizeFraction);
            } else {
                return Integer.MAX_VALUE;
            }
        } else if (maxFlingDistance != FLING_DISTANCE_DISABLE) {
            return maxFlingDistance;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    private void updateSnap() {
        if (recyclerView == null || recyclerView.getLayoutManager() == null) {
            return;
        }
        View snapView = findSnapView(recyclerView.getLayoutManager());
        if (snapView != null) {
            int adapterPosition = recyclerView.getChildAdapterPosition(snapView);
            if (adapterPosition != RecyclerView.NO_POSITION) {
                scrollTo(adapterPosition, true);
            }
        }
    }

    /**
     * @return true if the scroll will snap to a view, false otherwise
     */
    private boolean scrollTo(int position, boolean smooth) {
        if (recyclerView.getLayoutManager() != null) {
            if (smooth) {
                RecyclerView.SmoothScroller smoothScroller
                        = createScroller(recyclerView.getLayoutManager());
                if (smoothScroller != null) {
                    smoothScroller.setTargetPosition(position);
                    recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
                    return true;
                }
            } else {
                RecyclerView.ViewHolder viewHolder
                        = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder != null) {
                    int[] distances = calculateDistanceToFinalSnap(recyclerView.getLayoutManager(),
                            viewHolder.itemView);
                    recyclerView.scrollBy(distances[0], distances[1]);
                    return true;
                }
            }
        }
        return false;
    }

    private int getDistanceToStart(View targetView, @NonNull OrientationHelper helper) {
        int distance;
        // If we don't care about padding, just snap to the start of the view
        if (!snapToPadding) {
            int childStart = helper.getDecoratedStart(targetView);
            if (childStart >= helper.getStartAfterPadding() / 2) {
                distance = childStart - helper.getStartAfterPadding();
            } else {
                distance = childStart;
            }
        } else {
            distance = helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
        }
        return distance;
    }

    private int getDistanceToEnd(View targetView, @NonNull OrientationHelper helper) {
        int distance;

        if (!snapToPadding) {
            int childEnd = helper.getDecoratedEnd(targetView);
            if (childEnd >= helper.getEnd() - (helper.getEnd() - helper.getEndAfterPadding()) / 2) {
                distance = helper.getDecoratedEnd(targetView) - helper.getEnd();
            } else {
                distance = childEnd - helper.getEndAfterPadding();
            }
        } else {
            distance = helper.getDecoratedEnd(targetView) - helper.getEndAfterPadding();
        }

        return distance;
    }

    /**
     * Returns the first view that we should snap to.
     *
     * @param layoutManager the RecyclerView's LayoutManager
     * @param helper        orientation helper to calculate view sizes
     * @param start         true if we want the view closest to the start,
     *                      false if we want the one closest to the end
     * @return the first view in the LayoutManager to snap to, or null if we shouldn't snap to any
     */
    @Nullable
    private View findEdgeView(RecyclerView.LayoutManager layoutManager,
                              OrientationHelper helper,
                              boolean start) {

        if (layoutManager.getChildCount() == 0 || !(layoutManager instanceof LinearLayoutManager)) {
            return null;
        }

        final LinearLayoutManager lm = (LinearLayoutManager) layoutManager;

        // If we're at the end of the list, we shouldn't snap
        // to avoid having the last item not completely visible.
        if (isAtEndOfList(lm) && !snapLastItem) {
            return null;
        }

        View edgeView = null;
        int distanceToEdge = Integer.MAX_VALUE;

        for (int i = 0; i < lm.getChildCount(); i++) {
            View currentView = lm.getChildAt(i);
            int currentViewDistance;
            if ((start && !isRtl) || (!start && isRtl)) {
                if (!snapToPadding) {
                    currentViewDistance = Math.abs(helper.getDecoratedStart(currentView));
                } else {
                    currentViewDistance = Math.abs(helper.getStartAfterPadding()
                            - helper.getDecoratedStart(currentView));
                }
            } else {
                if (!snapToPadding) {
                    currentViewDistance = Math.abs(helper.getDecoratedEnd(currentView)
                            - helper.getEnd());
                } else {
                    currentViewDistance = Math.abs(helper.getEndAfterPadding()
                            - helper.getDecoratedEnd(currentView));
                }
            }
            if (currentViewDistance < distanceToEdge) {
                distanceToEdge = currentViewDistance;
                edgeView = currentView;
            }
        }
        return edgeView;
    }

    private boolean isAtEndOfList(LinearLayoutManager lm) {
        if ((!lm.getReverseLayout() && gravity == Gravity.START)
                || (lm.getReverseLayout() && gravity == Gravity.END)
                || (!lm.getReverseLayout() && gravity == Gravity.TOP)
                || (lm.getReverseLayout() && gravity == Gravity.BOTTOM)) {
            return lm.findLastCompletelyVisibleItemPosition() == lm.getItemCount() - 1;
        } else {
            return lm.findFirstCompletelyVisibleItemPosition() == 0;
        }
    }

    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (verticalHelper == null || verticalHelper.getLayoutManager() != layoutManager) {
            verticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return verticalHelper;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (horizontalHelper == null || horizontalHelper.getLayoutManager() != layoutManager) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return horizontalHelper;
    }

}
