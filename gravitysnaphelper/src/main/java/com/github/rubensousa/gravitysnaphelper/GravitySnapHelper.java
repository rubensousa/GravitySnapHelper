package com.github.rubensousa.gravitysnaphelper;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;


public class GravitySnapHelper extends LinearSnapHelper {

    private OrientationHelper mVerticalHelper;
    private OrientationHelper mHorizontalHelper;
    private int mGravity;
    private boolean mIsRtl;

    @SuppressLint("RtlHardcoded")
    public GravitySnapHelper(int gravity) {
        if (gravity != Gravity.LEFT && gravity != Gravity.RIGHT
                && gravity != Gravity.START && gravity != Gravity.END
                && gravity != Gravity.BOTTOM && gravity != Gravity.TOP) {
            throw new IllegalArgumentException("Invalid gravity value. Use LEFT | RIGHT | START " +
                    "| END | BOTTOM | TOP constants");
        }

        mGravity = gravity;
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
            throws IllegalStateException {
        if (recyclerView != null) {
            mIsRtl = recyclerView.getContext().getResources().getBoolean(R.bool.is_rtl);
        }
        super.attachToRecyclerView(recyclerView);
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        int[] out = new int[2];

        if (layoutManager.canScrollHorizontally()) {
            if (mGravity == Gravity.START) {
                out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager), false);
            } else { // END
                out[0] = distanceToEnd(targetView, getHorizontalHelper(layoutManager), false);
            }
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            if (mGravity == Gravity.TOP) {
                out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager), false);
            } else { // BOTTOM
                out[1] = distanceToEnd(targetView, getVerticalHelper(layoutManager), false);
            }
        } else {
            out[1] = 0;
        }
        return out;
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            switch (mGravity) {
                case Gravity.START:
                    return findStartView(layoutManager, getHorizontalHelper(layoutManager));
                case Gravity.TOP:
                    return findStartView(layoutManager, getVerticalHelper(layoutManager));
                case Gravity.END:
                    return findEndView(layoutManager, getHorizontalHelper(layoutManager));
                case Gravity.BOTTOM:
                    return findEndView(layoutManager, getVerticalHelper(layoutManager));
            }
        }

        return super.findSnapView(layoutManager);
    }

    private int distanceToStart(View targetView, OrientationHelper helper, boolean fromEnd) {
        if (mIsRtl && !fromEnd) {
            return distanceToEnd(targetView, helper, true);
        }
        return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
    }

    private int distanceToEnd(View targetView, OrientationHelper helper, boolean fromStart) {
        if (mIsRtl && !fromStart) {
            return distanceToStart(targetView, helper, true);
        }
        return helper.getDecoratedEnd(targetView) - helper.getEndAfterPadding();
    }

    /**
     * Returns the first view that we should snap to.
     *
     * @param layoutManager the recyclerview's layout manager
     * @param helper        orientation helper to calculate view sizes
     * @return the first view in the LayoutManager to snap to
     */
    private View findStartView(RecyclerView.LayoutManager layoutManager,
                               OrientationHelper helper) {

        if (layoutManager instanceof LinearLayoutManager) {
            int firstChild = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            if (firstChild == RecyclerView.NO_POSITION) {
                return null;
            }

            View child = layoutManager.findViewByPosition(firstChild);


            float visibleWidth;

            // We should return the child if it's visible width
            // is greater than 0.5 of it's total width.
            // In a RTL configuration, we need to check the start point and in LTR the end point
            if (mIsRtl) {
                visibleWidth = (float) (helper.getTotalSpace() - helper.getDecoratedStart(child))
                        / helper.getDecoratedMeasurement(child);
            } else {
                visibleWidth = (float) Math.abs(helper.getDecoratedEnd(child))
                        / helper.getDecoratedMeasurement(child);
            }


            if (visibleWidth > 0.5f) {
                return child;
            } else {
                // If we're at the end of the list, we shouldn't snap
                // to avoid having the last item not completely visible.
                boolean startOfList = mIsRtl ? ((LinearLayoutManager) layoutManager)
                        .findFirstCompletelyVisibleItemPosition() == 0
                        : ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition()
                        == layoutManager.getItemCount() - 1;

                if (startOfList) {
                    return null;
                } else {
                    // If the child wasn't returned, we need to return
                    // the next view close to the start.
                    return layoutManager.findViewByPosition(firstChild + 1);

                }
            }
        }

        return super.findSnapView(layoutManager);
    }

    private View findEndView(RecyclerView.LayoutManager layoutManager,
                             OrientationHelper helper) {

        if (layoutManager instanceof LinearLayoutManager) {
            int lastChild = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

            if (lastChild == RecyclerView.NO_POSITION) {
                return null;
            }

            View child = layoutManager.findViewByPosition(lastChild);

            if (helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
                    <= helper.getTotalSpace()) {
                return child;
            } else {
                if (((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition()
                        == 0) {
                    return null;
                } else {
                    return layoutManager.findViewByPosition(lastChild - 1);
                }
            }
        }

        return super.findSnapView(layoutManager);
    }

    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

}
