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
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorOpenInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.folder.ui.FileBrowserController;
import org.olat.core.commons.services.folder.ui.FileBrowserSelectionMode;
import org.olat.core.commons.services.folder.ui.FolderQuota;
import org.olat.core.commons.services.folder.ui.event.FileBrowserSelectionEvent;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.commons.services.tag.ui.component.FlexiTableTagFilter;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileFilter;
import org.olat.modules.project.ProjFileInfo;
import org.olat.modules.project.ProjFileRef;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjFileDataModel.FileCols;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
abstract class ProjFileListController extends FormBasicController  implements Activateable2, FlexiTableComponentDelegate {
	
	private static final String TAB_ID_MY = "My";
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_RECENTLY = "Recently";
	private static final String TAB_ID_NEW = "New";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String FILTER_KEY_MY = "my";
	private static final String CMD_SELECT = "select";
	private static final String CMD_OPEN = "open";
	private static final String CMD_METADATA = "metadata";
	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_RESTORE = "restore";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_DELETE_PERMANENTLY = "deletePermanently";
	
	private FormLink bulkDownloadButton;
	private FormLink bulkDeleteButton;
	private FlexiFiltersTab tabMy;
	protected FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRecently;
	private FlexiFiltersTab tabNew;
	private FlexiFiltersTab tabDeleted;
	private FlexiTableElement tableEl;
	private ProjFileDataModel dataModel;
	
	private CloseableModalController cmc;
	private ProjFileUploadController fileUploadCtrl;
	private ProjFileCreateController fileCreateCtrl;
	private ProjFileEditController fileEditCtrl;
	private ProjRecordAVController recordAVController;
	private ConfirmationController deleteSoftlyConfirmationCtrl;
	private ConfirmationController deletePermanentlyConfirmationCtrl;
	private BulkDeleteConfirmationController bulkDeleteConfirmationCtrl;
	private Controller docEditorCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private FileBrowserController addFromBrowserCtrl;
	
	protected final ProjectBCFactory bcFactory;
	protected final ProjProject project;
	protected final ProjProjectSecurityCallback secCallback;
	private final Date lastVisitDate;
	private final MapperKey avatarMapperKey;
	private final Formatter formatter;
	
	@Autowired
	protected ProjectService projectService;
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AVModule avModule;

	public ProjFileListController(UserRequest ureq, WindowControl wControl, String pageName, ProjectBCFactory bcFactory,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate,
			MapperKey avatarMapperKey) {
		super(ureq, wControl, pageName);
		this.bcFactory = bcFactory;
		this.project = project;
		this.secCallback = secCallback;
		this.lastVisitDate = lastVisitDate;
		this.avatarMapperKey = avatarMapperKey;
		this.formatter = Formatter.getInstance(getLocale());
	}
	
	protected abstract boolean isFullTable();
	
	protected abstract Integer getNumLastModified();

