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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.ExportCurriculumMediaResource;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerDataModel.CurriculumCols;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;
import org.olat.modules.lecture.LectureModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumListManagerController extends FormBasicController implements Activateable2, TooledController {
	
	private FlexiTableElement tableEl;
	private Link newCurriculumButton;
	private Link importCurriculumButton;
	private CurriculumManagerDataModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CurriculumComposerController composerCtrl;
	private EditCurriculumController newCurriculumCtrl;
	private ImportCurriculumController importCurriculumCtrl;
	private EditCurriculumOverviewController editCurriculumCtrl;
	private ConfirmCurriculumDeleteController deleteCurriculumCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CurriculumElementLecturesController lecturesCtrl;
	
	private int counter = 0;
	private final Roles roles;
	private final boolean isMultiOrganisations;
	private final CurriculumSecurityCallback secCallback;

	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public CurriculumListManagerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "manage_curriculum");
		this.toolbarPanel = toolbarPanel;
		this.secCallback = secCallback;
		roles = ureq.getUserSession().getRoles();
		toolbarPanel.addListener(this);
		isMultiOrganisations = organisationService.isMultiOrganisations();

		initForm(ureq);
		loadModel(null, true);
	}
	
	@Override
	public void initTools() {
		if(secCallback.canNewCurriculum()) {
			importCurriculumButton = LinkFactory.createToolLink("import.curriculum", translate("import.curriculum"), this, "o_icon_import");
			importCurriculumButton.setElementCssClass("o_sel_import_curriculum");
			toolbarPanel.addTool(importCurriculumButton, Align.left);
			
			newCurriculumButton = LinkFactory.createToolLink("add.curriculum", translate("add.curriculum"), this, "o_icon_add");
			newCurriculumButton.setElementCssClass("o_sel_add_curriculum");
			toolbarPanel.addTool(newCurriculumButton, Align.left);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.identifier, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.externalId, "select"));
		if(organisationModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(isMultiOrganisations, CurriculumCols.organisation));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.numOfElements));
		if(lectureModule.isEnabled()) {
			DefaultFlexiColumnModel lecturesCol = new DefaultFlexiColumnModel("table.header.lectures", CurriculumCols.lectures.ordinal(), "lectures",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.lectures"), "lectures", null, "o_icon o_icon_lecture o_icon-fw"),
							null));
			lecturesCol.setExportable(false);
			columnsModel.addFlexiColumnModel(lecturesCol);
		}
		
		DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel("edit.icon", CurriculumCols.edit.ordinal(), "edit",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("edit"), "edit"),
						new StaticFlexiCellRenderer(translate("select"), "edit")));
		editCol.setExportable(false);
		columnsModel.addFlexiColumnModel(editCol);
		if(secCallback.canEditCurriculum()) {
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(CurriculumCols.tools);
			toolsCol.setExportable(false);
			toolsCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		tableModel = new CurriculumManagerDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyTableSettings("table.curriculum.empty", null, "o_icon_curriculum_element", "add.curriculum", "o_icon_add", true);
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-manage");
	}
	
	private void loadModel(String searchString, boolean reset) {
		
		// curriculum owners, curriculum manages and administrators can edit curriculums
		// principals can only view them
		CurriculumSearchParameters managerParams = new CurriculumSearchParameters();
		managerParams.setSearchString(searchString);
		managerParams.setCurriculumAdmin(getIdentity());
		List<CurriculumInfos> managerCurriculums = curriculumService.getCurriculumsWithInfos(managerParams);
		List<CurriculumRow> rows = managerCurriculums.stream()
				.map(cur -> forgeManagedRow(cur, true)).collect(Collectors.toList());
		Set<CurriculumRow> deduplicateRows = new HashSet<>(rows);
		
		if(roles.isPrincipal()) {
			CurriculumSearchParameters principalParams = new CurriculumSearchParameters();
			principalParams.setSearchString(searchString);
			principalParams.setCurriculumPrincipal(getIdentity());
			List<CurriculumInfos> principalsCurriculums = curriculumService.getCurriculumsWithInfos(principalParams);
			List<CurriculumRow> principalsRows = principalsCurriculums.stream()
					.map(cur -> forgeManagedRow(cur, false))
					.filter(row -> !deduplicateRows.contains(row))
					.collect(Collectors.toList());
			rows.addAll(principalsRows);
			deduplicateRows.addAll(principalsRows);
		}
		
		CurriculumSearchParameters ownerParams = new CurriculumSearchParameters();
		ownerParams.setSearchString(searchString);
		ownerParams.setElementOwner(getIdentity());
		List<CurriculumInfos> reOwnersCurriculums = curriculumService.getCurriculumsWithInfos(ownerParams);
		List<CurriculumRow> reOwnerRows = reOwnersCurriculums.stream()
				.filter(c -> !managerCurriculums.contains(c))
				.map(CurriculumRow::new)
				.filter(row -> !deduplicateRows.contains(row))
				.collect(Collectors.toList());
		
		rows.addAll(reOwnerRows);
		
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}
	
	/**
	 * This create a row with management rights.
	 * 
	 * @param curriculum The curriculum informations
	 * @return A curriculum row
	 */
	private CurriculumRow forgeManagedRow(CurriculumInfos curriculum, boolean canManage) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		CurriculumRow row = new CurriculumRow(curriculum, toolsLink, canManage);
		toolsLink.setUserObject(row);
		return row;
	}

	@Override
	protected void doDispose() {
		if(toolbarPanel != null) {
			toolbarPanel.removeListener(this);
		}
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Curriculum".equalsIgnoreCase(type)) {
			Long curriculumKey = entries.get(0).getOLATResourceable().getResourceableId();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			activateCurriculum(ureq, curriculumKey, subEntries);
		}
	}
	
	private void activateCurriculum(UserRequest ureq, Long curriculumKey, List<ContextEntry> entries) {
		if(composerCtrl != null && curriculumKey.equals(composerCtrl.getCurriculum().getKey())) return;
		
		List<CurriculumRow> rows = tableModel.getObjects();
		for(CurriculumRow row:rows) {
			if(curriculumKey.equals(row.getKey())) {
				doSelectCurriculum(ureq, row, entries);
				break;
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newCurriculumCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(tableEl.getQuickSearchString(), true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editCurriculumCtrl == source || importCurriculumCtrl == source
				|| deleteCurriculumCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(tableEl.getQuickSearchString(), false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(importCurriculumCtrl);
		removeAsListenerAndDispose(deleteCurriculumCtrl);
		removeAsListenerAndDispose(newCurriculumCtrl);
		removeAsListenerAndDispose(cmc);
		importCurriculumCtrl = null;
		deleteCurriculumCtrl = null;
		newCurriculumCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newCurriculumButton == source) {
			doNewCurriculum(ureq);
		} else if(importCurriculumButton == source) {
			doImportCurriculum(ureq);
		} else if(toolbarPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() instanceof CurriculumComposerController) {
					removeAsListenerAndDispose(composerCtrl);
					composerCtrl = null;
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					doSelectCurriculum(ureq, tableModel.getObject(se.getIndex()), null);
				} else if("edit".equals(cmd)) {
					doEditCurriculum(ureq, tableModel.getObject(se.getIndex()));
				} else if("lectures".equals(cmd)) {
					doOpenLectures(ureq, tableModel.getObject(se.getIndex()));
				} 
			} else if(event instanceof FlexiTableSearchEvent) {
				doSearch((FlexiTableSearchEvent)event);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doNewCurriculum(ureq);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, (CurriculumRow)link.getUserObject(), link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSearch(FlexiTableSearchEvent event) {
		loadModel(event.getSearch(), true);
	}
	
	private void doImportCurriculum(UserRequest ureq) {
		if(guardModalController(importCurriculumCtrl)) return;

		importCurriculumCtrl = new ImportCurriculumController(ureq, getWindowControl());
		listenTo(importCurriculumCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", importCurriculumCtrl.getInitialComponent(), true, translate("import.curriculum"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doNewCurriculum(UserRequest ureq) {
		if(guardModalController(newCurriculumCtrl)) return;

		newCurriculumCtrl = new EditCurriculumController(ureq, getWindowControl(), secCallback);
		listenTo(newCurriculumCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newCurriculumCtrl.getInitialComponent(), true, translate("add.curriculum"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditCurriculum(UserRequest ureq, CurriculumRow row) {
		removeAsListenerAndDispose(editCurriculumCtrl);
		
		Curriculum curriculum = curriculumService.getCurriculum(row);
		if(curriculum == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			editCurriculumCtrl = new EditCurriculumOverviewController(ureq, getWindowControl(), curriculum, secCallback);
			listenTo(editCurriculumCtrl);
			toolbarPanel.pushController(row.getDisplayName(), editCurriculumCtrl);
		}
	}
	
	private void doDeleteCurriculum(UserRequest ureq, CurriculumRow row) {
		removeAsListenerAndDispose(deleteCurriculumCtrl);
		
		Curriculum curriculum = curriculumService.getCurriculum(row);
		if(curriculum == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			deleteCurriculumCtrl = new ConfirmCurriculumDeleteController(ureq, getWindowControl(), row);
			listenTo(deleteCurriculumCtrl);
			
			String title = translate("delete.curriculum.title", StringHelper.escapeHtml(row.getDisplayName()));
			cmc = new CloseableModalController(getWindowControl(), "close", deleteCurriculumCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doExportCurriculum(UserRequest ureq, CurriculumRow row) {
		Curriculum curriculum = curriculumService.getCurriculum(row);
		MediaResource mr = new ExportCurriculumMediaResource(curriculum);
		ureq.getDispatchResult().setResultingMediaResource(mr);
	}
	
	private void doSelectCurriculum(UserRequest ureq, CurriculumRow row, List<ContextEntry> entries) {
		Curriculum curriculum = curriculumService.getCurriculum(row);
		if(curriculum == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(Curriculum.class, row.getKey()), null);
			boolean canManage = row.canManage();
			List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculum, getIdentity());
			List<CurriculumElementRef> ownedElements = memberships.stream()
					.filter(CurriculumElementMembership::isCurriculumElementOwner)
					.map(m -> new CurriculumElementRefImpl(m.getCurriculumElementKey()))
					.collect(Collectors.toList());
			CurriculumSecurityCallback curriculumSecCallback = CurriculumSecurityCallbackFactory.createCallback(canManage, ownedElements);
			composerCtrl = new CurriculumComposerController(ureq, swControl, toolbarPanel, curriculum, curriculumSecCallback);
			listenTo(composerCtrl);
			toolbarPanel.pushController(row.getDisplayName(), composerCtrl);
			composerCtrl.activate(ureq, entries, null);
		}
	}
	
	private void doOpenLectures(UserRequest ureq, CurriculumRow row) {
		removeAsListenerAndDispose(lecturesCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Lectures", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		Curriculum curriculum = curriculumService.getCurriculum(row);
		lecturesCtrl = new CurriculumElementLecturesController(ureq, bwControl, toolbarPanel, curriculum, null, true, secCallback);
		listenTo(lecturesCtrl);
		toolbarPanel.pushController(row.getDisplayName(), null, row);
		toolbarPanel.pushController(translate("lectures"), lecturesCtrl);
		
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		Curriculum curriculum = curriculumService.getCurriculum(row);
		if(curriculum == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, curriculum);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private class ToolsController extends BasicController {
		
		private Link editLink;
		private Link deleteLink;
		private Link exportLink;
		private final VelocityContainer mainVC;

		private CurriculumRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumRow row, Curriculum curriculum) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(4);
			
			//edit
			editLink = addLink("edit", "o_icon_edit", links);
			exportLink = addLink("export", "o_icon_export", links);
			
			if(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}
			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doEditCurriculum(ureq, row);
			} else if(deleteLink == source) {
				close();
				doDeleteCurriculum(ureq, row);
			} else if(exportLink == source) {
				close();
				doExportCurriculum(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
