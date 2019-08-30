/*
 * Copyright 2019 Rúben Sousa
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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An {@link OrientationAwareRecyclerView} that uses a default {@link GravitySnapHelper}
 */
public class GravitySnapRecyclerView extends OrientationAwareRecyclerView {

    @NonNull
    final private GravitySnapHelper snapHelper;

    public GravitySnapRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public GravitySnapRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GravitySnapRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.GravitySnapRecyclerView, defStyleAttr, 0);
        int snapGravity = typedArray.getInt(
                R.styleable.GravitySnapRecyclerView_snapGravity, 0);
        switch (snapGravity) {
            case 0:
                snapHelper = new GravitySnapHelper(Gravity.START);
                break;
            case 1:
                snapHelper = new GravitySnapHelper(Gravity.TOP);
                break;
            case 2:
                snapHelper = new GravitySnapHelper(Gravity.END);
                break;
            case 3:
                snapHelper = new GravitySnapHelper(Gravity.BOTTOM);
                break;
            case 4:
                snapHelper = new GravitySnapHelper(Gravity.CENTER);
            default:
                throw new IllegalArgumentException("Invalid gravity value. Use START " +
                        "| END | BOTTOM | TOP | CENTER constants");
        }

        snapHelper.setSnapToPadding(typedArray.getBoolean(
                R.styleable.GravitySnapRecyclerView_snapToPadding, false));

        snapHelper.enableLastItemSnap(typedArray.getBoolean(
                R.styleable.GravitySnapRecyclerView_snapLastItem, false));

        snapHelper.setMaxFlingSizeFraction(typedArray.getFloat(
                R.styleable.GravitySnapRecyclerView_snapMaxFlingSizeFraction,
                GravitySnapHelper.FLING_SIZE_FRACTION_DEFAULT));

        snapHelper.setScrollMsPerInch(typedArray.getFloat(
                R.styleable.GravitySnapRecyclerView_snapScrollMsPerInch, 100f));

        enableSnapping(typedArray.getBoolean(
                R.styleable.GravitySnapRecyclerView_snapEnabled, true));

        typedArray.recycle();
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (!snapHelper.smoothScrollToPosition(position)) {
            super.smoothScrollToPosition(position);
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if (!snapHelper.scrollToPosition(position)) {
            super.scrollToPosition(position);
        }
    }

    @NonNull
    public GravitySnapHelper getSnapHelper() {
        return snapHelper;
    }

    public void enableSnapping(Boolean enable) {
        if (enable) {
            snapHelper.attachToRecyclerView(this);
        } else {
            snapHelper.attachToRecyclerView(null);
        }
    }

    public int getCurrentSnappedPosition() {
        return snapHelper.getCurrentSnappedPosition();
    }

    public void setSnapListener(@Nullable GravitySnapHelper.SnapListener listener) {
        snapHelper.setSnapListener(listener);
    }
}
