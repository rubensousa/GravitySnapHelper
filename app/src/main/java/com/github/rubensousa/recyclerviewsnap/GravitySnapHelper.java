package com.github.rubensousa.recyclerviewsnap;

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
        mGravity = gravity;
        if (mGravity == Gravity.LEFT) {
            mGravity = Gravity.START;
        } else if (mGravity == Gravity.RIGHT) {
            mGravity = Gravity.END;
        }
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
                out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager));
            } else { // END
                out[0] = distanceToEnd(targetView, getHorizontalHelper(layoutManager));
            }
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            if (mGravity == Gravity.TOP) {
                out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager));
            } else { // BOTTOM
                out[1] = distanceToEnd(targetView, getVerticalHelper(layoutManager));
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

    private int distanceToStart(View targetView, OrientationHelper helper) {
        if (mIsRtl) {
            return distanceToEnd(targetView, helper);
        }
        return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
    }

    private int distanceToEnd(View targetView, OrientationHelper helper) {
        if (mIsRtl) {
            return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
        }
        return helper.getDecoratedEnd(targetView) - helper.getEndAfterPadding();
    }

    private View findStartView(RecyclerView.LayoutManager layoutManager,
                               OrientationHelper helper) {

        if (layoutManager instanceof LinearLayoutManager) {
            int firstChild = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            if (firstChild == RecyclerView.NO_POSITION) {
                return null;
            }

            View child = layoutManager.findViewByPosition(firstChild);

            if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                    && helper.getDecoratedEnd(child) > 0) {
                return child;
            } else {
                if (((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition()
                        == layoutManager.getItemCount() - 1) {
                    return null;
                } else {
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
