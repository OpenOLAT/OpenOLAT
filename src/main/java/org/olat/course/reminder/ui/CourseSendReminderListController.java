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
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
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
import org.olat.core.gui.control.WindowControl;
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
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSendReminderListController extends FormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USER_PROPS_ID = CourseSendReminderListController.class.getName();
	
	private FlexiTableElement tableEl;
	private CourseSendReminderTableModel tableModel;
	
	private final Reminder reminder;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ReminderService reminderService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public CourseSendReminderListController(UserRequest ureq, WindowControl wControl, Reminder reminder) {
		super(ureq, wControl, "send_reminder_list");
		this.reminder = reminder;
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SendCols.sendTime.i18nKey(), SendCols.sendTime.ordinal(), true, SendCols.sendTime.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("resend", translate("resend"), "resend"));
		
		tableModel = new CourseSendReminderTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_course_sent_reminder_list");
		updateModel();
	}
	
	private void updateModel() {
		List<SentReminder> sentReminders = reminderService.getSentReminders(reminder);
		List<SentReminderRow> rows = new ArrayList<>(sentReminders.size());
		
		for(SentReminder sentReminder:sentReminders) {
			Identity identity = sentReminder.getIdentity();
			SentReminderRow row = new SentReminderRow(reminder, sentReminder, identity, userPropertyHandlers, getLocale());
			rows.add(row);	
		}

		tableModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				SentReminderRow row = tableModel.getObject(se.getIndex());
				if("resend".equals(cmd)) {
					doResend(row);
				} else if(UserConstants.FIRSTNAME.equals(cmd) || UserConstants.LASTNAME.equals(cmd)) {
					doOpenIdentity(ureq, row);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
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
		updateModel();
	}
	
	private void doOpenIdentity(UserRequest ureq, SentReminderRow row) {
		String businessPath = "[Identity:" + row.getIdentityKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
