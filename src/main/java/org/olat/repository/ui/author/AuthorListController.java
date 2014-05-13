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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.TransientRepositoryEntryRef;
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

	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	
	
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
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public AuthorListController(UserRequest ureq, WindowControl wControl, String i18nName,
			SearchAuthorRepositoryEntryViewParams searchParams) {
		super(ureq, wControl, "entries");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));

		this.searchParams = searchParams;
		
		importLink = LinkFactory.createLink("cmd.import.ressource", getTranslator(), this);
		importLink.setDomReplacementWrapperRequired(false);
		
		Set<String> types = repositoryHandlerFactory.getSupportedTypes();

		createDropdown = new Dropdown("cmd.create.ressource", "cmd.create.ressource", false, getTranslator());
		for(String type:types) {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
			if(handler != null && handler.isCreate()) {
				addCreateLink(type, createDropdown);
			}
		}
		
		dataSource = new AuthoringEntryDataSource(searchParams, this);

		initForm(ureq);

		stackPanel = new TooledStackedPanel(i18nName, getTranslator(), this);
		stackPanel.setShowCloseLink(false);
		stackPanel.pushController(translate(i18nName), this);
		stackPanel.addTool(importLink);
		stackPanel.addTool(createDropdown);
	}
	
	private void addCreateLink(String type, Dropdown dropdown) {
		Link createLink = LinkFactory.createLink(type, getTranslator(), this);
		createLink.setUserObject(type);
		dropdown.addComponent(createLink);
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//search form
		searchCtrl = new AuthorSearchController(ureq, getWindowControl(), true, mainForm);
		listenTo(searchCtrl);
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.ac.i18nKey(), Cols.ac.ordinal(), new ACRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type.i18nKey(), Cols.type.ordinal(), new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal()));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal(), "select", renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.author.i18nKey(), Cols.author.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.authors.i18nKey(), Cols.authors.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.access.i18nKey(), Cols.access.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,  new AccessRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage.i18nKey(), Cols.lastUsage.ordinal(), false, null));
		
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("details", -1, "details",
				new StaticFlexiCellRenderer("" /* translate("details")*/, "details", "o_icon-lg o_icon_details")));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit", -1, "edit",
				new StaticFlexiCellRenderer("" /* translate("edit") */, "edit", "o_icon-lg o_icon_edit")));
		
		model = new AuthoringEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(ureq, getWindowControl(), "table", model, 20, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(true);
		tableEl.setExtendedSearch(searchCtrl, false);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setFilters(null, getFilters());
		tableEl.setMultiSelect(true);
		
		addOwnersButton = uifactory.addFormLink("tools.add.owners", formLayout, Link.BUTTON);
	}
	
	private List<FlexiTableFilter> getFilters() {
		Set<String> supportedTypes = repositoryHandlerFactory.getSupportedTypes();
		List<FlexiTableFilter> resources = new ArrayList<>(supportedTypes.size() + 1);
		for(String type:supportedTypes) {
			resources.add(new FlexiTableFilter(translate(type), type));
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
		} else if(source instanceof Link && ((Link)source).getUserObject() instanceof String) {
			String type = (String)((Link)source).getUserObject();
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
			if(handler != null) {
				doCreate(ureq, type, handler);
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
				doOpenDetails(ureq, createCtrl.getAddedEntry());
				cleanUp();
			} else if(CreateRepositoryEntryController.CREATION_WIZARD.equals(event)) {
				doPostCreateWizard(ureq, createCtrl.getAddedEntry(), createCtrl.getHandler());
			} else {
				cleanUp();
			}
		}  else if(importCtrl == source) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				doOpenDetails(ureq, importCtrl.getImportedEntry());
				cleanUp();
			} else {
				cleanUp();
			}
		} else if(wizardCtrl == source) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
				RepositoryEntry newEntry = (RepositoryEntry)wizardCtrl.getRunContext().get("authoringNewEntry");
				cleanUp();
				doOpenDetails(ureq, newEntry);
			}
		} else if(searchCtrl == source) {
			if(event instanceof SearchEvent) {
				SearchEvent se = (SearchEvent)event;
				doSearch(se);
			}
		} else if(detailsCtrl == source) {
			if(event instanceof OpenEvent) {
				OpenEvent oe = (OpenEvent)event;
				RepositoryEntryRef repoEntryKey = oe.getRepositoryEntry();
				doOpenDetails(ureq, repoEntryKey);
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
	
	private AuthoringEntryDetailsController doOpenDetails(UserRequest ureq, RepositoryEntryRef entry) {
		RepositoryEntryAuthorView view = repositoryService.loadAuthorView(getIdentity(), entry);
		String fullnameAuthor = "";
		AuthoringEntryRow row = new AuthoringEntryRow(view, fullnameAuthor);
		return doOpenDetails(ureq, row);
	}
	
	private AuthoringEntryDetailsController doOpenDetails(UserRequest ureq, AuthoringEntryRow row) {
		stackPanel.popUpToRootController(ureq);

		removeAsListenerAndDispose(detailsCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		detailsCtrl = new AuthoringEntryDetailsController(ureq, getWindowControl(), stackPanel, row);
		listenTo(detailsCtrl);
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
	
	private void doCreate(UserRequest ureq, String type, RepositoryHandler handler) {
		if(createCtrl != null) return;

		removeAsListenerAndDispose(createCtrl);
		createCtrl = new CreateRepositoryEntryController(ureq, getWindowControl(), type, handler);
		listenTo(createCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate(handler.getCreateLabelI18nKey());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createCtrl.getInitialComponent(),
				true, title);
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
		if(se.getType() != null) {
			searchParams.setResourceTypes(Collections.singletonList(se.getType()));
		} else {
			searchParams.setResourceTypes(null);
		}

		searchParams.setIdAndRefs(se.getId());
		searchParams.setAuthor(se.getAuthor());
		searchParams.setDisplayname(se.getDisplayname());
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
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void launchResourceEditor(UserRequest ureq, AuthoringEntryRow row) {
		String businessPath = "[RepositoryEntry:" + row.getKey() + "][Editor:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	@Override
	public void forgeMarkLink(AuthoringEntryRow row) {
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
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