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

import org.opencms.ade.galleries.client.CmsCategoriesTabHandler;
import org.opencms.ade.galleries.client.CmsGalleriesTabHandler;
import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.CmsSearchTabHandler;
import org.opencms.ade.galleries.client.CmsTypesTabHandler;
import org.opencms.ade.galleries.client.CmsVfsTabHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabbedPanelStyle;
import org.opencms.gwt.client.ui.I_CmsAutoHider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;

/**
 * Provides the method for the gallery dialog.<p>
 * 
 * @since 8.0.
 */
public class CmsGalleryDialog extends Composite
implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer>, ResizeHandler, HasResizeHandlers {

    /** The initial dialog width. */
    public static final int DIALOG_HEIGHT = 486;

    /** The initial dialog width. */
    public static final int DIALOG_WIDTH = 600;

    /** The parent panel for the gallery dialog. */
    protected FlowPanel m_parentPanel;

    /** The tabbed panel. */
    protected CmsTabbedPanel<A_CmsTab> m_tabbedPanel;

    /** The auto-hide parent to this dialog if present. */
    private I_CmsAutoHider m_autoHideParent;

    /** The categories tab. */
    private CmsCategoriesTab m_categoriesTab;

    /** The gallery controller. */
    private CmsGalleryController m_controller;

    /** The HTML id of the dialog element. */
    private String m_dialogElementId;

    /** The drag and drop handler. */
    private CmsDNDHandler m_dndHandler;

    /** The galleries tab. */
    private CmsGalleriesTab m_galleriesTab;

    /** The flag for the initails search. */
    private boolean m_isInitialSearch;

    /** The command which should be executed when this widget is attached to the DOM. */
    private Command m_onAttachCommand;

    /** The results tab. */
    private CmsResultsTab m_resultsTab;

    /** The Full-text search tab. */
    private CmsSearchTab m_searchTab;

    /** The show preview button. */
    private CmsPushButton m_showPreview;

    /** The types tab. */
    private CmsTypesTab m_typesTab;

    /** The VFS folder tab. */
    private CmsVfsTab m_vfsTab;

    /**
     * The constructor.<p> 
     * 
     * @param dndHandler the reference to the dnd manager
     * @param autoHideParent the auto-hide parent to this dialog if present
     */
    public CmsGalleryDialog(CmsDNDHandler dndHandler, I_CmsAutoHider autoHideParent) {

        this(CmsTabbedPanelStyle.buttonTabs);
        m_dndHandler = dndHandler;
        m_autoHideParent = autoHideParent;
    }

