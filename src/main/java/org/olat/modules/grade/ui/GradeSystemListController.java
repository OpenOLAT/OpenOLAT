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
package org.olat.modules.grade.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
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
import org.olat.modules.grade.GradeScaleStats;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemSearchParams;
import org.olat.modules.grade.ui.GradeSystemDataModel.GradeSystemCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeSystemListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private GradeSystemDataModel dataModel;
	private FormLink addButton;
	
	private CloseableModalController cmc;
	private GradeSystemCreateController createCtrl;
	private GradeSystemEditController editCtrl;
	private DialogBoxController deleteCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;
	
	@Autowired
	private GradeService gradeService;

	public GradeSystemListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer titleCont = FormLayoutContainer.createDefaultFormLayout("title.cont", getTranslator());
		titleCont.setFormTitle(translate("grade.system.admin.title"));
		titleCont.setRootForm(mainForm);
		formLayout.add(titleCont);
		
		FormLayoutContainer buttonsTopCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		buttonsTopCont.setFormTitle("grade.system.admin.title");
		buttonsTopCont.setElementCssClass("o_button_group o_button_group_right");
		buttonsTopCont.setRootForm(mainForm);
		formLayout.add(buttonsTopCont);
		
		addButton = uifactory.addFormLink("grade.system.add", buttonsTopCont, Link.BUTTON);
		addButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradeSystemCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradeSystemCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradeSystemCols.usageCount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradeSystemCols.enabled));
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(GradeSystemCols.tools);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		dataModel = new GradeSystemDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "grade-systems");
	}

	private void loadModel() {
		List<GradeSystem> gradeSystems = gradeService.getGradeSystems(new GradeSystemSearchParams());
		Map<Long, Long> gradeSystemKeyToCount = gradeService.getGradeScaleStats().stream()
				.collect(Collectors.toMap(GradeScaleStats::getGradeSystemKey, GradeScaleStats::getCount));
		List<GradeSystemRow> rows = new ArrayList<>(gradeSystems.size());
		for (GradeSystem gradeSystem : gradeSystems) {
			GradeSystemRow row = new GradeSystemRow(gradeSystem);
			
			row.setName(GradeUIFactory.translateGradeSystem(getTranslator(), gradeSystem));
			
			int usageCount = gradeSystemKeyToCount.getOrDefault(gradeSystem.getKey(), Long.valueOf(0)).intValue();
			row.setScaleCount(usageCount);
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + gradeSystem.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
			
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addButton) {
			doAddGradeSystem(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null && cmd.startsWith("tools")) {
				GradeSystemRow gradeSystemRow = (GradeSystemRow)link.getUserObject();
				doOpenTools(ureq, gradeSystemRow, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if (createCtrl == source) {
			if (event == Event.DONE_EVENT) {
				GradeSystem gradeSystem = createCtrl.getGradeSystem();
				loadModel();
				cmc.deactivate();
				cleanUp();
				doEditGradeSystem(ureq, gradeSystem);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (editCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (deleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				GradeSystem gradeSystem = (GradeSystem)deleteCtrl.getUserObject();
				doDeleteGradeSystem(gradeSystem);
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(deleteCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		createCtrl = null;
		deleteCtrl = null;
		toolsCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAddGradeSystem(UserRequest ureq) {
		if(guardModalController(createCtrl)) return;
		
		createCtrl = new GradeSystemCreateController(ureq, getWindowControl());
		listenTo(createCtrl);
		
		String title = translate("grade.system.add");
		cmc = new CloseableModalController(getWindowControl(), "close", createCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditGradeSystem(UserRequest ureq, GradeSystem gradeSystem) {
		if(guardModalController(editCtrl)) return;
		
		editCtrl = new GradeSystemEditController(ureq, getWindowControl(), gradeSystem);
		listenTo(editCtrl);
		
		String title = translate("grade.system.edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, GradeSystem gradeSystem) {
		String title = translate("grade.system.delete.title");
		String text = translate("grade.system.delete.text", GradeUIFactory.translateGradeSystem(getTranslator(), gradeSystem));
		deleteCtrl = activateYesNoDialog(ureq, title, text, deleteCtrl);
		deleteCtrl.setUserObject(gradeSystem);
	}

	private void doDeleteGradeSystem(GradeSystem gradeSystem) {
		if (!gradeService.hasGradeScale(gradeSystem)) {
			gradeService.deleteGradeSystem(gradeSystem);
		}
		loadModel();
	}
	
	private void doOpenTools(UserRequest ureq, GradeSystemRow gradeSystemRow, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), gradeSystemRow);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private Link editLink;
		private Link deleteLink;
		
		private final GradeSystemRow gradeSystemRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, GradeSystemRow gradeSystemRow) {
			super(ureq, wControl);
			this.gradeSystemRow = gradeSystemRow;
			
			VelocityContainer mainVC = createVelocityContainer("grade_system_tools");
			
			editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			
			if (gradeSystemRow.getScaleCount() == 0) {
				deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
				deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			this.fireEvent(ureq, Event.DONE_EVENT);
			if(editLink == source) {
				doEditGradeSystem(ureq, gradeSystemRow.getGradeSystem());
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, gradeSystemRow.getGradeSystem());
			}
		}
		
	}

}
