//
// GraphsConflictsOrderEntity.java
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

import java.util.Comparator;

import com.mobeelizer.mobile.android.api.MobeelizerDatabase;

/**
 * {@link MobeelizerDatabase} data mapping object used in graph's conflict example.
 * 
 * @see OverlayedEntity
 * @see GraphsConflictsItemEntity
 * @see Comparator
 */
public class GraphsConflictsOrderEntity extends OverlayedEntity implements Comparator<GraphsConflictsOrderEntity> {

    private String guid;

    private String owner;

    private boolean conflicted;

    private boolean modified;

    private String name;

    private int status;

    /**
     * Get an object ID
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Set an object ID
     * 
     * @param guid
     *            ID
     */
    public void setGuid(final String guid) {
        this.guid = guid;
    }

    /**
     * Get currently logged in user
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set the user
     * 
     * @param owner
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     * Is an entity in conflict
     */
    public boolean isConflicted() {
        return conflicted;
    }

    /**
     * Change conflicted state
     * 
     * @param conflicted
     *            New state
     */
    public void setConflicted(final boolean conflicted) {
        this.conflicted = conflicted;
    }

    /**
     * Is this entity modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Change the modified state
     * 
     * @param modified
     *            New state
     */
    public void setModified(final boolean modified) {
        this.modified = modified;
    }

    /**
     * Get entity name member
     */
    public String getName() {
        return name;
    }

    /**
     * Set new member name
     * 
     * @param name
     *            New name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get entity status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set new status
     * 
     * @param status
     *            New status
     */
    public void setStatus(final int status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((guid == null) ? 0 : guid.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GraphsConflictsOrderEntity other = (GraphsConflictsOrderEntity) obj;
        if (guid == null) {
            if (other.guid != null) {
                return false;
            }
        } else if (!guid.equals(other.guid)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final GraphsConflictsOrderEntity lhs, final GraphsConflictsOrderEntity rhs) {
        if (lhs.name == null && rhs.name != null) {
            return -1;
        } else if (lhs.name != null && rhs.name == null) {
            return 1;
        } else if (lhs.name == null && rhs.name == null) {
            return 0;
        } else {
            return lhs.name.compareTo(rhs.name);
        }
    }
}
