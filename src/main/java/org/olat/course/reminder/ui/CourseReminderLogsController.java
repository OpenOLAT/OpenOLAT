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

import static org.olat.course.reminder.ui.CourseSendReminderListController.USER_PROPS_ID;
import static org.olat.course.reminder.ui.CourseSendReminderListController.USER_PROPS_OFFSET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.reminder.model.SentReminderRow;
import org.olat.course.reminder.ui.CourseSendReminderTableModel.SendCols;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.SentReminder;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseReminderLogsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CourseSendReminderTableModel tableModel;
	
	private StepsMainRunController wizardCtrl;
	
	private final RepositoryEntry repositoryEntry;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ReminderService reminderService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public CourseReminderLogsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "send_reminder_list");
		this.repositoryEntry = repositoryEntry;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SendCols.status.i18nKey(), SendCols.status.ordinal(),
				 true, SendCols.status.name(), new StatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SendCols.reminder.i18nKey(), SendCols.reminder.ordinal(),
				"reminder", true, SendCols.reminder.name(), new StaticFlexiCellRenderer("reminder", new TextFlexiCellRenderer())));
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			
			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName)
					|| UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex, userPropertyHandler.getName(), true, propName,
						new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SendCols.sendTime.i18nKey(), SendCols.sendTime.ordinal(),
				true, SendCols.sendTime.name()));

		DefaultFlexiColumnModel resendCol = new DefaultFlexiColumnModel("resend", translate("resend"), "resend");
		resendCol.setAlwaysVisible(true);
		resendCol.setExportable(false);
		columnsModel.addFlexiColumnModel(resendCol);
		
		tableModel = new CourseSendReminderTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_course_sent_reminder_log_list");
		tableEl.setAndLoadPersistedPreferences(ureq, "course-reminders-logs-v2");
		updateModel();
	}
	
	protected void updateModel() {
		List<SentReminder> sentReminders = reminderService.getSentReminders(repositoryEntry);
		List<SentReminderRow> rows = new ArrayList<>(sentReminders.size());
		
		for(SentReminder sentReminder:sentReminders) {
			Identity identity = sentReminder.getIdentity();
			Reminder reminder = sentReminder.getReminder();
			SentReminderRow row = new SentReminderRow(reminder, sentReminder, identity, userPropertyHandlers, getLocale());
			rows.add(row);	
		}

		tableModel.setObjects(rows);
		tableEl.reset();
		tableEl.setVisible(rows.size() > 0);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (wizardCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateModel();
			}
			getWindowControl().pop();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(wizardCtrl);
		wizardCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				SentReminderRow row = tableModel.getObject(se.getIndex());
				if("reminder".equals(cmd)) {
					doOpenReminder(ureq, row);
				} else if("resend".equals(cmd)) {
					doResend(row);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenReminder(UserRequest ureq, SentReminderRow row) {
		removeAsListenerAndDispose(wizardCtrl);
		
		Reminder reminder = reminderService.loadByKey(row.getReminderKey());
		wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), new RulesEditStep(ureq, reminder),
				doSaveReminder(), null, translate("edit.reminder"), "");
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private StepRunnerCallback doSaveReminder() {
		return (uureq, control, runContext) -> {
			Reminder reminder = (Reminder)runContext.get(RulesEditStep.CONTEXT_KEY);
			reminderService.save(reminder);
			return StepsMainRunController.DONE_MODIFIED;
		};
	}
	
	private void doResend(SentReminderRow row) {
		Reminder reloadedReminder = reminderService.loadByKey(row.getReminderKey());
		Identity id = securityManager.loadIdentityByKey(row.getIdentityKey());
		List<Identity> identitiesToRemind = Collections.singletonList(id);
		MailerResult result = reminderService.sendReminder(reloadedReminder, identitiesToRemind);
		if(result.getReturnCode() != MailerResult.OK) {
			MailHelper.printErrorsAndWarnings(result, getWindowControl(), false, getLocale());
		} else {
			showInfo("reminder.resend");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}