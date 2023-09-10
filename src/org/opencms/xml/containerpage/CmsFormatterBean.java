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

package org.opencms.xml.containerpage;

import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * A bean containing formatter configuration data as strings.<p>
 * 
 * @since 8.0.0
 */
public class CmsFormatterBean {

    /** Default formatter type constant. */
    public static final String PREVIEW_TYPE = "_PREVIEW_";

    /** The width of the preview window for the formatters. */
    public static final int PREVIEW_WIDTH = 640;

    /** Wildcard formatter type for width based formatters. */
    public static final String WILDCARD_TYPE = "*";

    /** The formatter container type. */
    private String m_containerType;

    /** Indicates if this formatter is to be used as preview in the ADE gallery GUI. */
    private boolean m_isPreviewFormatter;

    /** Indicates if this is a type based or width based formatter. */
    private boolean m_isTypeFormatter;

    /** The formatter JSP. */
    private String m_jspRootPath;

    /** The UUID of the JSP resource for this formatter. */
    private CmsUUID m_jspStructureId;

    /** The location this formatter was configured in. */
    private String m_location;

    /** If true, will match any container/width combination. */
    private boolean m_matchAll;

    /** The formatter max width. */
    private int m_maxWidth;

    /** The formatter min width. */
    private int m_minWidth;

    /** Indicates if the content should be searchable in the online index when this formatter is used. */
    private boolean m_search;

