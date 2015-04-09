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
package org.olat.course.reminder.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.reminder.model.ReminderRow;
import org.olat.course.reminder.ui.CourseReminderTableModel.ReminderCols;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseReminderListController extends FormBasicController {
	
	private FormLink addButton;
	private FlexiTableElement tableEl;
	private CourseReminderTableModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private ToolsController toolsCtrl;
	private DialogBoxController deleteDialogBox;
	private CourseReminderEditController reminderEditCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CourseSendReminderListController sendReminderListCtrl;

	private final AtomicInteger counter = new AtomicInteger();
	private RepositoryEntry repositoryEntry;
	
	@Autowired
	private ReminderService reminderManager;
	
	public CourseReminderListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, TooledStackedPanel toolbarPanel) {
		super(ureq, wControl, "reminder_list");
		this.toolbarPanel = toolbarPanel;
		this.repositoryEntry = repositoryEntry;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addButton = uifactory.addFormLink("add.reminder", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.id.i18nKey(), ReminderCols.id.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(ReminderCols.description.i18nKey(), ReminderCols.description.ordinal(),
				"edit", new StaticFlexiCellRenderer("edit", new TextFlexiCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.creator.i18nKey(), ReminderCols.creator.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.creationDate.i18nKey(), ReminderCols.creationDate.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.lastModified.i18nKey(), ReminderCols.lastModified.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.sendTime.i18nKey(), ReminderCols.sendTime.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.send.i18nKey(), ReminderCols.send.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.tools.i18nKey(), ReminderCols.tools.ordinal()));
		
		tableModel = new CourseReminderTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		updateModel();
	}
	
	private void updateModel() {
		List<Reminder> reminders = reminderManager.getReminders(repositoryEntry);
		List<ReminderRow> rows = new ArrayList<>(reminders.size());
		for(Reminder reminder:reminders) {
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
			toolsLink.setTitle(translate("tools"));

			ReminderRow row = new ReminderRow(reminder, 3, toolsLink);
			toolsLink.setUserObject(row);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addButton == source) {
			doAddReminder(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				ReminderRow row = (ReminderRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ReminderRow row = tableModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEdit(ureq, row);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(reminderEditCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				//do
				updateModel();
			}
			toolbarPanel.popController(reminderEditCtrl);
			cleanUp();
		} else if(deleteDialogBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doDelete(ureq, (ReminderRow)deleteDialogBox.getUserObject());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(reminderEditCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		reminderEditCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
	}

	
	private void doOpenTools(UserRequest ureq, ReminderRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doAddReminder(UserRequest ureq) {
		removeAsListenerAndDispose(reminderEditCtrl);
		
		Reminder newReminder = reminderManager.createReminder(repositoryEntry);
		reminderEditCtrl = new CourseReminderEditController(ureq, getWindowControl(), newReminder);
		listenTo(reminderEditCtrl);
		
		toolbarPanel.pushController(translate("new.reminder"), reminderEditCtrl);	
	}
	
	private void doSendReminderList(UserRequest ureq, ReminderRow row) {
		removeAsListenerAndDispose(sendReminderListCtrl);
	
		Reminder reminder = reminderManager.loadByKey(row.getKey());
		sendReminderListCtrl = new CourseSendReminderListController(ureq, getWindowControl(), reminder);
		listenTo(sendReminderListCtrl);
		
		toolbarPanel.pushController(translate("send.reminder"), sendReminderListCtrl);	
	}
	
	private void doConfirmDelete(UserRequest ureq, ReminderRow row) {
		String desc = StringHelper.escapeHtml(row.getDescription());
		deleteDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.delete.text", desc), deleteDialogBox);
		deleteDialogBox.setUserObject(row);
	}

	private void doDelete(UserRequest ureq, ReminderRow row) {
		Reminder reminder = reminderManager.loadByKey(row.getKey());
		reminderManager.delete(reminder);
		updateModel();
	}

	private void doEdit(UserRequest ureq, ReminderRow row) {
		removeAsListenerAndDispose(reminderEditCtrl);
		
		Reminder reminder = reminderManager.loadByKey(row.getKey());
		reminderEditCtrl = new CourseReminderEditController(ureq, getWindowControl(), reminder);
		listenTo(reminderEditCtrl);
		
		toolbarPanel.pushController(translate("edit.reminder"), reminderEditCtrl);	
	}
	
	private void doDuplicate(UserRequest ureq, ReminderRow row) {
		Reminder reminder = reminderManager.loadByKey(row.getKey());
		reminderManager.duplicate(reminder);
		updateModel();
	}
	
	private void doSend(UserRequest ureq, ReminderRow row) {
		Reminder reminder = reminderManager.loadByKey(row.getKey());
		reminderManager.sendReminder(reminder);
		updateModel();
	}

	private class ToolsController extends BasicController {
		
		private ReminderRow row;
		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ReminderRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();

			addLink("edit", "edit", "o_icon o_icon-fw o_icon_edit", links);
			addLink("duplicate", "duplicate", "o_icon o_icon-fw o_icon_copy", links);
			addLink("send", "send", "o_icon o_icon-fw o_icon_send", links);
			addLink("show.sent", "show.sent", "o_icon o_icon-fw o_icon_show_send", links);
			addLink("delete", "delete", "o_icon o_icon-fw o_icon_delete_item", links);

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
		}
		
		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				
				toolsCalloutCtrl.deactivate();
				cleanUp();
				if("edit".equals(cmd)) {
					doEdit(ureq, row);
				} else if("show.sent".equals(cmd)) {
					doSendReminderList(ureq, row);
				} else if("delete".equals(cmd)) {
					doConfirmDelete(ureq, row);
				} else if("duplicate".equals(cmd)) {
					doDuplicate(ureq, row);
				} else if("send".equals(cmd)) {
					doSend(ureq, row);
				}
					
			}
		}
	}
}