    /**
     * The default constructor for the gallery dialog.<p>
     *  
     * @param style the style for the panel
     */
    public CmsGalleryDialog(CmsTabbedPanelStyle style) {

        initCss();

        m_isInitialSearch = false;
        // parent widget
        m_parentPanel = new FlowPanel();
        m_parentPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().parentPanel());
        m_dialogElementId = HTMLPanel.createUniqueId();
        m_parentPanel.getElement().setId(m_dialogElementId);
        // set the default height of the dialog
        m_parentPanel.getElement().getStyle().setHeight((DIALOG_HEIGHT), Unit.PX);
        // tabs
        m_tabbedPanel = new CmsTabbedPanel<A_CmsTab>(style);
        // add tabs to parent widget
        m_parentPanel.add(m_tabbedPanel);
        m_showPreview = new CmsPushButton();
        m_showPreview.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SHOW_0));
        m_showPreview.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showPreview());
        m_showPreview.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_parentPanel.removeStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hidePreview());
            }
        });
        m_showPreview.setVisible(false);
        m_parentPanel.add(m_showPreview);
        // All composites must call initWidget() in their constructors.
        initWidget(m_parentPanel);
        addResizeHandler(this);
    }

    /**
     * Ensures all style sheets are loaded.<p>
     */
    private void initCss() {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.previewDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.croppingDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.imageEditorFormCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.imageAdvancedFormCss().ensureInjected();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
    }

    /** 
     * Adds a new tab to the gallery dialog.<p>
     * 
     * @param tab the tab to add 
     * @param name the label for the tab 
     */
    public void addTab(A_CmsTab tab, String name) {

        m_tabbedPanel.add(tab, name);
    }

    /**
     * Disables the search tab.<p>
     */
    public void disableSearchTab() {

        m_tabbedPanel.disableTab(m_resultsTab, Messages.get().key(Messages.GUI_GALLERY_NO_PARAMS_0));
    }

    /**
     * Enables the search tab.<p>
     */
    public void enableSearchTab() {

        m_tabbedPanel.enableTab(m_resultsTab);
    }

    /**
     * Displays the search result in the result tab.<p>
     * 
     * @param searchObj the search object
     */
    public void fillResultTab(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> paramPanels = null;
        if (!searchObj.isEmpty()) {
            enableSearchTab();
            paramPanels = new ArrayList<CmsSearchParamPanel>();
            Iterator<A_CmsTab> it = m_tabbedPanel.iterator();
            while (it.hasNext()) {
                A_CmsTab tab = it.next();
                CmsSearchParamPanel panel = tab.getParamPanel(searchObj);
                if (panel != null) {
                    paramPanels.add(panel);
                }
            }
            m_resultsTab.fillContent(searchObj, paramPanels);
        }
    }

    /**
     * Fill the tabs with the content provided from the info bean. <p>
     * 
     * @param tabIds the tabs to show 
     * @param controller the reference to the gallery controller
     */
    public void fillTabs(GalleryTabId[] tabIds, CmsGalleryController controller) {

        m_controller = controller;
        int i;
        for (i = 0; i < tabIds.length; i++) {
            switch (tabIds[i]) {
                case cms_tab_types:
                    m_typesTab = new CmsTypesTab(new CmsTypesTabHandler(controller), m_dndHandler);
                    m_typesTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_typesTab, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));
                    break;
                case cms_tab_galleries:
                    m_galleriesTab = new CmsGalleriesTab(new CmsGalleriesTabHandler(controller));
                    m_galleriesTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_galleriesTab, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));
                    break;
                case cms_tab_categories:
                    m_categoriesTab = new CmsCategoriesTab(new CmsCategoriesTabHandler(controller));
                    m_categoriesTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_categoriesTab, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));
                    break;
                case cms_tab_search:
                    m_searchTab = new CmsSearchTab(
                        new CmsSearchTabHandler(controller),
                        m_autoHideParent,
                        m_controller.getStartLocale(),
                        m_controller.getAvailableLocales(),
                        m_controller.getSearchScope());
                    m_searchTab.enableExpiredResourcesSearch(controller.getDialogMode() == I_CmsGalleryProviderConstants.GalleryMode.ade);
                    m_searchTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_searchTab, Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
                    break;
                case cms_tab_vfstree:
                    m_vfsTab = new CmsVfsTab(new CmsVfsTabHandler(controller));
                    m_vfsTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_vfsTab, Messages.get().key(Messages.GUI_TAB_TITLE_VFS_0));
                    break;
                default:
                    break;
            }
        }
        m_resultsTab = new CmsResultsTab(new CmsResultsTabHandler(controller), m_dndHandler);
        m_resultsTab.setTabTextAccessor(getTabTextAccessor(i));
        m_tabbedPanel.addWithLeftMargin(m_resultsTab, Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));
        disableSearchTab();

        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);
    }

    /**
     * Returns the categories tab widget.<p>
     * 
     * @return the categories widget
     */
    public CmsCategoriesTab getCategoriesTab() {

        return m_categoriesTab;
    }

    /**
     * Returns the gallery controller.<p>
     * 
     * @return the gallery controller 
     */
    public CmsGalleryController getController() {

        return m_controller;
    }

    /**
     * Returns the HTML id of the dialog element.<p>
     * 
     * @return the HTML id of the dialog element
     */
    public String getDialogId() {

        return m_dialogElementId;
    }

    /**
     * Returns the drag and drop handler.<p>
     *
     * @return the drag and drop handler
     */
    public CmsDNDHandler getDndHandler() {

        return m_dndHandler;
    }

    /**
     * Returns the galleries tab widget.<p>
     * 
     * @return the galleries widget
     */
    public CmsGalleriesTab getGalleriesTab() {

        return m_galleriesTab;
    }

    /**
     * Returns the parent panel of the dialog.<p>
     *
     * @return the parent
     */
    public FlowPanel getParentPanel() {

        return m_parentPanel;
    }

    /**
     * Returns the results tab widget.<p>
     * 
     * @return the results widget
     */
    public CmsResultsTab getResultsTab() {

        return m_resultsTab;
    }

    /**
     * Returns the searchTab.<p>
     *
     * @return the searchTab
     */
    public CmsSearchTab getSearchTab() {

        return m_searchTab;
    }

    /**
     * Returns the types tab widget.<p>
     * 
     * @return the types widget
     */
    public CmsTypesTab getTypesTab() {

        return m_typesTab;
    }

    /**
     * Returns the VFS tab widget.<p>
     * 
     * @return the VFS tab widget 
     */
    public CmsVfsTab getVfsTab() {

        return m_vfsTab;
    }

    /**
     * Hides or shows the show-preview-button.<p>
     * 
     * @param hide <code>true</code> to hide the button
     */
    public void hideShowPreviewButton(boolean hide) {

        m_showPreview.setVisible(!hide);
    }

    /**
     * @see com.google.gwt.event.logical.shared.BeforeSelectionHandler#onBeforeSelection(com.google.gwt.event.logical.shared.BeforeSelectionEvent)
     */
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {

        int selectedIndex = m_tabbedPanel.getSelectedIndex();
        int newIndex = event.getItem().intValue();
        if (m_tabbedPanel.isDisabledTab(newIndex)) {
            event.cancel();
            return;
        }
        if (selectedIndex != newIndex) {
            m_tabbedPanel.getWidget(selectedIndex).onDeselection();
        }
    }

    /**
     * 
     * @see com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google.gwt.event.logical.shared.ResizeEvent)
     */
    public void onResize(ResizeEvent event) {

        // TODO: implement
        // int newHeight = event.getHeight();
        // int newWidth = event.getWidth();

    }

    /**
     * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
     */
    public void onSelection(SelectionEvent<Integer> event) {

        int selectedIndex = m_tabbedPanel.getSelectedIndex();

        A_CmsTab tabWidget = m_tabbedPanel.getWidget(selectedIndex);
        if ((tabWidget instanceof CmsResultsTab) && m_isInitialSearch) {
            // no search here
            m_isInitialSearch = false;
        } else {
            tabWidget.onSelection();
        }
    }

    /**
     * Selects a tab by the given id.<p>
     * 
     * @param tabId the tab id
     * @param fireEvent <code>true</code> to fire the tab event
     */
    public void selectTab(GalleryTabId tabId, boolean fireEvent) {

        Iterator<A_CmsTab> it = m_tabbedPanel.iterator();
        while (it.hasNext()) {
            A_CmsTab tab = it.next();
            if (tabId == GalleryTabId.valueOf(tab.getTabId())) {
                m_tabbedPanel.selectTab(tab, fireEvent);
                break;
            }
        }
    }

    /**
     * Selects a tab.<p>
     * 
     * @param tabIndex the tab index to beselected
     * @param isInitial flag for initial search
     */
    public void selectTab(int tabIndex, boolean isInitial) {

        m_isInitialSearch = isInitial;
        m_tabbedPanel.selectTab(tabIndex);
    }

    /**
     * Sets the size of the gallery parent panel and triggers the event to the tab.<p>
     * 
     * @param width the new width 
     * @param height the new height
     */
    public void setDialogSize(int width, int height) {

        if (height > DIALOG_HEIGHT) {
            m_parentPanel.setHeight(Integer.toString(height - 2));
            m_parentPanel.setWidth(Integer.toString(width - 2));
            ResizeEvent.fire(this, width, height);
        }
    }

    /**
     * Sets the on attach command.<p>
     *
     * @param onAttachCommand the on attach command to set
     */
    public void setOnAttachCommand(Command onAttachCommand) {

        m_onAttachCommand = onAttachCommand;
    }

    /**
     * Creates a tab text accessor for a given text.<p>
     * 
     * @param pos the index of the tab 
     * 
     * @return the tab text accessor for the tab at index pos 
     */
    protected HasText getTabTextAccessor(final int pos) {

        HasText tabText = new HasText() {

            /**
             * @see com.google.gwt.user.client.ui.HasText#getText()
             */
            public String getText() {

                return m_tabbedPanel.getTabText(pos);

            }

            /**
             * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
             */
            public void setText(String text) {

                m_tabbedPanel.setTabText(pos, text);
            }
        };
        return tabText;
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        if (m_onAttachCommand != null) {
            m_onAttachCommand.execute();
            m_onAttachCommand = null;
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                updateSizes();
            }
        });
    }

    /**
     * Updates variable ui-element dimensions, execute after dialog has been attached and it's content is displayed.<p>
     */
    protected void updateSizes() {

        m_resultsTab.updateListSize();
    }
}
