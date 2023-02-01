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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
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
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
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
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteFilter;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjNoteDataModel.NoteCols;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.project.ui.event.OpenNoteEvent;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.olat.user.ui.UserDisplayNameCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
abstract class ProjNoteListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
	private static final String TAB_ID_MY = "My";
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_RECENTLY = "Recently";
	private static final String TAB_ID_NEW = "New";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String FILTER_KEY_MY = "my";
	private static final String CMD_SELECT = "select";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_OPEN_WINDOW = "open.window";
	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_DELETE = "delete";
	
	private final BreadcrumbedStackedPanel stackPanel;
	private FormLayoutContainer dummyCont;
	private FormLink createLink;
	private FlexiFiltersTab tabMy;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRecently;
	private FlexiFiltersTab tabNew;
	private FlexiFiltersTab tabDeleted;
	private FlexiTableElement tableEl;
	private ProjNoteDataModel dataModel;
	
	private CloseableModalController cmc;
	private ProjNoteEditController noteCreateCtrl;
	private ProjNoteController noteCtrl;
	private ProjConfirmationController deleteConfirmationCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	protected final ProjProject project;
	protected final ProjProjectSecurityCallback secCallback;
	private final Date lastVisitDate;
	private final MapperKey avatarMapperKey;
	private final Formatter formatter;
	
	@Autowired
	protected ProjectService projectService;
	@Autowired
	private UserManager userManager;

	public ProjNoteListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			String pageName, ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate,
			MapperKey avatarMapperKey) {
		super(ureq, wControl, pageName);
		this.stackPanel = stackPanel;
		this.project = project;
		this.secCallback = secCallback;
		this.lastVisitDate = lastVisitDate;
		this.avatarMapperKey = avatarMapperKey;
		this.formatter = Formatter.getInstance(getLocale());
	}
	
	protected abstract boolean isFullTable();
	
	protected abstract Integer getNumLastModified();

	protected abstract void onModelLoaded();
	
	protected abstract void doSelectNote(UserRequest ureq, ProjNoteRef noteRef, boolean edit);
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dummyCont = FormLayoutContainer.createCustomFormLayout("dummy", getTranslator(), velocity_root + "/empty.html");
		dummyCont.setRootForm(mainForm);
		formLayout.add(dummyCont);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NoteCols.id));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NoteCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NoteCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NoteCols.lastModifiedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NoteCols.lastModifiedBy, UserDisplayNameCellRenderer.get()));
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(NoteCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		dataModel = new ProjNoteDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(isFullTable());

		tableEl.setCssDelegate(ProjNoteListCssDelegate.DELEGATE);
		if (isFullTable()) {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		} else {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		}
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("note_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		if (isFullTable()) {
			initFilters();
			initFilterTabs(ureq);
		}
		doSelectFilterTab(null);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues myValues = new SelectionValues();
		myValues.add(SelectionValues.entry(FILTER_KEY_MY, translate("note.filter.my.value")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("note.filter.my"), ProjNoteFilter.my.name(), myValues, true));
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(ProjectStatus.active.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.active)));
		statusValues.add(SelectionValues.entry(ProjectStatus.deleted.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.deleted)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("status"), ProjNoteFilter.status.name(), statusValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabMy = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_MY,
				translate("note.list.tab.my"),
				TabSelectionBehavior.reloadData,
				List.of(
						FlexiTableFilterValue.valueOf(ProjNoteFilter.status, ProjectStatus.active.name()),
						FlexiTableFilterValue.valueOf(ProjNoteFilter.my, FILTER_KEY_MY)));
		tabs.add(tabMy);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, ProjectStatus.active.name())));
		tabs.add(tabAll);
		
		tabRecently = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RECENTLY,
				translate("tab.recently"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, ProjectStatus.active.name())));
		tabs.add(tabRecently);
		
		tabNew = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_NEW,
				translate("tab.new"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, ProjectStatus.active.name())));
		tabs.add(tabNew);
		
		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, ProjectStatus.deleted.name())));
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
		if (secCallback.canCreateNotes() && (tabDeleted == null || tabDeleted != tab)) {
			tableEl.setEmptyTableSettings("note.list.empty.message", null, FlexiTableElement.TABLE_EMPTY_ICON, "note.create", "o_icon_add", false);
		} else {
			tableEl.setEmptyTableSettings("note.list.empty.message", null, FlexiTableElement.TABLE_EMPTY_ICON);
		}
	}

	public void reload(UserRequest ureq) {
		loadModel(ureq, false);
	}
	
	protected void loadModel(UserRequest ureq, boolean sort) {
		ProjNoteSearchParams searchParams = createSearchParams();
		applyFilters(searchParams);
		List<ProjNoteInfo> noteInfos = projectService.getNoteInfos(searchParams);
		List<ProjNoteRow> rows = new ArrayList<>(noteInfos.size());
		
		for (ProjNoteInfo info : noteInfos) {
			ProjNoteRow row = new ProjNoteRow(info);
			
			row.setDisplayName(ProjectUIFactory.getDisplayName(getTranslator(), info.getNote()));
			String text = Formatter.truncate(info.getNote().getText(), 250);
			row.setText(text);
			
			String modifiedDate = formatter.formatDateRelative(info.getNote().getArtefact().getContentModifiedDate());
			String modifiedBy = userManager.getUserDisplayName(info.getNote().getArtefact().getContentModifiedBy().getKey());
			String modified = translate("date.by", modifiedDate, modifiedBy);
			row.setModified(modified);
			
			forgeUsersPortraits(ureq, row, info.getMembers());
			forgeSelectLink(row);
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

	private void forgeUsersPortraits(UserRequest ureq, ProjNoteRow row, Set<Identity> members) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(new ArrayList<>(members));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + row.getKey(), flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("members"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		row.setUserPortraits(usersPortraitCmp);
	}
	
	private ProjNoteSearchParams createSearchParams() {
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		searchParams.setNumLastModified(getNumLastModified());
		return searchParams;
	}

	private void applyFilters(ProjNoteSearchParams searchParams) {
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabNew) {
			searchParams.setCreatedAfter(lastVisitDate);
		} else {
			searchParams.setCreatedAfter(null);
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjNoteFilter.my.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_KEY_MY)) {
					searchParams.setCreators(List.of(getIdentity()));
				} else {
					searchParams.setCreators(null);
				}
			}
			
			if (ProjNoteFilter.status.name() == filter.getFilter()) {
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
			tableEl.sort(new SortKey(NoteCols.lastModifiedDate.name(), false));
		} else if (tableEl.getSelectedFilterTab() == tabMy || tableEl.getSelectedFilterTab() == tabAll || tableEl.getSelectedFilterTab() == tabDeleted) {
			tableEl.sort( new SortKey(NoteCols.displayName.name(), true));
		} else if (tableEl.getSelectedFilterTab() == tabNew) {
			tableEl.sort(new SortKey(NoteCols.creationDate.name(), false));
		}
	}
	
	private void forgeSelectLink(ProjNoteRow row) {
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		link.setI18nKey(row.getDisplayName());
		link.setElementCssClass("o_link_plain");
		link.setUserObject(row);
		row.setSelectLink(link);
	}
	
	private void forgeToolsLink(ProjNoteRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(3);
		if (rowObject instanceof ProjNoteRow) {
			ProjNoteRow projRow = (ProjNoteRow)rowObject;
			if (projRow.getUserPortraits() != null) {
				cmps.add(projRow.getUserPortraits());
			}
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
			} else {
				selectFilterTab(ureq, tabAll);
				if (ProjNote.TYPE.equals(type)) {
					Long key = entry.getOLATResourceable().getResourceableId();
					activate(ureq, key, false);
				}
			}
		} else if (state instanceof OpenNoteEvent) {
			OpenNoteEvent onEvent = (OpenNoteEvent)state;
			selectFilterTab(ureq, tabAll);
			activate(ureq, onEvent.getNote().getKey(), onEvent.isEdit());
		}
	}
	
	private void activate(UserRequest ureq, Long key, boolean edit) {
		ProjNoteRow row = dataModel.getObjectByKey(key);
		if (row != null) {
			int index = dataModel.getObjects().indexOf(row);
			if (index >= 1 && tableEl.getPageSize() > 1) {
				int page = index / tableEl.getPageSize();
				tableEl.setPage(page);
			}
			doSelectNote(ureq, () -> row.getKey(), edit);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (noteCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (noteCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				stackPanel.popController(noteCtrl);
			} else if (event instanceof OpenArtefactEvent) {
				fireEvent(ureq, event);
			}
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete(ureq, (ProjNoteRef)deleteConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			loadModel(ureq, false);
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
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(noteCreateCtrl);
		removeAsListenerAndDispose(noteCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		noteCreateCtrl = null;
		noteCtrl = null;
		cmc = null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter("select");
			if (StringHelper.containsNonWhitespace(key) && StringHelper.isLong(key)) {
				doSelectNote(ureq, () -> Long.valueOf(key), false);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink){
			doCreateNote(ureq);
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ProjNoteRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doSelectNote(ureq, row, false);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq, false);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				doSelectFilterTab(((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq, true);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreateNote(ureq);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof ProjNoteRow) {
				ProjNoteRow row = (ProjNoteRow)link.getUserObject();
				doSelectNote(ureq, row, false);
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ProjNoteRow) {
				doOpenTools(ureq, (ProjNoteRow)link.getUserObject(), link);
			} 
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected void doCreateNote(UserRequest ureq) {
		if (guardModalController(noteCreateCtrl)) return;
		
		ProjNote note = projectService.createNote(getIdentity(), project);
		noteCreateCtrl = new ProjNoteEditController(ureq, getWindowControl(), note, Set.of(getIdentity()), true, false);
		listenTo(noteCreateCtrl);
		
		String title = translate("note.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", noteCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doOpenNote(UserRequest ureq, ProjNoteRef noteRef, boolean edit) {
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setNotes(List.of(noteRef));
		List<ProjNoteInfo> noteInfos = projectService.getNoteInfos(searchParams);
		if (noteInfos.isEmpty()) {
			loadModel(ureq, false);
			return;
		}
		
		ProjNoteInfo noteInfo = noteInfos.get(0);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(ProjNote.TYPE, noteRef.getKey()), null);
		noteCtrl = new ProjNoteController(ureq, swControl, secCallback, noteInfo, edit, avatarMapperKey);
		listenTo(noteCtrl);
		String title = Formatter.truncate(ProjectUIFactory.getDisplayName(getTranslator(), noteInfo.getNote()), 50);
		stackPanel.pushController(title, noteCtrl);
	}
	
	private void doOpenWindow(ProjNoteRef row) {
		String url = ProjectBCFactory.getNoteUrl(project, row);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}
	
	private void doDownload(UserRequest ureq, ProjNoteRef noteRef) {
		ProjNote note = projectService.getNote(() -> noteRef.getKey());
		if (note == null) {
			loadModel(ureq, false);
			return;
		}
		
		StringMediaResource resource = ProjectUIFactory.createMediaResource(note);
		ureq.getDispatchResult().setResultingMediaResource(resource);
		
		projectService.createActivityDownload(getIdentity(), note.getArtefact());
	}
	
	private void doConfirmDelete(UserRequest ureq, ProjNoteRef noteRef) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		ProjNote note = projectService.getNote(noteRef);
		if (note == null || ProjectStatus.deleted == note.getArtefact().getStatus()) {
			return;
		}
		
		String message = translate("note.delete.confirmation.message", note.getTitle());
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"note.delete.confirmation.confirm", "note.delete.confirmation.button");
		deleteConfirmationCtrl.setUserObject(note);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", deleteConfirmationCtrl.getInitialComponent(),
				true, translate("note.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, ProjNoteRef note) {
		projectService.deleteNoteSoftly(getIdentity(), note);
		loadModel(ureq, false);
	}
	
	private void doOpenTools(UserRequest ureq, ProjNoteRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private static final class ProjNoteListCssDelegate extends DefaultFlexiTableCssDelegate {
		
		private static final ProjNoteListCssDelegate DELEGATE = new ProjNoteListCssDelegate();
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o_proj_note_list";
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return FlexiTableRendererType.custom == type
					? "o_proj_note_rows o_block_top o_proj_cards"
					: "o_proj_note_rows o_block_top";
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_proj_note_row";
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final ProjNoteRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ProjNoteRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("note_tools");
			
			ProjNote note = projectService.getNote(row);
			if (note != null) {
				boolean participant = row.getMembers().contains(getIdentity());
				if (secCallback.canEditNote(note, participant)) {
					addLink("note.edit", CMD_EDIT, "o_icon o_icon_edit", false);
				}
				
				addLink("open.in.new.window", CMD_OPEN_WINDOW, "o_icon o_icon_content_popup", true);
				addLink("download", CMD_DOWNLOAD, "o_icon o_icon_download", false);
				
				if (secCallback.canDeleteNote(note, participant)) {
					addLink("delete", CMD_DELETE, "o_icon " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted), false);
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
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					doSelectNote(ureq, row, true);
				} else if (CMD_OPEN_WINDOW.equals(cmd)) {
					doOpenWindow(row);
				} else if (CMD_DOWNLOAD.equals(cmd)) {
					doDownload(ureq, row);
				} else if(CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}

}
