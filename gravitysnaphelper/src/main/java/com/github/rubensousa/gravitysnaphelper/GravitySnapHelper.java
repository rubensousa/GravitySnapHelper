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
 * use {@link GravitySnapHelper#setMaxFlingDistanceFromSize(float)}
 */
public class GravitySnapHelper extends LinearSnapHelper {

    public static final int FLING_DISTANCE_DEFAULT = -1;
    private int gravity;
    private boolean isRtl;
    private boolean snapLastItem;
    private int currentSnappedPosition;
    private boolean isScrolling = false;
    private boolean snapToPadding = false;
    private float scrollMsPerInch = 100f;
    private int maxFlingDistance = GravitySnapHelper.FLING_DISTANCE_DEFAULT;
    private float maxFlingDistanceOffset = 0f;
    private OrientationHelper verticalHelper;
    private OrientationHelper horizontalHelper;
    private GravitySnapHelper.SnapListener listener;
    private RecyclerView recyclerView;
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE && listener != null) {
                if (currentSnappedPosition != RecyclerView.NO_POSITION && isScrolling) {
                    listener.onSnap(currentSnappedPosition);
                }
            }
            isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
        }
    };

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

    public GravitySnapHelper(int gravity) {
        this(gravity, false, null);
    }

    public GravitySnapHelper(int gravity, @NonNull SnapListener snapListener) {
        this(gravity, false, snapListener);
    }

    public GravitySnapHelper(int gravity, boolean enableSnapLastItem) {
        this(gravity, enableSnapLastItem, null);
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
            throws IllegalStateException {
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
            if (this.recyclerView != null) {
                this.recyclerView.removeOnScrollListener(scrollListener);
            }
            this.recyclerView = null;
        }
        super.attachToRecyclerView(recyclerView);
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
                out[0] = distanceToStart(targetView, getHorizontalHelper(lm));
            } else {
                out[0] = distanceToEnd(targetView, getHorizontalHelper(lm));
            }
        } else if (lm.canScrollVertically()) {
            if (gravity == Gravity.TOP) {
                out[1] = distanceToStart(targetView, getVerticalHelper(lm));
            } else {
                out[1] = distanceToEnd(targetView, getVerticalHelper(lm));
            }
        }
        return out;
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
            currentSnappedPosition = recyclerView.getChildAdapterPosition(snapView);
        } else {
            currentSnappedPosition = RecyclerView.NO_POSITION;
        }
        return snapView;
    }

    @Override
    public int[] calculateScrollDistance(int velocityX, int velocityY) {
        if (recyclerView == null
                || (verticalHelper == null && horizontalHelper == null)
                || (maxFlingDistance == -1 && maxFlingDistanceOffset == 0f)) {
            return super.calculateScrollDistance(velocityX, velocityY);
        }
        final int[] out = new int[2];
        Scroller scroller = new Scroller(recyclerView.getContext(),
                new DecelerateInterpolator());
        int maxDistance = getMaxFlingDistance();
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
     * Enable snapping of the last item that's snappable.
     * The default value is false, because you can't see the last item completely
     * if this is enabled.
     *
     * @param snap true if you want to enable snapping of the last snappable item
     */
    public void enableLastItemSnap(boolean snap) {
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
     * Changes the max scroll distance depending on the available size of the RecyclerView.
     * <p>
     * Example: if you pass 0.5f and the RecyclerView measures 600dp,
     * the max scroll distance will be 300dp.
     *
     * @param offset size offset to be used for the max scroll distance
     */
    public void setMaxFlingDistanceFromSize(float offset) {
        maxFlingDistanceOffset = offset;
    }

    public void smoothScrollToPosition(int position) {
        scrollTo(position, true);
    }

    public void scrollToPosition(int position) {
        scrollTo(position, false);
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
     * Changes the gravity of this {@link GravitySnapHelper}
     * and dispatches a smooth scroll for the new snap position.
     *
     * @param newGravity one of the following: {@link Gravity#START}, {@link Gravity#TOP},
     *                   {@link Gravity#END}, {@link Gravity#BOTTOM}, {@link Gravity#CENTER}
     */
    public void setGravity(int newGravity) {
        if (this.gravity != newGravity) {
            this.gravity = newGravity;
            updateSnap(true);
        }
    }

    public int getCurrentSnappedPosition() {
        if (currentSnappedPosition == RecyclerView.NO_POSITION) {
            updateSnap(false);
        }
        return currentSnappedPosition;
    }

    private void updateSnap(boolean scroll) {
        if (recyclerView == null || recyclerView.getLayoutManager() == null) {
            return;
        }
        View snapView = findSnapView(recyclerView.getLayoutManager());
        if (snapView != null && scroll) {
            int adapterPosition = recyclerView.getChildAdapterPosition(snapView);
            if (adapterPosition != RecyclerView.NO_POSITION) {
                scrollTo(adapterPosition, true);
            }
        }
    }

    private void scrollTo(int position, boolean smooth) {
        if (recyclerView.getLayoutManager() != null) {
            RecyclerView.ViewHolder viewHolder
                    = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                int[] distances = calculateDistanceToFinalSnap(recyclerView.getLayoutManager(),
                        viewHolder.itemView);
                if (smooth) {
                    recyclerView.smoothScrollBy(distances[0], distances[1]);
                } else {
                    recyclerView.scrollBy(distances[0], distances[1]);
                }
            } else {
                if (smooth) {
                    recyclerView.smoothScrollToPosition(position);
                } else {
                    recyclerView.scrollToPosition(position);
                }
            }
        }
    }

    private int distanceToStart(View targetView, @NonNull OrientationHelper helper) {
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

    private int distanceToEnd(View targetView, @NonNull OrientationHelper helper) {
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
     * @return the first view in the LayoutManager to snap to
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

    private int getMaxFlingDistance() {
        if (maxFlingDistanceOffset != 0f) {
            if (verticalHelper != null) {
                return (int) (recyclerView.getHeight() * maxFlingDistanceOffset);
            } else if (horizontalHelper != null) {
                return (int) (recyclerView.getWidth() * maxFlingDistanceOffset);
            } else {
                return Integer.MAX_VALUE;
            }
        } else if (maxFlingDistance != FLING_DISTANCE_DEFAULT) {
            return maxFlingDistance;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Changes the max scroll distance in absolute values.
     *
     * @param distance max scroll distance in pixels
     *                 or {@link GravitySnapHelper#FLING_DISTANCE_DEFAULT}
     *                 to reset the max scroll distance
     */
    public void setMaxFlingDistance(@Px int distance) {
        maxFlingDistance = distance;
        maxFlingDistanceOffset = 0f;
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

    public interface SnapListener {
        void onSnap(int position);
    }

}
