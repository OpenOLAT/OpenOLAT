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
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumService.RemovedRepositoryEntry;
import org.olat.modules.curriculum.ui.CurriculumElementRepositoryTableModel.RepoCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.AuthorListConfiguration;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringEntryRowSelectionEvent;
import org.olat.repository.ui.author.RuntimeTypeRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class CurriculumElementTemplateListController extends FormBasicController implements FlexiTableCssDelegate {

	private static final String CMD_INSTANTIATE = "instantiate";
	private static final String CMD_TOOLS = "ttools";
	
	private FormLink addTemplateButton;
	private FormLink removeTemplatesButton;
	private FlexiTableElement tableEl;
	private CurriculumElementRepositoryTableModel tableModel;
	
	private int counter = 0;
	private final CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	private final CurriculumElementType curriculumElementType;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private DialogBoxController confirmRemoveCtrl;
	private AuthorListController templateSearchCtr;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmInstantiateTemplateController confirmInstantiateCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	
	public CurriculumElementTemplateListController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "curriculum_element_templates");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		curriculumElementType = curriculumElement.getType();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("curriculum.templates.hint", null);
		setFormInfoHelp("manual_user/area_modules/Curriculum_Management/");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.repoEntry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.displayname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.externalId));// visible if managed
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.runtimeType, new RuntimeTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.access, new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.instantiateTemplate));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(RepoCols.tools));

		tableModel = new CurriculumElementRepositoryTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setCssDelegate(this);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-element-templates-list");
				
		// special rights for managers
		if(secCallback.canManagerCurriculumElementResources(curriculumElement)
				&& (curriculumElementType != null && curriculumElementType.getMaxRepositoryEntryRelations() == 1)) {
			// 1) add
			addTemplateButton = uifactory.addFormLink("add.template", formLayout, Link.BUTTON);
			addTemplateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			// 2) remove
			removeTemplatesButton = uifactory.addFormLink("remove.resources", formLayout, Link.BUTTON);
			tableEl.addBatchButton(removeTemplatesButton);
			// empty behavior
			String[] emptyI18nArgs = {
					curriculumElementType.getDisplayName()
				};
			tableEl.setEmptyTableSettings("table.templates.empty", "table.templates.empty.hint",
					"o_CourseModule_icon", "add.template", "o_icon_add", true, emptyI18nArgs);
		} else {			
			// default empty message with out create hint
			tableEl.setEmptyTableSettings("table.templates.empty", null, "o_CourseModule_icon");
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
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(curriculumElement);
		List<CurriculumElementRepositoryRow> rows = templates.stream()
				.map(template -> forgeRow(template))
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		return tableModel.getRowCount();
	}
	
	private CurriculumElementRepositoryRow forgeRow(RepositoryEntry template) {
		CurriculumElementRepositoryRow row = new CurriculumElementRepositoryRow(template);
		String id = String.valueOf(++counter);
		FormLink instantiateLink = uifactory.addFormLink("instantiate.".concat(id), CMD_INSTANTIATE, "instantiate.template", tableEl, Link.LINK);
		instantiateLink.setUserObject(row);
		row.setInstantiateLink(instantiateLink);
		
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator(), CMD_TOOLS);
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		
		return row;
	}
	
	void updateAddButtonAndEmptyMessages(int linkedCourses) {
		String[] emptyI18nArgs = {
				curriculumElementType == null ? "" : curriculumElementType.getDisplayName()
			};
		if(linkedCourses >= 1) {
			tableEl.setEmptyTableSettings("table.templates.empty", "table.templates.empty.linked.hint",
					"o_CourseModule_icon", null, null, true, emptyI18nArgs);
		} else {
			tableEl.setEmptyTableSettings("table.templates.empty", "table.templates.empty.hint",
					"o_CourseModule_icon", "add.template", "o_icon_add", true, emptyI18nArgs);
		}
		
		if(addTemplateButton != null) {
			addTemplateButton.setEnabled(tableModel.getRowCount() == 0 && linkedCourses == 0);
		}
		
		boolean instantiateEnabled = secCallback.canNewCurriculumElement() && linkedCourses == 0;
		List<CurriculumElementRepositoryRow> rows = this.tableModel.getObjects();
		for(CurriculumElementRepositoryRow row:rows) {
			if(row.getInstantiateLink() != null) {
				row.getInstantiateLink().setVisible(instantiateEnabled);
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
		} else if(templateSearchCtr == source) {
			if(event instanceof AuthoringEntryRowSelectionEvent se) {
				doAddTemplate(se.getRow());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmInstantiateCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
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
		removeAsListenerAndDispose(templateSearchCtr);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		templateSearchCtr = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTemplateButton == source) {
			doChooseTemplate(ureq);
		} else if(removeTemplatesButton == source) {
			doConfirmRemoveTemplates(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("select".equals(se.getCommand())) {
					doSelectRepositoryEntry(ureq, tableModel.getObject(se.getIndex()));
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doChooseTemplate(ureq);
			}
		} else if(source instanceof FormLink link) {
			if(CMD_INSTANTIATE.equals(link.getCmd())
					&& link.getUserObject() instanceof CurriculumElementRepositoryRow row) {
				doInstantiateTemplate(ureq, row);
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
	
	private void doChooseTemplate(UserRequest ureq) {
		if(guardModalController(templateSearchCtr)) return;
		
		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration tableConfig = AuthorListConfiguration.selectRessource("curriculum-template-v1", "CourseModule");
		tableConfig.setSelectRepositoryEntry(SelectionMode.single);
		tableConfig.setBatchSelect(true);
		tableConfig.setImportRessources(false);
		tableConfig.setCreateRessources(false);
		tableConfig.setAllowedRuntimeTypes(List.of(RepositoryEntryRuntimeType.template));
		
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.addResourceTypes("CourseModule");
		searchParams.setRuntimeTypes(List.of(RepositoryEntryRuntimeType.template));
		templateSearchCtr = new AuthorListController(ureq, getWindowControl(), searchParams, tableConfig);
		listenTo(templateSearchCtr);
		templateSearchCtr.selectFilterTab(ureq, templateSearchCtr.getMyCoursesTab());
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), templateSearchCtr.getInitialComponent(),
				true, translate("add.template"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddTemplate(RepositoryEntryRef entryRef) {
		RepositoryEntry entry = repositoryService.loadBy(entryRef);
		if(entry != null && entry.getRuntimeType() == RepositoryEntryRuntimeType.template) {
			curriculumService.addRepositoryTemplate(curriculumElement, entry);
			showInfo("info.repositorytemplate.added");
		}
	}
	
	private void doConfirmRemoveTemplate(UserRequest ureq, CurriculumElementRepositoryRow row) {
		String title = translate("confirm.remove.resource.title");
		confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("confirm.remove.resource.text", ""), confirmRemoveCtrl);
		confirmRemoveCtrl.setUserObject(List.of(row.getRepositoryEntry()));
	}
	
	private void doConfirmRemoveTemplates(UserRequest ureq) {
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
			i18nKey = resourcesToRemove.size() == 1 ? "info.repositorytemplate.removed" : "info.repositorytemplates.removed";
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
	
	private void doInstantiateTemplate(UserRequest ureq, CurriculumElementRepositoryRow row) {
		RepositoryEntry template = row.getRepositoryEntry();
		confirmInstantiateCtrl = new ConfirmInstantiateTemplateController(ureq, getWindowControl(),
				curriculumElement, template);
		listenTo(confirmInstantiateCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmInstantiateCtrl.getInitialComponent(),
				true, translate("instantiate.template.title"));
		listenTo(cmc);
		cmc.activate();
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
				doConfirmRemoveTemplate(ureq, row);
			}
		}
	}
}
