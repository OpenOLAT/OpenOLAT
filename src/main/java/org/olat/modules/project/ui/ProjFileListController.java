/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileFilter;
import org.olat.modules.project.ProjFileRef;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjFileDataModel.FileCols;
import org.olat.user.UserManager;
import org.olat.user.ui.UserDisplayNameCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
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
	private static final String CMD_EDIT = "edit";
	private static final String CMD_VIEW = "view";
	private static final String CMD_METADATA = "metadata";
	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_DELETE = "delete";
	
	private FormLayoutContainer dummyCont;
	private FormLink createLink;
	private FlexiFiltersTab tabMy;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRecently;
	private FlexiFiltersTab tabNew;
	private FlexiFiltersTab tabDeleted;
	private FlexiTableElement tableEl;
	private ProjFileDataModel dataModel;
	
	private CloseableModalController cmc;
	private ProjFileUploadController fileUploadCtrl;
	private ProjFileCreateController fileCreateCtrl;
	private ProjFileEditController fileEditCtrl;
	private ProjConfirmationController deleteConfirmationCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	protected final ProjProject project;
	protected final ProjProjectSecurityCallback secCallback;
	private final Date lastVisitDate;
	private final Formatter formatter;
	
	@Autowired
	protected ProjectService projectService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;

	public ProjFileListController(UserRequest ureq, WindowControl wControl, String pageName, ProjProject project,
			ProjProjectSecurityCallback secCallback, Date lastVisitDate) {
		super(ureq, wControl, pageName);
		this.project = project;
		this.secCallback = secCallback;
		this.lastVisitDate = lastVisitDate;
		this.formatter = Formatter.getInstance(getLocale());
	}
	
	protected abstract boolean isFullTable();
	
	protected abstract Integer getNumLastModified();

	protected abstract void onModelLoaded();
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dummyCont = FormLayoutContainer.createCustomFormLayout("dummy", getTranslator(), velocity_root + "/empty.html");
		dummyCont.setRootForm(mainForm);
		formLayout.add(dummyCont);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileCols.id));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.lastModifiedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FileCols.lastModifiedBy, UserDisplayNameCellRenderer.get()));
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(FileCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		dataModel = new ProjFileDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(isFullTable());

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
		
		if (isFullTable()) {
			initFilters();
			initFilterTabs(ureq);
		}
		doSelectFilterTab(null);
		loadModel(ureq, true);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues myValues = new SelectionValues();
		myValues.add(SelectionValues.entry(FILTER_KEY_MY, translate("file.filter.my.value")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("file.filter.my"), ProjFileFilter.my.name(), myValues, true));
		
		
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
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
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
			tableEl.setEmptyTableSettings("file.list.empty.message", null, "o_icon_proj_file", "file.upload", "o_icon_upload", false);
		} else {
			tableEl.setEmptyTableSettings("file.list.empty.message", null, "o_icon_proj_file");
		}
	}
	
	public void reload(UserRequest ureq) {
		loadModel(ureq, false);
	}
	
	protected void loadModel(UserRequest ureq, boolean sort) {
		ProjFileSearchParams searchParams = createSearchParams();
		applyFilters(searchParams);
		List<ProjFile> files = projectService.getFiles(searchParams);
		List<ProjFileRow> rows = new ArrayList<>(files.size());
		
		for (ProjFile file : files) {
			ProjFileRow row = new ProjFileRow(file);
			VFSMetadata vfsMetadata = file.getVfsMetadata();
			
			String modifiedDate = formatter.formatDateRelative(vfsMetadata.getFileLastModified());
			String modifiedBy = userManager.getUserDisplayName(vfsMetadata.getFileLastModifiedBy());
			String modified = translate("date.by", modifiedDate, modifiedBy);
			row.setModified(modified);
			
			VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
			if (vfsItem instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
				boolean thumbnailAvailable = vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsMetadata);
				if (thumbnailAvailable) {
					VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(vfsLeaf, 650, 1000, false);
					if (thumbnail != null) {
						row.setThumbnailAvailable(true);
						VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
						String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
						row.setThumbnailUrl(thumbnailUrl);
					}
				}
			}
			
			forgeSelectLink(row, vfsMetadata);
			forgeToolsLink(row);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		if (sort) {
			sortTable();
		}
		tableEl.reset(true, true, true);
		
		onModelLoaded();
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
			if (ProjFileFilter.my.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_KEY_MY)) {
					searchParams.setCreators(List.of(getIdentity()));
				} else {
					searchParams.setCreators(null);
				}
			}
			
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
		}
	}
	
	private void sortTable() {
		if (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabRecently) {
			tableEl.sort(new SortKey(FileCols.lastModifiedDate.name(), false));
		} else if (tableEl.getSelectedFilterTab() == tabMy || tableEl.getSelectedFilterTab() == tabAll || tableEl.getSelectedFilterTab() == tabDeleted) {
			tableEl.sort( new SortKey(FileCols.displayName.name(), true));
		} else if (tableEl.getSelectedFilterTab() == tabNew) {
			tableEl.sort(new SortKey(FileCols.creationDate.name(), false));
		}
	}
	
	private void forgeSelectLink(ProjFileRow row, VFSMetadata vfsMetadata) {
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		link.setI18nKey(row.getDisplayName());
		link.setElementCssClass("o_link_plain");
		String iconCSS = CSSHelper.createFiletypeIconCssClassFor(vfsMetadata.getFilename());
		link.setIconLeftCSS("o_icon " + iconCSS);
		if (vfsMetadata.isLocked()) {
			link.setIconRightCSS("o_icon o_icon_locked");
		}
		link.setUserObject(row);
		row.setSelectLink(link);
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
		if (rowObject instanceof ProjFileRow) {
			ProjFileRow projRow = (ProjFileRow)rowObject;
			if (projRow.getSelectLink() != null) {
				cmps.add(projRow.getSelectLink().getComponent());
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
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (fileUploadCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (fileCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (fileEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete(ureq, (ProjFileRef)deleteConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
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
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(fileEditCtrl);
		removeAsListenerAndDispose(fileUploadCtrl);
		removeAsListenerAndDispose(fileCreateCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		fileEditCtrl = null;
		fileUploadCtrl = null;
		fileCreateCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink){
			doCreateFile(ureq);
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ProjFileRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doOpenOrDownload(ureq, row.getKey());
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq, false);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				doSelectFilterTab(((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq, true);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doUploadFile(ureq);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof ProjFileRow) {
				ProjFileRow row = (ProjFileRow)link.getUserObject();
				doOpenOrDownload(ureq, row.getKey());
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ProjFileRow) {
				doOpenTools(ureq, (ProjFileRow)link.getUserObject(), link);
			} 
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	protected void doUploadFile(UserRequest ureq) {
		if (guardModalController(fileUploadCtrl)) return;
		
		fileUploadCtrl = new ProjFileUploadController(ureq, getWindowControl(), project);
		listenTo(fileUploadCtrl);
		
		String title = translate("file.upload");
		cmc = new CloseableModalController(getWindowControl(), "close", fileUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	protected void doCreateFile(UserRequest ureq) {
		if (guardModalController(fileCreateCtrl)) return;
		
		fileCreateCtrl = new ProjFileCreateController(ureq, getWindowControl(), project);
		listenTo(fileCreateCtrl);
		
		String title = translate("file.create");
		cmc = new CloseableModalController(getWindowControl(), "close", fileCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenFile(UserRequest ureq, ProjFile file, VFSLeaf vfsLeaf, Mode mode) {
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(mode)
				.build(vfsLeaf);
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		
		if (Mode.EDIT == mode) {
			projectService.createActivityEdit(getIdentity(), file);
		} else {
			projectService.createActivityRead(getIdentity(), file.getArtefact());
		}
	}
	
	protected void doEditMetadata(UserRequest ureq, ProjFileRef fileRef) {
		if (guardModalController(fileEditCtrl)) return;
		
		ProjFile file = projectService.getFile(() -> fileRef.getKey());
		if (file == null) {
			loadModel(ureq, false);
			return;
		}
		
		fileEditCtrl = new ProjFileEditController(ureq, getWindowControl(), file, false);
		listenTo(fileEditCtrl);
		
		String title = translate("edit.metadata");
		cmc = new CloseableModalController(getWindowControl(), "close", fileEditCtrl.getInitialComponent(), true, title, true);
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
			VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
			if (vfsItem instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
				Roles roles = ureq.getUserSession().getRoles();
				
				if (secCallback.canEditFile(file, getIdentity()) && docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, vfsMetadata, Mode.EDIT)) {
					doOpenFile(ureq, file, vfsLeaf, Mode.EDIT);
				} else if (docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, vfsMetadata, Mode.VIEW)) {
					doOpenFile(ureq, file, vfsLeaf, Mode.VIEW);
				} else {
					doDownload(ureq, file.getArtefact(), vfsLeaf);
				}
			}
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, ProjFileRef fileRef) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		ProjFile file = projectService.getFile(fileRef);
		if (file == null || ProjectStatus.deleted == file.getArtefact().getStatus()) {
			return;
		}
		
		String message = translate("file.delete.confirmation.message", ProjectUIFactory.getDisplayName(file));
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"file.delete.confirmation.confirm", "file.delete.confirmation.button");
		deleteConfirmationCtrl.setUserObject(file);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", deleteConfirmationCtrl.getInitialComponent(),
				true, translate("file.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, ProjFileRef file) {
		projectService.deleteFileSoftly(getIdentity(), file);
		loadModel(ureq, false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doOpenTools(UserRequest ureq, ProjFileRow row, FormLink link) {
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
			this.row = row;
			
			mainVC = createVelocityContainer("file_tools");
			
			file = projectService.getFile(row);
			if (file != null) {
				VFSMetadata vfsMetadata = file.getVfsMetadata();
				VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
				if (vfsItem instanceof VFSLeaf) {
					vfsLeaf = (VFSLeaf)vfsItem;
					Roles roles = ureq.getUserSession().getRoles();
					
					if (secCallback.canEditFile(file, getIdentity()) && docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, vfsMetadata, Mode.EDIT)) {
						addLink("file.edit", CMD_EDIT, "o_icon o_icon_edit", true);
					} else if (docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, vfsMetadata, Mode.VIEW)) {
						addLink("file.view", CMD_VIEW, "o_icon o_icon_preview", true);
					}
					
					if (secCallback.canEditFile(file, getIdentity())) {
						addLink("edit.metadata", CMD_METADATA, "o_icon o_icon_edit", false);
					}
					
					addLink("download", CMD_DOWNLOAD, "o_icon o_icon_download", false);
					
					if (secCallback.canDeleteFile(file, getIdentity())) {
						addLink("delete", CMD_DELETE, "o_icon " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted), false);
					}
				}
			}
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, boolean newWindow) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			if (newWindow) {
				link.setNewWindow(true, true);
			}
			mainVC.put(name, link);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if (source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					doOpenFile(ureq, file, vfsLeaf, Mode.EDIT);
				} else if (CMD_VIEW.equals(cmd)) {
					doOpenFile(ureq, file, vfsLeaf, Mode.VIEW);
				} else if (CMD_METADATA.equals(cmd)) {
					doEditMetadata(ureq, row);
				} else if (CMD_DOWNLOAD.equals(cmd)) {
					doDownload(ureq, file.getArtefact(), vfsLeaf);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}

}
