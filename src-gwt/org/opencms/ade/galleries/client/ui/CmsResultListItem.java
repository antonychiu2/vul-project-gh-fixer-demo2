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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.shared.CmsIconUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Provides the specific list item for the results list.<p>
 * 
 * @since 8.0.
 */
public class CmsResultListItem extends CmsListItem {

    /** The delete button. */
    private CmsPushButton m_deleteButton;

    /** The name. */
    private String m_name;

    /** The preview button. */
    private CmsPushButton m_previewButton;

    /** The resource type name of the resource. */
    private String m_resourceType;

    /** The select button. */
    private CmsPushButton m_selectButton;

    /** The vfs path. */
    private String m_vfsPath;

    /**
     * Creates a new result list item with a main widget.<p>
     * 
     * @param resultItem the result item
     * @param dndHandler the drag and drop handler
     */
    public CmsResultListItem(CmsResultItemBean resultItem, CmsDNDHandler dndHandler) {

        resultItem.addAdditionalInfo(Messages.get().key(Messages.GUI_PREVIEW_LABEL_PATH_0), resultItem.getPath());
        CmsResultItemWidget resultItemWidget = new CmsResultItemWidget(resultItem);
        initContent(resultItemWidget);
        if (dndHandler != null) {
            setId(resultItem.getClientId());
            if (resultItem.getTitle() != null) {
                setName(resultItem.getTitle().toLowerCase().replace("/", "-").replace(" ", "_"));
            } else {
                // TODO: check if another name makes more sense
                setName(resultItem.getClientId());
            }
            initMoveHandle(dndHandler);
        } else {
            if (resultItemWidget.hasTileView()) {
                addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingItem());
            }
        }
        // add delete button
        m_deleteButton = createDeleteButton();
        if (!resultItem.isEditable()) {
            m_deleteButton.disable(resultItem.getNoEditReson());
        }
        resultItemWidget.addButton(m_deleteButton);

        // add  preview button
        m_previewButton = new CmsPushButton();
        m_previewButton.setImageClass(I_CmsImageBundle.INSTANCE.style().searchIcon());
        m_previewButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_previewButton.setTitle(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SHOW_0));
        resultItemWidget.addButton(m_previewButton);

        m_selectButton = new CmsPushButton();
        // TODO: use different icon
        m_selectButton.setImageClass(I_CmsImageBundle.INSTANCE.style().addIcon());
        m_selectButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_selectButton.setTitle(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));
        m_selectButton.setVisible(false);
        resultItemWidget.addButton(m_selectButton);

        // add file icon
        resultItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(resultItem.getType(), resultItem.getPath(), false));
        if (!resultItem.isReleasedAndNotExpired()) {
            addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().expired());
        }
    }

    /**
     * Creates the delete button for this item.<p>
     * 
     * @return the delete button
     */
    public static CmsPushButton createDeleteButton() {

        CmsPushButton result = new CmsPushButton();
        result.setImageClass(I_CmsImageBundle.INSTANCE.style().deleteIcon());
        result.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        result.setTitle(Messages.get().key(Messages.GUI_RESULT_BUTTON_DELETE_0));
        return result;
    }

    /**
     * Adds the delete button click handler.<p>
     * 
     * @param handler the click handler
     */
    public void addDeleteClickHandler(ClickHandler handler) {

        m_deleteButton.addClickHandler(handler);
    }

    /**
     * Adds a double click event handler.<p>
     * 
     * @param handler the event handler to add
     *  
     * @return the handler registration for removing the event handler 
     */
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {

        return addDomHandler(handler, DoubleClickEvent.getType());
    }

    /**
     * Adds the preview button click handler.<p>
     * 
     * @param handler the click handler
     */
    public void addPreviewClickHandler(ClickHandler handler) {

        m_previewButton.addClickHandler(handler);
    }

    /**
     * Adds the select button click handler.<p>
     * 
     * @param handler the click handler
     */
    public void addSelectClickHandler(ClickHandler handler) {

        m_selectButton.setVisible(true);
        m_selectButton.addClickHandler(handler);
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
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the vfs path.<p>
     *
     * @return the vfs path
     */
    public String getVfsPath() {

        return m_vfsPath;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the resource type name.<p>
     *
     * @param resourceType the resource type name to set
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }
}