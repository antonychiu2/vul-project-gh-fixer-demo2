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

package org.opencms.ade.upload.client.ui;

import org.opencms.ade.upload.client.Messages;
import org.opencms.ade.upload.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.upload.shared.CmsUploadData;
import org.opencms.ade.upload.shared.CmsUploadFileBean;
import org.opencms.ade.upload.shared.CmsUploadProgessInfo;
import org.opencms.ade.upload.shared.I_CmsUploadConstants;
import org.opencms.ade.upload.shared.rpc.I_CmsUploadService;
import org.opencms.ade.upload.shared.rpc.I_CmsUploadServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsProgressBar;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.util.CmsChangeHeightAnimation;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Provides an upload dialog.<p>
 * 
 * @since 8.0.0
 */
public abstract class A_CmsUploadDialog extends CmsPopup {

    /**
     * Provides the upload progress information.<p>
     * 
     * Has a progressbar and a table for showing details.<p>
     */
    private class CmsUploadProgressInfo extends FlowPanel {

        /** The progress bar. */
        private CmsProgressBar m_bar;

        /** The table for showing upload details. */
        private FlexTable m_fileinfo;

        /** A sorted list of the filenames to upload. */
        private List<String> m_orderedFilenamesToUpload;

        /** Signals if the progress was set at least one time. */
        private boolean m_started;

        /**
         * Default constructor.<p>
         */
        public CmsUploadProgressInfo() {

            // get a ordered list of filenames
            m_orderedFilenamesToUpload = new ArrayList<String>(getFilesToUpload().keySet());
            Collections.sort(m_orderedFilenamesToUpload, String.CASE_INSENSITIVE_ORDER);

            // create the progress bar
            m_bar = new CmsProgressBar();

            // create the file info table
            m_fileinfo = new FlexTable();
            m_fileinfo.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().fileInfoTable());

