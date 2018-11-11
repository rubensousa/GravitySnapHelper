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
                snapView = findStartView(lm, getHorizontalHelper(lm));
                break;
            case Gravity.END:
                snapView = findEndView(lm, getHorizontalHelper(lm));
                break;
            case Gravity.TOP:
                snapView = findStartView(lm, getVerticalHelper(lm));
                break;
            case Gravity.BOTTOM:
                snapView = findEndView(lm, getVerticalHelper(lm));
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
        if ((pos == 0 && (!isRtl && !lm.getReverseLayout())
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
        /**
         * The last position or the first position
         * (when there's a reverse layout or we're on RTL mode) must collapse to the padding edge.
         */
        if ((pos == 0 && (isRtl || lm.getReverseLayout())
                || pos == lm.getItemCount() - 1 && (!isRtl && !lm.getReverseLayout()))
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
    private View findStartView(LinearLayoutManager lm, @NonNull OrientationHelper helper) {
        View startView = null;

        int distanceToStart = Integer.MAX_VALUE;

        // TODO Optimize. We only need to check the first/last 2 views for each scenario.
        for (int i = 0; i < lm.getChildCount(); i++) {
            View currentView = lm.getChildAt(i);
            int currentViewDistance;
            if (!isRtl) {
                currentViewDistance = Math.abs(helper.getDecoratedStart(currentView));
            } else {
                currentViewDistance = Math.abs(helper.getDecoratedEnd(currentView)
                        - helper.getEnd());
            }
            if (currentViewDistance < distanceToStart) {
                distanceToStart = currentViewDistance;
                startView = currentView;
            }
        }

        // If we're at the end of the list, we shouldn't snap
        // to avoid having the last item not completely visible.
        if (isAtEndOfList(lm) && !snapLastItem) {
            return null;
        }

        return startView;
    }

    @Nullable
    private View findEndView(LinearLayoutManager lm, @NonNull OrientationHelper helper) {

        View endView = null;
        int distanceToEnd = Integer.MAX_VALUE;

        for (int i = 0; i < lm.getChildCount(); i++) {
            View currentView = lm.getChildAt(i);
            int currentViewDistance;
            if (!isRtl) {
                currentViewDistance = Math.abs(helper.getDecoratedEnd(currentView)
                        - helper.getEnd());
            } else {
                currentViewDistance = Math.abs(helper.getDecoratedStart(currentView));
            }
            if (currentViewDistance < distanceToEnd) {
                distanceToEnd = currentViewDistance;
                endView = currentView;
            }
        }

        // If we're at the end of the list, we shouldn't snap
        // to avoid having the last item not completely visible.
        if (isAtEndOfList(lm) && !snapLastItem) {
            return null;
        }

        return endView;
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
