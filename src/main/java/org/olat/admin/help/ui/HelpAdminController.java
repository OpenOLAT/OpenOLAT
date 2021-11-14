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
package org.olat.admin.help.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.admin.help.ui.HelpAdminTableModel.HelpAdminTableColumn;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Date: 01 Apr 2020
 * @author aboeckle
 */
public class HelpAdminController extends FormBasicController {

	private static final String TABLE_NAME = "help_table";

	private final AtomicInteger counter = new AtomicInteger();

	private HelpAdminTableModel tableModel; 
	private FlexiTableElement tableEl;
	private DefaultFlexiColumnModel upDownColumn;

	private DropdownItem addHelpDropDown;
	private FormLink addAcademy;
	private FormLink addOOTeach;
	private FormLink addConfluence;
	private FormLink addCourse;
	private FormLink addSupport;
	private FormLink addCustom1;
	private FormLink addCustom2;
	private FormLink addCustom3;

	private CloseableModalController cmc;
	private HelpAdminEditController editController;
	private HelpAdminEditController addController;
	private HelpAdminDeleteConfirmController deleteConfirmController;


	@Autowired
	private HelpModule helpModule;

	public HelpAdminController(UserRequest ureq, WindowControl control) {
		super(ureq, control, "help");

		initForm(ureq);
		loadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer layout = (FormLayoutContainer)formLayout;
		layout.contextPut("isHelpEnabled", helpModule.isHelpEnabled());

		addHelpDropDown = uifactory.addDropdownMenu("help.admin.add.help", "help.admin.add.help", formLayout, getTranslator());
		addHelpDropDown.setOrientation(DropdownOrientation.right);
		addHelpDropDown.setExpandContentHeight(true); // prevent cut drop downs

		addAcademy = uifactory.addFormLink("help.admin.academy", formLayout);
		addOOTeach = uifactory.addFormLink("help.admin.ooTeach", formLayout);
		addConfluence = uifactory.addFormLink("help.admin.confluence", formLayout);
		addCourse = uifactory.addFormLink("help.admin.course", formLayout);
		addSupport = uifactory.addFormLink("help.admin.support", formLayout);
		addCustom1 = uifactory.addFormLink("help.admin.custom1", formLayout);
		addCustom2 = uifactory.addFormLink("help.admin.custom2", formLayout);
		addCustom3 = uifactory.addFormLink("help.admin.custom3", formLayout);

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		upDownColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.upDown);
		DefaultFlexiColumnModel labelColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.label);
		DefaultFlexiColumnModel iconColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.icon);
		iconColumn.setCellRenderer(new HelpAdminTableIconRenderer());
		DefaultFlexiColumnModel actionColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.action);
		DefaultFlexiColumnModel usertoolColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.usertool);
		usertoolColumn.setCellRenderer(new HelpAdminTableBooleanCellRenderer());
		DefaultFlexiColumnModel authoringColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.authoring);
		authoringColumn.setCellRenderer(new HelpAdminTableBooleanCellRenderer());
		DefaultFlexiColumnModel loginColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.dmz);
		loginColumn.setCellRenderer(new HelpAdminTableBooleanCellRenderer());
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.edit);
		DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel(HelpAdminTableColumn.delete);

		columnModel.addFlexiColumnModel(upDownColumn);
		columnModel.addFlexiColumnModel(labelColumn);
		columnModel.addFlexiColumnModel(iconColumn);
		columnModel.addFlexiColumnModel(actionColumn);
		columnModel.addFlexiColumnModel(authoringColumn);
		columnModel.addFlexiColumnModel(usertoolColumn);
		columnModel.addFlexiColumnModel(loginColumn);
		columnModel.addFlexiColumnModel(editColumn);
		columnModel.addFlexiColumnModel(deleteColumn);

		tableModel = new HelpAdminTableModel(columnModel, ureq);
		tableEl = uifactory.addTableElement(getWindowControl(), TABLE_NAME, tableModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}

	private void loadData() {
		addHelpDropDown.removeAllFormItems();
		
		// Fill drop down element
		for (String helpPlugin : helpModule.getRemainingPlugins()) {
			switch (helpPlugin) {
			case HelpModule.ACADEMY:
				addHelpDropDown.addElement(addAcademy);
				break;
			case HelpModule.OOTEACH:
				addHelpDropDown.addElement(addOOTeach);
				break;
			case HelpModule.CONFLUENCE:
				addHelpDropDown.addElement(addConfluence);
				break;
			case HelpModule.COURSE:
				addHelpDropDown.addElement(addCourse);
				break;
			case HelpModule.SUPPORT:
				addHelpDropDown.addElement(addSupport);
				break;
			case HelpModule.CUSTOM_1:
				addHelpDropDown.addElement(addCustom1);
				break;
			case HelpModule.CUSTOM_2:
				addHelpDropDown.addElement(addCustom2);
				break;
			case HelpModule.CUSTOM_3:
				addHelpDropDown.addElement(addCustom3);
				break;
			default:
				break;
			}
		}
		
		addHelpDropDown.setVisible(addHelpDropDown.size() > 0);

		// Fill table
		List<HelpAdminTableContentRow> tableRows = new ArrayList<>();
		List<String> helpPlugins = helpModule.getHelpPluginList();
		for (int i = 0; i < helpPlugins.size(); i++) {
			String helpPlugin = helpPlugins.get(i);
			HelpAdminTableContentRow tableRow;

			switch (helpPlugin) {
			case HelpModule.ACADEMY_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.ACADEMY, 
						helpModule.getAcademyIcon(), 
						helpModule.getAcademyEnabled().contains(HelpModule.USERTOOL), 
						helpModule.getAcademyEnabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getAcademyEnabled().contains(HelpModule.DMZ));
				break;
			case HelpModule.OOTEACH_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.OOTEACH, 
						helpModule.getOOTeachIcon(), 
						helpModule.getOOTeachEnabled().contains(HelpModule.USERTOOL), 
						helpModule.getOOTeachEnabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getOOTeachEnabled().contains(HelpModule.DMZ));
				break;
			case HelpModule.CONFLUENCE_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.CONFLUENCE, 
						helpModule.getConfluenceIcon(), 
						helpModule.getConfluenceEnabled().contains(HelpModule.USERTOOL), 
						helpModule.getConfluenceEnabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getConfluenceEnabled().contains(HelpModule.DMZ));
				break;
			case HelpModule.COURSE_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.COURSE, 
						helpModule.getCourseIcon(), 
						helpModule.getCourseEnabled().contains(HelpModule.USERTOOL), 
						helpModule.getCourseEnabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getCourseEnabled().contains(HelpModule.DMZ));
				break;
			case HelpModule.CUSTOM_1_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.CUSTOM_1, 
						helpModule.getCustom1Icon(), 
						helpModule.getCustom1Enabled().contains(HelpModule.USERTOOL), 
						helpModule.getCustom1Enabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getCustom1Enabled().contains(HelpModule.DMZ));
				break;
			case HelpModule.CUSTOM_2_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.CUSTOM_2, 
						helpModule.getCustom2Icon(), 
						helpModule.getCustom2Enabled().contains(HelpModule.USERTOOL), 
						helpModule.getCustom2Enabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getCustom2Enabled().contains(HelpModule.DMZ));
				break;
			case HelpModule.CUSTOM_3_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.CUSTOM_3, 
						helpModule.getCustom3Icon(), 
						helpModule.getCustom3Enabled().contains(HelpModule.USERTOOL), 
						helpModule.getCustom3Enabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getCustom3Enabled().contains(HelpModule.DMZ));
				break;
			case HelpModule.SUPPORT_KEY:
				tableRow = new HelpAdminTableContentRow(
						HelpModule.SUPPORT, 
						helpModule.getSupportIcon(), 
						helpModule.getSupportEnabled().contains(HelpModule.USERTOOL), 
						helpModule.getSupportEnabled().contains(HelpModule.AUTHORSITE), 
						helpModule.getSupportEnabled().contains(HelpModule.DMZ));
				break;
			default:
				tableRow = new HelpAdminTableContentRow();
				break;
			}

			if (helpPlugins.size() > 1) {
				tableEl.setColumnModelVisible(upDownColumn, true);
				UpDown upDown = UpDownFactory.createUpDown("up_down_" + helpPlugin, UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
				upDown.setUserObject(tableRow);
				if (i == 0) {
					upDown.setTopmost(true);
				} else if (i == helpPlugins.size() - 1) {
					upDown.setLowermost(true);
				} 
				tableRow.setUpDown(upDown);
			} else {
				tableEl.setColumnModelVisible(upDownColumn, false);
			}

			FormLink editLink = uifactory.addFormLink("edit_" + counter.incrementAndGet(), "edit", "help.admin.edit", null, null, Link.LINK);
			editLink.setUserObject(tableRow);

			FormLink deleteLink = uifactory.addFormLink("delete_" + counter.incrementAndGet(), "delete", "help.admin.delete", null, null, Link.LINK);
			deleteLink.setUserObject(tableRow);

			tableRow.setEditLink(editLink);
			tableRow.setDeleteLink(deleteLink);

			tableRows.add(tableRow);
		}

		tableModel.setObjects(tableRows);
		tableEl.reset(true, true, true);
		
		// Save positions, necessary if new entries added
		saveHelpPluginPositions(tableRows);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {	
		if (source == addAcademy) {
			doOpenAddHelpDialog(ureq, HelpModule.ACADEMY);
		} else if (source == addOOTeach) {
			doOpenAddHelpDialog(ureq, HelpModule.OOTEACH);
		} else if (source == addConfluence) {
			doOpenAddHelpDialog(ureq, HelpModule.CONFLUENCE);
		} else if (source == addCourse) {
			doOpenAddHelpDialog(ureq, HelpModule.COURSE);
		} else if (source == addSupport) {
			doOpenAddHelpDialog(ureq, HelpModule.SUPPORT);
		} else if (source == addCustom1) {
			doOpenAddHelpDialog(ureq, HelpModule.CUSTOM_1);
		} else if (source == addCustom2) {
			doOpenAddHelpDialog(ureq, HelpModule.CUSTOM_2);
		} else if (source == addCustom3) {
			doOpenAddHelpDialog(ureq, HelpModule.CUSTOM_3);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink) source;

			if("edit".equals(link.getCmd())) {
				HelpAdminTableContentRow row = (HelpAdminTableContentRow) link.getUserObject();

				doOpenEditHelpDialog(ureq, row);
			} else if ("delete".equals(link.getCmd())) {
				HelpAdminTableContentRow row = (HelpAdminTableContentRow) link.getUserObject();

				doOpenDeleteHelpDialog(ureq, row);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editController) {
			if (event.equals(FormEvent.CANCELLED_EVENT)) {
				cleanUp();
			} else if (event.equals(FormEvent.DONE_EVENT)) {
				cleanUp();
				loadData();
			}
		} else if (source == deleteConfirmController) {
			if (event.equals(FormEvent.CANCELLED_EVENT)) {
				cleanUp();
			} else if (event.equals(FormEvent.DONE_EVENT)) {
				cleanUp();
				initForm(ureq);
				loadData();
			}
		} else if (source == addController) {
			if (event.equals(Event.CANCELLED_EVENT)) {
				cleanUp();
			} else if (event.equals(Event.DONE_EVENT)) {
				cleanUp();
				initForm(ureq);
				loadData();
			}
		} else if (source == cmc) {
			cleanUp();
		}

		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent) {
			UpDownEvent ude = (UpDownEvent) event;
			doMoveHelpPlugin((UpDown)source, ude.getDirection());
		}
		super.event(ureq, source, event);
	}

	private void doMoveHelpPlugin(UpDown upDown, Direction direction) {
		List<HelpAdminTableContentRow> tableRows = tableModel.getObjects();

		Integer index = tableRows.indexOf(upDown.getUserObject());
		if (Direction.UP.equals(direction)) {
			Collections.swap(tableRows, index - 1, index);
		} else {
			Collections.swap(tableRows, index, index + 1);
		}
		doDisableUpDowns(tableRows);

		tableModel.setObjects(tableRows);
		tableEl.reset(true, true, true);

		saveHelpPluginPositions(tableRows);
	}

	/**
	 * Hides up on the first line and down on the last line of the table
	 * @param tableRows
	 */
	private void doDisableUpDowns(List<HelpAdminTableContentRow> tableRows) {

		for (HelpAdminTableContentRow tableRow : tableRows) {
			tableRow.getUpDown().setTopmost(false);
			tableRow.getUpDown().setLowermost(false);
		}
		tableRows.get(0).getUpDown().setTopmost(true);
		tableRows.get(tableRows.size()-1).getUpDown().setLowermost(true);
	}

	private void saveHelpPluginPositions(List<HelpAdminTableContentRow> tableRows) {
		for (HelpAdminTableContentRow tableRow : tableRows) {
			helpModule.setPosition(tableRow.getHelpPlugin(), tableRows.indexOf(tableRow));
		}

	}

	private void cleanUp() {
		if (cmc != null) {
			cmc.deactivate();
		}

		removeAsListenerAndDispose(editController);
		removeAsListenerAndDispose(addController);
		removeAsListenerAndDispose(cmc);

		addController = null;
		editController = null;
		cmc = null;
	}

	private void doOpenAddHelpDialog(UserRequest ureq, String helpPluginToAdd) {
		addController = new HelpAdminEditController(ureq, getWindowControl(), helpPluginToAdd);
		listenTo(addController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(), true, translate("help.admin.add.help"), true);
		listenTo(cmc);

		cmc.activate();
	}

	private void doOpenEditHelpDialog(UserRequest ureq, HelpAdminTableContentRow row) {
		editController = new HelpAdminEditController(ureq, getWindowControl(), row);
		listenTo(editController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent(), true, translate("help.admin.edit.title"), true);
		listenTo(cmc);

		cmc.activate();
	}

	private void doOpenDeleteHelpDialog(UserRequest ureq, HelpAdminTableContentRow row) {
		deleteConfirmController = new HelpAdminDeleteConfirmController(ureq, getWindowControl(), row);
		listenTo(deleteConfirmController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmController.getInitialComponent(), true, translate("help.admin.delete.title"), true);
		listenTo(cmc);

		cmc.activate();
	}
}