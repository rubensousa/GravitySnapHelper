package com.github.rubensousa.gravitysnaphelper;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GravityPagerSnapHelper extends PagerSnapHelper {

    private GravityDelegate delegate;

    public GravityPagerSnapHelper(int gravity) {
        this.delegate = new GravityDelegate(gravity);
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
            throws IllegalStateException {
        delegate.attachToRecyclerView(recyclerView);
        super.attachToRecyclerView(recyclerView);
    }

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        return delegate.calculateDistanceToFinalSnap(layoutManager, targetView);
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return delegate.findSnapView(layoutManager);
    }
}
