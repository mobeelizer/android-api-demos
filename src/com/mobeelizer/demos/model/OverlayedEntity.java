//
// OverlayedEntity.java
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

package com.mobeelizer.demos.model;

import com.mobeelizer.demos.custom.EntityState;

/**
 * Base entity with methods and field used in code which determines whether to show an addition or removal overlay animation. An
 * instance with the default value won't start the animation.
 * 
 * <p>
 * The entity state value is not stored in database and it is reseted after starting the animation therefore it needs to be set
 * before each use.
 */
public abstract class OverlayedEntity {

    private EntityState entityState = EntityState.NONE;

    /**
     * Get current entity state. The default value is set to {@link EntityState#NONE}
     * 
     * @see EntityState
     */
    public EntityState getEntityState() {
        return entityState;
    }

    /**
     * Change entity state.
     * 
     * @param entityState
     *            New state
     * 
     * @see EntityState
     */
    public void setEntityState(final EntityState entityState) {
        this.entityState = entityState;
    }
}
