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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.List;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
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
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.ui.ReasonAdminDataModel.ReasonCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReasonAdminController extends FormBasicController {
	
	private FormLink addReasonButton;
	private FlexiTableElement tableEl;
	private ReasonAdminDataModel dataModel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private EditReasonController editReasonCtrl;
	private DialogBoxController deleteDialogCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private int counter = 0;
	
	@Autowired
	private LectureService lectureService;
	
	public ReasonAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_reason");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_lecture_reasons");
		
		addReasonButton = uifactory.addFormLink("add.reason", formLayout, Link.BUTTON);
		addReasonButton.setIconLeftCSS("o_icon o_icon_add_item");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReasonCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReasonCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReasonCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReasonCols.enabled));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
				new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit"), null));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(ReasonCols.tools);
		toolsColumn.setExportable(false);
		toolsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		dataModel = new ReasonAdminDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "absences-reasons");
	}
	
	private void loadModel() {
		List<Reason> reasons = lectureService.getAllReasons();
		List<ReasonRow> rows = new ArrayList<>(reasons.size());
		for(Reason reason:reasons) {
			String linkName = "tools-" + counter++;
			FormLink toolsLink = uifactory.addFormLink(linkName, "", null, flc, Link.LINK | Link.NONTRANSLATED);
			toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setUserObject(reason);
			flc.add(linkName, toolsLink);
			rows.add(new ReasonRow(reason, toolsLink));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editReasonCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if(deleteDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				Reason row = (Reason)deleteDialogCtrl.getUserObject();
				doDelete(row);
				loadModel();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(editReasonCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		editReasonCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addReasonButton == source) {
			doAddReason(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ReasonRow row = dataModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEditReason(ureq, row.getReason());
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null && cmd.startsWith("tools-")) {
				Reason row = (Reason)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEditReason(UserRequest ureq, Reason reason) {
		editReasonCtrl = new EditReasonController(ureq, getWindowControl(), reason);
		listenTo(editReasonCtrl);
		
		String title = translate("edit.reason");
		cmc = new CloseableModalController(getWindowControl(), "close", editReasonCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddReason(UserRequest ureq) {
		editReasonCtrl = new EditReasonController(ureq, getWindowControl());
		listenTo(editReasonCtrl);
		
		String title = translate("add.reason");
		cmc = new CloseableModalController(getWindowControl(), "close", editReasonCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenTools(UserRequest ureq, Reason row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doCopy(Reason reason) {
		String copiedTitle = translate("reason.copy", new String[] { reason.getTitle() });
		lectureService.createReason(copiedTitle, reason.getDescription(), true);
		loadModel();
		showInfo("reason.copied");
	}

	private void doConfirmDelete(UserRequest ureq, Reason reason) {
		if(lectureService.isReasonInUse(reason)) {
			showWarning("reason.in.use");
		} else {
			String text = translate("confirm.delete.reason", new String[] { reason.getTitle() });
			deleteDialogCtrl = activateYesNoDialog(ureq, translate("delete.title"), text, deleteDialogCtrl);
			deleteDialogCtrl.setUserObject(reason);
		}
	}
	
	private void doDelete(Reason reason) {
		lectureService.deleteReason(reason);
		showInfo("reason.deleted");
	}
	
	private class ToolsController extends BasicController {
		
		private final Link deleteLink, copyLink;
		
		private final Reason reason;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, Reason reason) {
			super(ureq, wControl);
			this.reason = reason;
			
			VelocityContainer mainVC = createVelocityContainer("reason_tools");
			
			copyLink = LinkFactory.createLink("copy", "copy", getTranslator(), mainVC, this, Link.LINK);
			copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");

			putInitialPanel(mainVC);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			this.fireEvent(ureq, Event.DONE_EVENT);
			if(copyLink == source) {
				doCopy(reason);
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, reason);
			}
		}
	}
}
