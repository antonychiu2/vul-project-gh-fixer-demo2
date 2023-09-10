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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.client.CmsPublishItemStatus.Signal;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsResourceStateUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * A panel representing a single publish group.<p>
 * 
 * @since 8.0.0
 */
public class CmsPublishGroupPanel extends Composite {

    /** The CSS bundle used for this widget. */
    protected static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /** Text metrics key. */
    private static final String TM_PUBLISH_LIST = "PublishList";

    /** The handler which is called when the publish item selection changes. */
    protected I_CmsPublishSelectionChangeHandler m_selectionChangeHandler;

    /** The group header (containing the label and add/remove buttons). */
    private CmsSimpleListItem m_header = new CmsSimpleListItem();

    /** The data model for the publish dialog. */
    protected CmsPublishDataModel m_model;

    /** The root panel of this widget. */
    private CmsList<CmsTreeItem> m_panel = new CmsList<CmsTreeItem>();

    /** The button for selecting all resources in the group. */
    private CmsPushButton m_selectAll;

    /** The group index for this panel's corresponding group. */
    protected int m_groupIndex;

    /** The number of loaded publish item widgets for this group (used for scrolling).<p> */
    private int m_itemIndex;

    /** The publish resources of the current group.<p>*/
    private List<CmsPublishResource> m_publishResources;

    /** The global map of selection controllers of *ALL* groups (to which this group's selection controllers are added). */
    private Map<CmsUUID, CmsPublishItemSelectionController> m_controllersById;

    /** The button for deselecting all resources in the group. */
    private CmsPushButton m_selectNone;

    /** A flag which indicates whether only resources with problems should be shown. */
    private boolean m_showProblemsOnly;

    /**
     * Constructs a new instance.<p>
     * 
     * @param title the title of the group
     * @param groupIndex the index of the group which this panel should render
     * @param selectionChangeHandler the handler for selection changes for publish resources
     * @param model the data model for the publish resources
     * @param controllersById the map of selection controllers to which this panel's selection controllers should be added
     * @param showProblemsOnly if true, sets this panel into "show resources with problems only" mode
     */
    public CmsPublishGroupPanel(
        String title,
        int groupIndex,
        I_CmsPublishSelectionChangeHandler selectionChangeHandler,
        CmsPublishDataModel model,
        Map<CmsUUID, CmsPublishItemSelectionController> controllersById,
        boolean showProblemsOnly) {

        initWidget(m_panel);
        m_panel.add(m_header);
        m_model = model;
        m_groupIndex = groupIndex;
        m_publishResources = model.getGroups().get(groupIndex).getResources();
        m_controllersById = controllersById;
        m_panel.truncate(TM_PUBLISH_LIST, CmsPublishDialog.DIALOG_WIDTH);
        initSelectButtons();
        if (hasOnlyProblemResources()) {
            m_selectAll.setEnabled(false);
            m_selectNone.setEnabled(false);
        }
        m_showProblemsOnly = showProblemsOnly;
        if (hasNoProblemResources() && showProblemsOnly) {
            this.setVisible(false);
        }

        HTML label = new HTML();
        label.setHTML(title + CmsPublishSelectPanel.formatResourceCount(m_publishResources.size()));
        label.addStyleName(CSS.groupHeader());
        m_header.add(label);

        FlowPanel clear = new FlowPanel();
        clear.setStyleName(CSS.clear());
        m_header.add(clear);
        m_selectionChangeHandler = selectionChangeHandler;
    }

