
// UIUtils.java
// 
// Copyright (C) 2012 Mobeelizer Ltd. All Rights Reserved.
// 
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License. You may obtain a copy 
// of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
// License for the specific language governing permissions and limitations under
// the License.
// 

package com.mobeelizer.demos.utils;

import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.Button;

/**
 * Helper class for common operations on UI elements
 */
public class UIUtils {

    /**
     * Clipping value cannot be set in XML button background definition so it needs to be set here. Using this method is required
     * to show the part of the oval shape on the button background.
     * 
     * @param b
     *            Button which drawable will be clipped.
     */
    public static final void prepareClip(final Button b) {
        StateListDrawable sld = (StateListDrawable) b.getBackground();
        // for each state
        for (int i = 0; i < 4; i++) {
            sld.selectDrawable(i);
            // get all layers
            LayerDrawable ld = (LayerDrawable) sld.getCurrent();
            if (ld != null) {
                // and select the one to be clipped
                ((ClipDrawable) ld.getDrawable(2)).setLevel(3500);
            }

        }
    }
}
