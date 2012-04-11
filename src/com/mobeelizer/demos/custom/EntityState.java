//
// EntityState.java
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

package com.mobeelizer.demos.custom;

/**
 * Holds all states used for displaying adding/removing item overlay animation. The A and S suffix determines type of the
 * animation. For <b>A</b> it's {@code anim/list_item_overlay} and for <b>S</b> {@code anim/list_item_sync_overlay}. The
 * difference between those two is the time of the animation.
 */
public enum EntityState {
    /**
     * When the state is set to {@code NEW_A} value an addition (green) overlay animation will be played.<br/>
     * This animation is defined in {@code res/anim/list_item_overlay.xml} file.
     */
    NEW_A,

    /**
     * When the state is set to {@code REMOVED_A} value an removal (red) overlay animation will be played.<br/>
     * This animation is defined in {@code res/anim/list_item_overlay.xml} file.
     */
    REMOVED_A,

    /**
     * When the state is set to {@code NEW_S} value an sync addition (green) overlay animation will be played.<br/>
     * This animation is defined in {@code res/anim/list_item_sync_overlay.xml} file.
     */
    NEW_S,

    /**
     * When the state is set to {@code REMOVED_S} value an sync removal (green) overlay animation will be played.<br/>
     * This animation is defined in {@code res/anim/list_item_sync_overlay.xml} file.
     */
    REMOVED_S,

    /** When the state is set to {@code NONE} value there will be no animation played. */
    NONE
}
