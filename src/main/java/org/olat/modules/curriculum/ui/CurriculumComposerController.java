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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.ui.CurriculumComposerTableModel.ElementCols;
import org.olat.modules.curriculum.ui.component.CurriculumElementStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerController extends FormBasicController implements TooledController {
	
	private Link newElementButton;
	private FlexiTableElement tableEl;
	private CurriculumComposerTableModel tableModel;
	private TooledStackedPanel toolbarPanel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ReferencesController referencesCtrl; 
	private EditCurriculumElementController newElementCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditCurriculumElementController newSubElementCtrl;
	private MoveCurriculumElementController moveElementCtrl;
	
	private int counter;
	private final Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumComposerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "manage_curriculum_structure");
		this.toolbarPanel = toolbarPanel;
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		initForm(ureq);
		loadModel();
	}

	@Override
	public void initTools() {
		if(secCallback.canNewCurriculumElement()) {
			newElementButton = LinkFactory.createToolLink("add.curriculum.element", translate("add.curriculum.element"), this, "o_icon_add");
			newElementButton.setElementCssClass("o_sel_add_curriculum_element");
			toolbarPanel.addTool(newElementButton, Align.left);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.key, "select"));

		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.beginDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.endDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.status, new CurriculumElementStatusCellRenderer(getTranslator())));

		DefaultFlexiColumnModel selectColumn = new DefaultFlexiColumnModel("select", translate("select"), "select");
		selectColumn.setExportable(false);
		selectColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(selectColumn);
		if(secCallback.canEditCurriculumElement()) {
			DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(ElementCols.tools);
			toolsColumn.setExportable(false);
			toolsColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
		
		tableModel = new CurriculumComposerTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_curriculum_el_listing");
		tableEl.setEmtpyTableMessageKey("table.curriculum.element.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(40);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-composer");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(curriculum);
		List<CurriculumElementRow> rows = new ArrayList<>(elements.size());
		Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
		for(CurriculumElementInfos element:elements) {
			CurriculumElementRow row = forgeRow(element);
			rows.add(row);
			keyToRows.put(element.getKey(), row);
		}
		//parent line
		for(CurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		Collections.sort(rows, new FlexiTreeNodeComparator());
		
		tableModel.setObjects(rows);
		tableEl.reset(false, true, true);
	}
	
	private CurriculumElementRow forgeRow(CurriculumElementInfos element) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		
		FormLink resourcesLink = null;
		if(element.getNumOfResources() > 1) {
			resourcesLink = uifactory.addFormLink("resources_" + (++counter), "resources", String.valueOf(element.getNumOfResources()), null, null, Link.NONTRANSLATED);
		}
		CurriculumElementRow row = new CurriculumElementRow(element.getCurriculumElement(), element.getNumOfResources(),
				toolsLink, resourcesLink);
		toolsLink.setUserObject(row);
		if(resourcesLink != null) {
			resourcesLink.setUserObject(row);
		}
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newElementCtrl == source || newSubElementCtrl == source || moveElementCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(moveElementCtrl);
		removeAsListenerAndDispose(newElementCtrl);
		removeAsListenerAndDispose(cmc);
		moveElementCtrl = null;
		newElementCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newElementButton == source) {
			doNewCurriculumElement(ureq);
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
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doEditCurriculumElement(ureq, row);
				}
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, (CurriculumElementRow)link.getUserObject(), link);
			} else if("resources".equals(cmd)) {
				doOpenReferences(ureq, (CurriculumElementRow)link.getUserObject(), link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doNewCurriculumElement(UserRequest ureq) {
		if(newElementCtrl != null) return;

		newElementCtrl = new EditCurriculumElementController(ureq, getWindowControl(), null, curriculum, secCallback);
		listenTo(newElementCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doNewSubCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement parentElement = curriculumService.getCurriculumElement(row);
		if(parentElement == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			newSubElementCtrl = new EditCurriculumElementController(ureq, getWindowControl(), parentElement, curriculum, secCallback);
			newSubElementCtrl.setParentElement(parentElement);
			listenTo(newSubElementCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", newSubElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doEditCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(CurriculumElement.class, row.getKey()), null);
			EditCurriculumElementOverviewController editCtrl = new EditCurriculumElementOverviewController(ureq, swControl, element, curriculum, secCallback);
			listenTo(editCtrl);
			toolbarPanel.pushController(row.getDisplayName(), editCtrl);
		}
	}
	
	private void doMoveCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			List<CurriculumElement> elementsToMove = Collections.singletonList(element);
			moveElementCtrl = new MoveCurriculumElementController(ureq, getWindowControl(), elementsToMove, curriculum);
			listenTo(moveElementCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", moveElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, element);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenReferences(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null ) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), element);
			listenTo(referencesCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void launch(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private class ReferencesController extends BasicController {
		
		public ReferencesController(UserRequest ureq, WindowControl wControl, CurriculumElement element) {
			super(ureq, wControl);
			setTranslator(CurriculumComposerController.this.getTranslator());
			VelocityContainer mainVC = createVelocityContainer("references");

			List<RepositoryEntry> refs = curriculumService.getRepositoryEntries(element);

			List<String> refLinks = new ArrayList<>(refs.size());
			for(RepositoryEntry ref:refs) {
				String name = "ref-" + (++counter);
				Link refLink = LinkFactory.createLink(name, "reference", getTranslator(), mainVC, this, Link.NONTRANSLATED);
				refLink.setCustomDisplayText(StringHelper.escapeHtml(ref.getDisplayname()));
				refLink.setUserObject(ref);
				refLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(ref));
				refLinks.add(name);
			}
			mainVC.contextPut("referenceLinks", refLinks);
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link) {
				fireEvent(ureq, Event.DONE_EVENT);
				Link link = (Link)source;
				if("reference".equals(link.getCommand())) {
					RepositoryEntryRef uobject = (RepositoryEntryRef)link.getUserObject();
					launch(ureq, uobject);
				}
			}
		}

		@Override
		protected void doDispose() {
			//
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link editLink;
		private Link moveLink;
		private Link newLink;
		private Link deleteLink;
		
		private CurriculumElementRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl,
				CurriculumElementRow row, CurriculumElement element) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(4);
			
			//edit
			editLink = addLink("edit", "o_icon_edit", links);
			if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.move)) {
				moveLink = addLink("move.element", "o_icon_move", links);
			}
			if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.addChildren)) {
				newLink = addLink("add.element.under", "o_icon_levels", links);
			}
			if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.delete)) {
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
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doEditCurriculumElement(ureq, row);
			} else if(moveLink == source) {
				close();
				doMoveCurriculumElement(ureq, row);
			} else if(deleteLink == source) {
				close();
				showWarning("Not implemented");
			} else if(newLink == source) {
				close();
				doNewSubCurriculumElement(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
