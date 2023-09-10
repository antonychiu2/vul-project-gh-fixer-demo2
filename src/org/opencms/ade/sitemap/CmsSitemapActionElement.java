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

package org.opencms.ade.sitemap;

import org.opencms.ade.publish.CmsPublishActionElement;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.CmsRpcException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Sitemap action used to generate the sitemap editor.<p>
 * 
 * See jsp file <tt>/system/modules/org.opencms.ade.sitemap/sitemap.jsp</tt>.<p>
 * 
 * @since 8.0.0
 */
public class CmsSitemapActionElement extends CmsGwtActionElement {

    /** The module name. */
    public static final String MODULE_NAME = "sitemap";

    /** The current sitemap data. */
    private CmsSitemapData m_sitemapData;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsSitemapActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        StringBuffer sb = new StringBuffer();
        String prefetchedData = serialize(
            I_CmsSitemapService.class.getMethod("prefetch", String.class),
            getSitemapData());
        sb.append(CmsSitemapData.DICT_NAME).append("='").append(prefetchedData).append("';");
        sb.append(ClientMessages.get().export(getRequest()));
        return wrapScript(sb).toString();
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export(".vfsMode"));
        sb.append(export());
        sb.append(new CmsPublishActionElement(getJspContext(), getRequest(), getResponse()).export());
        sb.append(createNoCacheScript(MODULE_NAME));
        return sb.toString();
    }

    /**
     * Returns the needed server data for client-side usage.<p> 
     *
     * @return the needed server data for client-side usage
     */
    public CmsSitemapData getSitemapData() {

        if (m_sitemapData == null) {
            try {
                m_sitemapData = CmsVfsSitemapService.newInstance(getRequest()).prefetch(getCoreData().getUri());
            } catch (CmsRpcException e) {
                // ignore, should never happen, and it is already logged
            }
        }
        return m_sitemapData;
    }

    /**
     * Returns the editor title.<p>
     * 
     * @return the editor title
     */
    public String getTitle() {

        return Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_EDITOR_TITLE_1, getCoreData().getUri());
    }
}
