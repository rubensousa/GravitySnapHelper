package com.github.rubensousa.gravitysnaphelper;


import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
            if ((gravity == Gravity.START || gravity == Gravity.END)) {
                isRtl = isRtl();
            }
            if (listener != null) {
                recyclerView.addOnScrollListener(scrollListener);
            }
            this.recyclerView = recyclerView;
        }
    }

    private boolean isRtl() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        }
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())
                == View.LAYOUT_DIRECTION_RTL;
    }

    @NonNull
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        int[] out = new int[2];

        if (layoutManager.canScrollHorizontally()) {
            if ((isRtl && gravity == Gravity.END) || (!isRtl && gravity == Gravity.START)) {
                out[0] = distanceToStart(targetView, layoutManager, getHorizontalHelper(layoutManager));
            } else {
                out[0] = distanceToEnd(targetView, layoutManager, getHorizontalHelper(layoutManager));
            }
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            if (gravity == Gravity.TOP) {
                out[1] = distanceToStart(targetView, layoutManager, getVerticalHelper(layoutManager));
            } else { // BOTTOM
                out[1] = distanceToEnd(targetView, layoutManager, getVerticalHelper(layoutManager));
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

    private int distanceToStart(View targetView, RecyclerView.LayoutManager lm,
                                @NonNull OrientationHelper helper) {
        int pos = recyclerView.getChildLayoutPosition(targetView);
        int distance;
        // Check if we have padding so that the first view stays in the correct position
        if ((pos == 0 && !isRtl || pos == lm.getItemCount() - 1 && isRtl)
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

    private int distanceToEnd(View targetView, RecyclerView.LayoutManager lm,
                              @NonNull OrientationHelper helper) {
        int pos = recyclerView.getChildLayoutPosition(targetView);
        int distance;
        // Check if we have padding so that the last view stays in the correct position
        if ((pos == 0 && isRtl || pos == lm.getItemCount() - 1 && !isRtl)
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
