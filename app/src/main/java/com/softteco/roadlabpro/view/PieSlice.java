/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 *
 * 	   Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.softteco.roadlabpro.view;

import android.graphics.Path;
import android.graphics.Region;

import com.softteco.roadlabpro.util.UIUtils;

public class PieSlice {

    private final Path mPath = new Path();
    private final Region mRegion = new Region();
    private int mColor = 0xFF33B5E5;
    private int mSelectedColor = -1;
    private float mValue;
    private float mOldValue;
    private float mGoalValue;
    private String mTitle;

    public PieSlice(final float value) {
        setValue(value);
    }

    public PieSlice(final int color) {
        this(color, 100);
    }

    public PieSlice(final int color, final float value) {
        this(value);
        setColor(color);
    }
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(final String title) {
        mTitle = title;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(final int color) {
        mColor = color;
    }

    public int getSelectedColor() {
        if (-1 == mSelectedColor) mSelectedColor = UIUtils.darkenColor(mColor);
        return mSelectedColor;
    }

    public void setSelectedColor(final int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(final float value) {
        mValue = value;
    }

    public float getOldValue() {
        return mOldValue;
    }

    public void setOldValue(final float oldValue) { mOldValue = oldValue; }

    public float getGoalValue() {
        return mGoalValue;
    }

    public void setGoalValue(final float goalValue) { mGoalValue = goalValue; }

    public Path getPath() {
        return mPath;
    }

    public Region getRegion() {
        return mRegion;
    }
}