	protected abstract void onModelLoaded();
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileCols.id));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.tags, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.involved));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.lastModifiedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.lastModifiedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileCols.deletedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileCols.deletedBy));
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(FileCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		dataModel = new ProjFileDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(isFullTable());
		if (isFullTable()) {
			tableEl.setAndLoadPersistedPreferences(ureq, "project-files-all");
		}
		
		tableEl.setCssDelegate(ProjFileListCssDelegate.DELEGATE);
		if (isFullTable()) {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		} else {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		}
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("file_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		// Load before init filter because the filter uses the loaded data to get the available values.
		loadModel(ureq, false);
		if (isFullTable()) {
			initBulkLinks();
			initFilters();
			initFilterTabs(ureq);
		}
		// Sort when the filter is selected
		sortTable();
		doSelectFilterTab(null);
	}

	private void initBulkLinks() {
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		bulkDownloadButton = uifactory.addFormLink("download", flc, Link.BUTTON);
		bulkDownloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		tableEl.addBatchButton(bulkDownloadButton);
		
		if (secCallback.canEditFiles()) {
			bulkDeleteButton = uifactory.addFormLink("delete", flc, Link.BUTTON + Link.NONTRANSLATED);
			bulkDeleteButton.setIconLeftCSS("o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
			bulkDeleteButton.setI18nKey(translate("delete"));
			tableEl.addBatchButton(bulkDeleteButton);
		}
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues myValues = new SelectionValues();
		myValues.add(SelectionValues.entry(FILTER_KEY_MY, translate("file.filter.my.value")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("file.filter.my"), ProjFileFilter.my.name(), myValues, true));
		
		List<TagInfo> tagInfos = projectService.getTagInfos(project, null);
		if (!tagInfos.isEmpty()) {
			filters.add(new FlexiTableTagFilter(translate("tags"), ProjFileFilter.tag.name(), tagInfos, true));
		}
		
		SelectionValues suffixValues = new SelectionValues();
		dataModel.getObjects().stream()
				.map(row -> FileUtils.getFileSuffix(row.getFilename().toLowerCase()))
				.filter(StringHelper::containsNonWhitespace)
				.distinct()
				.forEach(suffix -> suffixValues.add(SelectionValues.entry(suffix, suffix)));
		suffixValues.sort(SelectionValues.VALUE_ASC);
		if (suffixValues.size() > 0) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("file.type"), ProjFileFilter.type.name(), suffixValues, true));
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(ProjectStatus.active.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.active)));
		statusValues.add(SelectionValues.entry(ProjectStatus.deleted.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.deleted)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("status"), ProjFileFilter.status.name(), statusValues, true));
		
		filters.add(new FlexiTableTextFilter(translate("file.filter.filename"), ProjFileFilter.filename.name(), true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(6);
		
		tabMy = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_MY,
				translate("file.list.tab.my"),
				TabSelectionBehavior.reloadData,
				List.of(
						FlexiTableFilterValue.valueOf(ProjFileFilter.status, ProjectStatus.active.name()),
						FlexiTableFilterValue.valueOf(ProjFileFilter.my, FILTER_KEY_MY)));
		tabs.add(tabMy);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjFileFilter.status, ProjectStatus.active.name())));
		tabs.add(tabAll);
		
		tabRecently = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RECENTLY,
				translate("tab.recently"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjFileFilter.status, ProjectStatus.active.name())));
		tabs.add(tabRecently);
		
		tabNew = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_NEW,
				translate("tab.new"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjFileFilter.status, ProjectStatus.active.name())));
		tabs.add(tabNew);
		
		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjFileFilter.status, ProjectStatus.deleted.name())));
		tabs.add(tabDeleted);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		doSelectFilterTab(tab);
		loadModel(ureq, true);
	}
	
	private void doSelectFilterTab(FlexiFiltersTab tab) {
		if (secCallback.canCreateFiles() && (tabDeleted == null || tabDeleted != tab)) {
			tableEl.setEmptyTableSettings("file.list.empty.message", "file.empty.hint.readwrite", "o_icon_proj_file", "file.upload", "o_icon_filehub_add", false);
		} else {
			tableEl.setEmptyTableSettings("file.list.empty.message", "file.empty.hint.readonly", "o_icon_proj_file");
		}
		
		if (bulkDeleteButton != null) {
			if (tab != tabDeleted) {
				bulkDeleteButton.setI18nKey(translate("delete"));
			} else {
				bulkDeleteButton.setI18nKey(translate("delete.permanently"));
			}
		}
	}
	
	public void reload(UserRequest ureq) {
		loadModel(ureq, false);
	}
	
	protected void loadModel(UserRequest ureq, boolean sort) {
		ProjFileSearchParams searchParams = createSearchParams();
		applyFilters(searchParams);
		List<ProjFileInfo> infos = projectService.getFileInfos(searchParams, ProjArtefactInfoParams.of(true, false, true));
		List<ProjFileRow> rows = new ArrayList<>(infos.size());
		
		for (ProjFileInfo info : infos) {
			ProjFileRow row = new ProjFileRow(info.getFile());
			VFSMetadata vfsMetadata = info.getFile().getVfsMetadata();
			
			String modifiedDate = formatter.formatDateRelative(vfsMetadata.getFileLastModified());
			String modifiedBy = userManager.getUserDisplayName(vfsMetadata.getFileLastModifiedBy());
			row.setLastModifiedByName(modifiedBy);
			String modified = translate("date.by", modifiedDate, modifiedBy);
			row.setModified(modified);
			
			if (row.getDeletedBy() != null) {
				row.setDeletedByName(userManager.getUserDisplayName(row.getDeletedBy().getKey()));
			}
			
			row.setTagKeys(info.getTags().stream().map(Tag::getKey).collect(Collectors.toSet()));
			row.setFormattedTags(TagUIFactory.getFormattedTags(getLocale(), info.getTags()));
			
			VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
			if (vfsItem instanceof VFSLeaf vfsLeaf && isThumbnailAvailable(vfsLeaf, vfsMetadata)) {
				VFSLeaf thumbnail = getThumbnail(vfsLeaf);
				if (thumbnail != null) {
					row.setThumbnailAvailable(true);
					VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
					String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
					row.setThumbnailUrl(thumbnailUrl);
				}
			}
			
			row.setMemberKeys(info.getMembers().stream().map(Identity::getKey).collect(Collectors.toSet()));
			
			forgeSelectLink(row, info.getFile(), vfsMetadata, ureq.getUserSession().getRoles());
			forgeUsersPortraits(ureq, row, info.getMembers());
			forgeToolsLink(row);
			
			rows.add(row);
		}
		
		applyFilters(rows);
		dataModel.setObjects(rows);
		if (sort) {
			sortTable();
		}
		tableEl.reset(true, true, true);
		
		onModelLoaded();
	}

	private boolean isThumbnailAvailable(VFSLeaf vfsLeaf, VFSMetadata vfsMetadata) {
		if (isAudio(vfsLeaf)) {
			return true;
		}
		if (vfsLeaf.getSize() == 0) {
			return false;
		}
		return vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsMetadata);
	}

	private boolean isAudio(VFSLeaf vfsLeaf) {
		if ("m4a".equalsIgnoreCase(FileUtils.getFileSuffix(vfsLeaf.getRelPath()))) {
			return true;
		}
		return false;
	}

	private VFSLeaf getThumbnail(VFSLeaf vfsLeaf) {
		if (isAudio(vfsLeaf)) {
			return vfsRepositoryService.getLeafFor(avModule.getAudioWaveformUrl());
		}
		return vfsRepositoryService.getThumbnail(vfsLeaf, 650, 1000, false);
	}
	
	private ProjFileSearchParams createSearchParams() {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		searchParams.setNumLastModified(getNumLastModified());
		return searchParams;
	}

	private void applyFilters(ProjFileSearchParams searchParams) {
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabNew) {
			searchParams.setCreatedAfter(lastVisitDate);
		} else {
			searchParams.setCreatedAfter(null);
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjFileFilter.type.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					searchParams.setSuffixes(values);
				} else {
					searchParams.setSuffixes(null);
				}
			}
			
			if (ProjFileFilter.status.name() == filter.getFilter()) {
				List<String> status = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ProjectStatus::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setStatus(null);
				}
			}
			if (ProjFileFilter.filename.name() == filter.getFilter()) {
				String value = filter.getValue();
				if (StringHelper.containsNonWhitespace(value)) {
					Set<String> filenames = Arrays.stream(value.split(","))
							.map(String::trim)
							.filter(StringHelper::containsNonWhitespace)
							.collect(Collectors.toSet());
					searchParams.setExactFilenames(filenames);
				} else {
					searchParams.setExactFilenames(null);
				}
			}
		}
	}
	
	private void applyFilters(List<ProjFileRow> rows) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjFileFilter.my.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_KEY_MY)) {
					Long identityKey = getIdentity().getKey();
					rows.removeIf(row -> !row.getMemberKeys().contains(identityKey));
				}
			}
			
			if (ProjFileFilter.tag.name().equals(filter.getFilter())) {
				List<String> values = ((FlexiTableTagFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					Set<Long> selectedTagKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					rows.removeIf(row -> row.getTagKeys() == null || !row.getTagKeys().stream().anyMatch(key -> selectedTagKeys.contains(key)));
				}
			}
		}
	}
	
	private void sortTable() {
		if (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabRecently) {
			tableEl.sort(new SortKey(FileCols.lastModifiedDate.name(), false));
		} else if (tableEl.getSelectedFilterTab() == tabNew) {
			tableEl.sort(new SortKey(FileCols.creationDate.name(), false));
		} else {
			tableEl.sort(new SortKey(FileCols.displayName.name(), true));
		}
	}
	
	private void forgeSelectLink(ProjFileRow row, ProjFile file, VFSMetadata vfsMetadata, Roles roles) {
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		FormLink classicLink = uifactory.addFormLink("selectc_" + row.getKey(), CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		
		link.setElementCssClass("o_link_plain");
		
		link.setI18nKey(StringHelper.escapeHtml(row.getDisplayName()));
		classicLink.setI18nKey(StringHelper.escapeHtml(row.getDisplayName()));
		
		String iconCSS = CSSHelper.createFiletypeIconCssClassFor(vfsMetadata.getFilename());
		link.setIconLeftCSS("o_icon " + iconCSS);
		
		VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			LockInfo lock = vfsLockManager.getLock(vfsLeaf);
			if (lock != null && lock.isLocked()) {
				link.setIconRightCSS("o_icon o_icon_locked");
				classicLink.setIconRightCSS("o_icon o_icon_locked");
			}
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
					vfsMetadata, true, DocEditorService.modesEditView(secCallback.canEditFile(file)));
			if (editorInfo.isNewWindow()) {
				link.setNewWindow(true, true, false);
				classicLink.setNewWindow(true, true, false);
				row.setOpenInNewWindow(true);
			}
		}
		
		link.setUserObject(row);
		classicLink.setUserObject(row);
		row.setSelectLink(link);
		row.setSelectClassicLink(classicLink);
	}

	private void forgeUsersPortraits(UserRequest ureq, ProjFileRow row, Set<Identity> members) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(new ArrayList<>(members));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + row.getKey(), flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		row.setUserPortraits(usersPortraitCmp);
	}
	
	private void forgeToolsLink(ProjFileRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof ProjFileRow projRow) {
			if (projRow.getSelectLink() != null) {
				cmps.add(projRow.getSelectLink().getComponent());
			}
			if (projRow.getUserPortraits() != null) {
				cmps.add(projRow.getUserPortraits());
			}
			if (projRow.getToolsLink() != null) {
				cmps.add(projRow.getToolsLink().getComponent());
			}
		}
		return cmps;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String type = entry.getOLATResourceable().getResourceableTypeName();
			FlexiFiltersTab tab = tableEl.getFilterTabById(type);
			if (tab != null) {
				selectFilterTab(ureq, tab);
			} else {
				selectFilterTab(ureq, tabAll);
				if (ProjectBCFactory.TYPE_FILE.equals(type)) {
					Long key = entry.getOLATResourceable().getResourceableId();
					ProjFile file = projectService.getFile(() -> key);
					if (file != null) {
						if (ProjectStatus.deleted == file.getArtefact().getStatus()) {
							selectFilterTab(ureq, tabDeleted);
						}
						tableEl.setFiltersValues(null, List.of(ProjFileFilter.status.name()), List.of(FlexiTableFilterValue.valueOf(ProjFileFilter.status, file.getArtefact().getStatus()),
								FlexiTableFilterValue.valueOf(ProjFileFilter.filename, file.getVfsMetadata().getFilename())));
						tableEl.expandFilters(true);
						loadModel(ureq, true);
						doOpenFileInLightbox(ureq, file);
					}
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (fileUploadCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				showUploadSuccessMessage(Collections.singletonList(fileUploadCtrl.getFilename()));
				fileUploadCtrl.getFileEl().reset();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (addFromBrowserCtrl == source) {
			cmc.deactivate();
			if (event instanceof FileBrowserSelectionEvent selectionEvent) {
				doUploadFile(ureq, selectionEvent.getFileElement());
			}
		} else if (fileCreateCtrl == source
				|| fileEditCtrl == source
				|| recordAVController == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteSoftlyConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDeleteSoftly(ureq, (ProjFileRef) deleteSoftlyConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (deletePermanentlyConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDeletePermanently(ureq, (ProjFileRef) deletePermanentlyConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (bulkDeleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doBulkDelete(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (docEditorCtrl == source) {
			cleanUp();
		} else if (cmc == source) {
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
		removeAsListenerAndDispose(bulkDeleteConfirmationCtrl);
		removeAsListenerAndDispose(deleteSoftlyConfirmationCtrl);
		removeAsListenerAndDispose(deletePermanentlyConfirmationCtrl);
		removeAsListenerAndDispose(addFromBrowserCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(fileEditCtrl);
		removeAsListenerAndDispose(fileUploadCtrl);
		removeAsListenerAndDispose(fileCreateCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(recordAVController);
		removeAsListenerAndDispose(cmc);
		bulkDeleteConfirmationCtrl = null;
		deleteSoftlyConfirmationCtrl = null;
		deletePermanentlyConfirmationCtrl = null;
		addFromBrowserCtrl = null;
		toolsCalloutCtrl = null;
		fileEditCtrl = null;
		fileUploadCtrl = null;
		fileCreateCtrl = null;
		docEditorCtrl = null;
		toolsCtrl = null;
		recordAVController = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String fileKey = ureq.getParameter("select_file");
			if (StringHelper.isLong(fileKey)) {
				Long key = Long.valueOf(fileKey);
				doOpenOrDownload(ureq, key);
				return;
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq, false);
			} else if (event instanceof FlexiTableFilterTabEvent flexiTableFilterTabEvent) {
				doSelectFilterTab((flexiTableFilterTabEvent).getTab());
				loadModel(ureq, true);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doAddFromBrowser(ureq);
			}
		} else if (bulkDownloadButton == source) {
			doBulkDownload(ureq);
		} else if (bulkDeleteButton == source) {
			doConfirmBulkDelete(ureq);
		} else if (source instanceof FormLink link) {
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof ProjFileRow row) {
				doOpenOrDownload(ureq, row.getKey());
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ProjFileRow row) {
				doOpenTools(ureq, row, link);
			} 
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
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

	protected void doUploadFile(UserRequest ureq, FileElement fileElement) {
		if (guardModalController(fileUploadCtrl)) return;

		fileUploadCtrl = new ProjFileUploadController(ureq, getWindowControl(), project, fileElement);
		listenTo(fileUploadCtrl);

		String title = translate("file.upload");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), fileUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	protected void doAddFromBrowser(UserRequest ureq) {
		if (guardModalController(addFromBrowserCtrl)) return;
		VFSContainer currentContainer = projectService.getProjectContainer(project);
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.upload");
			loadModel(ureq, false);
		}

		FolderQuota folderQuota = new FolderQuota(ureq, null, 0L);

		removeAsListenerAndDispose(addFromBrowserCtrl);
		addFromBrowserCtrl = new FileBrowserController(ureq, getWindowControl(), FileBrowserSelectionMode.sourceSingle,
				folderQuota, translate("add"));
		listenTo(addFromBrowserCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				addFromBrowserCtrl.getInitialComponent(), true, translate("browser.add"), true);
		listenTo(cmc);
		cmc.activate();
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

	protected void doCreateFile(UserRequest ureq) {
		if (guardModalController(fileCreateCtrl)) return;
		
		fileCreateCtrl = new ProjFileCreateController(ureq, getWindowControl(), project);
		listenTo(fileCreateCtrl);
		
		String title = translate("file.create");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), fileCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	protected void doRecordVideo(UserRequest ureq) {
		if (guardModalController(recordAVController)) {
			return;
		}

		recordAVController = new ProjRecordAVController(ureq, getWindowControl(), project, false);
		listenTo(recordAVController);

		String title = translate("record.video");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recordAVController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	protected void doRecordAudio(UserRequest ureq) {
		if (guardModalController(recordAVController)) {
			return;
		}

		recordAVController = new ProjRecordAVController(ureq, getWindowControl(), project, true);
		listenTo(recordAVController);

		String title = translate("record.audio");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recordAVController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenFile(UserRequest ureq, ProjFile file, VFSLeaf vfsLeaf) {
		VFSContainer projectContainer = projectService.getProjectContainer(project);
		HTMLEditorConfig htmlEditorConfig = HTMLEditorConfig.builder(projectContainer, vfsLeaf.getName())
				.withAllowCustomMediaFactory(false)
				.withDisableMedia(true)
				.build();
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withFireSavedEvent(true)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
		DocEditorOpenInfo docEditorOpenInfo = docEditorService.openDocument(ureq, getWindowControl(), configs,
				DocEditorService.modesEditView(secCallback.canEditFile(file)));
		docEditorCtrl = listenTo(docEditorOpenInfo.getController());
		
		if (Mode.EDIT == docEditorOpenInfo.getMode()) {
			reload(ureq);
		} else {
			projectService.createActivityRead(getIdentity(), file.getArtefact());
		}
	}
	
	private void doOpenFileInLightbox(UserRequest ureq, ProjFile file) {
		VFSMetadata vfsMetadata = file.getVfsMetadata();
		VFSContainer projectContainer = projectService.getProjectContainer(project);
		VFSItem vfsItem = projectContainer.resolve(file.getVfsMetadata().getFilename());
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
					ureq.getUserSession().getRoles(), vfsLeaf, vfsMetadata,
					true, DocEditorService.modesEditView(secCallback.canEditFile(file)));
			if (editorInfo.isEditorAvailable() && !editorInfo.isNewWindow()) {
				doOpenFile(ureq, file, vfsLeaf);
			}
		}
	}
	
	protected void doEditMetadata(UserRequest ureq, ProjFileRef fileRef) {
		if (guardModalController(fileEditCtrl)) return;
		
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setFiles(List.of(fileRef));
		List<ProjFileInfo> fileInfos = projectService.getFileInfos(searchParams, ProjArtefactInfoParams.MEMBERS);
		if (fileInfos == null || fileInfos.isEmpty()) {
			loadModel(ureq, false);
			return;
		}
		
		ProjFileInfo fileInfo = fileInfos.get(0);
		ProjFile file = fileInfo.getFile();
		fileEditCtrl = new ProjFileEditController(ureq, getWindowControl(), bcFactory, file, fileInfo.getMembers(), false, false);
		listenTo(fileEditCtrl);
		
		String title = translate("edit.metadata");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), fileEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDownload(UserRequest ureq, ProjArtefact artefact, VFSLeaf vfsLeaf) {
		VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
		projectService.createActivityDownload(getIdentity(), artefact);
	}
	
	private void doOpenOrDownload(UserRequest ureq, Long key) {
		ProjFile file = projectService.getFile(() -> key);
		if (file != null) {
			VFSMetadata vfsMetadata = file.getVfsMetadata();
			VFSContainer projectContainer = projectService.getProjectContainer(project);
			VFSItem vfsItem = projectContainer.resolve(file.getVfsMetadata().getFilename());
			if (vfsItem instanceof VFSLeaf vfsLeaf) {
				DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
						ureq.getUserSession().getRoles(), vfsLeaf, vfsMetadata,
						true, DocEditorService.modesEditView(secCallback.canEditFile(file)));
				if (editorInfo.isEditorAvailable()) {
					doOpenFile(ureq, file, vfsLeaf);
				} else {
					doDownload(ureq, file.getArtefact(), vfsLeaf);
				}
			}
		}
	}
	
	private void doBulkDownload(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		List<ProjFileRow> selectedRows = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		ProjFileSearchParams downloadSearchParams = new ProjFileSearchParams();
		downloadSearchParams.setFiles(selectedRows);
		doDownload(ureq, downloadSearchParams);
	}
	
	protected void doDownloadAll(UserRequest ureq) {
		ProjFileSearchParams downloadSearchParams = new ProjFileSearchParams();
		downloadSearchParams.setStatus(List.of(ProjectStatus.active));
		doDownload(ureq, downloadSearchParams);
	}

	private void doDownload(UserRequest ureq, ProjFileSearchParams downloadSearchParams) {
		Collection<ProjFile> files = projectService.getFiles(downloadSearchParams);
		MediaResource resource = projectService.createMediaResource(getIdentity(), project, files, List.of(),
				project.getTitle() + "_files");
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doRestore(UserRequest ureq, ProjFileRef file) {
		projectService.restoreFile(getIdentity(), file);
		loadModel(ureq, false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmDelete(UserRequest ureq, ProjFileRef fileRef) {
		if (guardModalController(deleteSoftlyConfirmationCtrl)) return;
		
		ProjFile file = projectService.getFile(fileRef);
		if (file == null || ProjectStatus.deleted == file.getArtefact().getStatus()) {
			return;
		}
		
		String message = translate("file.delete.softly.confirmation.message", StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(file)));
		deleteSoftlyConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), message, null, translate("delete"), true);
		deleteSoftlyConfirmationCtrl.setUserObject(file);
		listenTo(deleteSoftlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteSoftlyConfirmationCtrl.getInitialComponent(),
				true, translate("file.delete.softly.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteSoftly(UserRequest ureq, ProjFileRef file) {
		projectService.deleteFileSoftly(getIdentity(), file);
		loadModel(ureq, false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doConfirmDeletePermanently(UserRequest ureq, ProjFileRef fileRef) {
		if (guardModalController(deleteSoftlyConfirmationCtrl)) return;
		
		ProjFile file = projectService.getFile(fileRef);
		if (file == null || ProjectStatus.deleted != file.getArtefact().getStatus()) {
			return;
		}
		
		deletePermanentlyConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("file.delete.permanently.confirmation.message", StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(file))),
				translate("file.delete.permanently.confirmation.confirm"),
				translate("delete"), true);
		deletePermanentlyConfirmationCtrl.setUserObject(file);
		listenTo(deletePermanentlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deletePermanentlyConfirmationCtrl.getInitialComponent(),
				true, translate("file.delete.permanently.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeletePermanently(UserRequest ureq, ProjFileRef file) {
		projectService.deleteFilePermanently(getIdentity(), file);
		loadModel(ureq, false);
		fireEvent(ureq, Event.CHANGED_EVENT);
		showInfo("file.delete.permanently.success");
	}
	
	private void doConfirmBulkDelete(UserRequest ureq) {
		if (tableEl.getSelectedFilterTab() == tabDeleted) {
			doConfirmBulkDeletePermanently(ureq);
		} else {
			doConfirmBulkDeleteSoftly(ureq);
		}
	}

	private void doConfirmBulkDeleteSoftly(UserRequest ureq) {
		if (guardModalController(bulkDeleteConfirmationCtrl)) return;
		
		List<ProjFile> filesToDelete = getFilesToBulkDeleteSoftly();
		if (filesToDelete.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<String> filenames = filesToDelete.stream()
				.map(ProjectUIFactory::getDisplayName)
				.sorted()
				.toList();
		
		bulkDeleteConfirmationCtrl = new BulkDeleteConfirmationController(ureq, getWindowControl(),
				translate("file.bulk.delete.softly.message", String.valueOf(filesToDelete.size())), null, translate("delete"),
				translate("file.bulk.delete.softly.label"), filenames, "file.bulk.delete.softly.show.all");
		listenTo(bulkDeleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkDeleteConfirmationCtrl.getInitialComponent(),
				true, translate("file.bulk.delete.softly.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmBulkDeletePermanently(UserRequest ureq) {
		if (guardModalController(bulkDeleteConfirmationCtrl)) return;
		
		List<ProjFile> filesToDelete = getFilesToBulkDeletePermanently();
		if (filesToDelete.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<String> filenames = filesToDelete.stream()
				.map(ProjectUIFactory::getDisplayName)
				.sorted()
				.toList();
		
		bulkDeleteConfirmationCtrl = new BulkDeleteConfirmationController(ureq, getWindowControl(),
				translate("file.bulk.delete.permanently.message", String.valueOf(filesToDelete.size())),
				translate("file.bulk.delete.permanently.confirm", String.valueOf(filesToDelete.size())),
				translate("delete"), translate("file.bulk.delete.permanently.label"), filenames,
				"file.bulk.delete.permanently.show.all");
		listenTo(bulkDeleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkDeleteConfirmationCtrl.getInitialComponent(),
				true, translate("file.bulk.delete.permanently.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkDelete(UserRequest ureq) {
		if (tableEl.getSelectedFilterTab() == tabDeleted) {
			doBulkDeletePermanently(ureq);
		} else {
			doBulkDeleteSoftly(ureq);
		}
	}

	private void doBulkDeleteSoftly(UserRequest ureq) {
		getFilesToBulkDeleteSoftly().forEach(file -> projectService.deleteFileSoftly(getIdentity(), file));
		loadModel(ureq, false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private List<ProjFile> getFilesToBulkDeleteSoftly() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return List.of();
		}
		
		List<ProjFileRow> selectedRows = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setFiles(selectedRows);
		searchParams.setStatus(List.of(ProjectStatus.active));
		return projectService.getFiles(searchParams).stream()
				.filter(file -> secCallback.canDeleteFile(file, getIdentity()))
				.toList();
	}

	private void doBulkDeletePermanently(UserRequest ureq) {
		getFilesToBulkDeletePermanently().forEach(file -> projectService.deleteFilePermanently(getIdentity(), file));
		loadModel(ureq, false);
		fireEvent(ureq, Event.CHANGED_EVENT);
		showInfo("file.bulk.delete.permanently.success");
	}

	private List<ProjFile> getFilesToBulkDeletePermanently() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return List.of();
		}
		
		List<ProjFileRow> selectedRows = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setFiles(selectedRows);
		searchParams.setStatus(List.of(ProjectStatus.deleted));
		return projectService.getFiles(searchParams).stream()
				.filter(file -> secCallback.canDeleteFilePermanently(file, getIdentity()))
				.toList();
	}
	
	private void doOpenTools(UserRequest ureq, ProjFileRow row, FormLink link) {
		addToHistory(ureq, this);
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private static final class ProjFileListCssDelegate extends DefaultFlexiTableCssDelegate {
		
		private static final ProjFileListCssDelegate DELEGATE = new ProjFileListCssDelegate();
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o_proj_file_list";
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return FlexiTableRendererType.custom == type
					? "o_proj_file_rows o_block_top o_proj_cards"
					: "o_proj_file_rows o_block_top";
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_proj_file_row";
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final ProjFileRow row;
		private final ProjFile file;
		private VFSLeaf vfsLeaf;

		
		public ToolsController(UserRequest ureq, WindowControl wControl, ProjFileRow row) {
			super(ureq, wControl);
			setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
			this.row = row;
			
			mainVC = createVelocityContainer("file_tools");
			
			file = projectService.getFile(row);
			if (file != null) {
				VFSMetadata vfsMetadata = file.getVfsMetadata();
				VFSContainer projectContainer = projectService.getProjectContainer(project);
				VFSItem vfsItem = projectContainer.resolve(file.getVfsMetadata().getFilename());
				if (vfsItem instanceof VFSLeaf) {
					vfsLeaf = (VFSLeaf)vfsItem;
					Roles roles = ureq.getUserSession().getRoles();
					DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
							vfsMetadata, true, DocEditorService.modesEditView(secCallback.canEditFile(file)));
					if (editorInfo.isEditorAvailable()) {
						Link link = LinkFactory.createLink("file.open", CMD_OPEN, getTranslator(), mainVC, this, Link.LINK + Link.NONTRANSLATED);
						link.setCustomDisplayText(editorInfo.getModeButtonLabel(getTranslator()));
						link.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
						if (editorInfo.isNewWindow()) {
							link.setNewWindow(true, true);
						}
					}
					
					if (secCallback.canEditFile(file)) {
						addLink("edit.metadata", CMD_METADATA, "o_icon o_icon-fw o_icon_edit_metadata");
					}
					
					addLink("download", CMD_DOWNLOAD, "o_icon o_icon-fw o_icon_download");
					
					if (secCallback.canRestoreFile(file)) {
						addLink("restore", CMD_RESTORE, "o_icon o_icon-fw o_icon_restore");
					}
						
					if (secCallback.canDeleteFile(file, getIdentity())) {
						addLink("delete", CMD_DELETE, "o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
					} else if (secCallback.canDeleteFilePermanently(file, getIdentity())) {
						addLink("delete.permanently", CMD_DELETE_PERMANENTLY, "o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
					}
				}
			}
			
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
				if (CMD_OPEN.equals(cmd)) {
					doOpenFile(ureq, file, vfsLeaf);
				} else if (CMD_METADATA.equals(cmd)) {
					doEditMetadata(ureq, row);
				} else if (CMD_DOWNLOAD.equals(cmd)) {
					doDownload(ureq, file.getArtefact(), vfsLeaf);
				} else if (CMD_RESTORE.equals(cmd)) {
					doRestore(ureq, row);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				} else if (CMD_DELETE_PERMANENTLY.equals(cmd)) {
					doConfirmDeletePermanently(ureq, row);
				}
			}
		}
	}

}