    /**
     * Creates a basic list item widget for a given publish resource bean.<p>
     * 
     * @param resourceBean the publish resource bean
     * 
     * @return the list item widget representing the publish resource bean 
     */
    public static CmsListItemWidget createListItemWidget(CmsPublishResource resourceBean) {

        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(getTitle(resourceBean));
        info.setSubTitle(resourceBean.getName());
        String stateLabel = org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_RESOURCE_STATE_0);
        String stateName = CmsResourceStateUtil.getStateName(resourceBean.getState());
        // this can be null for the source resources of broken relations in the 'broken links' 
        // panel since these resources don't have to be new or deleted or changed
        if (stateName != null) {
            info.addAdditionalInfo(stateLabel, stateName, CmsResourceStateUtil.getStateStyle(resourceBean.getState()));
        }
        CmsListItemWidget itemWidget = new CmsListItemWidget(info);
        if (resourceBean.getInfo() != null) {
            Image warningImage = new Image(I_CmsImageBundle.INSTANCE.warningSmallImage());
            warningImage.setTitle(resourceBean.getInfo().getValue());
            String permaVisible = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible();
            warningImage.addStyleName(permaVisible);
            itemWidget.addButton(warningImage);
        }
        itemWidget.setIcon(CmsIconUtil.getResourceIconClasses(resourceBean.getResourceType(), false));
        return itemWidget;
    }

    /** 
     * Utility method for getting the title of a publish resource bean, or a default title 
     * if the bean has no title.<p>
     * 
     * @param resourceBean the resource bean for which the title should be retrieved
     *  
     * @return the bean's title, or a default title
     */
    private static String getTitle(CmsPublishResource resourceBean) {

        String title = resourceBean.getTitle();
        if ((title == null) || title.equals("")) {
            title = Messages.get().key(Messages.GUI_NO_TITLE_0);
        }
        return title;
    }

    /**
     * Adds the list item for the next publish resource and returns  true on success, while
     * also incrementing the internal item index.<p>
     * 
     * @return true if an item was added
     */
    public boolean addNextItem() {

        if (m_itemIndex >= m_publishResources.size()) {
            return false;
        }
        CmsPublishResource res = m_publishResources.get(m_itemIndex);
        m_itemIndex += 1;
        if ((res.getInfo() == null) && m_showProblemsOnly) {
            return false;
        } else {
            addItem(res);
            return true;
        }
    }

    /**
     * Returns true if there are more potential items to add.<p>
     * 
     * @return true if there are possibly more items 
     */
    public boolean hasMoreItems() {

        return m_itemIndex < m_publishResources.size();
    }

    /**
     * Returns true if the corresponding group has no  resources with problems.<p>
     * 
     * @return true if the group for this panel has no resources with problems 
     */
    protected boolean hasNoProblemResources() {

        return 0 == m_model.countResourcesInGroup(new CmsPublishDataModel.HasProblems(), m_model.getGroups().get(
            m_groupIndex).getResources());
    }

    /**
     * Returns true if the corresponding group has only resources with problems.<p>
     * 
     * @return true if the group for this panel has only resources with problems. 
     */
    protected boolean hasOnlyProblemResources() {

        return m_model.getGroups().get(m_groupIndex).getResources().size() == m_model.countResourcesInGroup(
            new CmsPublishDataModel.HasProblems(),
            m_model.getGroups().get(m_groupIndex).getResources());
    }

    /**
     * Adds a resource bean to this group.<p>
     * 
     * @param resourceBean the resource bean which should be added
     */
    private void addItem(CmsPublishResource resourceBean) {

        CmsTreeItem row = buildItem(resourceBean, m_model.getStatus(resourceBean.getId()), false);
        m_panel.add(row);

        for (CmsPublishResource related : resourceBean.getRelated()) {
            row.addChild(buildItem(related, m_model.getStatus(related.getId()), true));
        }
    }

    /**
     * Creates a widget from resource bean data.<p>
     * 
     * @param resourceBean the resource bean for which a widget should be constructed
     * @param status the publish item status
     * @param isSubItem true if this is not a top-level publish item  
     * 
     * @return a widget representing the resource bean
     */
    private CmsTreeItem buildItem(final CmsPublishResource resourceBean, CmsPublishItemStatus status, boolean isSubItem) {

        CmsListItemWidget itemWidget = createListItemWidget(resourceBean);
        final CmsStyleVariable styleVar = new CmsStyleVariable(itemWidget);
        styleVar.setValue(CSS.itemToKeep());

        final CmsCheckBox checkbox = new CmsCheckBox();
        CmsTreeItem row;
        row = new CmsTreeItem(false, checkbox, itemWidget);
        if (isSubItem) {
            checkbox.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }

        row.setOpen(true);
        row.addStyleName(CSS.publishRow());

        // we do not need most of the interactive elements for the sub-items 
        if (!isSubItem) {
            ClickHandler checkboxHandler = new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    boolean checked = checkbox.isChecked();
                    m_model.signal(checked ? Signal.publish : Signal.unpublish, resourceBean.getId());
                    m_selectionChangeHandler.onChangePublishSelection();
                }
            };
            checkbox.addClickHandler(checkboxHandler);

            final boolean hasProblem = (resourceBean.getInfo() != null);
            if (hasProblem) {
                // can't select resource with problems
                checkbox.setChecked(false);
                checkbox.setEnabled(false);
            }

            final CmsCheckBox remover = new CmsCheckBox();
            final CmsPublishItemSelectionController controller = new CmsPublishItemSelectionController(
                resourceBean.getId(),
                checkbox,
                remover,
                styleVar,
                hasProblem);
            m_controllersById.put(resourceBean.getId(), controller);

            remover.setTitle(Messages.get().key(Messages.GUI_PUBLISH_REMOVE_BUTTON_0));
            remover.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent e) {

                    boolean remove = remover.isChecked();
                    m_model.signal(remove ? Signal.remove : Signal.unremove, resourceBean.getId());
                    m_selectionChangeHandler.onChangePublishSelection();
                }
            });
            itemWidget.addButtonToFront(remover);

            controller.update(status);
        }
        return row;
    }

    /**
     * Initializes the "select all/none" buttons, adds them to the group header and 
     * attaches event handlers to them.<p>
     */
    private void initSelectButtons() {

        m_selectAll = new CmsPushButton();
        m_selectAll.setText(Messages.get().key(Messages.GUI_PUBLISH_TOP_PANEL_ALL_BUTTON_0));
        m_selectAll.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageChecked());
        m_selectAll.setSize(I_CmsButton.Size.small);
        m_selectAll.setUseMinWidth(true);
        m_selectAll.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                m_model.signalGroup(Signal.publish, m_groupIndex);
                CmsPublishGroupPanel.this.m_selectionChangeHandler.onChangePublishSelection();
            }
        });

        m_selectNone = new CmsPushButton();
        m_selectNone.setText(Messages.get().key(Messages.GUI_PUBLISH_TOP_PANEL_NONE_BUTTON_0));
        m_selectNone.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageUnchecked());
        m_selectNone.setSize(I_CmsButton.Size.small);
        m_selectNone.setUseMinWidth(true);
        m_selectNone.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                m_model.signalGroup(Signal.unpublish, m_groupIndex);
                CmsPublishGroupPanel.this.m_selectionChangeHandler.onChangePublishSelection();
            }
        });

        FlowPanel selectButtons = new FlowPanel();
        selectButtons.add(m_selectAll);
        selectButtons.add(m_selectNone);
        selectButtons.setStyleName(CSS.selectButtons());
        m_header.add(selectButtons);
    }
}
