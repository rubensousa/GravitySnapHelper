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


import android.view.Gravity;
import android.view.View;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

class GravityDelegate {

    private OrientationHelper verticalHelper;
    private OrientationHelper horizontalHelper;
    private int gravity;
    private boolean isRtl;
    private boolean snapLastItem;
    private GravitySnapHelper.SnapListener listener;
    private boolean snapping;
    private int lastSnappedPosition;
    private RecyclerView recyclerView;
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE && snapping && listener != null) {
                if (lastSnappedPosition != RecyclerView.NO_POSITION) {
                    listener.onSnap(lastSnappedPosition);
                }
                snapping = false;
            }
        }
    };

    public GravityDelegate(int gravity, boolean enableSnapLast,
                           @Nullable GravitySnapHelper.SnapListener listener) {
        if (gravity != Gravity.START && gravity != Gravity.END
                && gravity != Gravity.BOTTOM && gravity != Gravity.TOP) {
            throw new IllegalArgumentException("Invalid gravity value. Use START " +
                    "| END | BOTTOM | TOP constants");
        }
        this.snapLastItem = enableSnapLast;
        this.gravity = gravity;
        this.listener = listener;
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
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
        }
    }

    public void smoothScrollToPosition(int position) {
        scrollTo(position, true);
    }

    public void scrollToPosition(int position) {
        scrollTo(position, false);
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

    @NonNull
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        int[] out = new int[2];

        if (!(layoutManager instanceof LinearLayoutManager)) {
            return out;
        }

        LinearLayoutManager lm = (LinearLayoutManager) layoutManager;

        if (lm.canScrollHorizontally()) {
            if ((isRtl && gravity == Gravity.END) || (!isRtl && gravity == Gravity.START)) {
                out[0] = distanceToStart(targetView, lm, getHorizontalHelper(lm));
            } else {
                out[0] = distanceToEnd(targetView, lm, getHorizontalHelper(lm));
            }
        } else {
            out[0] = 0;
        }

        if (lm.canScrollVertically()) {
            if (gravity == Gravity.TOP) {
                out[1] = distanceToStart(targetView, lm, getVerticalHelper(lm));
            } else { // BOTTOM
                out[1] = distanceToEnd(targetView, lm, getVerticalHelper(lm));
            }
        } else {
            out[1] = 0;
        }

        return out;
    }

    @Nullable
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return null;
        }
        View snapView = null;
        LinearLayoutManager lm = (LinearLayoutManager) layoutManager;
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
        }
        snapping = snapView != null;
        if (snapView != null) {
            lastSnappedPosition = recyclerView.getChildAdapterPosition(snapView);
        }
        return snapView;
    }

    public void enableLastItemSnap(boolean snap) {
        snapLastItem = snap;
    }

    private int distanceToStart(View targetView, LinearLayoutManager lm,
                                @NonNull OrientationHelper helper) {
        int pos = recyclerView.getChildLayoutPosition(targetView);
        int distance;
        if ((pos == 0 && (!isRtl || lm.getReverseLayout())
                || pos == lm.getItemCount() - 1 && (isRtl || lm.getReverseLayout()))
                && !recyclerView.getClipToPadding()) {
            int childStart = helper.getDecoratedStart(targetView);
            if (childStart >= helper.getStartAfterPadding() / 2) {
                distance = childStart - helper.getStartAfterPadding();
            } else {
                distance = childStart;
            }
        } else {
            distance = helper.getDecoratedStart(targetView);
        }
        return distance;
    }

    private int distanceToEnd(View targetView, LinearLayoutManager lm,
                              @NonNull OrientationHelper helper) {
        int pos = recyclerView.getChildLayoutPosition(targetView);
        int distance;

        // The last position or the first position
        // (when there's a reverse layout or we're on RTL mode) must collapse to the padding edge.
        if ((pos == 0 && (isRtl || lm.getReverseLayout())
                || pos == lm.getItemCount() - 1 && (!isRtl || lm.getReverseLayout()))
                && !recyclerView.getClipToPadding()) {
            int childEnd = helper.getDecoratedEnd(targetView);
            if (childEnd >= helper.getEnd() - (helper.getEnd() - helper.getEndAfterPadding()) / 2) {
                distance = helper.getDecoratedEnd(targetView) - helper.getEnd();
            } else {
                distance = childEnd - helper.getEndAfterPadding();
            }
        } else {
            distance = helper.getDecoratedEnd(targetView) - helper.getEnd();
        }
        return distance;
    }

    /**
     * Returns the first view that we should snap to.
     *
     * @param lm     the recyclerview's layout manager
     * @param helper orientation helper to calculate view sizes
     * @return the first view in the LayoutManager to snap to
     */
    @Nullable
    private View findEdgeView(LinearLayoutManager lm, OrientationHelper helper, boolean start) {
        if (lm.getChildCount() == 0) {
            return null;
        }

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
                currentViewDistance = Math.abs(helper.getDecoratedStart(currentView));
            } else {
                currentViewDistance = Math.abs(helper.getDecoratedEnd(currentView)
                        - helper.getEnd());
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
                || (lm.getReverseLayout() && gravity == Gravity.END)) {
            return lm.findLastCompletelyVisibleItemPosition() == lm.getItemCount() - 1;
        } else {
            return lm.findFirstCompletelyVisibleItemPosition() == 0;
        }
    }

    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (verticalHelper == null) {
            verticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return verticalHelper;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (horizontalHelper == null) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return horizontalHelper;
    }
}
