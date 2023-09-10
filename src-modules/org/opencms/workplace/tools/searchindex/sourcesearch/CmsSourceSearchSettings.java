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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.util.CmsStringUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Settings bean for the dialog.
 * <p>
 * 
 * @since 7.5.3
 * 
 */

public class CmsSourceSearchSettings {

    /** The content search result list attribute name in the session. */
    public static final String ATTRIBUTE_NAME_SOURCESEARCH_RESULT_LIST = "sourcesearchResultList";

    /** Display message. */
    private String m_message;

    /** The paths to collect resources. */
    private List<String> m_paths = new LinkedList<String>();

    /** The project to use. */
    private String m_project;

    /** The replace pattern. */
    private String m_replacepattern;

    /** The list of resource paths to process: all should be files. */
    private String[] m_resources;

    /** The search pattern. */
    private String m_searchpattern;

    /**
     * Bean constructor with cms object for path validation.<p>
     */
    public CmsSourceSearchSettings() {

        super();
        m_paths.add("/");
    }

    /**
     * @return the message
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * @return the paths
     */
    public List<String> getPaths() {

        return m_paths;
    }

    /**
     * @return the project
     */
    public String getProject() {

        return m_project;
    }

    /**
     * @return the replace pattern
     */
    public String getReplacepattern() {

        return m_replacepattern;
    }

    /**
     * @return the resources
     */
    public String getResources() {

        return CmsStringUtil.arrayAsString(m_resources, ",");
    }

    /**
     * Returns the resources paths in an array.<p>
     * 
     * @return the resources paths in an array.
     */
    public String[] getResourcesArray() {

        return m_resources;
    }

    /**
     * @return the search pattern
     */
    public String getSearchpattern() {

        return m_searchpattern;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(final String message) {

        // nop, this is hardcoded... just has to be here for "bean - convention".
    }

    /**
     * Sets the paths.<p>
     * 
     * @param paths the paths to set
     */
    public void setPaths(final List<String> paths) {

        m_paths = paths;
    }

    /**
     * @param project the project to work in
     */
    public void setProject(String project) {

        m_project = project;
    }

    /**
     * Sets the replace pattern.<p>
     * 
     * @param replacepattern the replace pattern
     */
    public void setReplacepattern(String replacepattern) {

        m_replacepattern = replacepattern;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(final String resources) {

        m_resources = CmsStringUtil.splitAsArray(resources, ",");

    }

    /**
     * Sets the search pattern.<p>
     * 
     * @param searchpattern the search pattern
     */
    public void setSearchpattern(String searchpattern) {

        m_searchpattern = searchpattern;
    }
}
