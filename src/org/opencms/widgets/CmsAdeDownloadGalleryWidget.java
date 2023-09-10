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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.json.JSONObject;

/**
 * ADE download gallery widget implementations.<p>
 * 
 * @since 8.0.0 
 */
public class CmsAdeDownloadGalleryWidget extends A_CmsAdeGalleryWidget {

    /** The gallery name. */
    private static final String GALLERY_NAME = "download";

    /**
     * Constructor.<p>
     */
    public CmsAdeDownloadGalleryWidget() {

        this("");
    }

    /**
     * Creates a new gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    protected CmsAdeDownloadGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryName()
     */
    @Override
    public String getGalleryName() {

        return GALLERY_NAME;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsAdeDownloadGalleryWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getAdditionalGalleryInfo(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected JSONObject getAdditionalGalleryInfo(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        return null;
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryTypes()
     */
    @Override
    protected String getGalleryTypes() {

        return CmsResourceTypeBinary.getStaticTypeName() + "," + CmsResourceTypeImage.getStaticTypeName();
    }
}
