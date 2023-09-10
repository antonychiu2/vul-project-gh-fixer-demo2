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

package org.opencms.ade.containerpage;

import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.galleries.CmsGalleryActionElement;
import org.opencms.ade.publish.CmsPublishActionElement;
import org.opencms.ade.upload.CmsUploadActionElement;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.CmsRpcException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Action element for container-page editor includes.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerpageActionElement extends CmsGwtActionElement {

    /** The module name. */
    public static final String MODULE_NAME = "containerpage";

    /** The current container page data. */
    private CmsCntPageData m_cntPageData;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsContainerpageActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(ClientMessages.get().export(getRequest()));
        String prefetchedData = serialize(I_CmsContainerpageService.class.getMethod("prefetch"), getCntPageData());
        sb.append(CmsCntPageData.DICT_NAME).append("='").append(prefetchedData).append("';");
        sb.append(CmsContainer.KEY_CONTAINER_DATA).append("= new Array();");
        return wrapScript(sb).toString();
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export());
        sb.append(new CmsPublishActionElement(null, getRequest(), null).export());
        sb.append(new CmsGalleryActionElement(null, getRequest(), null).exportForContainerpage());
        sb.append(export());
        sb.append(new CmsUploadActionElement(getJspContext(), getRequest(), getResponse()).export());
        sb.append(createNoCacheScript(MODULE_NAME));
        return sb.toString();
    }

    /**
     * Returns the needed server data for client-side usage.<p> 
     *
     * @return the needed server data for client-side usage
     */
    public CmsCntPageData getCntPageData() {

        if (m_cntPageData == null) {
            try {
                m_cntPageData = CmsContainerpageService.newInstance(getRequest()).prefetch();
            } catch (CmsRpcException e) {
                // ignore, should never happen, and it is already logged
            }
        }
        return m_cntPageData;
    }
}