    /**
     * Constructor for creating a new formatter configuration with resource structure id.<p>
     * 
     * @param containerType the formatter container type 
     * @param jspRootPath the formatter JSP VFS root path
     * @param jspStructureId the structure id of the formatter JSP
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width 
     * @param preview indicates if this formatter is to be used for the preview in the ADE gallery GUI
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     */
    public CmsFormatterBean(
        String containerType,
        String jspRootPath,
        CmsUUID jspStructureId,
        int minWidth,
        int maxWidth,
        boolean preview,
        boolean searchContent,
        String location) {

        m_jspRootPath = jspRootPath;
        m_jspStructureId = jspStructureId;

        m_containerType = containerType;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_containerType)) {
            m_containerType = WILDCARD_TYPE;
        }
        m_isTypeFormatter = !WILDCARD_TYPE.equals(m_containerType);

        m_minWidth = minWidth;
        m_maxWidth = maxWidth;

        m_isPreviewFormatter = preview;
        m_search = searchContent;
        m_location = location;
    }

    /**
     * Constructor for creating a new formatter configuration without resource structure id.<p>
     * 
     * @param containerType the formatter container type 
     * @param jspRootPath the formatter JSP VFS root path
     * @param minWidthStr the formatter min width
     * @param maxWidthStr the formatter max width 
     * @param preview indicates if this formatter is to be used for the preview in the ADE gallery GUI
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     */
    public CmsFormatterBean(
        String containerType,
        String jspRootPath,
        String minWidthStr,
        String maxWidthStr,
        String preview,
        String searchContent,
        String location) {

        m_jspRootPath = jspRootPath;

        m_containerType = containerType;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_containerType)) {
            m_containerType = WILDCARD_TYPE;
        }

        m_minWidth = -1;
        m_maxWidth = Integer.MAX_VALUE;
        m_isTypeFormatter = true;

        if (WILDCARD_TYPE.equals(m_containerType)) {
            // wildcard formatter; index by width
            m_isTypeFormatter = false;
            // if no width available, use -1

            try {
                m_minWidth = Integer.parseInt(minWidthStr);
            } catch (NumberFormatException e) {
                //ignore; width will be -1 
            }
            try {
                m_maxWidth = Integer.parseInt(maxWidthStr);
            } catch (NumberFormatException e) {
                //ignore; maxWidth will be max. integer 
            }
        }

        m_isPreviewFormatter = Boolean.valueOf(preview).booleanValue();

        m_search = CmsStringUtil.isEmptyOrWhitespaceOnly(searchContent)
        ? true
        : Boolean.valueOf(searchContent).booleanValue();

        m_location = location;
    }

    /**
     * Constructor for creating a formatter bean which matches all container/width combinations.<p>
     * 
     * @param jspRootPath the jsp root path 
     * @param jspStructureId the jsp structure id 
     * @param location the formatter location 
     */
    CmsFormatterBean(String jspRootPath, CmsUUID jspStructureId, String location) {

        this("*", jspRootPath, jspStructureId, -1, Integer.MAX_VALUE, false, false, location);
        m_matchAll = true;
    }

    /**
     * Checks if the given container type matches the ADE gallery preview type.<p>
     * 
     * @param containerType the container type to check
     * 
     * @return <code>true</code> if the given container type matches the ADE gallery preview type
     */
    public static boolean isPreviewType(String containerType) {

        return PREVIEW_TYPE.equals(containerType);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsFormatterBean) {
            CmsFormatterBean other = (CmsFormatterBean)obj;

            if (other.m_isTypeFormatter == m_isTypeFormatter) {
                // not same formatter type means not equal
                if (m_isTypeFormatter) {
                    // this is a type formatter, we use just the type name
                    return CmsStringUtil.isEqual(m_containerType, other.m_containerType);
                } else {
                    // this is a width formatter, we use both min and max width
                    return (m_minWidth == other.m_minWidth) && (m_maxWidth == other.m_maxWidth);
                }
            }
        }
        return false;
    }

    /**
     * Returns the formatter container type.<p>
     * 
     * If this is "*", then the formatter is a width based formatter.<p>
     * 
     * @return the formatter container type 
     */
    public String getContainerType() {

        return m_containerType;
    }

    /**
     * Returns the root path of the formatter JSP in the OpenCms VFS.<p>
     * 
     * @return the root path of the formatter JSP in the OpenCms VFS.<p>
     */
    public String getJspRootPath() {

        return m_jspRootPath;
    }

    /**
     * Returns the structure id of the JSP resource for this formatter.<p>
     * 
     * @return the structure id of the JSP resource for this formatter
     */
    public CmsUUID getJspStructureId() {

        return m_jspStructureId;
    }

    /**
     * Returns the location this formatter was defined in.<p>
     * 
     * This will be an OpenCms VFS root path, either to the XML schema XSD, or the
     * configuration file this formatter was defined in, or to the JSP that 
     * makes up this formatter.<p>
     * 
     * @return the location this formatter was defined in
     */
    public String getLocation() {

        return m_location;
    }

    /**
     * Returns the maximum formatter width.<p>
     * 
     * If this is not set, then {@link Integer#MAX_VALUE} is returned.<p>
     *  
     * @return the maximum formatter width 
     */
    public int getMaxWidth() {

        return m_maxWidth;
    }

    /**
     * Returns the minimum formatter width.<p>
     * 
     * If this is not set, then <code>-1</code> is returned.<p>
     * 
     * @return the minimum formatter width
     */
    public int getMinWidth() {

        return m_minWidth;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_containerType.hashCode() ^ ((m_minWidth * 33) ^ m_maxWidth);
    }

    /** 
     * Returns true if this formatter should match all type/width combinations.<p>
     * 
     * @return true if this formatter should match all type/width combinations 
     */
    public boolean isMatchAll() {

        return m_matchAll;
    }

    /**
     * Indicates if this formatter is to be used as preview in the ADE gallery GUI.
     * 
     * @return <code>true</code> if this formatter is to be used as preview in the ADE gallery GUI
     */
    public boolean isPreviewFormatter() {

        return m_isPreviewFormatter;
    }

    /**
     * Returns <code>true</code> in case an XML content formatted with this formatter should be included in the 
     * online full text search.<p>
     * 
     * @return <code>true</code> in case an XML content formatted with this formatter should be included in the 
     * online full text search
     */
    public boolean isSearchContent() {

        return m_search;
    }

    /**
     * Returns <code>true</code> in case this formatter is based on type information.<p>
     * 
     * @return <code>true</code> in case this formatter is based on type information
     */
    public boolean isTypeFormatter() {

        return m_isTypeFormatter;
    }

    /**
     * Sets the structure id of the JSP for this formatter.<p>
     *
     * This is "package visible" as it should be only called from {@link CmsFormatterConfiguration#initialize(org.opencms.file.CmsObject)}.<p>
     * 
     * @param jspStructureId the structure id of the JSP for this formatter
     */
    void setJspStructureId(CmsUUID jspStructureId) {

        // package visibility is wanted
        m_jspStructureId = jspStructureId;
    }
}