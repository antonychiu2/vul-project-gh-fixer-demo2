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

package org.opencms.ade.galleries.shared;

import org.opencms.util.CmsStringUtil;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a single VFS resource entry for use by the VFS tab of the galleries.<p>
 */
public class CmsVfsEntryBean implements IsSerializable {

    /** Flag to indicate if the user has write permissions to the folder. */
    private boolean m_editable;

    /** Flag indicating whether this is entry should be displayed at the top level of the tree. */
    private boolean m_isRoot;

    /** The site path of this VFS entry. */
    private String m_sitePath;

    /** The folder title. */
    private String m_title;

    /**
     * Creates a new VFS entry bean.<p>
     * 
     * @param sitePath the site path
     * @param title the folder title
     * @param isRoot flag indicating whether this is entry should be displayed at the top level of the tree
     * @param editable <code>true</code> if the user has write permissions to the folder
     */
    public CmsVfsEntryBean(String sitePath, String title, boolean isRoot, boolean editable) {

        m_sitePath = sitePath;
        m_isRoot = isRoot;
        m_editable = editable;
        m_title = title;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsVfsEntryBean() {

        // do nothing 
    }

    /**
     * Gets the name which should be displayed in the widget representing this VFS entry.<p>
     * 
     * @return the name to display
     */
    public String getDisplayName() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_title)) {
            return m_title;
        }
        if (m_isRoot) {
            return m_sitePath;
        } else {
            String fixedPath = m_sitePath.replaceFirst("/$", "");
            int lastSlash = fixedPath.lastIndexOf('/');
            if (lastSlash == -1) {
                return fixedPath;
            }
            return fixedPath.substring(lastSlash + 1);
        }
    }

    /**
     * Returns the site path of this VFS tree. 
     * 
     * @return the site path 
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the editable flag. Indicate if the user has write permissions to the folder.<p>
     *
     * @return the editable flag
     */
    public boolean isEditable() {

        return m_editable;
    }

    /**
     * Returns true if this entry is a top-level entry.<p>
     * 
     * @return true if this is a top-level entry 
     */
    public boolean isRoot() {

        return m_isRoot;
    }

    /**
     * Sets if the user has write permissions to the folder.<p>
     *
     * @param editable <code>true</code> if the user has write permissions to the folder
     */
    public void setEditable(boolean editable) {

        m_editable = editable;
    }

}
