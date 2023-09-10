/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.publish.shared;

import org.opencms.db.CmsResourceState;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A publish resource.<p>
 * 
 * @since 7.6 
 */
public class CmsPublishResource implements IsSerializable {

    /** The resource type name.*/
    private String m_resourceType;

    /** The resource id.*/
    private CmsUUID m_id;

    /** The additional information, if any. */
    private CmsPublishResourceInfo m_info;

    /** The resource name.*/
    private String m_name;

    /** The related resources.*/
    private List<CmsPublishResource> m_related;

    /** Flag to indicate if the resource can be removed from the user's publish list.*/
    private boolean m_removable;

    /** The resource state.*/
    private CmsResourceState m_state;

    /** The resource title.*/
    private String m_title;

    /** 
     * Creates a new publish group bean.<p> 
     * 
     * @param id the resource id
     * @param name the resource name
     * @param title the resource title
     * @param resourceType the resource type name
     * @param state the resource state
     * @param removable to indicate if the resource can be removed from the user's publish list
     * @param info the additional information, if any
     * @param related the related resources
     **/
    public CmsPublishResource(
        CmsUUID id,
        String name,
        String title,
        String resourceType,
        CmsResourceState state,
        boolean removable,
        CmsPublishResourceInfo info,
        List<CmsPublishResource> related) {

        super();
        m_resourceType = resourceType;
        m_id = id;
        m_name = name;
        // m_related = ((related == null) ? Collections.<CmsPublishResource> emptyList() : related);
        // HACK: GWT serialization does not like unmodifiable collections :(
        m_related = ((related == null) ? new ArrayList<CmsPublishResource>() : related);
        m_state = state;
        m_title = title;
        m_removable = removable;
        m_info = info;
    }

    /**
     * For serialization.<p>
     */
    protected CmsPublishResource() {

        // for serialization
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the additional info.<p>
     *
     * @return the additional info
     */
    public CmsPublishResourceInfo getInfo() {

        return m_info;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the related resources.<p>
     *
     * @return the related resources
     */
    public List<CmsPublishResource> getRelated() {

        return m_related;
    }

    /**
     * Returns the state.<p>
     *
     * @return the state
     */
    public CmsResourceState getState() {

        return m_state;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the removable flag.<p>
     *
     * @return the removable flag
     */
    public boolean isRemovable() {

        return m_removable;
    }
}
