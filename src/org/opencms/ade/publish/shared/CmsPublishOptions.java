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

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean encapsulating all ADE publish options.<p>
 * 
 * @since 7.6 
 */
public class CmsPublishOptions implements IsSerializable {

    /** Flag to include related resources. */
    private boolean m_includeRelated;

    /** Flag to include siblings. */
    private boolean m_includeSiblings;

    /** The id of the project to publish. */
    private CmsUUID m_projectId;

    /** 
     * Creates a new publish options bean.<p> 
     **/
    public CmsPublishOptions() {

        m_includeRelated = true;
        m_projectId = CmsUUID.getNullUUID();
    }

    /**
     * Creates a new publish options bean.<p> 
     * 
     * @param includeRelated Flag to include related resources
     * @param includeSiblings Flag to include siblings
     * @param projectId The id of the project to publish
     */
    public CmsPublishOptions(boolean includeRelated, boolean includeSiblings, CmsUUID projectId) {

        m_includeRelated = includeRelated;
        m_includeSiblings = includeSiblings;
        m_projectId = projectId;
    }

    /**
     * Returns the project id.<p>
     *
     * @return the project id
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Checks if to include related resources.<p>
     *
     * @return <code>true</code> if to include related resources
     */
    public boolean isIncludeRelated() {

        return m_includeRelated;
    }

    /**
     * Checks if to include siblings.<p>
     *
     * @return <code>true</code> if to include siblings
     */
    public boolean isIncludeSiblings() {

        return m_includeSiblings;
    }

    /**
     * Sets the flag to include related resources.<p>
     *
     * @param includeRelated the flag to set
     */
    public void setIncludeRelated(boolean includeRelated) {

        m_includeRelated = includeRelated;
    }

    /**
     * Sets the flag to include siblings.<p>
     *
     * @param includeSiblings the flag to set
     */
    public void setIncludeSiblings(boolean includeSiblings) {

        m_includeSiblings = includeSiblings;
    }

    /**
     * Sets the id of the project to publish.<p>
     *
     * @param projectId the id to set
     */
    public void setProjectId(CmsUUID projectId) {

        m_projectId = projectId;
    }
}
