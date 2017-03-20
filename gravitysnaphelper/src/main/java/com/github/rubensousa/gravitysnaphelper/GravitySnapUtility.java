package com.github.rubensousa.gravitysnaphelper;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;

/**
 * Created by a.rinaldi on 20/03/2017.
 */

/*package*/ class GravitySnapUtility {

    private OrientationHelper mVerticalHelper;
    private OrientationHelper mHorizontalHelper;

    boolean mSnapping;
    private int mGravity;
    SnapListener mSnapListener;
    private boolean mIsRtlHorizontal;
    private boolean mSnapLastItemEnabled;

    // region Construction

    public GravitySnapUtility(int gravity, boolean enableSnapLastItem, SnapListener snapListener) {
        if (gravity != Gravity.START && gravity != Gravity.END
                && gravity != Gravity.BOTTOM && gravity != Gravity.TOP) {
            throw new IllegalArgumentException("Invalid gravity value. Use START " +
                    "| END | BOTTOM | TOP constants");
        }
        mSnapListener = snapListener;
        mGravity = gravity;
        mSnapLastItemEnabled = enableSnapLastItem;
    }

    // endregion

    // region Public methods

    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            if (mGravity == Gravity.START || mGravity == Gravity.END) {
                mIsRtlHorizontal
                        = recyclerView.getContext().getResources().getBoolean(R.bool.is_rtl);
            }
            if (mSnapListener != null) {
                recyclerView.addOnScrollListener(mScrollListener);
            }
        }
    }

    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
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

    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        View snapView = null;
        if (layoutManager instanceof LinearLayoutManager) {
            switch (mGravity) {
                case Gravity.START:
                    snapView = findStartView(layoutManager, getHorizontalHelper(layoutManager));
                    break;
                case Gravity.END:
                    snapView = findEndView(layoutManager, getHorizontalHelper(layoutManager));
                    break;
                case Gravity.TOP:
                    snapView = findStartView(layoutManager, getVerticalHelper(layoutManager));
                    break;
                case Gravity.BOTTOM:
                    snapView = findEndView(layoutManager, getVerticalHelper(layoutManager));
                    break;
            }
        }

        mSnapping = snapView != null;

        return snapView;
    }

    public void enableLastItemSnap(boolean snap) {
        mSnapLastItemEnabled = snap;
    }

    // endregion

    // region Private methods

    private int distanceToStart(View targetView, OrientationHelper helper, boolean fromEnd) {
        if (mIsRtlHorizontal && !fromEnd) {
            return distanceToEnd(targetView, helper, true);
        }

        return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
    }

    private int distanceToEnd(View targetView, OrientationHelper helper, boolean fromStart) {
        if (mIsRtlHorizontal && !fromStart) {
            return distanceToStart(targetView, helper, true);
        }

        return helper.getDecoratedEnd(targetView) - helper.getEndAfterPadding();
    }

    /**
     * Returns the first view that we should snap to.
     *
     * @param layoutManager the RecyclerView's layout manager
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
            if (mIsRtlHorizontal) {
                visibleWidth = (float) (helper.getTotalSpace() - helper.getDecoratedStart(child))
                        / helper.getDecoratedMeasurement(child);
            } else {
                visibleWidth = (float) helper.getDecoratedEnd(child)
                        / helper.getDecoratedMeasurement(child);
            }

            // If we're at the end of the list, we shouldn't snap
            // to avoid having the last item not completely visible.
            boolean endOfList = ((LinearLayoutManager) layoutManager)
                    .findLastCompletelyVisibleItemPosition()
                    == layoutManager.getItemCount() - 1;

            if (visibleWidth > 0.5f && !endOfList) {
                return child;
            } else if (mSnapLastItemEnabled && endOfList) {
                return child;
            } else if (endOfList) {
                return null;
            } else {
                // If the child wasn't returned, we need to return
                // the next view close to the start.
                return layoutManager.findViewByPosition(firstChild + 1);
            }
        }

        return null;
    }

    /**
     * Returns the last view that we should snap to.
     *
     * @param layoutManager the RecyclerView's layout manager
     * @param helper        orientation helper to calculate view sizes
     * @return the last view in the LayoutManager to snap to
     */
    private View findEndView(RecyclerView.LayoutManager layoutManager,
                             OrientationHelper helper) {

        if (layoutManager instanceof LinearLayoutManager) {
            int lastChild = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

            if (lastChild == RecyclerView.NO_POSITION) {
                return null;
            }

            View child = layoutManager.findViewByPosition(lastChild);

            float visibleWidth;

            if (mIsRtlHorizontal) {
                visibleWidth = (float) helper.getDecoratedEnd(child)
                        / helper.getDecoratedMeasurement(child);
            } else {
                visibleWidth = (float) (helper.getTotalSpace() - helper.getDecoratedStart(child))
                        / helper.getDecoratedMeasurement(child);
            }

            // If we're at the start of the list, we shouldn't snap
            // to avoid having the first item not completely visible.
            boolean startOfList = ((LinearLayoutManager) layoutManager)
                    .findFirstCompletelyVisibleItemPosition() == 0;

            if (visibleWidth > 0.5f && !startOfList) {
                return child;
            } else if (mSnapLastItemEnabled && startOfList) {
                return child;
            } else if (startOfList) {
                return null;
            } else {
                // If the child wasn't returned, we need to return the previous view
                return layoutManager.findViewByPosition(lastChild - 1);
            }
        }
        return null;
    }

    private int getSnappedPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof LinearLayoutManager) {
            if (mGravity == Gravity.START || mGravity == Gravity.TOP) {
                return ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
            } else if (mGravity == Gravity.END || mGravity == Gravity.BOTTOM) {
                return ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
            }
        }

        return RecyclerView.NO_POSITION;

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

    // endregion

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                mSnapping = false;
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE && mSnapping && mSnapListener != null) {
                int position = getSnappedPosition(recyclerView);
                if (position != RecyclerView.NO_POSITION) {
                    mSnapListener.onSnap(position);
                }
                mSnapping = false;
            }
        }
    };
}
