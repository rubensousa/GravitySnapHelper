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


import androidx.annotation.Nullable;

/**
 * A {@link GravitySnapHelper} that sets a default max scroll distance
 * of the size of the RecyclerView by setting
 * {@link GravitySnapHelper#setMaxFlingSizeFraction(float)} to 1.0f by default
 *
 * @deprecated Use {@link GravitySnapHelper} instead
 */
@Deprecated
public class GravityPagerSnapHelper extends GravitySnapHelper {

    public GravityPagerSnapHelper(int gravity) {
        this(gravity, null);
    }

    public GravityPagerSnapHelper(int gravity,
                                  @Nullable GravitySnapHelper.SnapListener snapListener) {
        super(gravity, false, snapListener);
        setMaxFlingSizeFraction(1.0f);
        setScrollMsPerInch(50f);
    }
}
