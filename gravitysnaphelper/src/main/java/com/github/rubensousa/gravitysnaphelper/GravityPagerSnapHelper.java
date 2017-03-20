/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2016 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific languag`e governing permissions and
 * limitations under the License.
 */

package com.github.rubensousa.gravitysnaphelper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GravityPagerSnapHelper
        extends PagerSnapHelper {

    private GravitySnapUtility mSnapUtility;

    // region Construction

    public GravityPagerSnapHelper(int gravity) {
        this(gravity, false, null);
    }

    public GravityPagerSnapHelper(int gravity, boolean enableSnapLastItem) {
        this(gravity, enableSnapLastItem, null);
    }

    public GravityPagerSnapHelper(int gravity, boolean enableSnapLastItem, SnapListener snapListener) {
        super();
        mSnapUtility = new GravitySnapUtility(gravity, enableSnapLastItem, snapListener);
    }

    // endregion

    // region PagerSnapHelper implementation

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        mSnapUtility.attachToRecyclerView(recyclerView);
        super.attachToRecyclerView(recyclerView);
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        return mSnapUtility.calculateDistanceToFinalSnap(layoutManager, targetView);
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return mSnapUtility.findSnapView(layoutManager);
    }

    // endregion

    // region Public methods

    /**
     * Enable snapping of the last item that's snappable.
     * The default value is false, because you can't see the last item completely
     * if this is enabled.
     *
     * @param snap true if you want to enable snapping of the last snappable item
     */
    public void enableLastItemSnap(boolean snap) {
        mSnapUtility.enableLastItemSnap(snap);
    }

    // endregion
}
