/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorOpenInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.folder.ui.FolderDataModel.FolderCols;
import org.olat.core.commons.services.folder.ui.component.QuotaBar;
import org.olat.core.commons.services.folder.ui.event.FileBrowserSelectionEvent;
import org.olat.core.commons.services.folder.ui.event.FolderAddEvent;
import org.olat.core.commons.services.folder.ui.event.FolderDeleteEvent;
import org.olat.core.commons.services.folder.ui.event.FolderRootEvent;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseRenderer;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataContainer;
import org.olat.core.commons.services.vfs.VFSMetadataItem;
import org.olat.core.commons.services.vfs.VFSMetadataLeaf;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.vfs.model.VFSTransientMetadata;
import org.olat.core.commons.services.vfs.ui.version.RevisionListController;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.ui.WebDAVController;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FileElementInfos;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.DropFileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.UploadFileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel.BreadCrumb;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.BulkDeleteConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.mail.ui.SendDocumentsByEMailController;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.CopySourceLeaf;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.NamedLeaf;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSExternalLeaf;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSSuccess;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.course.CoursefolderWebDAVNamedContainer;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.search.SearchModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	private static final Logger log = Tracing.createLoggerFor(FolderController.class);
			
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_FROM_ME = "FromMe";
	private static final String TAB_ID_FOR_ME = "ForMe";
	private static final String FILTER_TYPE = "filter.type";
	private static final String FILTER_INITIALIZED_BY = "filter.initialized.by";
	private static final String FILTER_MODIFIED_DATE = "filter.modified.date";
	private static final String FILTER_TITLE = "filter.title";
	private static final String FILTER_STATUS = "filter.status";
	private static final String FILTER_LICENSE = "filter.license";
	private static final String CMD_FOLDER = "folder";
	private static final String CMD_FILE = "file";
	private static final String CMD_FILE_EDITOR = "efile";
	private static final String CMD_PATH = "path";
	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_COPY = "copy";
	private static final String CMD_MOVE = "move";
	private static final String CMD_RENAME = "rename";
	private static final String CMD_METADATA = "metadata";
	private static final String CMD_VERSION = "version";
	private static final String CMD_ZIP = "zip";
	private static final String CMD_UNZIP = "unzip";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_DELETE_PERMANENTLY = "delete.permanently";
	private static final String CMD_RESTORE = "restore";
	private static final String CMD_CRUMB_PREFIX = "/oobc_";
	
	private FormLink viewFolderLink;
	private FormLink viewFileLink;
	private FormLink trashLink;
	private FormLink viewSearchLink;
	private TextElement quickSearchEl;
	private FormLink quickSearchButton;
	private FileElement addFileEl;
	private DropdownItem createDropdown;
	private FormLink createFolderLink;
	private FormLink addBrowserLink;
	private FormLink createDocumentLink;
	private SpacerItem officeSpacer;
	private FormLink createWordLink;
	private FormLink createExcelLink;
	private FormLink createPowerPointLink;
	private SpacerItem recordSpacer;
	private FormLink recordVideoLink;
	private FormLink recordAudioLink;
	private DropdownItem cmdDropdown;
	private FormLink webdavLink;
	private FormLink quotaEditLink;
	private FormLink synchMetadataLink;
	private FormLink trashMenuLink;
	private FormLink bulkDownloadButton;
	private FormLink bulkMoveButton;
	private FormLink bulkCopyButton;
	private FormLink bulkZipButton;
	private FormLink bulkEmailButton;
	private FormLink bulkDeleteSoftlyButton;
	private FormLink bulkRestoreButton;
	private FormLink bulkDeletePermanentlyButton;
	private ComponentWrapperElement trashMessageEl;
	private TooledStackedPanel folderBreadcrumb;
	private QuotaBar quotaBar;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabFromMe;
	private FlexiFiltersTab tabForMe;
	private FolderDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CloseableModalController cmc;
	private FileBrowserController addFromBrowserCtrl;
	private CreateDocumentController createDocumentCtrl;
	private CreateFolderController createFolderCtrl;
	private RecordAVController recordAVController;
	private Controller docEditorCtrl;
	private WebDAVController webdavCtrl;
	private Controller quotaEditCtrl;
	private FileBrowserTargetController copySelectFolderCtrl;
	private LicenseCheckController licenseCheckCtrl;
	private OverwriteConfirmationController overwriteConfirmationCtrl;
	private RenameController renameCtrl;
	private Controller metadataCtrl;
	private RevisionListController revisonsCtrl;
	private ZipConfirmationController zipConfirmationCtrl;
	private ConfirmationController deleteSoftlyConfirmationCtrl;
	private ConfirmationController deletePermanentlyConfirmationCtrl;
	private FolderTargetController restoreSelectFolderCtrl;
	private SendDocumentsByEMailController emailCtrl;
	
	private final VFSContainer rootContainer;
	private VFSContainer currentContainer;
	private VFSContainer topMostDescendantsContainer;
	private VFSMetadata topMostDescendantsMetadata;
	private VFSItemFilter vfsFilter = new VFSSystemItemFilter();
	private final FolderControllerConfig config;
	private final boolean licensesEnabled;
	private final boolean webdavEnabled;
	private boolean searchEnabled;
	private boolean versionsEnabled;
	private boolean trashEnabled;
	private final Formatter formatter;
	private final Date newLabelDate = new Date();
	private FolderView folderView;
	private int counter = 0;
	private final List<String> trackedComponentNames = new ArrayList<>();

	@Autowired
	private FolderModule folderModule;
	@Autowired
	private VFSVersionModule vfsVersionModule;
	@Autowired
	private VFSRepositoryModule vfsRepositoryModule;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler licenseHandler;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SearchModule searchModule;
	@Autowired
	private AVModule avModule;
	@Autowired
	private MapperService mapperService;

	public FolderController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, FolderControllerConfig config) {
		super(ureq, wControl, "folder");
		this.rootContainer = rootContainer;
		this.config = config;
		this.licensesEnabled = licenseModule.isEnabled(licenseHandler);
		this.searchEnabled = config.isDisplaySearch() && searchModule.isSearchAllowed(ureq.getUserSession().getRoles());
		this.webdavEnabled = config.isDisplayWebDAVLink() && webDAVModule.isEnabled() && webDAVModule.isLinkEnabled()
				&& !ureq.getUserSession().getRoles().isGuestOnly();
		this.formatter = Formatter.getInstance(getLocale());
		
		setCurrentContainer(rootContainer);
		reloadVersionsEnabled();
		reloadTrashEnabled();
		
		if (config.isDisplaySubscription()) {
			VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(rootContainer);
			if (secCallback != null) {
				SubscriptionContext subsContext = secCallback.getSubscriptionContext();
				String data = rootContainer.getRelPath();
				if (subsContext != null && data != null) {
					String businessPath = wControl.getBusinessControl().getAsString();
					PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(FolderModule.class), data, businessPath);
					ContextualSubscriptionController subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
					listenTo(subscriptionCtrl);
					flc.put("subscription", subscriptionCtrl.getInitialComponent());
				}
			}
		}
		
		initForm(ureq);
		doOpenView(ureq, FolderView.folder);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int trashRetentionDays = vfsRepositoryModule.getTrashRetentionDays();
		if (trashRetentionDays >= 0) {
			String trashInfo = translate("trash.retention.info", String.valueOf(trashRetentionDays));
			trashMessageEl = new ComponentWrapperElement(
					TextFactory.createTextComponentFromString("trashMessage", trashInfo, "o_info_with_icon", false, null));
			formLayout.add(trashMessageEl);
		}
		
		folderBreadcrumb = new TooledStackedPanel("folderBreadcrumb", getTranslator(), this);
		formLayout.add(new ComponentWrapperElement(folderBreadcrumb));
		folderBreadcrumb.setInvisibleCrumb(0);
		folderBreadcrumb.setToolbarEnabled(false);
		folderBreadcrumb.getBackLink().setVisible(false);
		folderBreadcrumb.pushController(rootContainer.getName(), null, "/");
		
		viewFolderLink = uifactory.addFormLink("view.folder", formLayout, Link.BUTTON);
		viewFolderLink.setIconLeftCSS("o_icon o_icon-lg o_filetype_folder");
		viewFolderLink.setTitle("view.folder.title");
		
		viewFileLink = uifactory.addFormLink("view.file", formLayout, Link.BUTTON);
		viewFileLink.setIconLeftCSS("o_icon o_icon-lg o_filetype_file");
		viewFileLink.setTitle("view.file.title");
		
		trashLink = uifactory.addFormLink("trash", formLayout, Link.BUTTON);
		trashLink.setIconLeftCSS("o_icon o_icon-lg o_icon_trash");
		trashLink.setTitle("trash");
		
		viewSearchLink = uifactory.addFormLink("view.search", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		viewSearchLink.setIconLeftCSS("o_icon o_icon-lg o_icon_search");
		viewSearchLink.setElementCssClass("o_folder_view_search");
		viewSearchLink.setTitle(translate("view.search.title"));
		
		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setPlaceholderKey("enter.search.term", null);
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.setAriaLabel("enter.search.term");
		
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setDomReplacementWrapperRequired(false);
		quickSearchButton.setTitle(translate("search"));
		
		addFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "add", null, formLayout);
		addFileEl.addActionListener(FormEvent.ONCHANGE);// Needed for selenium tests
		addFileEl.setChooseButtonLabel(translate("upload.files"));
		addFileEl.setDragAndDropForm(true);
		addFileEl.setMultiFileUpload(true);
		
		createDropdown = uifactory.addDropdownMenu("create.dropdown", null, null, formLayout, getTranslator());
		createDropdown.setOrientation(DropdownOrientation.right);
		
		createFolderLink = uifactory.addFormLink("folder.create", formLayout, Link.LINK);
		createFolderLink.setIconLeftCSS("o_icon o_icon-fw o_icon_new_folder");
		createFolderLink.setElementCssClass("o_sel_folder_new_folder");
		createDropdown.addElement(createFolderLink);
		
		createDropdown.addElement(new SpacerItem("documentSpace"));
		
		addBrowserLink = uifactory.addFormLink("browser.add", formLayout, Link.LINK);
		addBrowserLink.setIconLeftCSS("o_icon o_icon-fw o_icon_filehub_add");
		addBrowserLink.setElementCssClass("o_sel_folder_add_browser");
		createDropdown.addElement(addBrowserLink);
		
		createDocumentLink = uifactory.addFormLink("document.create", formLayout, Link.LINK);
		createDocumentLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		createDocumentLink.setElementCssClass("o_sel_folder_new_document");
		createDropdown.addElement(createDocumentLink);
		
		officeSpacer = new SpacerItem("officeSpace");
		createDropdown.addElement(officeSpacer);
		
		createWordLink = uifactory.addFormLink("document.create.word", formLayout, Link.LINK);
		createWordLink.setIconLeftCSS("o_icon o_icon-fw o_FileResource-DOC_icon");
		createWordLink.setNewWindow(true, true, true);
		createDropdown.addElement(createWordLink);
		
		createExcelLink = uifactory.addFormLink("document.create.excel", formLayout, Link.LINK);
		createExcelLink.setIconLeftCSS("o_icon o_icon-fw o_FileResource-XLS_icon");
		createExcelLink.setNewWindow(true, true, true);
		createDropdown.addElement(createExcelLink);
		
		createPowerPointLink = uifactory.addFormLink("document.create.powerpoint", formLayout, Link.LINK);
		createPowerPointLink.setIconLeftCSS("o_icon o_icon-fw o_FileResource-PPT_icon");
		createPowerPointLink.setNewWindow(true, true, true);
		createDropdown.addElement(createPowerPointLink);
		
		recordSpacer = new SpacerItem("recordSpace");
		createDropdown.addElement(recordSpacer);
		
		recordVideoLink = uifactory.addFormLink("record.video", formLayout, Link.LINK);
		recordVideoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video_record");
		createDropdown.addElement(recordVideoLink);
		
		recordAudioLink = uifactory.addFormLink("record.audio", formLayout, Link.LINK);
		recordAudioLink.setIconLeftCSS("o_icon o_icon-fw o_icon_audio_record");
		createDropdown.addElement(recordAudioLink);
		
		cmdDropdown = uifactory.addDropdownMenuMore("cmds", flc, getTranslator());
		
		webdavLink = uifactory.addFormLink("webdav", formLayout, Link.LINK);
		webdavLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
		cmdDropdown.addElement(webdavLink);
		
		quotaEditLink = uifactory.addFormLink("quota.edit", formLayout, Link.LINK);
		quotaEditLink.setIconLeftCSS("o_icon o_icon-fw o_icon_quota");
		cmdDropdown.addElement(quotaEditLink);
		
		synchMetadataLink = uifactory.addFormLink("synch.metadata", formLayout, Link.LINK);
		synchMetadataLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reload");
		cmdDropdown.addElement(synchMetadataLink);
		
		trashMenuLink = uifactory.addFormLink("quota.edit.menu", "trash", null, formLayout, Link.LINK);
		trashMenuLink.setIconLeftCSS("o_icon o_icon-fw o_icon_trash");
		trashMenuLink.setElementCssClass("o_folder_trash_menu_item");
		cmdDropdown.addElement(trashMenuLink);
		
		quotaBar = new QuotaBar("quota", null, getLocale());
		formLayout.add(new ComponentWrapperElement(quotaBar));
	}
	
	private void updateCommandUI(UserRequest ureq) {
		boolean canDecendants = VFSStatus.YES == currentContainer.canDescendants();
		viewFileLink.setVisible(canDecendants);
		trashLink.setVisible(canViewTrash());
		boolean canSearch = canSearch();
		viewSearchLink.setVisible(canSearch);
		quickSearchEl.setVisible(canSearch);
		quickSearchButton.setVisible(canSearch);
		
		boolean canEditCurrentContainer = VFSStatus.YES == currentContainer.canWrite();
		addFileEl.setVisible(canEditCurrentContainer);
		createDropdown.setVisible(canEditCurrentContainer);
		addBrowserLink.setVisible(canEditCurrentContainer);
		createDocumentLink.setVisible(canEditCurrentContainer);
		createFolderLink.setVisible(canEditCurrentContainer);
		createWordLink.setVisible(canEditCurrentContainer && canCreateDocument(ureq, DocTemplates.SUFFIX_DOCX));
		createExcelLink.setVisible(canEditCurrentContainer && canCreateDocument(ureq, DocTemplates.SUFFIX_XLSX));
		createPowerPointLink.setVisible(canEditCurrentContainer && canCreateDocument(ureq, DocTemplates.SUFFIX_PPTX));
		officeSpacer.setVisible(createWordLink.isVisible() || createExcelLink.isVisible() || createPowerPointLink.isVisible());
		recordSpacer.setVisible(canEditCurrentContainer && avModule.isRecordingEnabled());
		recordVideoLink.setVisible(canEditCurrentContainer && avModule.isVideoRecordingEnabled());
		recordAudioLink.setVisible(canEditCurrentContainer && avModule.isAudioRecordingEnabled());
		
		webdavLink.setVisible(webdavEnabled);
		quotaEditLink.setVisible(config.isDisplayQuotaLink() && canEditQuota(ureq));
		synchMetadataLink.setVisible(VFSStatus.YES == currentContainer.canMeta());
		trashMenuLink.setVisible(canViewTrash());
		cmdDropdown.setVisible(webdavLink.isVisible() || synchMetadataLink.isVisible() || quotaEditLink.isVisible() || trashMenuLink.isVisible());
		
		boolean trashView = FolderView.trash == folderView;
		if (trashMessageEl != null) {
			trashMessageEl.setVisible(trashView);
		}
		bulkDownloadButton.setVisible(!trashView);
		bulkMoveButton.setVisible(FolderView.folder == folderView && canEditCurrentContainer);
		bulkCopyButton.setVisible(FolderView.folder == folderView && canEditCurrentContainer);
		bulkZipButton.setVisible(!trashView && canEditCurrentContainer);
		bulkEmailButton.setVisible(!trashView);
		
		boolean canDelete = VFSStatus.YES == currentContainer.canDelete();
		bulkDeleteSoftlyButton.setVisible(!trashView && canDelete);
		bulkRestoreButton.setVisible(trashView && canDelete);
		bulkDeletePermanentlyButton.setVisible(trashView && canDelete);
		
		flc.setDirty(true);
	}

	private boolean canSearch() {
		if (config.isFileHub()) {
			return VFSStatus.YES == currentContainer.canDescendants() || currentContainer instanceof CoursefolderWebDAVNamedContainer;
		}
		return searchEnabled;
	}

	private boolean canEditQuota(UserRequest ureq) {
		if (quotaManager.hasMinimalRolesToEditquota(ureq.getUserSession().getRoles())) {
			Quota quota = VFSManager.isTopLevelQuotaContainer(currentContainer);
			if(quota != null) {
				return quotaManager.hasQuotaEditRights(ureq.getIdentity(), ureq.getUserSession().getRoles(), quota);
			}
		}
		return false;
	}
	
	private boolean canCreateDocument(UserRequest ureq, String suffix) {
		return docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), suffix, DocEditor.Mode.EDIT,
				hasMetadata(currentContainer), false);
	}
	
	private void doOpenView(UserRequest ureq, FolderView view) {
		boolean viewChanged = folderView != view;
		this.folderView = view;
		
		if (FolderView.file == folderView) {
			VFSContainer topMostDescendantsContainer = getTopMostDescendantsContainer();
			if (topMostDescendantsContainer != null) {
				updateCurrentContainer(ureq, topMostDescendantsContainer, false);
			} else {
				updateCurrentContainer(ureq, rootContainer, false);
			}
		} else if (FolderView.search == folderView) {
			if (config.isFileHub()) {
				VFSContainer topMostDescendantsContainer = getTopMostDescendantsContainer();
				if (topMostDescendantsContainer != null) {
					updateCurrentContainer(ureq, topMostDescendantsContainer, false);
				}
			} else {
				updateCurrentContainer(ureq, rootContainer, false);
			}
		} else if (FolderView.trash == folderView) {
			VFSContainer topMostDescendantsContainer = getTopMostDescendantsContainer();
			if (topMostDescendantsContainer != null) {
				updateCurrentContainer(ureq, topMostDescendantsContainer, false);
			}
		}
		
		if (viewChanged) {
			doOpenFolderView(ureq);
			updateViewUI();
		} else {
			loadModel(ureq);
		}
		updateCommandUI(ureq);
		
		updateViewCrumb(view);
	}

	private void updateViewCrumb(FolderView view) {
		if(!folderBreadcrumb.getBreadCrumbs().isEmpty()) {
			Link lastCrumb = folderBreadcrumb.getBreadCrumbs().get(folderBreadcrumb.getBreadCrumbs().size() - 1);
			if (lastCrumb.getUserObject() instanceof BreadCrumb breadCrumb && breadCrumb.getUserObject() instanceof String path) {
				if (path.startsWith(CMD_CRUMB_PREFIX)) {
					folderBreadcrumb.popContent();
				}
			}
		}
		if (FolderView.file == view) {
			folderBreadcrumb.pushController(translate("view.file"), null, CMD_CRUMB_PREFIX + FolderView.file.name());
		} else if (FolderView.search == view) {
			folderBreadcrumb.pushController(translate("search"), null, CMD_CRUMB_PREFIX + FolderView.search.name());
		} else if (FolderView.trash == view) {
			folderBreadcrumb.pushController(translate("trash"), null, CMD_CRUMB_PREFIX + FolderView.trash.name());
		}
	}
	
	private void updateViewUI() {
		if (FolderView.folder == folderView) {
			viewFolderLink.setElementCssClass("active");
		} else {
			viewFolderLink.setElementCssClass(null);
		}
		if (FolderView.file == folderView) {
			viewFileLink.setElementCssClass("active");
		} else {
			viewFileLink.setElementCssClass(null);
		}
		if (FolderView.trash == folderView) {
			trashLink.setElementCssClass("active");
		} else {
			trashLink.setElementCssClass(null);
		}
		
		flc.contextPut("searchView", FolderView.search == folderView);
		if (FolderView.search == folderView) {
			quickSearchEl.setFocus(true);
		} else {
			quickSearchEl.setValue(null);
		}
	}

	private void doOpenFolderView(UserRequest ureq) {
		Instant start = Instant.now();
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel iconCol = new DefaultFlexiColumnModel(FolderCols.icon, new FolderIconRenderer());
		iconCol.setIconHeader("o_icon o_icon-fw o_icon-lg"); // no icon
		iconCol.setHeaderLabel(translate("table.thumbnail"));
		iconCol.setExportable(false);
		columnsModel.addFlexiColumnModel(iconCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.title));
		if (FolderView.trash == folderView) {
			addTrashCols(columnsModel);
		} else {
			addCols(columnsModel);
		}
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(FolderCols.tools));
		
		dataModel = new FolderDataModel(columnsModel, getLocale(), FileBrowserSelectionMode.sourceMulti);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), flc);
		tableEl.setSortSettings(new FlexiTableSortOptions(true, new SortKey(FolderCols.title.name(), true)));
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setCssDelegate(new FolderCssDelegate(dataModel));
		VelocityContainer rowVC = createVelocityContainer("folder_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "folder");
		
		updateEmptyMessage();
		initBulkLinks();
		
		loadModel(ureq);
		
		if (FolderView.file == folderView || FolderView.search == folderView) {
			initFilters();
			initFilterTabs(ureq);
		}
		
		log.debug("Folder: view updated in {} millis", Duration.between(start, Instant.now()).toMillis());
	}

	private void addCols(FlexiTableColumnModel columnsModel) {
		FlexiCellRenderer downloadCellRenderer = new StaticFlexiCellRenderer(null, CMD_DOWNLOAD, null, "o_icon o_icon_fw o_icon_download", translate("download"));
		DefaultFlexiColumnModel downloadCol = new DefaultFlexiColumnModel(FolderCols.download, downloadCellRenderer);
		downloadCol.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_download");
		downloadCol.setHeaderLabel(translate("download"));
		downloadCol.setExportable(false);
		columnsModel.addFlexiColumnModel(downloadCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.createdBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.lastModifiedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.lastModifiedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.size));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.status, new FolderStatusCellRenderer()));
		if (FolderView.folder != folderView) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.path));
		}
		if (versionsEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.versions));
		}
		if (licensesEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.license, new LicenseRenderer(getLocale())));
		}
	}
	
	private void addTrashCols(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.deletedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.deletedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.size));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.path));
	}
	
	private void doQuickSearch(UserRequest ureq) {
		if (FolderView.search != folderView) {
			doOpenView(ureq, FolderView.search);
			updateCommandUI(ureq);
		} else {
			loadModel(ureq);
		}
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues creatorValues = new SelectionValues();
		dataModel.getObjects().stream()
				.map(FolderRow::getCreatedBy)
				.filter(StringHelper::containsNonWhitespace)
				.distinct()
				.forEach(suffix -> creatorValues.add(SelectionValues.entry(suffix, suffix)));
		creatorValues.sort(SelectionValues.VALUE_ASC);
		if (creatorValues.size() > 0) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("created.by"), FILTER_INITIALIZED_BY, creatorValues, true));
		}
		
		SelectionValues suffixValues = new SelectionValues();
		dataModel.getObjects().stream()
				.map(FolderRow::getFileSuffix)
				.filter(StringHelper::containsNonWhitespace)
				.distinct()
				.forEach(suffix -> suffixValues.add(SelectionValues.entry(suffix, suffix)));
		suffixValues.sort(SelectionValues.VALUE_ASC);
		if (suffixValues.size() > 0) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("table.type"), FILTER_TYPE, suffixValues, true));
		}
		
		filters.add(new FlexiTableDateRangeFilter(translate("modified.date"), FILTER_MODIFIED_DATE, true, true,
				getLocale()));
		
		filters.add(new FlexiTableTextFilter(translate("table.title"), FILTER_TITLE, true));
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(FolderStatus.locked.name(), translate("status.locked")));
		statusValues.add(SelectionValues.entry(FolderStatus.editing.name(), translate("status.editing")));
		statusValues.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("table.status"), FILTER_STATUS, statusValues, false));
		
		if (licensesEnabled) {
			SelectionValues licenseValues = new SelectionValues();
			List<LicenseType> activeLicenseTypes = licenseService.loadActiveLicenseTypes(licenseHandler);
			activeLicenseTypes.forEach(licenseType -> {
				licenseValues.add(entry(
						String.valueOf(licenseType.getKey()),
						StringHelper.escapeHtml(LicenseUIFactory.translate(licenseType, getLocale()))));
			});
			licenseValues.sort(VALUE_ASC);
			filters.add(new FlexiTableMultiSelectionFilter(translate("table.license"), FILTER_LICENSE, licenseValues, false));
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		tabFromMe = FlexiFiltersTabFactory.tab(
				TAB_ID_FROM_ME,
				translate("tab.from.me"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabFromMe);
		
		tabForMe = FlexiFiltersTabFactory.tab(
				TAB_ID_FOR_ME,
				translate("tab.for.me"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabForMe);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		loadModel(ureq);
	}

	private void initBulkLinks() {
		bulkDownloadButton = uifactory.addFormLink("download", flc, Link.BUTTON);
		bulkDownloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		tableEl.addBatchButton(bulkDownloadButton);
		
		bulkZipButton = uifactory.addFormLink("zip", flc, Link.BUTTON);
		bulkZipButton.setIconLeftCSS("o_icon o_icon-fw o_filetype_zip");
		tableEl.addBatchButton(bulkZipButton);
		
		bulkCopyButton = uifactory.addFormLink("copy.to", flc, Link.BUTTON);
		bulkCopyButton.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
		tableEl.addBatchButton(bulkCopyButton);
		
		bulkMoveButton = uifactory.addFormLink("move.to", flc, Link.BUTTON);
		bulkMoveButton.setIconLeftCSS("o_icon o_icon-fw o_icon_move");
		tableEl.addBatchButton(bulkMoveButton);
		
		bulkEmailButton = uifactory.addFormLink("email.send", flc, Link.BUTTON);
		bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		tableEl.addBatchButton(bulkEmailButton);
		
		bulkDeleteSoftlyButton = uifactory.addFormLink("delete", flc, Link.BUTTON);
		bulkDeleteSoftlyButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		tableEl.addBatchButton(bulkDeleteSoftlyButton);
		
		bulkRestoreButton = uifactory.addFormLink("restore", flc, Link.BUTTON);
		bulkRestoreButton.setIconLeftCSS("o_icon o_icon-fw o_icon_restore");
		tableEl.addBatchButton(bulkRestoreButton);
		
		bulkDeletePermanentlyButton = uifactory.addFormLink("delete.permanently", flc, Link.BUTTON);
		bulkDeletePermanentlyButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		tableEl.addBatchButton(bulkDeletePermanentlyButton);
	}

	private void loadModel(UserRequest ureq) {
		Instant start = Instant.now();

		removeOldComponents();
		List<FolderRow> rows = FolderView.trash == folderView
				? loadTrashRows(ureq)
				: loadRows(ureq);
		
		applyFilters(rows);
		
		Instant fordgeStart = Instant.now();
		rows.forEach(row -> {
			forgeThumbnail(row);
			forgeToolsLink(row);
		});
		log.debug("Folder: thumbnails and tools forged in {} millis", Duration.between(fordgeStart, Instant.now()).toMillis());
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		updateQuotaBarUI(ureq);
		
		log.debug("Folder: model ({} rows) loaded in total {} millis", rows.size(), Duration.between(start, Instant.now()).toMillis());
	}

	private List<FolderRow> loadRows(UserRequest ureq) {
		List<VFSItem> items = loadItems();
		
		Instant start = Instant.now();
		
		List<FolderRow> rows = new ArrayList<>(items.size());
		for (VFSItem vfsItem : items) {
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			
			if (vfsMetadata == null || !vfsMetadata.isDeleted()) {
				FolderRow row = createRow(ureq, vfsItem, vfsMetadata);
				rows.add(row);
			}
		}
		
		log.debug("Folder: rows created in {} millis", Duration.between(start, Instant.now()).toMillis());
		return rows;
	}

	private List<FolderRow> loadTrashRows(UserRequest ureq) {
		VFSContainer descendantsContainer = getTopMostDescendantsContainer();
		if (descendantsContainer == null) {
			return List.of();
		}
		List<VFSMetadata> vfsMetadatas = vfsRepositoryService.getDescendants(descendantsContainer.getMetaInfo(), Boolean.TRUE);
		
		Map<Long, VFSRevision> metadataKeyToLatestRevision = vfsRepositoryService.getRevisions(new ArrayList<>(vfsMetadatas))
				.stream()
				.collect(Collectors.toMap(
						revision -> revision.getMetadata().getKey(),
						Function.identity(),
						(u, v) -> u.getRevisionNr() > v.getRevisionNr() ? u: v));
		
		List<FolderRow> rows = new ArrayList<>(vfsMetadatas.size());
		for (VFSMetadata vfsMetadata : vfsMetadatas) {
			// Revision is fallback because deletion informations were not saved on the
			// metadata in earlier times.
			VFSRevision vfsRevision = metadataKeyToLatestRevision.get(vfsMetadata.getKey());
			
			VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
			if (vfsItem instanceof VFSContainer container) {
				vfsItem = new VFSMetadataContainer(vfsRepositoryService, false, vfsMetadata,
						container.getParentContainer(), container.getLocalSecurityCallback(),
						container.getDefaultItemFilter());
			} else if (vfsItem instanceof VFSLeaf leaf) {
				vfsItem = new VFSMetadataLeaf(vfsRepositoryService, vfsMetadata, leaf.getParentContainer(),
						leaf.getLocalSecurityCallback());
			}
			FolderRow row = createRow(ureq, vfsItem, vfsMetadata);
			row.setDeleted(true);
			row.setDeletedDate(FolderUIFactory.getDeletedDate(vfsMetadata, vfsRevision));
			row.setDeletedBy(FolderUIFactory.getDeletedBy(userManager, vfsMetadata, vfsRevision));
			
			rows.add(row);
		}
		return rows;
	}
	
	private VFSContainer getTopMostDescendantsContainer() {
		VFSContainer topMostDescendantsContainer = null;
		
		if (VFSStatus.YES == currentContainer.canDescendants()) {
			topMostDescendantsContainer = currentContainer;
		}
		if (folderBreadcrumb == null) {
			return topMostDescendantsContainer;
		}
		
		List<Link> breadCrumbs = folderBreadcrumb.getBreadCrumbs();
		for (int i = breadCrumbs.size()-1 ; i >= 0; i--) {
			Link link = breadCrumbs.get(i);
			if (link.getUserObject() instanceof BreadCrumb breadCrumb && breadCrumb.getUserObject() instanceof String relativePath) {
				String path = relativePath;
				if (path.startsWith(CMD_CRUMB_PREFIX)) {
					continue;
				}
				
				if (!path.startsWith("/")) {
					path = "/" + path;
				}
				
				VFSItem vfsItem = rootContainer.resolve(path);
				if (vfsItem instanceof VFSContainer vfsContainer) {
					if (VFSStatus.YES == vfsContainer.canDescendants() || vfsContainer instanceof CoursefolderWebDAVNamedContainer) {
						topMostDescendantsContainer = vfsContainer;
					} else {
						return topMostDescendantsContainer;
					}
				} else {
					return topMostDescendantsContainer;
				}
			}
		}
		return topMostDescendantsContainer;
	}

	private FolderRow createRow(UserRequest ureq, VFSItem vfsItem, VFSMetadata vfsMetadata) {
		FolderRow row = new FolderRow(vfsItem);
		row.setMetadata(vfsMetadata);
		row.setKey(Long.valueOf(counter++));
		
		row.setIconCssClass(FolderUIFactory.getIconCssClass(vfsMetadata, vfsItem));
		row.setTitle(FolderUIFactory.getDisplayName(vfsMetadata, vfsItem));
		row.setCreatedBy(FolderUIFactory.getCreatedBy(userManager, vfsMetadata));
		row.setLastModifiedDate(FolderUIFactory.getLastModifiedDate(vfsMetadata, vfsItem));
		row.setLastModifiedBy(FolderUIFactory.getLastModifiedBy(userManager, vfsMetadata));
		row.setModified(FolderUIFactory.getModified(formatter, row.getLastModifiedDate(), row.getLastModifiedBy()));
		row.setFileSuffix(FolderUIFactory.getFileSuffix(vfsMetadata, vfsItem));
		row.setTranslatedType(FolderUIFactory.getTranslatedType(getTranslator(), vfsMetadata, vfsItem));
		row.setNewLabel(getNewLabel(row));
		row.setSize(FolderUIFactory.getSize(vfsMetadata, vfsItem, config.isFileHub()));
		row.setTranslatedSize(FolderUIFactory.getTranslatedSize(getTranslator(), vfsItem, row.getSize()));
		if (versionsEnabled) {
			row.setVersions(FolderUIFactory.getVersions(vfsMetadata));
		}
		if (licensesEnabled) {
			row.setLicense(vfsRepositoryService.getLicense(vfsMetadata));
			if (row.getLicense() != null) {
				row.setTranslatedLicense(LicenseUIFactory.translate(row.getLicense().getLicenseType(), getLocale()));
			}
		}
		forgeTitleLink(ureq, row);
		forgeFilePath(row);
		forgeStatus(row);
		return row;
	}

	private List<VFSItem> loadItems() {
		Instant start = Instant.now();
		
		if (FolderView.folder == folderView) {
			List<VFSItem> items = getCachedContainer(currentContainer).getItems(vfsFilter);
			log.debug("Folder: items (folder view) loaded in {} millis", Duration.between(start, Instant.now()).toMillis());
			return items;
		}
		
		List<VFSItem> allItems = new ArrayList<>();
		loadItemsAndChildren(allItems, currentContainer);
		
		log.debug("Folder: items loaded in {} millis", Duration.between(start, Instant.now()).toMillis());
		
		return allItems;
	}
	
	private void loadItemsAndChildren(List<VFSItem> allItems, VFSContainer vfsContainer) {
		boolean descendantsLoaded = false;
		List<VFSItem> items = null;
		VFSContainer cachedContainer = getCachedContainer(vfsContainer);
		if (VFSStatus.YES == cachedContainer.canDescendants()) {
			items = cachedContainer.getDescendants(vfsFilter);
			descendantsLoaded = true;
		}
		
		if (items == null) {
			items = vfsContainer.getItems(vfsFilter);
		}
		List<VFSItem> visibleItems = FolderView.file == folderView
				? items.stream().filter(item -> item instanceof VFSLeaf).toList()
				: items;
		allItems.addAll(visibleItems);
		
		if (!descendantsLoaded) {
			log.debug("Folder: items loaded recursively");
			items.forEach(item -> {
				if (item instanceof VFSContainer childContainer) {
					loadItemsAndChildren(allItems, childContainer);
				}
			});
		}
	}
	
	private VFSContainer getCachedContainer(VFSContainer vfsContainer) {
		if (VFSStatus.YES == vfsContainer.canMeta() && VFSStatus.YES == vfsContainer.canDescendants()) {
			return new VFSMetadataContainer(vfsRepositoryService, true, vfsContainer);
		}
		return vfsContainer;
	}
	
	private VFSItem getUncachedItem(VFSItem item) {
		if (item instanceof VFSMetadataItem cachedItem) {
			return cachedItem.getItem();
		}
		
		return item;
	}

	private void forgeStatus(FolderRow row) {
		if (row.getVfsItem() instanceof VFSContainer) {
			if (StringHelper.containsNonWhitespace(row.getTranslatedSize())) {
				String elementsLabel = "<div class=\"o_folder_label o_folder_label_elements\">" + row.getSize() + "</div>";
				row.setElementsLabel(elementsLabel);
			}
		} else {
			String translatedStatus = null;
			String labels = null;
			LockInfo lock = vfsLockManager.getLockInfo(row.getVfsItem(), row.getMetadata());
			if (lock != null && lock.getLockedBy() != null && lock.isCollaborationLock()) {
				row.setStatus(FolderStatus.editing);
				translatedStatus = translate("status.editing");
				labels = "<div class=\"o_folder_label o_folder_label_editing\"><i class=\"o_icon o_icon_user\"> </i> " + translatedStatus + "</div>";
			} else if (lock != null) {
				row.setStatus(FolderStatus.locked);
				translatedStatus = translate("status.locked");
				labels = "<div class=\"o_folder_label o_folder_label_locked\"><i class=\"o_icon o_icon_locked\"> </i> " + translatedStatus + "</div>";
			}
			row.setTranslatedStatus(translatedStatus);
			row.setLabels(labels);
		}
	}
	
	private void forgeThumbnail(FolderRow row) {
		if (row.getVfsItem() instanceof VFSLeaf vfsLeaf && row.getMetadata() != null && isThumbnailAvailable(row.getMetadata(), vfsLeaf)) {
			if(vfsLeaf instanceof VFSExternalLeaf externalLeaf) {
				row.setThumbnailUrl(externalLeaf.getMetaInfo().getThumbnailUrl());
				row.setLargeThumbnailUrl(externalLeaf.getMetaInfo().getLargeThumbnailUrl());
			} else {
				// One mapper per thumbnail per leaf version. The mapper is cached for 10 min or all users.
				FolderThumbnailMapper thumbnailMapper = new FolderThumbnailMapper(vfsRepositoryService, avModule, row.getMetadata(), row.getVfsItem());
				MapperKey mapperKey = mapperService.register(null, getThumbnailMapperId(row.getMetadata()), thumbnailMapper, 30);
				registerMapperKey(mapperKey);
				row.setThumbnailUrl(mapperKey.getUrl());
			}
			row.setThumbnailAvailable(true);
			if (FolderThumbnailMapper.isAudio(row.getMetadata(), vfsLeaf)) {
				row.setThumbnailCss("o_folder_card_img_center");
			}
		}
	}
	
	private boolean isThumbnailAvailable(VFSMetadata vfsMetadata, VFSLeaf vfsLeaf) {
		if (FolderThumbnailMapper.isAudio(vfsMetadata, vfsLeaf)) {
			return true;
		}
		return vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsMetadata);
	}
	
	private String getThumbnailMapperId(VFSMetadata vfsMetadata) {
		return vfsMetadata.getUuid() + Formatter.formatDatetimeFilesystemSave(vfsMetadata.getFileLastModified());
	}
	
	private void forgeTitleLink(UserRequest ureq, FolderRow row) {
		if (row.getVfsItem() instanceof VFSContainer) {
			if (row.getMetadata() != null && row.getMetadata().isDeleted()) {
				StaticTextElement selectionEl = uifactory.addStaticTextElement("selection_" + counter++, null, StringHelper.escapeHtml(row.getTitle()), null);
				selectionEl.setElementCssClass("o_nowrap");
				selectionEl.setStaticFormElement(false);
				row.setSelectionItem(selectionEl);
				
				StaticTextElement titleEl = uifactory.addStaticTextElement("title_" + counter++, null, StringHelper.escapeHtml(row.getTitle()), null);
				titleEl.setStaticFormElement(false);
				row.setTitleItem(titleEl);
				trackedComponentNames.add(selectionEl.getName());
				trackedComponentNames.add(titleEl.getName());
			} else {
				row.setOpenable(true);
				
				FormLink selectionLink = uifactory.addFormLink("select_" + counter++, CMD_FOLDER, "", null, flc, Link.LINK + Link.NONTRANSLATED);
				FormLink titleLink = uifactory.addFormLink("title_" + counter++, CMD_FOLDER, "", null, flc, Link.NONTRANSLATED);
				
				selectionLink.setElementCssClass("o_link_plain");
				
				selectionLink.setI18nKey(StringHelper.escapeHtml(row.getTitle()));
				titleLink.setI18nKey(getTitleWithLabel(row));
				
				selectionLink.setUserObject(row);
				titleLink.setUserObject(row);
				row.setSelectionItem(selectionLink);
				row.setTitleItem(titleLink);
				trackedComponentNames.add(selectionLink.getName());
				trackedComponentNames.add(titleLink.getName());
			}
		} else {
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
					ureq.getUserSession().getRoles(), row.getVfsItem(), row.getMetadata(), true,
					DocEditorService.modesEditView(canEdit(row.getVfsItem())));
			if (editorInfo.isEditorAvailable()) {
				row.setOpenable(true);
				
				FormLink selectionEl = uifactory.addFormLink("file_" +  counter++, CMD_FILE, "", null, flc, Link.LINK + Link.NONTRANSLATED);
				FormLink titleEl = uifactory.addFormLink("file_" +  counter++, CMD_FILE, "", null, flc, Link.LINK + Link.NONTRANSLATED);
				
				selectionEl.setElementCssClass("o_link_plain");
				
				selectionEl.setI18nKey(StringHelper.escapeHtml(row.getTitle()));
				titleEl.setI18nKey(getTitleWithLabel(row));
				
				selectionEl.setUserObject(row);
				titleEl.setUserObject(row);
				
				if (editorInfo.isNewWindow() && !folderModule.isForceDownload(row.getVfsItem())) {
					selectionEl.setNewWindow(true, true, false);
					titleEl.setNewWindow(true, true, false);
					row.setOpenInNewWindow(true);
				}
				
				row.setSelectionItem(selectionEl);
				row.setTitleItem(titleEl);
				trackedComponentNames.add(selectionEl.getName());
				trackedComponentNames.add(titleEl.getName());
			} else {
				FormLink selectionDownloadLink = uifactory.addFormLink("file_" + counter++, CMD_DOWNLOAD, "", null, flc, Link.LINK + Link.NONTRANSLATED);
				selectionDownloadLink.setI18nKey(StringHelper.escapeHtml(row.getTitle()));
				selectionDownloadLink.setUserObject(row);
				selectionDownloadLink.setElementCssClass("o_link_plain");

				FormLink titleDownloadLink = uifactory.addFormLink("file_" + counter++, CMD_DOWNLOAD, "", null, flc, Link.LINK + Link.NONTRANSLATED);
				titleDownloadLink.setI18nKey(getTitleWithLabel(row));
				titleDownloadLink.setUserObject(row);

				row.setSelectionItem(selectionDownloadLink);
				row.setTitleItem(titleDownloadLink);

				trackedComponentNames.add(selectionDownloadLink.getName());
				trackedComponentNames.add(titleDownloadLink.getName());
			}
		}
	}

	/**
	 * Removes old UI components to prevent memory overflows.
	 */
	private void removeOldComponents() {
		Instant start = Instant.now();
		if (trackedComponentNames.isEmpty()) return;

		Iterator<String> iterator = trackedComponentNames.iterator();
		while (iterator.hasNext()) {
			String itemName = iterator.next();
			flc.remove(itemName);
			iterator.remove();
		}
		trackedComponentNames.clear();

		log.debug("Folder: Old flc components removed in {} millis", Duration.between(start, Instant.now()).toMillis());
	}

	private String getTitleWithLabel(FolderRow row) {
		String title = StringHelper.escapeHtml(row.getTitle());
		if (row.getNewLabel() != null) {
			title = row.getNewLabel() + " " + title;
		}
		return title;
	}
	
	private String getNewLabel(FolderRow row) {
		if (FolderUIFactory.isNew(newLabelDate, row.getMetadata(), row.getVfsItem())) {
			return "<div class=\"o_folder_label o_folder_label_new\">" + translate("new.label") + "</div>";
		}
		return null;
	}
	
	private void forgeFilePath(FolderRow row) {
		String filePath = null;
		if ((!config.isFileHub() || FolderView.trash == folderView) && topMostDescendantsMetadata!= null && row.getMetadata() != null) {
			String rowRelativePath = row.getMetadata().getRelativePath();
			String rootRelativePath = topMostDescendantsMetadata.getRelativePath() + "/" + topMostDescendantsMetadata.getFilename();
			if (rowRelativePath.startsWith(rootRelativePath)) {
				filePath = rowRelativePath.substring(rootRelativePath.length());
			}
		}
		
		if (filePath == null) {
			VFSItem vfsItem = row.getVfsItem();
			VFSItem parent = vfsItem.getParentContainer();
			if (parent == null) {
				return;
			}
			filePath =  VFSManager.getRelativeItemPath(parent, rootContainer, null);
		}
		
		filePath = filePath.replace("/" + VFSRepositoryService.TRASH_NAME, "");
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		row.setFilePath(filePath);
		
		if (FolderView.trash == folderView) {
			StaticTextElement pathEl = uifactory.addStaticTextElement("path_" + counter++, null, StringHelper.escapeHtml(row.getFilePath()), null);
			pathEl.setDomWrapperElement(DomWrapperElement.span);
			pathEl.setStaticFormElement(false);
			row.setFilePathItem(pathEl);
			trackedComponentNames.add(pathEl.getName());
		} else if (FolderView.folder != folderView) {
			FormLink pathEl = uifactory.addFormLink("path_" + counter++, CMD_PATH, "", null, null, Link.NONTRANSLATED);
			pathEl.setI18nKey(StringHelper.escapeHtml(row.getFilePath()));
			pathEl.setUserObject(row);
			row.setFilePathItem(pathEl);
			trackedComponentNames.add(pathEl.getName());
		}
	}
	
	private void forgeToolsLink(FolderRow row) {
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		trackedComponentNames.add(toolsLink.getName());
	}
	
	private void applyFilters(List<FolderRow> rows) {
		if (tableEl.getSelectedFilterTab() != null) {
			if (tableEl.getSelectedFilterTab() == tabFromMe) {
				String myUserDisplayName = userManager.getUserDisplayName(getIdentity().getKey());
				rows.removeIf(row -> row.getVfsItem() instanceof VFSContainer || !myUserDisplayName.equals(row.getCreatedBy()));
			} else {
				if (tableEl.getSelectedFilterTab() == tabForMe) {
					String myUserDisplayName = userManager.getUserDisplayName(getIdentity().getKey());
					rows.removeIf(row -> row.getVfsItem() instanceof VFSContainer || myUserDisplayName.equals(row.getCreatedBy()));
				}
			}
		}
		
		if (FolderView.search == folderView) {
			String searchValue = quickSearchEl.getValue();
			if (StringHelper.containsNonWhitespace(searchValue)) {
				List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
				rows.removeIf(row -> 
						containsNot(searchValues, row.getCreatedBy()) &&
						containsNot(searchValues, row.getTitle()) &&
						containsNot(searchValues, row.getDescription()) &&
						containsNot(searchValues, row.getFilename())
						);
			}
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_TYPE == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					rows.removeIf(row -> row.getFileSuffix() == null || !values.contains(row.getFileSuffix()));
				}
			}
			if (FILTER_INITIALIZED_BY == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					rows.removeIf(row -> row.getCreatedBy() == null || !values.contains(row.getCreatedBy()));
				}
			}
			if (FILTER_MODIFIED_DATE  == filter.getFilter()) {
				DateRange dateRange = ((FlexiTableDateRangeFilter)filter).getDateRange();
				if (dateRange != null) {
					Date filterStart = dateRange.getStart();
					if (filterStart != null) {
						rows.removeIf(row -> row.getLastModifiedDate() == null || !filterStart.before(row.getLastModifiedDate()));
					}
					Date filterEnd = dateRange.getEnd();
					if (filterEnd != null) {
						rows.removeIf(row -> row.getLastModifiedDate() == null || !filterEnd.after(row.getLastModifiedDate()));
					}
				}
			}
			if (FILTER_TITLE == filter.getFilter()) {
				String value = ((FlexiTableTextFilter)filter).getValue();
				if (StringHelper.containsNonWhitespace(value)) {
					String valueLower = value.toLowerCase();
					rows.removeIf(row -> row.getTitle() == null || row.getTitle().toLowerCase().indexOf(valueLower) < 0);
				}
			}
			if (FILTER_STATUS == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					rows.removeIf(row -> row.getStatus() == null || !values.contains(row.getStatus().name()));
				}
			}
			if (FILTER_LICENSE == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<Long> licenseTypeKeys = values.stream().map(Long::valueOf).toList();
					rows.removeIf(row -> row.getLicense() == null || !licenseTypeKeys.contains(row.getLicense().getLicenseType().getKey()));
				}
			}
		}
	}

	private boolean containsNot(List<String> searchValues, String candidate) {
		if (StringHelper.containsNonWhitespace(candidate)) {
			String candidateLowerCase = candidate.toLowerCase();
			return searchValues.stream().noneMatch(searchValue -> candidateLowerCase.indexOf(searchValue) >= 0);
		}
		return true;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
		VFSItem vfsItem = rootContainer.resolve(path);
		if (vfsItem instanceof VFSContainer) {
			doOpenFolderView(ureq);
			updateCurrentContainer(ureq, path, true);
		} else if (vfsItem instanceof VFSLeaf vfsLeaf) {
			doOpenFolderView(ureq);
			updateCurrentContainer(ureq, vfsLeaf.getParentContainer(), true);
			doOpenFileInLightbox(ureq, vfsLeaf);
		}
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof FolderRow folderRow) {
			if (folderRow.getSelectionItem() != null) {
				cmps.add(folderRow.getSelectionItem().getComponent());
			}
			if (folderRow.getToolsLink() != null) {
				cmps.add(folderRow.getToolsLink().getComponent());
			}
		}
		return cmps;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == folderBreadcrumb) {
			if (event instanceof PopEvent popEvent) {
				Object userObject = popEvent.getUserObject();
				if (userObject instanceof String relativePath) {
					if (relativePath.startsWith(CMD_CRUMB_PREFIX)) {
						updateCurrentContainer(ureq, currentContainer, false);
						doOpenView(ureq, FolderView.folder);
					} else {
						String parentPath = relativePath.substring(0, relativePath.lastIndexOf("/"));
						updateCurrentContainer(ureq, parentPath, false);
						doOpenView(ureq, FolderView.folder);
						if ("".equals(parentPath)) {
							fireEvent(ureq, FolderRootEvent.EVENT);
						}
					}
				}
			}
		} else if ("ONCLICK".equals(event.getCommand())) {
			String rowKey = ureq.getParameter("open");
			if (StringHelper.isLong(rowKey)) {
				Long key = Long.valueOf(rowKey);
				doOpenItem(ureq, key);
				return;
			} else if (StringHelper.containsNonWhitespace(ureq.getParameter("download"))) {
				Long downloadKey = Long.valueOf(ureq.getParameter("download"));
				dataModel.getObjects().stream()
						.filter(row -> downloadKey.equals(row.getKey()))
						.findFirst()
						.ifPresent(folderRow -> doDownload(ureq, folderRow));
				return;
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				selectFilterTab(ureq, ((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doAddFromBrowser(ureq);
			} else if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				FolderRow row = dataModel.getObject(se.getIndex());
				if (CMD_DOWNLOAD.equals(cmd)) {
					doDownload(ureq, row);
				}
			}
		} else if(addFileEl == source) {
			if(event instanceof UploadFileElementEvent ufee) {
				leaveTrash(ureq);
				doUploadFile(ureq, ufee.getUploadFilesInfos(), ufee.getUploadFolder());
			} else if(event instanceof DropFileElementEvent dfee) {
				doDropFileByName(ureq, dfee.getDirectory(), dfee.getFilename());
			}
		} else if (viewFolderLink == source) {
			doOpenView(ureq, FolderView.folder);
		} else if (viewFileLink == source) {
			doOpenView(ureq, FolderView.file);
		} else if (trashLink == source) {
			doOpenView(ureq, FolderView.trash);
		} else if (viewSearchLink == source) {
			doOpenView(ureq, FolderView.search);
		} else if (quickSearchEl == source) {
			doQuickSearch(ureq);
		} else if (quickSearchButton == source) {
			doQuickSearch(ureq);
		} else if (addBrowserLink == source) {
			doAddFromBrowser(ureq);
		} else if (createDocumentLink == source) {
			doCreateDocument(ureq);
		} else if (createWordLink == source) {
			doCreateOfficeDocument(ureq, DocTemplates.SUFFIX_DOCX);
		} else if (createExcelLink == source) {
			doCreateOfficeDocument(ureq, DocTemplates.SUFFIX_XLSX);
		} else if (createPowerPointLink == source) {
			doCreateOfficeDocument(ureq, DocTemplates.SUFFIX_PPTX);
		} else if (createFolderLink == source) {
			doCreateFolder(ureq);
		} else if (source == recordVideoLink) {
			doRecordVideo(ureq);
		} else if (source == recordAudioLink) {
			doRecordAudio(ureq);
		} else if (source == webdavLink) {
			doShowWebdav(ureq);
		} else if (source == quotaEditLink) {
			doEditQuota(ureq);
		} else if (source == synchMetadataLink) {
			doSynchMetadata(ureq);
		} else if (bulkDownloadButton == source) {
			doBulkDownload(ureq);
		} else if (bulkMoveButton == source) {
			doBulkMoveSelectFolder(ureq);
		} else if (bulkCopyButton == source) {
			doBulkCopySelectFolder(ureq);
		} else if (bulkZipButton == source) {
			doBulkZipConfirmation(ureq);
		} else if (bulkEmailButton == source) {
			doBulkEmail(ureq);
		} else if (bulkDeleteSoftlyButton == source) {
			doBulkConfirmDeleteSoftly(ureq);
		} else if (bulkRestoreButton == source) {
			doBulkRestoreSelectFolder(ureq);
		} else if (bulkDeletePermanentlyButton == source) {
			doBulkConfirmDeletePermanently(ureq);
		} else if (source instanceof FormLink link) {
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenTools(ureq, folderRow, link);
			} else if (CMD_FOLDER.equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenFolder(ureq, folderRow);
			} else if (CMD_PATH.equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenPath(ureq, folderRow);
			} else if (CMD_FILE.equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenFile(ureq, folderRow);
			} else if (CMD_DOWNLOAD.equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doDownload(ureq, folderRow);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addFromBrowserCtrl == source) {
			cmc.deactivate();
			// No clean up. Uploaded, temporary files are deleted when controller is disposed.
			// The clean up if no license check is needed.
			if (event instanceof FileBrowserSelectionEvent selectionEvent) {
				CopyMoveParams params = new CopyMoveParams(false, false, selectionEvent.getVfsItems(), null);
				params.setTargetContainer(currentContainer);
				doCopyMoveValidate(ureq, params);
			} else {
				cleanUp();
			}
			loadModel(ureq);
		} else if (createDocumentCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, new FolderAddEvent(createDocumentCtrl.getCreatedLeaf().getName()));
				markNews();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (createFolderCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, new FolderAddEvent(createFolderCtrl.getCreatedContainer().getName()));
				markNews();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (recordAVController == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, new FolderAddEvent(recordAVController.getCreatedLeaf().getName()));
				markNews();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if(docEditorCtrl == source) {
			cleanUp();
		} else if (webdavCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (quotaEditCtrl == source) {
			if (event == Event.CHANGED_EVENT) {
				reloadQuota();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (renameCtrl == source) {
			if (event == Event.DONE_EVENT) {
				markNews();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (metadataCtrl == source) {
			if (metadataCtrl instanceof MetadataEditController) {
				if (event == Event.DONE_EVENT) {
					markNews();
				}
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (revisonsCtrl == source) {
			if (FolderCommandStatus.STATUS_SUCCESS == revisonsCtrl.getStatus()) {
				markNews();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (copySelectFolderCtrl == source) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT && copySelectFolderCtrl.getUserObject() instanceof CopyMoveParams copyMoveParams) {
				copyMoveParams.setTargetContainer(copySelectFolderCtrl.getSelectedContainer());
				doCopyMoveValidate(ureq, copyMoveParams);
			} else {
				cleanUp();
			}
		} else if (licenseCheckCtrl == source) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				doCopyMoveValidated(ureq, licenseCheckCtrl.getCopyMoveParams());
			}
			cleanUp();
		} else if (overwriteConfirmationCtrl == source) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// If dialog closed without pressing a button, we abort the process
				if (overwriteConfirmationCtrl.getOverwrite() != null) {
					CopyMoveParams copyMoveParams = overwriteConfirmationCtrl.getCopyMoveParams();
					copyMoveParams.setOverwrite(overwriteConfirmationCtrl.getOverwrite());
					doCopyMoveValidateLicense(ureq, copyMoveParams);
				}
			} else {
				cleanUp();
			}
		} else if (zipConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doZip(ureq, zipConfirmationCtrl.getFileName(), zipConfirmationCtrl.getItemsToZip());
			}
			cmc.deactivate();
			cleanUp();
		} else if (emailCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (deleteSoftlyConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (deleteSoftlyConfirmationCtrl.getUserObject() instanceof VFSItem vfsItem) {
					doDeleteSoftly(ureq, vfsItem);
				} else {
					doBulkDeleteSoftly(ureq);
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (deletePermanentlyConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (deletePermanentlyConfirmationCtrl.getUserObject() instanceof VFSItem vfsItem) {
					doDeletePermanently(ureq, vfsItem);
				} else {
					doBulkDeletePermanently(ureq);
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (restoreSelectFolderCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (restoreSelectFolderCtrl.getUserObject() instanceof CopyMoveParams copyMoveParams) {
					doRestore(ureq, restoreSelectFolderCtrl.getSelectedContainer(), copyMoveParams.getItemsToCopy());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(addFromBrowserCtrl);
		removeAsListenerAndDispose(createDocumentCtrl);
		removeAsListenerAndDispose(createFolderCtrl);
		removeAsListenerAndDispose(recordAVController);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(webdavCtrl);
		removeAsListenerAndDispose(quotaEditCtrl);
		removeAsListenerAndDispose(renameCtrl);
		removeAsListenerAndDispose(metadataCtrl);
		removeAsListenerAndDispose(revisonsCtrl);
		removeAsListenerAndDispose(copySelectFolderCtrl);
		removeAsListenerAndDispose(licenseCheckCtrl);
		removeAsListenerAndDispose(overwriteConfirmationCtrl);
		removeAsListenerAndDispose(zipConfirmationCtrl);
		removeAsListenerAndDispose(emailCtrl);
		removeAsListenerAndDispose(deleteSoftlyConfirmationCtrl);
		removeAsListenerAndDispose(deletePermanentlyConfirmationCtrl);
		removeAsListenerAndDispose(restoreSelectFolderCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		addFromBrowserCtrl = null;
		createDocumentCtrl = null;
		createFolderCtrl = null;
		recordAVController = null;
		docEditorCtrl = null;
		webdavCtrl = null;
		quotaEditCtrl = null;
		renameCtrl = null;
		metadataCtrl = null;
		revisonsCtrl = null;
		copySelectFolderCtrl = null;
		licenseCheckCtrl = null;
		overwriteConfirmationCtrl = null;
		zipConfirmationCtrl = null;
		emailCtrl = null;
		deleteSoftlyConfirmationCtrl = null;
		deletePermanentlyConfirmationCtrl = null;
		restoreSelectFolderCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
		addFileEl.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doQuickSearch(ureq);
	}
	
	public void updateCurrentContainer(UserRequest ureq, VFSContainer container, boolean reload) {
		String relativePath = VFSManager.getRelativeItemPath(container, rootContainer, "/");
		updateCurrentContainer(ureq, relativePath, reload);
	}
	
	public void updateCurrentContainer(UserRequest ureq, String relativePath, boolean reload) {
		String path = relativePath;
		if (path == null) {
			path = "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		
		VFSItem vfsItem = rootContainer.resolve(path);
		if (vfsItem instanceof VFSContainer vfsContainer) {
			setCurrentContainer(vfsContainer);
		} else {
			setCurrentContainer(rootContainer);
			path = "/";
		}
		
		topMostDescendantsContainer = getTopMostDescendantsContainer();
		topMostDescendantsMetadata = topMostDescendantsContainer != null? topMostDescendantsContainer.getMetaInfo(): null;
		
		reloadVersionsEnabled();
		reloadTrashEnabled();
		updateEmptyMessage();
		updateFolderBreadcrumpUI(ureq, path);
		updateCommandUI(ureq);
		bulkEmailButton.setVisible(config.getEmailFilter().canEmail(path));
		if (reload) {
			loadModel(ureq);
		}
		updatePathResource(ureq, path);
	}

	private void setCurrentContainer(VFSContainer currentContainer) {
		this.currentContainer = getCachedContainer(currentContainer);
	}
	
	public String getCurrentPath() {
		return VFSManager.getRelativeItemPath(currentContainer, rootContainer, null);
	}
	
	private void reloadVersionsEnabled() {
		versionsEnabled = vfsVersionModule.isEnabled() && currentContainer.canVersion() == VFSStatus.YES;
	}
	
	private void reloadTrashEnabled() {
		VFSContainer topMostDescendantsContainer = getTopMostDescendantsContainer();
		trashEnabled = topMostDescendantsContainer != null && VFSStatus.YES == topMostDescendantsContainer.canDelete();
	}
	
	private void updateEmptyMessage() {
		if (VFSStatus.YES == currentContainer.canWrite()) {
			tableEl.setEmptyTableSettings("folder.empty", "folder.empty.hint.readwrite", "o_filetype_folder", "browser.add", "o_icon_filehub_add", false);
		} else {
			tableEl.setEmptyTableSettings("folder.empty", "folder.empty.hint.readonly", "o_filetype_folder");
		}
	}
	
	private void updateFolderBreadcrumpUI(UserRequest ureq, String path) {
		String[] pathParts = path.split("/");
		String ralativePath = "";
		folderBreadcrumb.popUpToRootController(ureq);
		for (int i = 1; i < pathParts.length; i++) {
			String pathPart = pathParts[i];
			ralativePath += "/" + pathPart;
			VFSItem vfsItem = rootContainer.resolve(ralativePath);
			if (vfsItem != null) {
				pathPart = vfsItem.getName();
			}
			folderBreadcrumb.pushController(pathPart, null, ralativePath);
		}
	}
	
	private void updatePathResource(UserRequest ureq, String path) {
		String pathParam = "path=" + path;
		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck(pathParam);
		addToHistory(ureq, ores, null);
	}

	private void doOpenFolder(UserRequest ureq, FolderRow folderRow) {
		if (isItemNotAvailable(ureq, folderRow, true)) return;
		
		if (folderRow.getVfsItem() instanceof VFSContainer vfsContainer) {
			if (FolderView.folder != folderView) {
				doOpenView(ureq, FolderView.folder);
			}
			updateCurrentContainer(ureq, vfsContainer, true);
		}
	}

	private void doOpenPath(UserRequest ureq, FolderRow folderRow) {
		VFSContainer parent = folderRow.getVfsItem().getParentContainer();
		if (parent == null) {
			showError("error.deleted.container");
			return;
		}
		
		if (isItemNotAvailable(ureq, parent, true)) {
			return;
		}
		
		if (FolderView.folder != folderView) {
			doOpenView(ureq, FolderView.folder);
		}
		updateCurrentContainer(ureq, parent, true);
	}

	private void doAddFromBrowser(UserRequest ureq) {
		if (guardModalController(addFromBrowserCtrl)) return;
		leaveTrash(ureq);
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.upload");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.upload.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(addFromBrowserCtrl);
		addFromBrowserCtrl = new FileBrowserController(ureq, getWindowControl(), FileBrowserSelectionMode.sourceMulti,
				folderQuota, translate("add"));
		listenTo(addFromBrowserCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				addFromBrowserCtrl.getInitialComponent(), true, translate("browser.add"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUploadFile(UserRequest ureq, List<FileElementInfos> uploadFilesInfos, String directory) {
		if(uploadFilesInfos != null && !uploadFilesInfos.isEmpty()) {
			VFSContainer container = currentContainer;
			if(StringHelper.containsNonWhitespace(directory)) {
				VFSItem dir = container.resolve(directory);
				if(dir instanceof VFSContainer subContainer) {
					container = subContainer;
				}
			}
			
			List<VFSItem> items = uploadFilesInfos.stream()
					.map(infos -> (VFSItem)new NamedLeaf(infos.fileName(), new LocalFileImpl(infos.file())))
					.toList();
			CopyMoveParams params = new CopyMoveParams(false, false, items, this::showUploadSuccessMessage);
			params.setTargetContainer(container);
			doCopyMoveValidate(ureq, params);
		}
	}

	private void showUploadSuccessMessage(List<String> filenames) {
		if (filenames != null && !filenames.isEmpty()) {
			if (filenames.size() == 1) {
				showInfo("upload.success.single", filenames.get(0));
			} else {
				showInfo("upload.success.multi", String.valueOf(filenames.size()));
			}
		}
	}
	
	private void doDropFileByName(UserRequest ureq, String directory, String filename) {
		if(StringHelper.containsNonWhitespace(directory) && StringHelper.containsNonWhitespace(filename)) {
			VFSContainer container = currentContainer;
			VFSItem dir = container.resolve(directory);
			VFSItem item = container.resolve(filename);
			if(dir instanceof VFSContainer subContainer && item != null
					&& canEdit(subContainer) && canMove(item, item.getMetaInfo())) {
				CopyMoveParams params = new CopyMoveParams(true, true, List.of(item), this::showDropSuccessMessage);
				params.setTargetContainer(subContainer);
				doCopyMoveValidate(ureq, params);
			}
		}
	}
	
	private void showDropSuccessMessage(List<String> filenames) {
		if (filenames != null && !filenames.isEmpty()) {
			if (filenames.size() == 1) {
				showInfo("drop.success.single", filenames.get(0));
			} else {
				showInfo("drop.success.multi", String.valueOf(filenames.size()));
			}
		}
	}

	private void doCreateDocument(UserRequest ureq) {
		if (guardModalController(createDocumentCtrl)) return;
		leaveTrash(ureq);
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.create.document");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(createDocumentCtrl);
		
		String currentContainerPath = VFSManager.getRelativeItemPath(currentContainer, rootContainer, "/");
		CreateDocumentConfig createDocumentConfig = new CreateDocumentConfig(rootContainer, currentContainer,
				currentContainerPath, config.getCustomLinkTreeModel());
		DocTemplates docTemplates = DocTemplates
				.editables(getIdentity(), ureq.getUserSession().getRoles(), getLocale(), hasMetadata(currentContainer))
				.build();
		createDocumentCtrl = new CreateDocumentController(ureq, getWindowControl(), currentContainer, docTemplates,
				createDocumentConfig);
		listenTo(createDocumentCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				createDocumentCtrl.getInitialComponent(), true, translate("document.create"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateOfficeDocument(UserRequest ureq, String suffix) {
		leaveTrash(ureq);
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.create.document");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.quota.exceeded");
			return;
		}
		
		DocTemplate template = switch (suffix) {
		case DocTemplates.SUFFIX_DOCX -> DocTemplates.builder(getLocale()).addDocx().build().getTemplates().get(0);
		case DocTemplates.SUFFIX_XLSX -> DocTemplates.builder(getLocale()).addXlsx().build().getTemplates().get(0);
		case DocTemplates.SUFFIX_PPTX -> DocTemplates.builder(getLocale()).addPptx().build().getTemplates().get(0);
		default -> null;
		};
		if (template == null) {
			showWarning("error.cannot.create.document");
			return;
		}
		
		String filename = template.getName() 
				+ "_" + StringHelper.escapeHtml(getIdentity().getUser().getFirstName())
				+ "_" + StringHelper.escapeHtml(getIdentity().getUser().getLastName())
				+ "_1";
		filename = StringHelper.transformDisplayNameToFileSystemName(filename) + "." + template.getSuffix();
		filename = VFSManager.similarButNonExistingName(currentContainer, filename);
		VFSLeaf vfsLeaf = currentContainer.createChildLeaf(filename);
		VFSManager.copyContent(template.getContentProvider().getContent(getLocale()), vfsLeaf, getIdentity());
		loadModel(ureq);
		
		doOpenFile(ureq, vfsLeaf, DocEditorService.MODES_EDIT_VIEW);
	}
	
	private void doCreateFolder(UserRequest ureq) {
		if (guardModalController(createFolderCtrl)) return;
		leaveTrash(ureq);
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.create.folder");
			updateCommandUI(ureq);
		}
		
		removeAsListenerAndDispose(createFolderCtrl);
		
		createFolderCtrl = new CreateFolderController(ureq, getWindowControl(), currentContainer);
		listenTo(createFolderCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				createFolderCtrl.getInitialComponent(), true, translate("folder.create"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRecordVideo(UserRequest ureq) {
		if (guardModalController(recordAVController)) return;
		leaveTrash(ureq);
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.record.video");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(recordAVController);
		recordAVController = new RecordAVController(ureq, getWindowControl(), currentContainer, false);
		listenTo(recordAVController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				recordAVController.getInitialComponent(), true, translate("record.video"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRecordAudio(UserRequest ureq) {
		if (guardModalController(recordAVController)) return;
		leaveTrash(ureq);
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.record.audio");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(recordAVController);
		recordAVController = new RecordAVController(ureq, getWindowControl(), currentContainer, true);
		listenTo(recordAVController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				recordAVController.getInitialComponent(), true, translate("record.audio"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenItem(UserRequest ureq, Long rowKey) {
		Optional<FolderRow> firstRow = dataModel.getObjects().stream()
			.filter(row -> rowKey.equals(row.getKey()))
			.findFirst();
		if (firstRow.isPresent()) {
			FolderRow row = firstRow.get();
			if (row.isDirectory()) {
				doOpenFolder(ureq, row);
			} else {
				doOpenFile(ureq, row);
			}
		}
	}

	private void doOpenFile(UserRequest ureq, FolderRow row) {
		if (isItemNotAvailable(ureq, row, true)) return;
		
		if (row.getVfsItem() instanceof VFSLeaf vfsLeaf) {
			if(folderModule.isForceDownload(vfsLeaf)) {
				doDownload(ureq, row);
			} else {
				doOpenFileInEditor(ureq, row);
			}
		}
	}
	
	private void doOpenFileInEditor(UserRequest ureq, FolderRow row) {
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem uncachedItem = getUncachedItem(row.getVfsItem());
		if (uncachedItem instanceof VFSLeaf vfsLeaf) {
			VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
			List<Mode> modes = DocEditorService.modesEditView(canEdit(vfsLeaf));
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
					ureq.getUserSession().getRoles(), vfsLeaf, vfsMetadata, vfsMetadata != null, modes);
			if (editorInfo.isEditorAvailable()) {
				doOpenFile(ureq, vfsLeaf, modes);
			} else {
				showWarning("error.cannot.open.leaf");
			}
		}
	}

	private void doOpenFile(UserRequest ureq, VFSLeaf vfsLeaf, List<Mode> modes) {
		String currentContainerPath = VFSManager.getRelativeItemPath(currentContainer, rootContainer, "/");
		HTMLEditorConfig htmlEditorConfig = CreateDocumentConfig.getHtmlEditorConfig(rootContainer, currentContainer,
				currentContainerPath, config.getCustomLinkTreeModel(), vfsLeaf);
				
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withVersionControlled(true)
				.withFireSavedEvent(true)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
		DocEditorOpenInfo docEditorOpenInfo = docEditorService.openDocument(ureq, getWindowControl(), configs, modes);
		docEditorCtrl = listenTo(docEditorOpenInfo.getController());
	}
	
	private void doOpenFileInLightbox(UserRequest ureq, VFSLeaf vfsLeaf) {
		if (isItemNotAvailable(ureq, vfsLeaf, false)) return;
		
		VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
				ureq.getUserSession().getRoles(), vfsLeaf, vfsMetadata,
				vfsMetadata != null, DocEditorService.MODES_VIEW);
		if (editorInfo.isEditorAvailable() && !editorInfo.isNewWindow()) {
			doOpenFile(ureq, vfsLeaf, DocEditorService.MODES_VIEW);
		}
	}

	private void doShowWebdav(UserRequest ureq) {
		if (guardModalController(webdavCtrl)) return;
		
		removeAsListenerAndDispose(webdavCtrl);
		webdavCtrl = new WebDAVController(ureq, getWindowControl());
		listenTo(webdavCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), webdavCtrl.getInitialComponent(),
				true, translate("webdav"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditQuota(UserRequest ureq) {
		if (guardModalController(quotaEditCtrl)) return;
		if (!canEditQuota(ureq)) {
			showWarning("error.cannot.edit.quota");
			updateCommandUI(ureq);
			return;
		}
		
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer == null || inheritingContainer.getLocalSecurityCallback().getQuota() == null) {
			showWarning("error.cannot.edit.quota");
			updateCommandUI(ureq);
			return;
		}
		
		removeAsListenerAndDispose(quotaEditCtrl);
		
		quotaEditCtrl = quotaManager.getQuotaEditorInstance(ureq, getWindowControl(),
				inheritingContainer.getLocalSecurityCallback().getQuota().getPath(), false, true);
		if (quotaEditCtrl == null) {
			showWarning("error.cannot.edit.quota");
			updateCommandUI(ureq);
			return;
		}
		
		listenTo(quotaEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), quotaEditCtrl.getInitialComponent(),
				true, translate("quota.edit"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean canRename(VFSItem vfsItem, VFSMetadata vfsMetadata) {
		return canEditMedatata(vfsItem, vfsMetadata);
	}
	
	public void doRename(UserRequest ureq, FolderRow row) {
		if (guardModalController(metadataCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		if (!canRename(row.getVfsItem(), row.getMetadata())) {
			showWarning(row.isDirectory()? "error.cannot.rename.folder": "error.cannot.rename.document");
			updateCommandUI(ureq);
			return;
		}
		
		removeAsListenerAndDispose(metadataCtrl);
		
		VFSItem vfsItem = getUncachedItem(row.getVfsItem());
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		renameCtrl = new RenameController(ureq, getWindowControl(), vfsItem, vfsMetadata);
		listenTo(renameCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), renameCtrl.getInitialComponent(),
				true, translate("rename"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSynchMetadata(UserRequest ureq) {
		if (isItemNotAvailable(ureq, currentContainer, false)) return;
		
		vfsRepositoryService.synchMetadatas(currentContainer);
		
		showInfo("synch.metadata.done");
		loadModel(ureq);
	}

	private boolean hasMetadata(VFSItem item) {
		if (item instanceof NamedContainerImpl namedContainer) {
			item = namedContainer.getDelegate();
		}
		if (item instanceof VFSContainer) {
			String name = item.getName();
			if (name.equals("_sharedfolder_") || name.equals("_courseelementdata")) {
				return false;
			}
		}
		return item != null && item.canMeta() == VFSStatus.YES;
	}

	private boolean canEditMedatata(VFSItem vfsItem, VFSMetadata vfsMetadata) {
		return canEdit(vfsItem) 
				&& (vfsItem.getParentContainer() != null? canEdit(vfsItem.getParentContainer()): true)
				&& isNotDeleted(vfsMetadata)
				&& !vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null);
	}
	
	private void doOpenMetadata(UserRequest ureq, FolderRow row) {
		if (guardModalController(metadataCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		removeAsListenerAndDispose(metadataCtrl);
		
		VFSItem vfsItem = getUncachedItem(row.getVfsItem());
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		String resourceUrl = isNotDeleted(vfsMetadata)? getResourceURL(getWindowControl(), vfsItem): null;
		if (canEditMedatata(vfsItem, vfsMetadata)) {
			metadataCtrl = new MetadataEditController(ureq, getWindowControl(), vfsItem, resourceUrl);
		} else {
			metadataCtrl = new MetaInfoController(ureq, getWindowControl(), vfsItem, resourceUrl);
		}
		listenTo(metadataCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), metadataCtrl.getInitialComponent(),
				true, translate("metadata"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private String getResourceURL(WindowControl wControl, VFSItem vfsItem) {
		String path = "path=" + VFSManager.getRelativeItemPath(vfsItem, rootContainer, null);
		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck(path);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, wControl);
		return BusinessControlFactory.getInstance().getAsURIString(bwControl.getBusinessControl(), false);
	}
	
	private boolean canEdit(VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf) {
			VFSContainer parentContainer = vfsItem.getParentContainer();
			if (parentContainer != null) {
				return parentContainer.canWrite() == VFSStatus.YES;
			}
		}
		
		return vfsItem.canWrite() == VFSStatus.YES;
	}
	
	private VFSContainer getTopMostEditableContainer() {
		if (currentContainer != null && VFSStatus.YES == currentContainer.canWrite()) {
			VFSContainer topMostDescendantsContainer = getTopMostDescendantsContainer();
			if (topMostDescendantsContainer != null) {
				return topMostDescendantsContainer;
			}
		}
		return currentContainer;
	}
	
	private void doCopySelectFolder(UserRequest ureq, FolderRow row) {
		doCopyMoveSelectFolder(ureq, row, false, false, "copy.to", "copy");
	}
	
	private void doMoveSelectFolder(UserRequest ureq, FolderRow row) {
		doCopyMoveSelectFolder(ureq, row, true, false, "move.to", "move");
	}
	
	private void doCopyMoveSelectFolder(UserRequest ureq, FolderRow row, boolean move, boolean suppressVersion,
			String titleI18nKey, String submitI18nKey) {
		if (guardModalController(copySelectFolderCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!canCopy(vfsItem, row.getMetadata())) {
			return;
		}
		
		removeAsListenerAndDispose(copySelectFolderCtrl);
		copySelectFolderCtrl = new FileBrowserTargetController(ureq, getWindowControl(), rootContainer, currentContainer,
				translate(submitI18nKey), !move);
		listenTo(copySelectFolderCtrl);
		copySelectFolderCtrl.setUserObject(new CopyMoveParams(move, suppressVersion, List.of(vfsItem), getCopyMoveSuccessMessage(move)));
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				copySelectFolderCtrl.getInitialComponent(), true, translate(titleI18nKey), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private Consumer<List<String>> getCopyMoveSuccessMessage(boolean move) {
		return move? this::showMoveSuccessMessage: this::showCopySuccessMessage;
	}
	
	private void showCopySuccessMessage(List<String> filenames) {
		if (filenames != null && !filenames.isEmpty()) {
			if (filenames.size() == 1) {
				showInfo("copy.success.single", filenames.get(0));
			} else {
				showInfo("copy.success.multi", String.valueOf(filenames.size()));
			}
		}
	}
	
	private void showMoveSuccessMessage(List<String> filenames) {
		if (filenames != null && !filenames.isEmpty()) {
			if (filenames.size() == 1) {
				showInfo("move.success.single", filenames.get(0));
			} else {
				showInfo("move.success.multi", String.valueOf(filenames.size()));
			}
		}
	}

	// FileBrowserCopyToController.doCopy() should do the same
	private void doCopyMoveValidate(UserRequest ureq, CopyMoveParams params) {
		if (isItemNotAvailable(ureq, params.getTargetContainer(), true)) return;
		
		if (!canEdit(params.getTargetContainer())) {
			showWarning("error.copy.target.read.only");
			cleanUp();
			return;
		}
		
		List<VFSItem> itemsWithSameNameExists = new ArrayList<>(1);
		for (VFSItem itemToCopy : params.getItemsToCopy()) {
			if (itemToCopy instanceof VFSContainer sourceContainer) {
				VFSContainer uncachedTargetContainer = (VFSContainer)getUncachedItem(params.getTargetContainer());
				VFSContainer uncachedSourceContainer = (VFSContainer)getUncachedItem(sourceContainer);
				if (VFSManager.isContainerDescendantOrSelf(uncachedTargetContainer, uncachedSourceContainer)) {
					showWarning(params.isMove()? "error.move.overlapping": "error.copy.overlapping");
					cleanUp();
					loadModel(ureq);
					return;
				}
			}
			if (vfsLockManager.isLockedForMe(itemToCopy, ureq.getIdentity(), VFSLockApplicationType.vfs, null)) {
				showWarning("error.copy.locked");
				cleanUp();
				loadModel(ureq);
				return;
			}
			if (itemToCopy.canCopy() != VFSStatus.YES) {
				showWarning("error.copy.other");
				cleanUp();
				loadModel(ureq);
				return;
			}
			
			if (params.getOverwrite() == null && itemToCopy.getParentContainer() != null 
					&& itemToCopy.getParentContainer().getRelPath().equalsIgnoreCase(params.getTargetContainer().getRelPath())) {
				// We assume all items to copy are in the same source folder.
				if (params.isMove()) {
					showWarning("error.move.same.source.target");
					cleanUp();
					loadModel(ureq);
					return;
				}
				
				// If copy is inside a folder, create a new item.
				// Overwriting and new versions makes no sense in this case.
				params.setOverwrite(Boolean.FALSE);
			}
			
			if (params.getTargetContainer().resolve(itemToCopy.getName()) != null) {
				// If an item with the same name exist, let the user decide if a new file should be created.
				itemsWithSameNameExists.add(itemToCopy);
			}
		}
		
		if (params.getOverwrite() == null && !itemsWithSameNameExists.isEmpty() && !params.isSuppressVersion()) {
			doShowOverwriteConfirmation(ureq, params, itemsWithSameNameExists);
			return;
		}
		
		
		doCopyMoveValidateLicense(ureq, params);
	}

	private void doShowOverwriteConfirmation(UserRequest ureq, CopyMoveParams params, List<VFSItem> itemsWithSameNameExists) {
		if (guardModalController(overwriteConfirmationCtrl)) {
			return;
		}
		
		overwriteConfirmationCtrl = new OverwriteConfirmationController(ureq, getWindowControl(), params, itemsWithSameNameExists);
		listenTo(overwriteConfirmationCtrl);
		
		String title = VFSStatus.YES == params.getTargetContainer().canVersion()
				? translate("overwrite.overwrite.title")
				: translate("overwrite.replace.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				overwriteConfirmationCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCopyMoveValidateLicense(UserRequest ureq, CopyMoveParams params) {
		if (licensesEnabled && folderModule.isForceLicenseCheck() && !params.isMove()) {
			int numMissingLicenses = 0;
			for (VFSItem itemToCopy : params.getItemsToCopy()) {
				if (isLicenseMissing(itemToCopy)) {
					numMissingLicenses++;
				}
			}
			
			if (numMissingLicenses > 0) {
				if (guardModalController(licenseCheckCtrl)) {
					return;
				}
				licenseCheckCtrl = new LicenseCheckController(ureq, getWindowControl(), params,
						numMissingLicenses);
				listenTo(licenseCheckCtrl);
				
				cmc = new CloseableModalController(getWindowControl(), translate("close"),
						licenseCheckCtrl.getInitialComponent(), true, translate("license.check.title"), true);
				listenTo(cmc);
				cmc.activate();
				return;
			}
		}
		
		doCopyMoveValidated(ureq, params);
		cleanUp();
	}

	private void doCopyMoveValidated(UserRequest ureq, CopyMoveParams params) {
		boolean versioning = versionsEnabled && !params.isSuppressVersion() && (params.getOverwrite() == null || params.getOverwrite().booleanValue());
		
		FolderAddEvent addEvent = new FolderAddEvent();
		VFSSuccess vfsStatus = VFSSuccess.SUCCESS;
		ListIterator<VFSItem> listIterator = params.itemsToCopy.listIterator();
		while (listIterator.hasNext() && vfsStatus == VFSSuccess.SUCCESS) {
			VFSItem vfsItemToCopy = listIterator.next();
			// Paranoia: Check isItemNotAvailable and canEdit before every single file.
			if (!isItemNotAvailable(ureq, params.getTargetContainer(), false) && canCopy(vfsItemToCopy, null)) {
				boolean fileIgnored = false;
				if (canEdit(params.getTargetContainer())) {
					vfsStatus = isQuotaAvailable(params.getTargetContainer(), vfsItemToCopy, params.move);
					if (vfsStatus == VFSSuccess.SUCCESS) {
						VFSItem targetItem = params.getTargetContainer().resolve(vfsItemToCopy.getName());
						if (versioning && vfsItemToCopy instanceof VFSLeaf newLeaf && targetItem instanceof VFSLeaf currentLeaf && targetItem.canVersion() == VFSStatus.YES) {
							if (isSameLeaf(vfsItemToCopy, targetItem)) {
								fileIgnored = true;
							} else {
								boolean success = vfsRepositoryService.addVersion(currentLeaf, ureq.getIdentity(), false, "", newLeaf.getInputStream());
								if (!success) {
									vfsStatus = VFSSuccess.ERROR_FAILED;
								}
							}
						} else {
							if (targetItem != null) {
								if (params.getOverwrite() != null && params.getOverwrite().booleanValue()) {
									if (isSameLeaf(vfsItemToCopy, targetItem)) {
										fileIgnored = true;
									} else {
										targetItem.deleteSilently();
									}
								} else {
									vfsItemToCopy = makeNameUnique(params.getTargetContainer(), vfsItemToCopy);
								}
							}
							if (!fileIgnored) {
								vfsItemToCopy = appendMissingLicense(vfsItemToCopy, params.getLicense());
								VFSContainer uncachedTargetContainer = (VFSContainer)getUncachedItem(params.getTargetContainer());
								vfsStatus = uncachedTargetContainer.copyFrom(vfsItemToCopy, getIdentity());
							}
						}
					}
				} else {
					vfsStatus = VFSSuccess.ERROR_FAILED;
				}
				if (vfsStatus == VFSSuccess.SUCCESS) {
					addEvent.addFilename(vfsItemToCopy.getName());
					if (params.isMove() && !fileIgnored) {
						vfsItemToCopy.deleteSilently();
					}
				}
			} else {
				vfsStatus = VFSSuccess.ERROR_FAILED;
			}
		}
		
		if (vfsStatus == VFSSuccess.ERROR_QUOTA_EXCEEDED) {
			showWarning("error.copy.quota.exceeded");
		} else if (vfsStatus == VFSSuccess.ERROR_QUOTA_ULIMIT_EXCEEDED) {
			showWarning("error.copy.quota.ulimit.exceeded");
		} else if (vfsStatus != VFSSuccess.SUCCESS) {
			showWarning("error.copy");
		} else if (params.getSuccessMessage() != null) {
			params.getSuccessMessage().accept(addEvent.getFilenames());
		}
		
		loadModel(ureq);
		markNews();
		if (!addEvent.getFilenames().isEmpty()) {
			if (params.isMove()) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				fireEvent(ureq, addEvent);
			}
		}
	}

	private VFSSuccess isQuotaAvailable(VFSContainer targetContainer, VFSItem vfsItemToCopy, boolean move) {
		if (!move && vfsItemToCopy instanceof VFSLeaf vfsLeaf) {
			long sizeKB = vfsLeaf.getSize() / 1024;
			long quotaULimitKB = VFSManager.getQuotaULimitKB(targetContainer);
			if (quotaULimitKB != Quota.UNLIMITED && quotaULimitKB < sizeKB) {
				return VFSSuccess.ERROR_QUOTA_ULIMIT_EXCEEDED;
			}
			long quotaLeft = VFSManager.getQuotaLeftKB(targetContainer);
			if (quotaLeft != Quota.UNLIMITED && quotaLeft < sizeKB) {
				return VFSSuccess.ERROR_QUOTA_EXCEEDED;
			}
		}
		return VFSSuccess.SUCCESS;
	}
	
	private boolean isSameLeaf(VFSItem vfsItem1, VFSItem vfsItem2) {
		if (vfsItem1 instanceof VFSLeaf vfsLeaf1 && vfsItem2 instanceof VFSLeaf vfsLeaf2) {
			if (vfsLeaf1.getRelPath() != null && vfsLeaf2.getRelPath() != null && vfsLeaf1.getRelPath().equalsIgnoreCase(vfsLeaf2.getRelPath())) {
				return true;
			}
		}
		return false;
	}

	private VFSItem makeNameUnique(VFSContainer targetContainer, VFSItem vfsItem) {
		String nonExistingName = VFSManager.similarButNonExistingName(targetContainer, vfsItem.getName());
		if (vfsItem instanceof VFSContainer) {
			return new NamedContainerImpl(nonExistingName, (VFSContainer)vfsItem);
		}
		return new NamedLeaf(nonExistingName, (VFSLeaf)vfsItem);
	}
	
	private VFSItem appendMissingLicense(VFSItem vfsItem, License license) {
		if (license != null && isLicenseMissing(vfsItem)) {
			VFSLeaf itemWithLicense = (VFSLeaf)vfsItem;
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			if (vfsMetadata == null) {
				vfsMetadata = new VFSTransientMetadata();
				itemWithLicense = new CopySourceLeaf(itemWithLicense, vfsMetadata);
			}
			vfsMetadata.setLicenseType(license.getLicenseType());
			vfsMetadata.setLicenseTypeName(license.getLicenseType().getName());
			vfsMetadata.setLicensor(license.getLicensor());
			vfsMetadata.setLicenseText(LicenseUIFactory.getLicenseText(license));
			return itemWithLicense;
		}
		return vfsItem;
	}
	
	private boolean isLicenseMissing(VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf) {
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			if (vfsMetadata == null || !StringHelper.containsNonWhitespace(vfsMetadata.getLicenseTypeName())) {
				return true;
			}
		}
		return false;
	}
	
	private void doBulkCopySelectFolder(UserRequest ureq) {
		doBulkCopyMoveSelectFolder(ureq, false, "copy.to", "copy");
	}
	
	private void doBulkMoveSelectFolder(UserRequest ureq) {
		doBulkCopyMoveSelectFolder(ureq, true, "move.to", "move");
	}
	
	private void doBulkCopyMoveSelectFolder(UserRequest ureq, boolean move, String titleI18nKey, String submitI18nKey) {
		if (guardModalController(copySelectFolderCtrl)) return;
		if (!canEdit(currentContainer)) {
			return;
		}
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSItem> itemsToCopy = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.filter(row -> canCopy(row.getVfsItem(), null))
				.map(FolderRow::getVfsItem)
				.toList();
		
		if (itemsToCopy.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		copySelectFolderCtrl = new FileBrowserTargetController(ureq, getWindowControl(), rootContainer, currentContainer,
				translate(submitI18nKey), !move);
		listenTo(copySelectFolderCtrl);
		copySelectFolderCtrl.setUserObject(new CopyMoveParams(move, false, itemsToCopy, getCopyMoveSuccessMessage(move)));
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				copySelectFolderCtrl.getInitialComponent(), true, translate(titleI18nKey), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean canMove(VFSItem vfsItem, VFSMetadata vfsMetadata) {
		if (vfsItem == null || vfsItem instanceof VirtualContainer || vfsItem.getParentContainer() instanceof VirtualContainer) {
			return false;
		}
		VFSContainer parentContainer = vfsItem.getParentContainer();
		if (parentContainer != null && parentContainer.canWrite() != VFSStatus.YES) {
			return false;
		}
		return VFSStatus.YES == vfsItem.canCopy() && isNotDeleted(vfsMetadata);
	}
	
	private boolean canCopy(VFSItem vfsItem, VFSMetadata vfsMetadata) {
		return vfsItem != null && VFSStatus.YES == vfsItem.canCopy() && isNotDeleted(vfsMetadata);
	}

	private void doDownload(UserRequest ureq, FolderRow row) {
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			if (hasMetadata(vfsLeaf)) {
				vfsRepositoryService.increaseDownloadCount(vfsLeaf);
			}
			VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
			resource.setDownloadable(true);
			ureq.getDispatchResult().setResultingMediaResource(resource);
		} else if (vfsItem instanceof VFSContainer vfsContainer) {
			FolderZipMediaResource resource = new FolderZipMediaResource(List.of(vfsContainer));
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doBulkDownload(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<FolderRow> selectedRows = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.toList();
		
		if (selectedRows.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		} else if (selectedRows.size() == 1) {
			doDownload(ureq, selectedRows.get(0));
		} else {
			List<VFSItem> items = selectedRows.stream()
					.map(FolderRow::getVfsItem)
					.toList();
			FolderZipMediaResource resource = new FolderZipMediaResource(items);
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doOpenVersions(UserRequest ureq, FolderRow row) {
		if (guardModalController(revisonsCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!hasVersion(row.getMetadata(), vfsItem) || !canEdit(vfsItem)) {
			return;
		}
		
		removeAsListenerAndDispose(metadataCtrl);
		
		boolean locked = vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null);
		revisonsCtrl = new RevisionListController(ureq, getWindowControl(), vfsItem, locked);
		listenTo(revisonsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), revisonsCtrl.getInitialComponent(),
				true, translate("versions"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean hasVersion(VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			if (vfsVersionModule.isEnabled() && vfsLeaf.canVersion() == VFSStatus.YES && vfsMetadata != null) {
				return vfsMetadata.getRevisionNr() > 1;
			}
		}
		return false;
	}
	
	private void doZipConfirmation(UserRequest ureq, FolderRow row) {
		if (guardModalController(zipConfirmationCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!canZip(vfsItem, row.getMetadata())) {
			return;
		}
		
		zipConfirmationCtrl = new ZipConfirmationController(ureq, getWindowControl(), currentContainer, List.of(row.getVfsItem()));
		listenTo(zipConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				zipConfirmationCtrl.getInitialComponent(), true, translate("zip"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doZip(UserRequest ureq, String fileName, List<VFSItem> itemsToZip) {
		List<VFSItem> vfsItems = itemsToZip.stream()
				.filter(vfsItem -> !isItemNotAvailable(ureq, vfsItem, false))
				.toList();
		
		if (vfsItems.isEmpty()) {
			showError("error.zip");
			loadModel(ureq);
			return;
		}
		
		VFSLeaf zipFile = currentContainer.createChildLeaf(fileName);
		if (zipFile == null) {
			showError("error.zip");
			return;
		}
		
		if (!ZipUtil.zip(vfsItems, zipFile, new VFSSystemItemFilter(), false)) {
			zipFile.deleteSilently();
			showError("error.zip");
		} else {
			vfsRepositoryService.itemSaved(zipFile, ureq.getIdentity());
			markNews();
		}
		
		long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
		if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
			zipFile.deleteSilently();
			showError("error.zip.quota.exceeded");
			loadModel(ureq);
			return;
		}
		
		loadModel(ureq);
		fireEvent(ureq, new FolderAddEvent(zipFile.getName()));
	}
	
	private void doBulkZipConfirmation(UserRequest ureq) {
		if (guardModalController(zipConfirmationCtrl)) return;
		if (!canEdit(currentContainer)) {
			return;
		}
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSItem> itemsToZip = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.map(FolderRow::getVfsItem)
				.toList();
		
		if (itemsToZip.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		zipConfirmationCtrl = new ZipConfirmationController(ureq, getWindowControl(), currentContainer, itemsToZip);
		listenTo(zipConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				zipConfirmationCtrl.getInitialComponent(), true, translate("zip"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private boolean canZip(VFSItem vfsItem, VFSMetadata vfsMetadata) {
		return vfsItem instanceof VFSContainer && canEdit(currentContainer) && isNotDeleted(vfsMetadata);
	}
	
	private void doUnzip(UserRequest ureq, FolderRow row) {
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!canUnzip(vfsItem, row.getMetadata())) {
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		String zipFilenameBase = vfsItem.getName().substring(0, vfsItem.getName().length() - 4);
		String unzipContainerName = VFSManager.similarButNonExistingName(currentContainer, zipFilenameBase);
		if (unzipContainerName == null) {
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		VFSContainer unzipContainer = currentContainer.createChildContainer(unzipContainerName);
		if (unzipContainer == null) {
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		if (!ZipUtil.unzipNonStrict((VFSLeaf)vfsItem, unzipContainer, ureq.getIdentity(), versionsEnabled)) {
			unzipContainer.deleteSilently();
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
		if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
			unzipContainer.deleteSilently();
			showError("error.unzip.quota.exceeded");
			loadModel(ureq);
			return;
		}
		
		if (unzipContainer.canMeta() == VFSStatus.YES) {
			VFSMetadata metaInfo = unzipContainer.getMetaInfo();
			if (metaInfo instanceof VFSMetadataImpl metadata) {
				metadata.setFileInitializedBy(getIdentity());
				vfsRepositoryService.updateMetadata(metaInfo);
			}
		}
		
		markNews();
		loadModel(ureq);
		FolderAddEvent addEvent = new FolderAddEvent(unzipContainer.getName());
		unzipContainer.getDescendants(null).forEach(unzippedItem -> addEvent.addFilename(unzippedItem.getName()));
		fireEvent(ureq, addEvent);
	}

	private boolean canUnzip(VFSItem vfsItem, VFSMetadata vfsMetadata) {
		if (config.isDisplayUnzip() && canEdit(currentContainer) && isNotDeleted(vfsMetadata) && vfsItem instanceof VFSLeaf vfsLeaf) {
			return vfsLeaf.getName().toLowerCase().endsWith(".zip");
		}
		return false;
	}
	
	private void doBulkEmail(UserRequest ureq) {
		if (guardModalController(emailCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSLeaf> selectedLeafs = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.filter(row -> config.getEmailFilter().canEmail(row.getFilePath()))
				.map(row -> { 
					if (row.getVfsItem() instanceof VFSLeaf vfsLeaf) {
						return vfsLeaf;
					}
					return null;
					})
				.filter(Objects::nonNull)
				.toList();
		
		if (selectedLeafs.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		emailCtrl = new SendDocumentsByEMailController(ureq, getWindowControl());
		listenTo(emailCtrl);
		emailCtrl.setFiles(rootContainer, selectedLeafs);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), emailCtrl.getInitialComponent(),
				true, translate("email.send"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeleteSoftly(UserRequest ureq, FolderRow row) {
		if (guardModalController(deleteSoftlyConfirmationCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		removeAsListenerAndDispose(deleteSoftlyConfirmationCtrl);
		
		VFSItem vfsItem = row.getVfsItem();
		if (canNotDeleteLeaf(vfsItem)) {
			showWarning("error.delete.locked.leaf");
			loadModel(ureq);
			return;
		}
		
		if (vfsItem instanceof LocalFolderImpl localFolder) {
			vfsItem = getCachedContainer(localFolder);
		}
		if (canNotDeleteContainer(vfsItem)) {
			showWarning("error.delete.locked.children");
			return;
		}
		
		String messageKey = row.isDirectory()
				? "delete.softly.confirmation.message.container"
				: "delete.softly.confirmation.message.leaf";
		deleteSoftlyConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate(messageKey, StringHelper.escapeHtml(vfsItem.getName())), null, translate("delete"),
				ButtonType.danger);
		deleteSoftlyConfirmationCtrl.setUserObject(vfsItem);
		listenTo(deleteSoftlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteSoftlyConfirmationCtrl.getInitialComponent(),
				true, translate("move.to.trash"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doDeleteSoftly(UserRequest ureq, VFSItem vfsItem) {
		if (isItemNotAvailable(ureq, vfsItem, true)) {
			return;
		}
		
		if (canNotDeleteLeaf(vfsItem)) {
			showWarning("error.delete.locked.leaf");
			loadModel(ureq);
			return;
		}
		
		if (vfsItem instanceof LocalFolderImpl localFolder) {
			vfsItem = getCachedContainer(localFolder);
		}
		if (canNotDeleteContainer(vfsItem)) {
			showWarning("error.delete.locked.children");
			return;
		}
		
		FolderDeleteEvent deleteEvent = new FolderDeleteEvent();
		deleteEvent.addFilename(vfsItem.getName());
		
		// Move to trash
		Instant start = Instant.now();
		vfsItem.delete();
		log.debug("Folder: Deleted softly in {} millis ({})", Duration.between(start, Instant.now()).toMillis(), vfsItem.getRelPath());
		
		markNews();
		loadModel(ureq);
		fireEvent(ureq, deleteEvent);
	}
	
	private void doBulkConfirmDeleteSoftly(UserRequest ureq) {
		if (guardModalController(deleteSoftlyConfirmationCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSItem> selecteditems = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(FolderRow::getVfsItem)
				.filter(Objects::nonNull)
				.filter(item -> !isItemNotAvailable(ureq, item, false))
				.filter(item -> !canNotDeleteLeaf(item) && !canNotDeleteContainer(item))
				.toList();
		
		if (selecteditems.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		deleteSoftlyConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("delete.softly.confirmation.message"), null, translate("delete"), ButtonType.danger);
		deleteSoftlyConfirmationCtrl.setUserObject(selecteditems);
		listenTo(deleteSoftlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteSoftlyConfirmationCtrl.getInitialComponent(),
				true, translate("move.to.trash"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkDeleteSoftly(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		List<VFSItem> itemsToDelete = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(FolderRow::getVfsItem)
				.filter(Objects::nonNull)
				.filter(item -> !isItemNotAvailable(ureq, item, false))
				.filter(item -> !canNotDeleteLeaf(item) && !canNotDeleteContainer(item))
				.toList();
		
		if (itemsToDelete.isEmpty()) {
			loadModel(ureq);
			return;
		}
		
		Instant start = Instant.now();
		FolderDeleteEvent deleteEvent = new FolderDeleteEvent();
		itemsToDelete.forEach(itemToDelete -> {
			deleteEvent.addFilename(itemToDelete.getName());
			itemToDelete.delete();
		});
		log.debug("Folder: Bulk deleted softly in {} millis", Duration.between(start, Instant.now()).toMillis());
		
		markNews();
		loadModel(ureq);
		fireEvent(ureq, deleteEvent);
	}
	
	private void doConfirmDeletePermanently(UserRequest ureq, FolderRow row) {
		if (guardModalController(deletePermanentlyConfirmationCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		removeAsListenerAndDispose(deletePermanentlyConfirmationCtrl);
		
		VFSItem vfsItem = row.getVfsItem();
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		if (vfsMetadata == null || !vfsMetadata.isDeleted()) {
			if (vfsItem instanceof VFSLeaf) {
				showWarning("error.delete.permanently.leaf");
			} else {
				showWarning("error.delete.permanently.container");
			}
			loadModel(ureq);
			return;
		}
		
		String message = vfsItem instanceof VFSLeaf
				? translate("delete.permanently.confirmation.message.leaf", StringHelper.escapeHtml(vfsItem.getName()))
				: translate("delete.permanently.confirmation.message.container", StringHelper.escapeHtml(vfsItem.getName()));
		deletePermanentlyConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), message, null,
				translate("delete"), ButtonType.danger);
		deletePermanentlyConfirmationCtrl.setUserObject(vfsItem);
		listenTo(deletePermanentlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deletePermanentlyConfirmationCtrl.getInitialComponent(),
				true, translate("delete.permanently"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doDeletePermanently(UserRequest ureq, VFSItem vfsItem) {
		if (isItemNotAvailable(ureq, vfsItem, true)) {
			return;
		}
		
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		if (isNotDeleted(vfsMetadata)) {
			if (vfsItem instanceof VFSLeaf) {
				showWarning("error.delete.permanently.leaf");
			} else {
				showWarning("error.delete.permanently.container");
			}
			loadModel(ureq);
			return;
		}
		
		Instant start = Instant.now();
		vfsItem.deleteSilently();
		log.debug("Folder: Deleted permanently in {} millis ({})", Duration.between(start, Instant.now()).toMillis(), vfsItem.getRelPath());
		
		loadModel(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doBulkConfirmDeletePermanently(UserRequest ureq) {
		if (guardModalController(deletePermanentlyConfirmationCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSItem> selecteditems = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(FolderRow::getVfsItem)
				.filter(Objects::nonNull)
				.filter(vfsIttem -> !isItemNotAvailable(ureq, vfsIttem, false))
				.filter(vfsItem -> canDeletePermamently(vfsItem) && !isNotDeleted(vfsItem.getMetaInfo()))
				.toList();
		
		if (selecteditems.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		List<String> filenames = selecteditems.stream()
				.map(VFSItem::getName)
				.sorted()
				.toList();
		deletePermanentlyConfirmationCtrl = new BulkDeleteConfirmationController(ureq, getWindowControl(),
				translate("delete.permanently.confirmation.message", String.valueOf(selecteditems.size())),
				translate("delete.permanently.confirmation", new String[] { String.valueOf(selecteditems.size()) }),
				translate("delete"),
				translate("delete.permanently.confirmation.label"), filenames, null);
		deletePermanentlyConfirmationCtrl.setUserObject(selecteditems);
		listenTo(deletePermanentlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deletePermanentlyConfirmationCtrl.getInitialComponent(),
				true, translate("delete.permanently"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkDeletePermanently(UserRequest ureq) {
		if (VFSStatus.YES != currentContainer.canDelete()) {
			return;
		}
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		List<VFSItem> selecteditems = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(row -> getUncachedItem(row.getVfsItem()))
				.filter(Objects::nonNull)
				.filter(vfsItem -> !isItemNotAvailable(ureq, vfsItem, false))
				.filter(vfsItem -> canDeletePermamently(vfsItem) && !isNotDeleted(vfsItem.getMetaInfo()))
				.sorted((i1, i2) -> Boolean.valueOf(i1 instanceof VFSContainer).compareTo(Boolean.valueOf(i2 instanceof VFSContainer))) // Files first
				.toList();
		
		if (selecteditems.isEmpty()) {
			loadModel(ureq);
			return;
		}
		
		Instant start = Instant.now();
		selecteditems.forEach(VFSItem::deleteSilently);
		log.debug("Folder: Bulk deleted permanently in {} millis", Duration.between(start, Instant.now()).toMillis());
		
		loadModel(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private boolean canDelete(VFSItem vfsItem) {
		if (vfsItem == null) {
			return false;
		}
		VFSContainer parentContainer = vfsItem.getParentContainer();
		if (parentContainer != null && parentContainer.canDelete() != VFSStatus.YES) {
			return false;
		}
		return vfsItem.canDelete() == VFSStatus.YES
				&& !vfsLockManager.isLockedForMe(vfsItem, vfsItem.getMetaInfo(), getIdentity(), VFSLockApplicationType.vfs, null);
	}
	
	private boolean canNotDeleteLeaf(VFSItem vfsItem) {
		return vfsItem instanceof VFSLeaf && !canDelete(vfsItem);
	}

	private boolean canNotDeleteContainer(VFSItem vfsItem) {
		return vfsItem instanceof VFSContainer vfsContainer  && (!canDelete(vfsItem) || hasLockedChild(vfsContainer));
	}
	
	private boolean isNotDeleted(VFSMetadata vfsMetadata) {
		return vfsMetadata == null || !vfsMetadata.isDeleted();
	}
	
	private boolean canDeletePermamently(VFSItem vfsItem) {
		return canDeletePermamently(vfsItem, vfsItem.getMetaInfo());
	}
	
	private boolean canDeletePermamently(VFSItem vfsItem, VFSMetadata vfsMetadata) {
		VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(vfsItem);
		
		if (secCallback == null) {
			// Items in the trash are loaded flat. The parents are unknown.
			VFSContainer trashRootContainer = getTopMostDescendantsContainer();
			secCallback = VFSManager.findInheritedSecurityCallback(trashRootContainer);
		}
		
		if (secCallback != null) {
			if (secCallback.canDeleteRevisionsPermanently()) {
				return true;
			}
			if (vfsMetadata != null 
					&& vfsMetadata.getFileInitializedBy() != null
					&& getIdentity().getKey().equals(vfsMetadata.getFileInitializedBy().getKey())) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean canViewTrash() {
		return trashEnabled;
	}
	
	private void leaveTrash(UserRequest ureq) {
		if (FolderView.trash == folderView) {
			VFSContainer editableContainer = getTopMostEditableContainer();
			updateCurrentContainer(ureq, editableContainer, true);
			doOpenView(ureq, FolderView.folder);
		}
	}
	
	private boolean canRestore() {
		return trashEnabled;
	}
	
	private void doRestoreSelectFolder(UserRequest ureq, FolderRow row) {
		if (guardModalController(restoreSelectFolderCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = getUncachedItem(row.getVfsItem());
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		if (isNotDeleted(vfsMetadata)) {
			if (vfsItem instanceof VFSContainer) {
				showError("error.restore.container");
			} else {
				showError("error.restore.leaf");
			}
			return;
		}
		
		removeAsListenerAndDispose(restoreSelectFolderCtrl);
		
		VFSContainer startContainer = currentContainer;
		VFSItem parentItem = rootContainer.resolve(row.getFilePath());
		if (parentItem != null && parentItem.exists()) {
			if (parentItem instanceof VFSContainer parentContainer) {
				startContainer = parentContainer;
			}
		}
		
		restoreSelectFolderCtrl = new FolderTargetController(ureq, getWindowControl(), rootContainer, startContainer,
				translate("restore"));
		listenTo(restoreSelectFolderCtrl);
		restoreSelectFolderCtrl.setUserObject(new CopyMoveParams(true, true, List.of(vfsItem), null));
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), restoreSelectFolderCtrl.getInitialComponent(),
				true, translate( "restore"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRestore(UserRequest ureq, VFSContainer targetContainer, List<VFSItem> itemsToCopy) {
		if (isItemNotAvailable(ureq, targetContainer, true)) return;
		
		if (!canEdit(targetContainer)) {
			showWarning("error.copy.target.read.only");
			return;
		}
		
		VFSItem reloadedTarget = vfsRepositoryService.getItemFor(targetContainer.getMetaInfo());
		if (!(reloadedTarget instanceof VFSContainer)) {
			showWarning("error.copy.target.read.only");
			return;
		}
		
		FolderAddEvent addEvent = new FolderAddEvent();
		VFSSuccess vfsStatus = VFSSuccess.SUCCESS;
		ListIterator<VFSItem> listIterator = itemsToCopy.listIterator();
		while (listIterator.hasNext() && vfsStatus == VFSSuccess.SUCCESS) {
			VFSItem vfsItemToCopy = listIterator.next();
			if (VFSSuccess.SUCCESS == vfsStatus) {
				vfsStatus = vfsItemToCopy.restore((VFSContainer)reloadedTarget);
				if (VFSSuccess.SUCCESS == vfsStatus) {
					addEvent.addFilename(vfsItemToCopy.getName());
				}
			}
		}
		
		if (vfsStatus == VFSSuccess.ERROR_QUOTA_EXCEEDED) {
			showWarning("error.restore.quota.exceeded");
		} else if (vfsStatus != VFSSuccess.SUCCESS) {
			log.debug("Restore error {}", vfsStatus);
			showWarning("error.restore");
		}
		
		loadModel(ureq);
		markNews();
		if (!addEvent.getFilenames().isEmpty()) {
			fireEvent(ureq, addEvent);
		}
	}
	
	private void doBulkRestoreSelectFolder(UserRequest ureq) {
		if (guardModalController(restoreSelectFolderCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSItem> selecteditems = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(row -> getUncachedItem(row.getVfsItem()))
				.filter(Objects::nonNull)
				.filter(vfsIttem -> !isItemNotAvailable(ureq, vfsIttem, false))
				.toList();
		
		if (selecteditems.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		removeAsListenerAndDispose(restoreSelectFolderCtrl);
		
		restoreSelectFolderCtrl = new FolderTargetController(ureq, getWindowControl(), rootContainer, rootContainer,
				translate("restore"));
		listenTo(restoreSelectFolderCtrl);
		restoreSelectFolderCtrl.setUserObject(new CopyMoveParams(true, true, selecteditems, null));
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), restoreSelectFolderCtrl.getInitialComponent(),
				true, translate( "restore"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean hasLockedChild(VFSContainer vfsContainer) {
		for (VFSItem vfsItem : vfsContainer.getItems()) {
			if (vfsLockManager.isLockedForMe(vfsItem, vfsItem.getMetaInfo(), getIdentity(), VFSLockApplicationType.vfs, null)) {
				return true;
			}
			if (vfsItem instanceof VFSContainer vfsChildContainer && hasLockedChild(vfsChildContainer)) {
				return true;
			}
		}
		return false;
	}

	private boolean isItemNotAvailable(UserRequest ureq, FolderRow row, boolean showDeletedMessage) {
		VFSItem vfsItem = row.getVfsItem();
		if (vfsItem == null && row.getMetadata() != null) {
			vfsItem = vfsRepositoryService.getItemFor(row.getMetadata());
		}
		return isItemNotAvailable(ureq, vfsItem, showDeletedMessage);
	}

	private boolean isItemNotAvailable(UserRequest ureq, VFSItem vfsItem, boolean showDeletedMessage) {
		if (vfsItem == null || !vfsItem.exists()) {
			if (showDeletedMessage) {
				if (vfsItem instanceof VFSContainer) {
					showError("error.deleted.container");
				} else {
					showError("error.deleted.leaf");
				}
				loadModel(ureq);
			}
			return true;
		}
		return false;
	}
	
	private void reloadQuota() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer == null || inheritingContainer.getLocalSecurityCallback().getQuota() == null) {
			return;
		}
		
		Quota customQuota = quotaManager.getCustomQuota(inheritingContainer.getLocalSecurityCallback().getQuota().getPath());
		if (customQuota != null) {
			inheritingContainer.getLocalSecurityCallback().setQuota(customQuota);
		}
	}

	private void updateQuotaBarUI(UserRequest ureq) {
		quotaBar.setQuota(getFolderQuota(ureq));
	}
	
	private FolderQuota getFolderQuota(UserRequest ureq) {
		Instant start = Instant.now();
		
		Quota quota = null;
		long actualUsage = 0;
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer != null) {
			quota = inheritingContainer.getLocalSecurityCallback().getQuota();
			if (quota != null && Quota.UNLIMITED != quota.getQuotaKB()) {
				if (VFSStatus.YES == inheritingContainer.canDescendants()) {
					// The size based on the metadata may differ slightly from the real value, as
					// invisible files such as thumbnails are not taken into account.
					actualUsage = vfsRepositoryService.getDescendantsSize(inheritingContainer.getMetaInfo(), Boolean.FALSE, null) / 1024l;
					log.debug("Folder: quota calculated (metadata) in {} millis", Duration.between(start, Instant.now()).toMillis());
				} else {
					actualUsage = VFSManager.getUsageKB(getUncachedItem(inheritingContainer));
					log.debug("Folder: quota calculated (files) in {} millis", Duration.between(start, Instant.now()).toMillis());
				}
			}
		}
		
		return new FolderQuota(ureq, quota, actualUsage);
	}
	
	private void markNews() {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (container != null) {
			VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
			if (secCallback != null) {
				SubscriptionContext subsContext = secCallback.getSubscriptionContext();
				if (subsContext != null) {
					notificationsManager.markPublisherNews(subsContext, getIdentity(), true);
				}
			}
		}
	}
	
	private void doOpenTools(UserRequest ureq, FolderRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		if (isItemNotAvailable(ureq, row, true)) {
			return;
		}
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final FolderRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, FolderRow row) {
			super(ureq, wControl);
			setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			VFSItem vfsItem = getUncachedItem(row.getVfsItem());
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			boolean isNotDeleted = isNotDeleted(vfsMetadata);

			if (row.isDirectory() && isNotDeleted) {
				addLink("open.button", CMD_FOLDER, "o_icon o_icon-fw o_icon_preview");
			}
			if (vfsItem instanceof VFSLeaf vfsLeaf) {
				Roles roles = ureq.getUserSession().getRoles();
				boolean canEdit = canEdit(vfsItem);
				DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
						vfsMetadata, vfsMetadata != null, DocEditorService.modesEditView(canEdit));
				if (editorInfo.isEditorAvailable()) {
					String command = canEdit ? CMD_FILE_EDITOR : CMD_FILE;
					Link link = LinkFactory.createLink("file.open", command, getTranslator(), mainVC, this, Link.LINK + Link.NONTRANSLATED);
					link.setCustomDisplayText(editorInfo.getModeButtonLabel(getTranslator()));
					link.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
					link.setUserObject(row);
					if (editorInfo.isNewWindow() && (canEdit || !folderModule.isForceDownload(row.getVfsItem()))) {
						link.setNewWindow(true, true);
					}
				}
			}
			
			addLink("download", CMD_DOWNLOAD, "o_icon o_icon-fw o_icon_download");
			
			boolean copyDivider = false;
			if (canRename(vfsItem, vfsMetadata)) {
				addLink("rename", CMD_RENAME, "o_icon o_icon-fw o_icon_rename");
				copyDivider = true;
			}
			if (canZip(vfsItem, vfsMetadata)) {
				addLink("zip", CMD_ZIP, "o_icon o_icon-fw o_filetype_zip");
				copyDivider = true;
			}
			if (canUnzip(vfsItem, vfsMetadata)) {
				addLink("unzip", CMD_UNZIP, "o_icon o_icon-fw o_filetype_zip");
				copyDivider = true;
			}
			if (canMove(vfsItem, vfsMetadata)) {
				addLink("move.to", CMD_MOVE, "o_icon o_icon-fw o_icon_move");
				copyDivider = true;
			}
			if (canCopy(vfsItem, vfsMetadata)) {
				addLink("copy.to", CMD_COPY, "o_icon o_icon-fw o_icon_copy");
				copyDivider = true;
			}
			mainVC.contextPut("copyDivider", copyDivider);
			
			boolean metadataDivider = false;
			if (hasMetadata(vfsItem)) {
				addLink("metadata", CMD_METADATA, "o_icon o_icon-fw o_icon_metadata");
				metadataDivider = true;
			}
			if (hasVersion(row.getMetadata(), vfsItem) && canEdit(vfsItem) && isNotDeleted) {
				addLink("versions", CMD_VERSION, "o_icon o_icon-fw o_icon_version");
				metadataDivider = true;
			}
			mainVC.contextPut("metadataDivider", metadataDivider);
			
			boolean deleteDivider = false;
			if (!isNotDeleted && canRestore()) {
				addLink("restore", CMD_RESTORE, "o_icon o_icon-fw o_icon_restore");
				deleteDivider = true;
			}
			if (canDelete(vfsItem) && isNotDeleted) {
				addLink("delete", CMD_DELETE, "o_icon o_icon-fw o_icon_delete_item");
				deleteDivider = true;
			}
			if (canDeletePermamently(vfsItem) && !isNotDeleted) {
				addLink("delete.permanently", CMD_DELETE_PERMANENTLY, "o_icon o_icon-fw o_icon_delete_item");
				deleteDivider = true;
			}
			mainVC.contextPut("deleteDivider", deleteDivider);
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if (source instanceof Link link) {
				String cmd = link.getCommand();
				if (CMD_FOLDER.equals(cmd)) {
					doOpenFolder(ureq, row);
				} else if (CMD_FILE.equals(cmd)) {
					doOpenFile(ureq, row);
				} else if (CMD_FILE_EDITOR.equals(cmd)) {
					doOpenFileInEditor(ureq, row);
				} else if (CMD_DOWNLOAD.equals(cmd)) {
					doDownload(ureq, row);
				} else if (CMD_MOVE.equals(cmd)) {
					doMoveSelectFolder(ureq, row);
				} else if (CMD_COPY.equals(cmd)) {
					doCopySelectFolder(ureq, row);
				} else if (CMD_RENAME.equals(cmd)) {
					doRename(ureq, row);
				} else if (CMD_METADATA.equals(cmd)) {
					doOpenMetadata(ureq, row);
				} else if (CMD_VERSION.equals(cmd)) {
					doOpenVersions(ureq, row);
				} else if (CMD_ZIP.equals(cmd)) {
					doZipConfirmation(ureq, row);
				} else if (CMD_UNZIP.equals(cmd)) {
					doUnzip(ureq, row);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeleteSoftly(ureq, row);
				} else if (CMD_RESTORE.equals(cmd)) {
					doRestoreSelectFolder(ureq, row);
				} else if (CMD_DELETE_PERMANENTLY.equals(cmd)) {
					doConfirmDeletePermanently(ureq, row);
				}
			}
		}
	}
	
	private final class FolderCssDelegate extends DefaultFlexiTableCssDelegate {
		
		private final FolderDataModel dataModel;
		
		public FolderCssDelegate(FolderDataModel dataModel) {
			this.dataModel = dataModel;
		}

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o o_table_rows_middle";
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			if (FlexiTableRendererType.classic == type) {
				return "o_table_middle o_table_nowrap";
			}
			if (FlexiTableRendererType.custom == type) {
				return "o_folder_table o_block_small_top";
			}
			return null;
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_folder_row";
		}

		@Override
		public List<Data> getRowDataAttributes(int pos) {
			FolderRow row = dataModel.getObject(pos);
			String filename = StringHelper.escapeForHtmlAttribute(row.getFilename());
			if(row.isDirectory()) {
				return List.of(new Data("upload-folder", filename));
			} 
			return List.of(new Data("drag-file", filename));
		}
	}
	
	public static final class CopyMoveParams {
		
		private final boolean move;
		private final boolean suppressVersion;
		private final List<VFSItem> itemsToCopy;
		private final Consumer<List<String>> successMessage;
		private VFSContainer targetContainer;
		private License license;
		private Boolean overwrite;
		
		public CopyMoveParams(boolean move, boolean suppressVersion, List<VFSItem> itemsToCopy,
				Consumer<List<String>> successMessage) {
			this.move = move;
			this.suppressVersion = suppressVersion;
			this.itemsToCopy = itemsToCopy;
			this.successMessage = successMessage;
		}

		public boolean isMove() {
			return move;
		}

		public boolean isSuppressVersion() {
			return suppressVersion;
		}

		public List<VFSItem> getItemsToCopy() {
			return itemsToCopy;
		}

		public Consumer<List<String>> getSuccessMessage() {
			return successMessage;
		}

		public VFSContainer getTargetContainer() {
			return targetContainer;
		}

		public void setTargetContainer(VFSContainer targetContainer) {
			this.targetContainer = targetContainer;
		}

		public License getLicense() {
			return license;
		}

		public void setLicense(License license) {
			this.license = license;
		}

		public Boolean getOverwrite() {
			return overwrite;
		}

		public void setOverwrite(Boolean overwrite) {
			this.overwrite = overwrite;
		}
		
	}

}
