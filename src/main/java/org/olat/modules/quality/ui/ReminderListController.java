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
package org.olat.modules.quality.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.mail.ui.BooleanCSSCellRenderer;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.ReminderDataModel.ReminderCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReminderListController extends FormBasicController implements TooledController {

	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
			
	private ReminderDataModel dataModel;
	private FlexiTableElement tableEl;
	private Link createReminderLink;
	
	private CloseableModalController cmc;
	private ReminderController reminderCtrl;
	private ReminderDeleteConfirmationController deleteConfirmationCtrl;
	
	private final QualitySecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	private final QualityDataCollectionRef dataCollectionRef;
	
	@Autowired
	private QualityService qualityService;
	
	public ReminderListController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityDataCollectionRef dataCollectionRef) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollectionRef = dataCollectionRef;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.sent,
				new BooleanCSSCellRenderer(getTranslator(), "o_icon o_icon-lg o_icon_qual_rem_sent",
						"o_icon o_icon-lg o_icon_qual_rem_pending", "reminder.sent.done", "reminder.sent.pending")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.sendDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.to));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.subject));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(ReminderCols.edit.i18nHeaderKey(),
				ReminderCols.edit.ordinal(), CMD_EDIT,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_EDIT, "o_icon o_icon-lg o_icon_qual_rem_edit", null),
						null));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel(ReminderCols.delete.i18nHeaderKey(),
				ReminderCols.delete.ordinal(), CMD_DELETE,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_DELETE, "o_icon o_icon-lg o_icon_qual_rem_delete", null),
						null));
		deleteColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(deleteColumn);

		dataModel = new ReminderDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "quality-reminders", dataModel, 25, true, getTranslator(), formLayout);
		loadModel();
	}
	
	private void loadModel() {
		List<QualityReminder> reminders = qualityService.loadReminders(dataCollectionRef);
		dataModel.setObjects(reminders);
		tableEl.reset();
	}

	@Override
	public void initTools() {
		if (secCallback.canEditReminders()) {
			createReminderLink = LinkFactory.createToolLink("reminder.create", translate("reminder.create"), this);
			createReminderLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_rem_create");
			stackPanel.addTool(createReminderLink, Align.left);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				QualityReminder reminder = dataModel.getObject(se.getIndex());
				if (CMD_EDIT.equals(cmd)) {
					doEditReminder(ureq, reminder);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeleteReminder(ureq, reminder);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (createReminderLink == source) {
			doCreateReminder(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == reminderCtrl) {
			cmc.deactivate();
			cleanUp();
			loadModel();
		} else if (source == deleteConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				QualityReminder reminder = deleteConfirmationCtrl.getReminder();
				doDeleteReminder(reminder);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
			loadModel();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(reminderCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		reminderCtrl = null;
		cmc = null;
	}

	private void doCreateReminder(UserRequest ureq) {
		reminderCtrl = new ReminderController(ureq, getWindowControl(), secCallback, dataCollectionRef);
		this.listenTo(reminderCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				reminderCtrl.getInitialComponent(), true, translate("reminder.create.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditReminder(UserRequest ureq, QualityReminder reminder) {
		reminderCtrl = new ReminderController(ureq, getWindowControl(), secCallback, reminder);
		listenTo(reminderCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				reminderCtrl.getInitialComponent(), true, translate("reminder.edit"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doConfirmDeleteReminder(UserRequest ureq, QualityReminder reminder) {
		deleteConfirmationCtrl = new ReminderDeleteConfirmationController(ureq, getWindowControl(), reminder);
		listenTo(deleteConfirmationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteConfirmationCtrl.getInitialComponent(), true, translate("reminder.delete.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doDeleteReminder(QualityReminder reminder) {
		qualityService.deleteReminder(reminder);
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