            // arrange the progress info
            addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().progressInfo());
            add(m_bar);
            add(m_fileinfo);
        }

        /**
         * Finishes the state of the progress bar.<p>
         */
        public void finish() {

            String length = formatBytes(getContentLength());
            int fileCount = m_orderedFilenamesToUpload.size();
            m_bar.setValue(100);
            m_fileinfo.removeAllRows();
            m_fileinfo.setHTML(0, 0, "<b>" + Messages.get().key(Messages.GUI_UPLOAD_FINISH_UPLOADED_0) + "</b>");
            m_fileinfo.setText(0, 1, Messages.get().key(
                Messages.GUI_UPLOAD_FINISH_UPLOADED_VALUE_4,
                new Integer(fileCount),
                new Integer(fileCount),
                getFileText(),
                length));
        }

        /**
         * Sets the progress information.<p>
         * 
         * @param info the progress info bean
         */
        public void setProgress(CmsUploadProgessInfo info) {

            int currFile = info.getCurrentFile();

            int currFileIndex = 0;
            if (currFile == 0) {
                // no files read so far 
            } else {
                currFileIndex = currFile - 1;
                if (currFileIndex >= m_orderedFilenamesToUpload.size()) {
                    currFileIndex = m_orderedFilenamesToUpload.size() - 1;
                }
            }

            if (getContentLength() == 0) {
                setContentLength(info.getContentLength());
            }

            String currFilename = m_orderedFilenamesToUpload.get(currFileIndex);
            String contentLength = formatBytes(getContentLength());
            int fileCount = m_orderedFilenamesToUpload.size();
            String readBytes = formatBytes(getBytesRead(info.getPercent()));

            m_bar.setValue(info.getPercent());

            if (!m_started) {
                m_started = true;
                m_fileinfo.setHTML(0, 0, "<b>"
                    + Messages.get().key(Messages.GUI_UPLOAD_PROGRESS_CURRENT_FILE_0)
                    + "</b>");
                m_fileinfo.setHTML(1, 0, "<b>" + Messages.get().key(Messages.GUI_UPLOAD_PROGRESS_UPLOADING_0) + "</b>");
                m_fileinfo.setHTML(2, 0, "");

                m_fileinfo.setText(0, 1, "");
                m_fileinfo.setText(1, 1, "");
                m_fileinfo.setText(2, 1, "");

                m_fileinfo.getColumnFormatter().setWidth(0, "100px");
            }

            m_fileinfo.setText(0, 1, currFilename);
            m_fileinfo.setText(1, 1, Messages.get().key(
                Messages.GUI_UPLOAD_PROGRESS_CURRENT_VALUE_3,
                new Integer(currFileIndex + 1),
                new Integer(fileCount),
                getFileText()));
            m_fileinfo.setText(2, 1, Messages.get().key(
                Messages.GUI_UPLOAD_PROGRESS_UPLOADING_VALUE_2,
                readBytes,
                contentLength));
        }

        /**
         * Returns the bytes that are read so far.<p>
         * 
         * The total request size is larger than the sum of all file sizes that are uploaded.
         * Because boundaries and the target folder or even some other information than only
         * the plain file contents are submited to the server.<p>
         * 
         * This method calculates the bytes that are read with the help of the file sizes.<p>
         *  
         * @param percent the server side determined percentage
         * 
         * @return the bytes that are read so far
         */
        private long getBytesRead(long percent) {

            return percent != 0 ? (getContentLength() * percent) / 100 : 0;
        }
    }

    /** Maximum width for the file item widget list. */
    private static final int DIALOG_WIDTH = 600;

    /** The size for kilobytes in bytes. */
    private static final float KILOBYTE = 1024L;

    /** The minimal height of the content wrapper. */
    private static final int MIN_CONTENT_HEIGHT = 110;

    /** Text metrics key. */
    private static final String TM_FILE_UPLOAD_LIST = "FileUploadList";

    /** The interval for updating the progress information in milliseconds. */
    private static final int UPDATE_PROGRESS_INTERVALL = 1000;

    /** Stores all files that were added. */
    private Map<String, CmsFileInfo> m_allFiles;

    /** Signals that the upload dialog was canceled. */
    private boolean m_canceled;

    /** Signals that the client currently loading. */
    private boolean m_clientLoading;

    /** The close handler. */
    private CloseHandler<PopupPanel> m_closeHandler;

    /** The sum of all file sizes. */
    private long m_contentLength;

    /** A flow panel with a dynamic height. */
    private FlowPanel m_contentWrapper;

    /** The upload data. */
    private CmsUploadData m_data;

    /** The user information text widget. */
    private HTML m_dialogInfo;

    /** The drag and drop message. */
    protected HTML m_dragAndDropMessage;

    /** The list of file item widgets. */
    private CmsList<I_CmsListItem> m_fileList;

    /** The list of filenames that should be unziped on the server. */
    private List<String> m_filesToUnzip;

    /** The Map of files to upload. */
    private Map<String, CmsFileInfo> m_filesToUpload;

    /** Stores the content height of the selection dialog. */
    private int m_firstContentHeight;

    /** Stores the height of the user information text widget of the selection dialog. */
    private int m_firstInfoHeight;

    /** Stores the height of the summary. */
    private int m_firstSummaryHeight;

    /** A local reference to the default gwt CSS. */
    private org.opencms.gwt.client.ui.css.I_CmsLayoutBundle m_gwtCss = org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE;

    /** The close handler registration. */
    private HandlerRegistration m_handlerReg;

    /** Stores the list items of all added files. */
    private Map<String, CmsListItem> m_listItems;

    /** A panel for showing client loading. */
    private FlowPanel m_loadingPanel;

    /** A timer to delay the loading animation. */
    private Timer m_loadingTimer;

    /** The main panel. */
    private FlowPanel m_mainPanel;

    /** The OK button. */
    private CmsPushButton m_okButton;

    /** The progress bar for the upload process. */
    private CmsUploadProgressInfo m_progressInfo;

    /** Signals whether the selection is done or not. */
    private boolean m_selectionDone;

    /** The user information text widget. */
    private HTML m_selectionSummary;

    /** The target folder to upload the selected files. */
    private String m_targetFolder;

    /** The timer for updating the progress. */
    private Timer m_updateProgressTimer = new Timer() {

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            updateProgress();
        }
    };

    /** The upload button of this dialog. */
    private CmsUploadButton m_uploadButton;

    /** The upload service instance. */
    private I_CmsUploadServiceAsync m_uploadService;

    /**
     * Default constructor.<p>
     */
    public A_CmsUploadDialog() {

        super(Messages.get().key(Messages.GUI_UPLOAD_DIALOG_TITLE_0));

        I_CmsLayoutBundle.INSTANCE.uploadCss().ensureInjected();
        m_data = (CmsUploadData)CmsRpcPrefetcher.getSerializedObject(getUploadService(), CmsUploadData.DICT_NAME);

        setModal(true);
        setGlassEnabled(true);
        catchNotifications();
        setWidth(DIALOG_WIDTH);

        // create a map that stores all files (upload, existing, invalid)
        m_allFiles = new HashMap<String, CmsFileInfo>();
        // create a map the holds all the list items for the selection dialog
        m_listItems = new HashMap<String, CmsListItem>();
        m_filesToUnzip = new ArrayList<String>();
        m_fileList = new CmsList<I_CmsListItem>();
        m_fileList.truncate(TM_FILE_UPLOAD_LIST, DIALOG_WIDTH - 50);

        // initialize a map that stores all the files that should be uploaded
        m_filesToUpload = new HashMap<String, CmsFileInfo>();

        // create the main panel
        m_mainPanel = new FlowPanel();

        // add the user info to the main panel
        m_dialogInfo = new HTML();
        m_dialogInfo.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogInfo());
        m_mainPanel.add(m_dialogInfo);

        // add the content wrapper to the main panel
        m_contentWrapper = new FlowPanel();
        m_contentWrapper.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().mainContentWidget());
        m_contentWrapper.addStyleName(m_gwtCss.generalCss().cornerAll());
        m_contentWrapper.getElement().getStyle().setPropertyPx("minHeight", MIN_CONTENT_HEIGHT);
        m_contentWrapper.add(m_fileList);
        m_mainPanel.add(m_contentWrapper);

        m_selectionSummary = new HTML();
        m_selectionSummary.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().summary());
        m_mainPanel.add(m_selectionSummary);

        // set the main panel as content of the popup
        setMainContent(m_mainPanel);

        // create and add the "OK", "Cancel" and upload button
        createButtons();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#addCloseHandler(com.google.gwt.event.logical.shared.CloseHandler)
     */
    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler) {

        m_closeHandler = handler;
        m_handlerReg = super.addCloseHandler(handler);
        return m_handlerReg;
    }

    /**
     * Creates a bean that can be used for the list item widget.<p>
     * 
     * @param file the info to create the bean for
     * 
     * @return a list info bean
     */
    public abstract CmsListInfoBean createInfoBean(CmsFileInfo file);

    /**
     * Returns the massage for too large files.<p>
     * 
     * @param file the file
     * 
     * @return the message
     */
    public abstract String getFileSizeTooLargeMessage(CmsFileInfo file);

    /**
     * Returns <code>true</code> if the file is too large, <code>false</code> otherwise.<p>
     * 
     * @param cmsFileInfo the file to check
     * 
     * @return <code>true</code> if the file is too large, <code>false</code> otherwise
     */
    public abstract boolean isTooLarge(CmsFileInfo cmsFileInfo);

    /**
     * Loads and shows this dialog.<p>
     */
    public void loadAndShow() {

        // enable or disable the OK button
        if (getFilesToUpload().isEmpty()) {
            disableOKButton(Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_NO_FILES_0));
            setDragAndDropMessage();
        } else {
            enableOKButton();
            removeDragAndDropMessage();
        }

        // set the user info
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_SELECTION_0), false);
        // set the selection summary
        updateSummary();

        // add a upload button
        m_uploadButton.createFileInput();

        // show the popup
        if (!isShowing()) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                /**
                 * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
                 */
                public void execute() {

                    setContentWrapperHeight();
                    center();
                }
            });
        }
        center();
    }

    /**
     * Sets the target folder.<p>
     * 
     * @param target the target folder to set 
     */
    public void setTargetFolder(String target) {

        m_targetFolder = target;
    }

    /**
     * Executes the submit action.<p>
     */
    public abstract void submit();

    /**
     * Updates the file summary.<p>
     */
    public abstract void updateSummary();

    /**
     * Adds the given file input field to this dialog.<p>
     * 
     * @param fileInput the file input field to add
     */
    protected void addFileInput(CmsFileInput fileInput) {

        // add the files selected by the user to the list of files to upload
        if (fileInput != null) {
            addFiles(Arrays.asList(fileInput.getFiles()));
        } else {
            loadAndShow();
        }
    }

    /**
     * Adds the given file input field to this dialog.<p>
     * 
     * @param fileInfos the file info objects 
     */
    protected void addFiles(List<CmsFileInfo> fileInfos) {

        if (fileInfos != null) {
            for (CmsFileInfo file : fileInfos) {

                // store all files
                m_allFiles.put(file.getFileName(), file);

                // add those files to the list of files to upload that potential candidates
                if (!isTooLarge(file) && (file.getFileSize() != 0)) {
                    m_filesToUpload.put(file.getFileName(), file);
                }

                // remove those files from the list to upload that were previously unchecked by the user
                if ((m_listItems.get(file.getFileName()) != null)
                    && (m_listItems.get(file.getFileName()).getCheckBox() != null)
                    && !m_listItems.get(file.getFileName()).getCheckBox().isChecked()) {
                    m_filesToUpload.remove(file.getFileName());
                }
            }

            // now rebuild the list: handle all files
            m_fileList.clearList();
            List<String> sortedFileNames = new ArrayList<String>(m_allFiles.keySet());
            Collections.sort(sortedFileNames, String.CASE_INSENSITIVE_ORDER);
            for (String filename : sortedFileNames) {
                CmsFileInfo file = m_allFiles.get(filename);
                addFileToList(file, false, isTooLarge(file));
            }
        }
        loadAndShow();
    }

    /**
     * Cancels the upload progress timer.<p>
     */
    protected void cancelUpdateProgress() {

        m_updateProgressTimer.cancel();
    }

    /**
     * Cancels the upload.<p>
     */
    protected void cancelUpload() {

        m_canceled = true;
        cancelUpdateProgress();

        CmsRpcAction<Boolean> callback = new CmsRpcAction<Boolean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getUploadService().cancelUpload(this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Boolean result) {

                hide();
            }
        };
        callback.execute();
    }

    /**
     * Creates the loading animation HTML and adds is to the content wrapper.<p>
     * 
     * @param msg the message to display below the animation
     */
    protected void createLoadingAnimation(String msg) {

        m_clientLoading = true;
        m_loadingPanel = new FlowPanel();
        m_loadingPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().loadingPanel());
        m_loadingPanel.addStyleName(m_gwtCss.generalCss().cornerAll());

        HTML animationDiv = new HTML();
        animationDiv.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().loadingAnimation());
        m_loadingPanel.add(animationDiv);

        HTML messageDiv = new HTML();
        messageDiv.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().loadingText());
        messageDiv.setHTML(msg);
        m_loadingPanel.add(messageDiv);

        m_contentWrapper.add(m_loadingPanel);
    }

    /**
     * Disables the OK button.<p>
     * 
     * @param disabledReason the reason for disabling the OK button
     */
    protected void disableOKButton(String disabledReason) {

        m_okButton.disable(disabledReason);
    }

    /**
     * Enables the OK button.<p>
     */
    protected void enableOKButton() {

        m_okButton.enable();
    }

    /**
     * Formats a given bytes value (file size).<p>
     *  
     * @param filesize the file size to format
     * 
     * @return the formated file size in KB
     */
    protected String formatBytes(long filesize) {

        double kByte = Math.ceil(filesize / KILOBYTE);
        String formated = NumberFormat.getDecimalFormat().format(new Double(kByte));
        return formated + " KB";
    }

    /**
     * Returns the contentLength.<p>
     *
     * @return the contentLength
     */
    protected long getContentLength() {

        return m_contentLength;
    }

    /**
     * Returns the contentWrapper.<p>
     *
     * @return the contentWrapper
     */
    protected FlowPanel getContentWrapper() {

        return m_contentWrapper;
    }

    /**
     * Returns the data.<p>
     *
     * @return the data
     */
    protected CmsUploadData getData() {

        return m_data;
    }

    /**
     * Returns the list of file names that have to unziped.<p>
     * 
     * @param all <code>true</code> if the returned list should contain those filenames that 
     * are not inside the map of files to upload. <code>false</code> only those filenames are 
     * returned that are also inside the map of files to upload
     * 
     * @return the list of file names that have to unziped
     */
    protected List<String> getFilesToUnzip(boolean all) {

        if (!all) {
            List<String> result = new ArrayList<String>();
            for (String fileName : m_filesToUnzip) {
                if (m_filesToUpload.keySet().contains(fileName)) {
                    result.add(fileName);
                }
            }
            return result;
        }
        return m_filesToUnzip;
    }

    /**
     * Returns the filesToUpload.<p>
     *
     * @return the filesToUpload
     */
    protected Map<String, CmsFileInfo> getFilesToUpload() {

        return m_filesToUpload;
    }

    /**
     * Returns "files" or "file" depending on the files to upload.<p>
     * 
     * @return "files" or "file" depending on the files to upload
     */
    protected String getFileText() {

        if (m_filesToUpload.size() == 1) {
            return Messages.get().key(Messages.GUI_UPLOAD_FILES_SINGULAR_0);
        }
        return Messages.get().key(Messages.GUI_UPLOAD_FILES_PLURAL_0);
    }

    /**
     * Returns the resource type name for a given filename.<p>
     * 
     * @param file the file info
     * 
     * @return the resource type name
     */
    protected String getResourceType(CmsFileInfo file) {

        String typeName = null;
        typeName = CmsCoreProvider.get().getExtensionMapping().get(file.getFileSuffix().toLowerCase());
        if (typeName == null) {
            typeName = "plain";
        }
        return typeName;
    }

    /**
     * Returns the targetFolder.<p>
     *
     * @return the targetFolder
     */
    protected String getTargetFolder() {

        return m_targetFolder;
    }

    /**
     * Returns the upload service instance.<p>
     * 
     * @return the upload service instance
     */
    protected I_CmsUploadServiceAsync getUploadService() {

        if (m_uploadService == null) {
            m_uploadService = GWT.create(I_CmsUploadService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.upload.CmsUploadService.gwt");
            ((ServiceDefTarget)m_uploadService).setServiceEntryPoint(serviceUrl);
        }
        return m_uploadService;
    }

    /**
     * Returns the upload JSP uri.<p>
     * 
     * @return the upload JSP uri
     */
    protected String getUploadUri() {

        return CmsCoreProvider.get().link(I_CmsUploadConstants.UPLOAD_ACTION_JSP_URI);
    }

    /**
     * Inserts a hidden form into.<p>
     *  
     * @param form the form to insert
     */
    protected void insertUploadForm(FormPanel form) {

        form.getElement().getStyle().setDisplay(Display.NONE);
        m_contentWrapper.add(form);
    }

    /**
     * The action that is executed if the user clicks on the OK button.<p>
     * 
     * If the selection dialog is currently shown the selected files are checked
     * otherwise the upload is triggered.<p>
     */
    protected void onOkClick() {

        if (!m_selectionDone) {
            checkSelection();
        } else {
            commit();
        }
    }

    /**
     * Execute to set the content wrapper height.<p>
     */
    protected void setContentWrapperHeight() {

        // set the max height of the content panel
        int fixedContent = 0;
        if (m_dialogInfo.isVisible()) {
            fixedContent += m_dialogInfo.getOffsetHeight();
        }
        if (m_selectionSummary.isVisible()) {
            fixedContent += m_selectionSummary.getOffsetHeight();
        }
        m_contentWrapper.getElement().getStyle().setPropertyPx("maxHeight", getAvailableHeight(fixedContent));
    }

    /**
     * Parses the upload response of the server and decides what to do.<p>
     * 
     * @param results a JSON Object
     */
    protected void parseResponse(String results) {

        cancelUpdateProgress();
        stopLoadingAnimation();

        if ((!m_canceled) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(results)) {
            JSONObject jsonObject = JSONParser.parseStrict(results).isObject();
            boolean success = jsonObject.get(I_CmsUploadConstants.KEY_SUCCESS).isBoolean().booleanValue();
            // If the upload is done so fast that we did not receive any progress information, then
            // the content length is unknown. For that reason take the request size to show how 
            // much bytes were uploaded.
            double size = jsonObject.get(I_CmsUploadConstants.KEY_REQUEST_SIZE).isNumber().doubleValue();
            long requestSize = new Double(size).longValue();
            if (m_contentLength == 0) {
                m_contentLength = requestSize;
            }
            if (success) {
                displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_FINISHING_0), false);
                m_progressInfo.finish();
                closeOnSuccess();
            } else {
                String message = jsonObject.get(I_CmsUploadConstants.KEY_MESSAGE).isString().stringValue();
                String stacktrace = jsonObject.get(I_CmsUploadConstants.KEY_STACKTRACE).isString().stringValue();
                showErrorReport(message, stacktrace);
            }
        }
    }

    /**
     * Decides how to go on depending on the information of the server response.<p>
     * 
     * Shows a warning if there is another upload process active (inside the same session).<p>
     * 
     * Otherwise if the list of files to upload contains already existent resources on the VFS or if there
     * are files selected that have invalid file names the overwrite dialog is shown.<p>
     * 
     * Only if there is no other upload process running and none of the selected files
     * is already existent on the VFS the upload is triggered.<p>
     * 
     * @param result the bean that contains the information to evaluate 
     */
    protected void proceedWorkflow(CmsUploadFileBean result) {

        if (result.isActive()) {
            m_okButton.enable();
            CmsNotification.get().send(Type.WARNING, Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_RUNNING_0));
        } else {
            if (!result.getExistingResourceNames().isEmpty() || !result.getInvalidFileNames().isEmpty()) {
                showOverwriteDialog(result);
            } else {
                commit();
            }
        }
    }

    /**
     * Sets the contentLength.<p>
     *
     * @param contentLength the contentLength to set
     */
    protected void setContentLength(long contentLength) {

        m_contentLength = contentLength;
    }

    /**
     * Sets the HTML of the selection summary.<p>
     * 
     * @param html the HTML to set as String 
     */
    protected void setSummaryHTML(String html) {

        m_selectionSummary.setHTML(html);
    }

    /**
     * Shows the error report.<p>
     * 
     * @param message the message to show
     * @param stacktrace the stacktrace to show
     */
    protected void showErrorReport(final String message, final String stacktrace) {

        if (!m_canceled) {
            CmsErrorDialog errDialog = new CmsErrorDialog(message, stacktrace);
            if (m_handlerReg != null) {
                m_handlerReg.removeHandler();
            }
            if (m_closeHandler != null) {
                errDialog.addCloseHandler(m_closeHandler);
            }
            hide();
            errDialog.center();
        }
    }

    /**
     * Retrieves the progress information from the server.<p>
     */
    protected void updateProgress() {

        CmsRpcAction<CmsUploadProgessInfo> callback = new CmsRpcAction<CmsUploadProgessInfo>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getUploadService().getUploadProgressInfo(this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onFailure(java.lang.Throwable)
             */
            @Override
            public void onFailure(Throwable t) {

                super.onFailure(t);
                cancelUpdateProgress();
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsUploadProgessInfo result) {

                updateProgressBar(result);
            }
        };
        callback.execute();
    }

    /**
     * Updates the progress bar.<p>
     * 
     * @param info the progress info
     */
    protected void updateProgressBar(CmsUploadProgessInfo info) {

        switch (info.getState()) {
            case notStarted:
                break;
            case running:
                m_progressInfo.setProgress(info);
                stopLoadingAnimation();
                break;
            case finished:
                m_progressInfo.finish();
                displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_FINISHING_0), false);
                startLoadingAnimation(Messages.get().key(Messages.GUI_UPLOAD_INFO_CREATING_RESOURCES_0), 1500);
                break;
            default:
                break;
        }
    }

    /**
     * Adds a click handler for the given checkbox.<p>
     * 
     * @param check the checkbox
     * @param file the file
     */
    private void addClickHandlerToCheckBox(final CmsCheckBox check, final CmsCheckBox unzip, final CmsFileInfo file) {

        check.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                // add or remove the file from the list of files to upload
                if (check.isChecked()) {
                    getFilesToUpload().put(file.getFileName(), file);
                    if (unzip != null) {
                        unzip.enable();
                    }
                } else {
                    getFilesToUpload().remove(file.getFileName());
                    if (unzip != null) {
                        unzip.disable(Messages.get().key(Messages.GUI_UPLOAD_FILE_NOT_SELECTED_0));
                    }
                }

                // disable or enable the OK button
                if (getFilesToUpload().isEmpty()) {
                    disableOKButton(Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_NO_FILES_0));
                } else {
                    enableOKButton();
                }

                // update summary
                updateSummary();

            }
        });
    }

    /**
     * Adds a file to the list.<p>
     * 
     * @param file the file to add
     * @param invalid signals if the filename is invalid
     * @param isTooLarge signals if the file size limit is exceeded
     * @param isFolder signals if the file is a folder
     */
    private void addFileToList(final CmsFileInfo file, boolean invalid, boolean isTooLarge) {

        CmsListInfoBean infoBean = createInfoBean(file);
        CmsListItemWidget listItemWidget = new CmsListItemWidget(infoBean);
        String icon = CmsIconUtil.getResourceIconClasses(getResourceType(file), file.getFileName(), false);
        listItemWidget.setIcon(icon);

        CmsCheckBox check = new CmsCheckBox();
        check.setChecked(false);
        if (!invalid && !isTooLarge) {
            if (file.getFileSize() == 0) {
                check.setChecked(false);
            }
            check.setChecked(m_filesToUpload.containsKey(file.getFileName()));
            check.setTitle(file.getFileName());
            if (!m_selectionDone && file.getFileName().toLowerCase().endsWith(".zip")) {
                final CmsCheckBox unzip = createUnzipCheckBox(file);
                addClickHandlerToCheckBox(check, unzip, file);
                listItemWidget.addButton(unzip);
            } else {
                addClickHandlerToCheckBox(check, null, file);
            }
        } else if (isTooLarge) {
            String message = getFileSizeTooLargeMessage(file);
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        } else {
            // is invalid
            String message = Messages.get().key(
                Messages.GUI_UPLOAD_FILE_INVALID_NAME_2,
                file.getFileName(),
                formatBytes(file.getFileSize()));
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        }

        CmsListItem listItem = new CmsListItem(check, listItemWidget);
        m_fileList.addItem(listItem);
        m_listItems.put(file.getFileName(), listItem);
    }

    /**
     * Changes the height of the content wrapper so that the dialog finally has the
     * same height that the dialog has when the min height is set on the selection screen.<p>
     */
    private void changeHeight() {

        int firstHeight = MIN_CONTENT_HEIGHT + m_firstInfoHeight + m_firstSummaryHeight + 2;
        int currentHeight = CmsDomUtil.getCurrentStyleInt(m_mainPanel.getElement(), CmsDomUtil.Style.height);
        int targetHeight = firstHeight - m_dialogInfo.getOffsetHeight() - m_selectionSummary.getOffsetHeight();
        if (currentHeight > firstHeight) {
            CmsChangeHeightAnimation.change(m_contentWrapper.getElement(), targetHeight, null, 750);
        }
    }

    /**
     * Before the upload data is effectively submited we have to check 
     * for already existent resources in the VFS.<p>
     * 
     * Executes the RPC call that checks the VFS for existing resources.
     * Passes the response object to a method that evaluates the result.<p>
     */
    private void checkSelection() {

        m_okButton.disable(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_OK_DISABLE_CHECKING_0));

        if (!m_selectionDone) {
            m_firstContentHeight = CmsDomUtil.getCurrentStyleInt(m_contentWrapper.getElement(), CmsDomUtil.Style.height);
            m_firstInfoHeight = m_dialogInfo.getOffsetHeight();
            m_firstSummaryHeight = m_selectionSummary.getOffsetHeight();
        }

        CmsRpcAction<CmsUploadFileBean> callback = new CmsRpcAction<CmsUploadFileBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                List<String> filesToCheck = new ArrayList<String>(getFilesToUpload().keySet());
                filesToCheck.removeAll(getFilesToUnzip(false));
                getUploadService().checkUploadFiles(filesToCheck, getTargetFolder(), this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsUploadFileBean result) {

                proceedWorkflow(result);
            }
        };
        callback.execute();
    }

    /**
     * Closes the dialog after a delay.<p>
     */
    private void closeOnSuccess() {

        Timer closeTimer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                A_CmsUploadDialog.this.hide();
            }
        };
        closeTimer.schedule(1500);
    }

    /**
     * Calls the submit action if there are any files selected for upload.<p>
     */
    private void commit() {

        m_selectionDone = true;
        if (!m_filesToUpload.isEmpty()) {
            m_okButton.disable(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_OK_DISABLE_UPLOADING_0));
            m_uploadButton.getElement().getStyle().setDisplay(Display.NONE);
            showProgress();
            submit();
        }
    }

    /**
     * Creates the "OK", the "Cancel" and the "Upload" button.<p>
     */
    private void createButtons() {

        addDialogClose(new Command() {

            public void execute() {

                cancelUpload();
            }
        });

        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setTitle(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        cancelButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        cancelButton.setSize(I_CmsButton.Size.medium);
        cancelButton.setUseMinWidth(true);
        cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                cancelUpload();
            }
        });
        addButton(cancelButton);

        m_okButton = new CmsPushButton();
        m_okButton.setTitle(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        m_okButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        m_okButton.setSize(I_CmsButton.Size.medium);
        m_okButton.setUseMinWidth(true);
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onOkClick();
            }
        });
        addButton(m_okButton);

        // add a new upload button
        m_uploadButton = new CmsUploadButton(this);
        m_uploadButton.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().uploadDialogButton());
        m_uploadButton.setText(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_ADD_FILES_0));
        addButton(m_uploadButton);
    }

    /**
     * Creates the unzip checkbox.<p>
     * 
     * @param file the file to create the checkbox for
     * 
     * @return the unzip checkbox
     */
    private CmsCheckBox createUnzipCheckBox(final CmsFileInfo file) {

        final CmsCheckBox unzip = new CmsCheckBox();
        unzip.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        unzip.setChecked(getFilesToUnzip(true).contains(file.getFileName()));
        unzip.setTitle(Messages.get().key(Messages.GUI_UPLOAD_UNZIP_FILE_0));
        if (!m_filesToUpload.containsKey(file.getFileName())) {
            unzip.disable(Messages.get().key(Messages.GUI_UPLOAD_FILE_NOT_SELECTED_0));
        }
        unzip.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                // add or remove the file from the list of files to upload
                if (unzip.isChecked()) {
                    getFilesToUnzip(true).add(file.getFileName());
                } else {
                    getFilesToUnzip(true).remove(file.getFileName());
                }
            }
        });
        return unzip;
    }

    /**
     * Sets the user info.<p>
     *  
     * @param msg the message to display
     * @param warning signals whether the message should be a warning or nor
     */
    private void displayDialogInfo(String msg, boolean warning) {

        StringBuffer buffer = new StringBuffer(64);
        if (!warning) {
            buffer.append("<p class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        } else {
            buffer.append("<div class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadCss().warningIcon());
            buffer.append("\"></div>");
            buffer.append("<p class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadCss().warningMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        }
        m_dialogInfo.setHTML(buffer.toString());
    }

    /**
     * Removes all widgets from the content wrapper.<p>
     */
    private void removeContent() {

        int widgetCount = m_contentWrapper.getWidgetCount();
        for (int i = 0; i < widgetCount; i++) {
            m_contentWrapper.remove(0);
        }
    }

    /**
     * Sets the height for the content so that the dialog finally has the same height
     * as the dialog has on the selection screen.<p>
     */
    private void setHeight() {

        int infoDiff = m_firstInfoHeight - m_dialogInfo.getOffsetHeight();
        int summaryDiff = m_firstSummaryHeight - m_selectionSummary.getOffsetHeight();
        int height = m_firstContentHeight + infoDiff + summaryDiff;
        m_contentWrapper.getElement().getStyle().setHeight(height, Unit.PX);
        m_contentWrapper.getElement().getStyle().clearProperty("minHeight");
        m_contentWrapper.getElement().getStyle().clearProperty("maxHeight");
    }

    /**
     * Shows the overwrite dialog.<p>
     * 
     * @param infoBean the info bean containing the existing and invalid file names
     */
    private void showOverwriteDialog(CmsUploadFileBean infoBean) {

        // update the dialog
        m_selectionDone = true;
        m_okButton.enable();
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_OVERWRITE_0), true);
        m_uploadButton.getElement().getStyle().setDisplay(Display.NONE);

        // clear the list
        m_fileList.clearList();

        // handle existing files
        List<String> existings = new ArrayList<String>(infoBean.getExistingResourceNames());
        Collections.sort(existings, String.CASE_INSENSITIVE_ORDER);
        for (String filename : existings) {
            addFileToList(m_filesToUpload.get(filename), false, false);
        }

        // handle the invalid files
        List<String> invalids = new ArrayList<String>(infoBean.getInvalidFileNames());
        Collections.sort(invalids, String.CASE_INSENSITIVE_ORDER);
        for (String filename : invalids) {
            addFileToList(m_filesToUpload.get(filename), true, false);
            m_filesToUpload.remove(filename);
        }

        // set the height of the content
        setHeight();
    }

    /**
     * Starts the upload progress bar.<p>
     */
    private void showProgress() {

        removeContent();
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_UPLOADING_0), false);
        m_selectionSummary.removeFromParent();
        m_progressInfo = new CmsUploadProgressInfo();
        m_contentWrapper.add(m_progressInfo);
        m_updateProgressTimer.scheduleRepeating(UPDATE_PROGRESS_INTERVALL);
        startLoadingAnimation(Messages.get().key(Messages.GUI_UPLOAD_CLIENT_LOADING_0), 0);
        setHeight();
        changeHeight();
    }

    /**
     * Starts the loading animation.<p>
     * 
     * Used while client is loading files from hard disk into memory.<p>
     * 
     * @param msg the message that should be displayed below the loading animation (can also be HTML as String)
     */
    private void startLoadingAnimation(final String msg, int delayMillis) {

        m_loadingTimer = new Timer() {

            @Override
            public void run() {

                createLoadingAnimation(msg);
            }
        };
        if (delayMillis > 0) {
            m_loadingTimer.schedule(delayMillis);
        } else {
            m_loadingTimer.run();
        }
    }

    /**
     * Stops the client loading animation.<p>
     */
    private void stopLoadingAnimation() {

        if (m_loadingTimer != null) {
            m_loadingTimer.cancel();
        }
        if (m_clientLoading) {
            m_contentWrapper.remove(m_loadingPanel);
            m_clientLoading = false;
        }
    }

    /**
     * Displays the 'use drag and drop' / 'no drag and drop available' message.<p>
     */
    protected void setDragAndDropMessage() {

        if (m_dragAndDropMessage == null) {
            m_dragAndDropMessage = new HTML();
            m_dragAndDropMessage.setStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().dragAndDropMessage());
            m_dragAndDropMessage.setText(Messages.get().key(Messages.GUI_UPLOAD_DRAG_AND_DROP_DISABLED_0));
        }
        getContentWrapper().add(m_dragAndDropMessage);
        getContentWrapper().getElement().getStyle().setBackgroundColor(
            I_CmsConstantsBundle.INSTANCE.css().notificationErrorBg());
    }

    /**
     * Removes the drag and drop message.<p>
     */
    protected void removeDragAndDropMessage() {

        if (m_dragAndDropMessage != null) {
            m_dragAndDropMessage.removeFromParent();
            m_dragAndDropMessage = null;
            getContentWrapper().getElement().getStyle().clearBackgroundColor();
        }
    }
}
