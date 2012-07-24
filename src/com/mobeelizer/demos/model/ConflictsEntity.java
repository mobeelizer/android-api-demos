//
// ConflictsEntity.java
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
 * {@link MobeelizerDatabase} data mapping object used in conflicts example.
 * 
 * @see OverlayedEntity
 * @see Comparator
 */
public class ConflictsEntity extends OverlayedEntity implements Comparator<ConflictsEntity> {

    private String guid;

    private String owner;

    private boolean conflicted;

    private boolean modified;

    private String title;

    private int score;

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
     * Get entity title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title
     * 
     * @param title
     *            New title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Get entity score
     */
    public int getScore() {
        return score;
    }

    /**
     * Set new score
     * 
     * @param score
     *            New score
     */
    public void setScore(final int score) {
        this.score = score;
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
        ConflictsEntity other = (ConflictsEntity) obj;
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
    public int compare(final ConflictsEntity lhs, final ConflictsEntity rhs) {
        if (lhs.title == null && rhs.title != null) {
            return -1;
        } else if (lhs.title != null && rhs.title == null) {
            return 1;
        } else if (lhs.title == null && rhs.title == null) {
            return 0;
        } else {
            return lhs.title.compareTo(rhs.title);
        }
    }
}
