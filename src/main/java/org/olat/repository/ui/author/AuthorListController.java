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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.RepositoryHandlerFactory.OrderedRepositoryHandler;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.model.TransientRepositoryEntryRef;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.AuthoringEntryDataModel.Cols;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorListController extends FormBasicController implements Activateable2, AuthoringEntryDataSourceUIFactory {

	private final String i18nName;
	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	
	private boolean withSearch;;
	
	private AuthoringEntryDataModel model;
	private AuthoringEntryDataSource dataSource;
	private final SearchAuthorRepositoryEntryViewParams searchParams;

	private CloseableModalController cmc;
	private StepsMainRunController wizardCtrl;
	private AuthorSearchController searchCtrl;
	private UserSearchController userSearchCtr;
	private AuthoringEntryDetailsController detailsCtrl;
	private ImportRepositoryEntryController importCtrl;
	private CreateRepositoryEntryController createCtrl;

	private Link importLink;
	private Dropdown createDropdown;
	private FormLink addOwnersButton;
	//only used as marker for dirty, model cannot load specific rows
	private final List<Integer> dirtyRows = new ArrayList<>();
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public AuthorListController(UserRequest ureq, WindowControl wControl, String i18nName,
			SearchAuthorRepositoryEntryViewParams searchParams, boolean withSearch) {
		super(ureq, wControl, "entries");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));

		this.i18nName = i18nName;
		this.withSearch = withSearch;
		this.searchParams = searchParams;

		dataSource = new AuthoringEntryDataSource(searchParams, this);
		initForm(ureq);

		stackPanel = new TooledStackedPanel(i18nName, getTranslator(), this);
		stackPanel.pushController(translate(i18nName), this);
		if(!withSearch) {
			importLink = LinkFactory.createLink("cmd.import.ressource", getTranslator(), this);
			importLink.setDomReplacementWrapperRequired(false);
			importLink.setIconLeftCSS("o_icon o_icon_import");
			stackPanel.addTool(importLink, Align.left);
			
			Collection<String> types = repositoryHandlerFactory.getSupportedTypes();
			createDropdown = new Dropdown("cmd.create.ressource", "cmd.create.ressource", false, getTranslator());
			createDropdown.setElementCssClass("o_sel_author_create");
			createDropdown.setIconCSS("o_icon o_icon_add");
			for(String type:types) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
				if(handler != null && handler.isCreate()) {
					addCreateLink(handler, createDropdown);
				}
			}
			stackPanel.addTool(createDropdown, Align.left);
		}
	}
	
	@Override
	public Translator getTranslator() {
		return super.getTranslator();
	}

	private void addCreateLink(RepositoryHandler handler, Dropdown dropdown) {
		Link createLink = LinkFactory.createLink(handler.getSupportedType(), getTranslator(), this);
		createLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(handler.getSupportedType()));
		createLink.setElementCssClass("o_sel_author_create-" + handler.getSupportedType());
		createLink.setUserObject(handler);
		dropdown.addComponent(createLink);
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//search form
		if(withSearch) {
			setFormDescription("table.search.author.desc");
			searchCtrl = new AuthorSearchController(ureq, getWindowControl(), true, mainForm);
			searchCtrl.setEnabled(false);
			listenTo(searchCtrl);
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), true, OrderBy.key.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.mark.i18nKey(), Cols.mark.ordinal(), true, OrderBy.favorit.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.type.i18nKey(), Cols.type.ordinal(), true, OrderBy.type.name(),
				FlexiColumnModel.ALIGNMENT_LEFT, new TypeRenderer()));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal(), "select",
				true, OrderBy.displayname.name(), renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.authors.i18nKey(), Cols.authors.ordinal(),
				true, OrderBy.authors.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal(),
				true, OrderBy.lifecycleLabel.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal(),
				true, OrderBy.lifecycleSoftkey.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal(),
				true, OrderBy.lifecycleStart.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal(),
				true, OrderBy.lifecycleEnd.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.author.i18nKey(), Cols.author.ordinal(),
				true, OrderBy.author.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.access.i18nKey(), Cols.access.ordinal(),
				true, OrderBy.access.name(), FlexiColumnModel.ALIGNMENT_LEFT, new AccessRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.ac.i18nKey(), Cols.ac.ordinal(),
				true, OrderBy.ac.name(), FlexiColumnModel.ALIGNMENT_LEFT, new ACRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(),
				true, OrderBy.creationDate.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage.i18nKey(), Cols.lastUsage.ordinal(),
				true, OrderBy.lastUsage.name()));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("details", -1, "details",
				new StaticFlexiCellRenderer("" /* translate("details")*/, "details", "o_icon-lg o_icon_details")));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit", Cols.editionSupported.ordinal(), "edit",
				new BooleanCellRenderer(new StaticFlexiCellRenderer("" /* translate("edit") */, "edit", "o_icon-lg o_icon_edit"), null)));
		
		model = new AuthoringEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(withSearch);
		tableEl.setExportEnabled(true);
		tableEl.setExtendedSearch(searchCtrl);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setSortSettings(new FlexiTableSortOptions(true, new SortKey(OrderBy.displayname.name(), true)));
		tableEl.setAndLoadPersistedPreferences(ureq, "authors-list-" + i18nName);
		if(!withSearch) {
			tableEl.reloadData();
			tableEl.setFilters(null, getFilters());
		}
		
		addOwnersButton = uifactory.addFormLink("tools.add.owners", formLayout, Link.BUTTON);
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<OrderedRepositoryHandler> supportedHandlers = repositoryHandlerFactory.getOrderRepositoryHandlers();
		List<FlexiTableFilter> resources = new ArrayList<>(supportedHandlers.size() + 1);
		for(OrderedRepositoryHandler handler:supportedHandlers) {
			String type = handler.getHandler().getSupportedType();
			String inconLeftCss = RepositoyUIFactory.getIconCssClass(type);
			resources.add(new FlexiTableFilter(translate(type), type, inconLeftCss));
		}
		return resources;
	}
	
	public TooledStackedPanel getStackPanel() {
		return stackPanel;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String segment = entry.getOLATResourceable().getResourceableTypeName();
		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		if("RepositoryEntry".equals(segment)) {
			Long repoEntryKey = entry.getOLATResourceable().getResourceableId();
			RepositoryEntryRef repoEntry = new TransientRepositoryEntryRef(repoEntryKey);
			doOpenDetails(ureq, repoEntry).activate(ureq, subEntries, entry.getTransientState());
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(importLink == source) {
			doImport(ureq);
		} else if(source instanceof Link && ((Link)source).getUserObject() instanceof RepositoryHandler) {
			RepositoryHandler handler = (RepositoryHandler)((Link)source).getUserObject();
			if(handler != null) {
				doCreate(ureq, handler);
			}
		} else if(event instanceof PopEvent) {
			PopEvent pop = (PopEvent)event;
			if(pop.getController() == detailsCtrl) {
				reloadDirtyRows();
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(createCtrl == source) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				doOpenDetailsSettings(ureq, createCtrl.getAddedEntry());
				cleanUp();
			} else if(CreateRepositoryEntryController.CREATION_WIZARD.equals(event)) {
				doPostCreateWizard(ureq, createCtrl.getAddedEntry(), createCtrl.getHandler());
			} else {
				cleanUp();
			}
		}  else if(importCtrl == source) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				doOpenDetailsSettings(ureq, importCtrl.getImportedEntry());
				cleanUp();
			} else {
				cleanUp();
			}
		} else if(wizardCtrl == source) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
				RepositoryEntry newEntry = (RepositoryEntry)wizardCtrl.getRunContext().get("authoringNewEntry");
				cleanUp();
				doOpenDetailsSettings(ureq, newEntry);
			}
		} else if(searchCtrl == source) {
			if(event instanceof SearchEvent) {
				SearchEvent se = (SearchEvent)event;
				doSearch(se);
			} else if(event == Event.CANCELLED_EVENT) {
				searchParams.setResourceTypes(null);
				searchParams.setIdAndRefs(null);
				searchParams.setAuthor(null);
				searchParams.setDisplayname(null);
				searchParams.setDescription(null);
			}
		} else if(detailsCtrl == source) {
			if(event instanceof OpenEvent) {
				OpenEvent oe = (OpenEvent)event;
				RepositoryEntryRef repoEntryKey = oe.getRepositoryEntry();
				doOpenDetails(ureq, repoEntryKey);
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				dirtyRows.add(new Integer(-1));
			} else if(event instanceof EntryChangedEvent) {
				EntryChangedEvent ce = (EntryChangedEvent)event;
				if(ce.getChange().equals(Change.deleted)) {
					stackPanel.popUpToRootController(ureq);
					dirtyRows.add(new Integer(-1));
					reloadDirtyRows();
				}
			}
		} else if(userSearchCtr == source) {
			@SuppressWarnings("unchecked")
			List<AuthoringEntryRow> rows = (List<AuthoringEntryRow>)userSearchCtr.getUserObject();
			if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent mice = (MultiIdentityChosenEvent) event; 
				doAddOwners(mice.getChosenIdentities(), rows);
			} else if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent  sice = (SingleIdentityChosenEvent) event;
				List<Identity> futureOwners = Collections.singletonList(sice.getChosenIdentity());
				doAddOwners(futureOwners, rows);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(userSearchCtr);
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(importCtrl);
		removeAsListenerAndDispose(wizardCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtr = null;
		createCtrl = null;
		importCtrl = null;
		wizardCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addOwnersButton == source) {
			Set<Integer> selections = tableEl.getMultiSelectedIndex();
			if(selections.isEmpty()) {
				//get rows
			} else {
				List<AuthoringEntryRow> rows = new ArrayList<>();
				for(Integer i:selections) {
					AuthoringEntryRow row = model.getObject(i.intValue());
					if(row != null) {
						rows.add(row);
					}
				}
				doAddOwners(ureq, rows);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("mark".equals(cmd)) {
				AuthoringEntryRow row = (AuthoringEntryRow)link.getUserObject();
				boolean marked = doMark(row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AuthoringEntryRow row = model.getObject(se.getIndex());
				if("details".equals(cmd)) {
					doOpenDetails(ureq, row);
				} else if("edit".equals(cmd)) {
					launchResourceEditor(ureq, row);
				} else if("select".equals(cmd)) {
					launch(ureq, row);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc) {
		//do not update the 
	}
	
	protected void reloadDirtyRows() {
		if(dirtyRows.size() > 0) {
			tableEl.reloadData();
			dirtyRows.clear();
		}
	}
	
	private AuthoringEntryDetailsController doOpenDetails(UserRequest ureq, RepositoryEntryRef ref) {
		stackPanel.popUpToRootController(ureq);

		removeAsListenerAndDispose(detailsCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", ref.getKey());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		detailsCtrl = new AuthoringEntryDetailsController(ureq, getWindowControl(), stackPanel, ref);
		listenTo(detailsCtrl);
		return detailsCtrl;
	}
	
	private AuthoringEntryDetailsController doOpenDetailsSettings(UserRequest ureq, RepositoryEntryRef entry) {
		detailsCtrl = doOpenDetails(ureq, entry);
		
		ContextEntry editEntry = BusinessControlFactory.getInstance().createContextEntry(AuthoringEntryDetailsController.EDIT_SETTINGS_ORES);
		List<ContextEntry> entries = Collections.singletonList(editEntry);
		detailsCtrl.activate(ureq, entries, null);
		return detailsCtrl;
	}
	
	private void doImport(UserRequest ureq) {
		if(importCtrl != null) return;

		removeAsListenerAndDispose(importCtrl);
		importCtrl = new ImportRepositoryEntryController(ureq, getWindowControl());
		listenTo(importCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate("cmd.import.ressource");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreate(UserRequest ureq, RepositoryHandler handler) {
		if(createCtrl != null) return;

		removeAsListenerAndDispose(createCtrl);
		createCtrl = new CreateRepositoryEntryController(ureq, getWindowControl(), handler);
		listenTo(createCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate(handler.getCreateLabelI18nKey());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createCtrl.getInitialComponent(),
				true, title);
		cmc.setCustomWindowCSS("o_sel_author_create_popup");
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doPostCreateWizard(UserRequest ureq, RepositoryEntry newEntry, RepositoryHandler handler) {
		if(wizardCtrl != null) return;
		
		cleanUp();
		wizardCtrl = handler.createWizardController(newEntry, ureq, getWindowControl());
		wizardCtrl.getRunContext().put("authoringNewEntry", newEntry);
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private void doSearch(SearchEvent se) {
		if(StringHelper.containsNonWhitespace(se.getType())) {
			searchParams.setResourceTypes(Collections.singletonList(se.getType()));
		} else {
			searchParams.setResourceTypes(null);
		}

		searchParams.setIdAndRefs(se.getId());
		searchParams.setAuthor(se.getAuthor());
		searchParams.setDisplayname(se.getDisplayname());
		searchParams.setDescription(se.getDescription());
		tableEl.reset();
	}
	
	private void doAddOwners(UserRequest ureq, List<AuthoringEntryRow> rows) {
		if(userSearchCtr != null) return;
		
		removeAsListenerAndDispose(userSearchCtr);
		userSearchCtr = new UserSearchController(ureq, getWindowControl(), false, true, UserSearchController.ACTION_KEY_CHOOSE_FINISH);
		userSearchCtr.setUserObject(rows);
		listenTo(userSearchCtr);
		
		String title = translate("tools.add.owners");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtr.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddOwners(List<Identity> futureOwners, List<AuthoringEntryRow> rows) {
		for(AuthoringEntryRow row:rows) {
			RepositoryEntry re = repositoryService.loadByKey(row.getKey());
			for(Identity futureOwner:futureOwners) {
				repositoryService.addRole(futureOwner, re, GroupRoles.owner.name());
			}
		}
	}
	
	private void launch(UserRequest ureq, AuthoringEntryRow row) {
		try {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(row.getResourceType());
			if(handler != null && handler.supportsLaunch()) {
				String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} else {
				doOpenDetails(ureq, row);
			}
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}
	
	private void launchResourceEditor(UserRequest ureq, AuthoringEntryRow row) {
		try {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}

	@Override
	public void forgeMarkLink(AuthoringEntryRow row) {
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
	}
	
	protected boolean doMark(AuthoringEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}
}