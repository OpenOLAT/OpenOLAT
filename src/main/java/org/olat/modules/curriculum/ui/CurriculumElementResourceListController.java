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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumService.AddRepositoryEntry;
import org.olat.modules.curriculum.CurriculumService.RemovedRepositoryEntry;
import org.olat.modules.curriculum.model.RepositoryEntryInfos;
import org.olat.modules.curriculum.ui.CurriculumElementRepositoryTableModel.RepoCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.RepositoryEntryACColumnDescriptor;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.AuthorListConfiguration;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringEntryRowSelectionEvent;
import org.olat.repository.ui.author.GuestAccessRenderer;
import org.olat.repository.ui.author.RuntimeTypeRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
class CurriculumElementResourceListController extends FormBasicController implements FlexiTableCssDelegate {

	private static final String CMD_TOOLS = "rtools";
	private static final String CMD_REFERENCES = "references";
	
	private FormLink addResourceButton;
	private FormLink removeResourcesButton;
	private FlexiTableElement tableEl;
	private CurriculumElementRepositoryTableModel tableModel;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private AuthorListController repoSearchCtr;
	private ReferencesController referencesCtrl;
	private DialogBoxController confirmRemoveCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmInstantiateTemplateController confirmInstantiateCtrl;
	
	private int counter = 0;
	private boolean instantiateTemplate;
	private final boolean resourcesManaged;
	private final CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	private final CurriculumElementType curriculumElementType;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	
	public CurriculumElementResourceListController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "curriculum_element_resources");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		this.curriculumElementType = curriculumElement.getType();
		resourcesManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.resources);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.ac, new RepositoryEntryACColumnDescriptor()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.repoEntry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.externalId));// visible if managed
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.displayname, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.lifecycleLabel));// visible if lifecycle
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.lifecycleSoftKey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.lifecycleStart, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.lifecycleEnd, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.runtimeType, new RuntimeTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.access, new AccessRenderer(getLocale())));
		if(loginModule.isGuestLoginEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.guests, new GuestAccessRenderer(getLocale())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.resources));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(RepoCols.tools));

		tableModel = new CurriculumElementRepositoryTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setCssDelegate(this);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-element-resource-list-v2");
		
		// empty behavior
		String[] emptyI18nArgs = {
				curriculumElementType == null ? "" : curriculumElementType.getDisplayName()
			};
		// special rights for managers
		if(!resourcesManaged && secCallback.canManagerCurriculumElementResources(curriculumElement)
				&& (curriculumElementType == null || curriculumElementType.getMaxRepositoryEntryRelations() != 0)) {
			// 1) add
			addResourceButton = uifactory.addFormLink("add.resource", formLayout, Link.BUTTON);
			addResourceButton.setElementCssClass("o_sel_curriculum_element_add_resource");
			addResourceButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			// 2) remove
			removeResourcesButton = uifactory.addFormLink("remove.resources", formLayout, Link.BUTTON);
			tableEl.addBatchButton(removeResourcesButton);
			
			tableEl.setEmptyTableSettings("table.resources.empty", "table.resources.empty.hint", "o_CourseModule_icon",
					"add.resource", "o_icon_add", true, emptyI18nArgs);
		} else {			
			// default empty message with out create hint
			tableEl.setEmptyTableSettings("table.resources.empty", null, "o_CourseModule_icon",
					null, null, false, emptyI18nArgs);
		}
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		CurriculumElementRepositoryRow row = tableModel.getObject(pos);
		if(row == null || row.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| row.getEntryStatus() == RepositoryEntryStatusEnum.deleted) {
			return "o_entry_deleted";
		}
		if(row.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			return "o_entry_closed";
		}
		return null;
	}
	
	int loadModel() {
		List<RepositoryEntryInfos> entries = curriculumService.getRepositoryEntriesWithInfos(curriculumElement);
		List<CurriculumElementRepositoryRow> rows = entries.stream()
				.map(this::forgeRow)
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		return rows.size();
	}
	
	private CurriculumElementRepositoryRow forgeRow(RepositoryEntryInfos template) {
		CurriculumElementRepositoryRow row = new CurriculumElementRepositoryRow(template.repositoryEntry());
		
		if(template.numOfLectureBlocks() > 0) {
			FormLink resourcesLink = uifactory.addFormLink("resources." + (++counter), CMD_REFERENCES,
					String.valueOf(template.numOfLectureBlocks()), tableEl, Link.LINK | Link.NONTRANSLATED);
			resourcesLink.setUserObject(row);
			row.setResourcesLink(resourcesLink);
		}

		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator(), CMD_TOOLS);
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		
		return row;
	}
	
	void updateAddButtonAndEmptyMessages(int linkedTemplates) {
		int maxRelations = curriculumElementType == null ? -1 : curriculumElementType.getMaxRepositoryEntryRelations();
		
		instantiateTemplate = false;
		if(addResourceButton != null) {
			boolean canAddResource = (maxRelations < 0 || tableModel.getRowCount() < maxRelations);
			addResourceButton.setEnabled(canAddResource  && linkedTemplates == 0);
			
			String[] emptyI18nArgs = {
					curriculumElementType == null ? "" : curriculumElementType.getDisplayName()
				};
			if(canAddResource && linkedTemplates == 1) {
				tableEl.setEmptyTableSettings("table.resources.empty", "table.resources.instantiate.template.hint", "o_CourseModule_icon",
						"instantiate.template", "o_icon_add", true, emptyI18nArgs);
				instantiateTemplate = true;
			} else if(canAddResource  && linkedTemplates == 0) {
				tableEl.setEmptyTableSettings("table.resources.empty", "table.resources.empty.hint", "o_CourseModule_icon",
						"add.resource", "o_icon_add", true, emptyI18nArgs);
			} else {
				tableEl.setEmptyTableSettings("table.resources.empty", "table.resources.empty.hint", "o_CourseModule_icon",
						null, null, true, emptyI18nArgs);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<RepositoryEntry> rows = (List<RepositoryEntry>)confirmRemoveCtrl.getUserObject();
				doRemove(ureq, rows);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(repoSearchCtr == source) {
			if(event instanceof AuthoringEntryRowSelectionEvent se) {
				doAddRepositoryEntry(se.getRow());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmInstantiateCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(repoSearchCtr);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		referencesCtrl = null;
		repoSearchCtr = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addResourceButton == source) {
			doChooseResources(ureq);
		} else if(removeResourcesButton == source) {
			doConfirmRemoveResources(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("select".equals(se.getCommand())) {
					doSelectRepositoryEntry(ureq, tableModel.getObject(se.getIndex()));
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				if(instantiateTemplate) {
					doInstantiateTemplate(ureq);
				} else {
					doChooseResources(ureq);
				}
			}
		}else if(source instanceof FormLink link) {
			if(CMD_REFERENCES.equals(link.getCmd())
					&& link.getUserObject() instanceof CurriculumElementRepositoryRow row) {
				doOpenReferences(ureq, row, link);
			} else if(CMD_TOOLS.equals(link.getCmd())
					&& link.getUserObject() instanceof CurriculumElementRepositoryRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doChooseResources(UserRequest ureq) {
		if(guardModalController(repoSearchCtr)) return;
		
		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration tableConfig = AuthorListConfiguration.selectRessource("curriculum-course-v1", "CourseModule");
		tableConfig.setSelectRepositoryEntry(SelectionMode.single);
		tableConfig.setBatchSelect(true);
		tableConfig.setImportRessources(false);
		tableConfig.setCreateRessources(false);
		tableConfig.setAllowedRuntimeTypes(List.of(RepositoryEntryRuntimeType.standalone, RepositoryEntryRuntimeType.curricular));
		
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.addResourceTypes("CourseModule");
		searchParams.setRuntimeTypes(tableConfig.getAllowedRuntimeTypes());
		repoSearchCtr = new AuthorListController(ureq, getWindowControl(), searchParams, tableConfig);
		listenTo(repoSearchCtr);
		repoSearchCtr.selectFilterTab(ureq, repoSearchCtr.getMyCoursesTab());
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent(),
				true, translate("add.resource"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddRepositoryEntry(RepositoryEntryRef entryRef) {
		RepositoryEntry entry = repositoryService.loadBy(entryRef);
		if(entry != null) {
			boolean hasRepositoryEntries = curriculumService.hasRepositoryEntries(curriculumElement);
			boolean moveLectureBlocks = !hasRepositoryEntries;
			AddRepositoryEntry infos = curriculumService.addRepositoryEntry(curriculumElement, entry, moveLectureBlocks);
			if(infos.lectureBlockMoved()) {
				showInfo("info.repositoryentry.added.lectureblock.moved");
			} else {
				showInfo("info.repositoryentry.added");
			}
		}
	}
	
	private void doInstantiateTemplate(UserRequest ureq) {
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(curriculumElement);
		if(templates.size() == 1) {
			confirmInstantiateCtrl = new ConfirmInstantiateTemplateController(ureq, getWindowControl(),
					curriculumElement, templates.get(0));
			listenTo(confirmInstantiateCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmInstantiateCtrl.getInitialComponent(),
					true, translate("instantiate.template.title"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmRemoveResource(UserRequest ureq, CurriculumElementRepositoryRow row) {
		String title = translate("confirm.remove.resource.title");
		confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("confirm.remove.resource.text", ""), confirmRemoveCtrl);
		confirmRemoveCtrl.setUserObject(List.of(row.getRepositoryEntry()));
	}
	
	private void doConfirmRemoveResources(UserRequest ureq) {
		Set<Integer> selectedRows = tableEl.getMultiSelectedIndex();
		if(selectedRows.isEmpty()) {
			showWarning("warning.atleastone.resource");
		} else {
			List<RepositoryEntry> rows = new ArrayList<>(selectedRows.size());
			for(Integer selectedRow:selectedRows) {
				CurriculumElementRepositoryRow row = tableModel.getObject(selectedRow.intValue());
				if(row != null) {
					rows.add(row.getRepositoryEntry());
				}
			}
			String title = translate("confirm.remove.resource.title");
			confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("confirm.remove.resource.text", ""), confirmRemoveCtrl);
			confirmRemoveCtrl.setUserObject(rows);
		}
	}
	
	private void doRemove(UserRequest ureq, List<RepositoryEntry> resourcesToRemove) {
		int lectureBlocksRemoved = 0;
		for(RepositoryEntry resourceToRemove:resourcesToRemove) {
			if(resourceToRemove.getRuntimeType() == RepositoryEntryRuntimeType.template) {
				curriculumService.removeRepositoryTemplate(curriculumElement, resourceToRemove);
			} else {
				RemovedRepositoryEntry infos = curriculumService.removeRepositoryEntry(curriculumElement, resourceToRemove);
				lectureBlocksRemoved += infos.lectureBlockMoved();
			}
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		String i18nKey;
		if(lectureBlocksRemoved == 0) {
			i18nKey = resourcesToRemove.size() == 1 ? "info.repositoryentry.removed" : "info.repositoryentries.removed";
		} else {
			i18nKey = resourcesToRemove.size() == 1 ? "info.repositoryentry.removed.lectureblock.moved" : "info.repositoryentries.removed.lectureblock.moved";
		}
		showInfo(i18nKey, Integer.toString(resourcesToRemove.size()));
	}
	
	private void doSelectRepositoryEntry(UserRequest ureq, CurriculumElementRepositoryRow entry) {
		if(entry == null) return;
		
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenReferences(UserRequest ureq, CurriculumElementRepositoryRow row, FormLink link) {
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		RepositoryEntry entry = row.getRepositoryEntry();
		referencesCtrl = new ReferencesController(ureq, getWindowControl(), getTranslator(), entry);
		listenTo(referencesCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementRepositoryRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private Link removeLink;
		private CurriculumElementRepositoryRow row;
		
		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumElementRepositoryRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			removeLink = LinkFactory.createLink("remove", "remove", getTranslator(), mainVC, this, Link.LINK);
			mainVC.put("remove", removeLink);
			mainVC.contextPut("links", List.of("remove"));

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(removeLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doConfirmRemoveResource(ureq, row);
			}
		}
	}
}
