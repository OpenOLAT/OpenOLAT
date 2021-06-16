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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.reminder.ui.CourseReminderSendTableModel.SendCols;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.SentReminder;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseReminderSendController extends FormBasicController {

	private final List<UserPropertyHandler> userPropertyHandlers;
	private CourseReminderSendTableModel dataModel;
	private FlexiTableElement tableEl;
	private FormSubmit sendUnsentLink;
	private FormLink sendAllLink;
	
	private RulesViewController rulesCtrl;
	
	private final Reminder reminder;
	private final boolean readonly;
	
	@Autowired
	private ReminderService reminderService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	protected BaseSecurity securityManager;

	public CourseReminderSendController(UserRequest ureq, WindowControl wControl, Reminder reminder, boolean readonly) {
		super(ureq, wControl, "send_reminders");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.reminder = reminder;
		this.readonly = readonly;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(CourseReminderSendTableModel.USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		rulesCtrl = new RulesViewController(ureq, getWindowControl(), reminder.getEntry(), reminder.getConfiguration());
		listenTo(rulesCtrl);
		flc.put("rules", rulesCtrl.getInitialComponent());
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = CourseReminderSendTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(CourseReminderSendTableModel.USAGE_IDENTIFIER , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SendCols.sendDate));
		
		dataModel = new CourseReminderSendTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setSortSettings(options);
		tableEl.setEmptyTableSettings("send.no.members", null, "o_icon_reminder");
		
		if (!readonly) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonsCont.setRootForm(mainForm);
			formLayout.add(buttonsCont);
			buttonsCont.setElementCssClass("o_button_group o_button_group_right");
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			sendAllLink = uifactory.addFormLink("send.all", buttonsCont, Link.BUTTON);
			sendAllLink.setElementCssClass("o_sel_course_reminder_send_all");
			sendUnsentLink = uifactory.addFormSubmitButton("send.unsent", buttonsCont);
			sendUnsentLink.setElementCssClass("o_sel_course_reminder_unsent");
		}
	}
	
	private void loadModel() {
		List<Identity> identites = reminderService.getIdentities(reminder);
		Map<Long, Date> identityKeyToSendDate = reminderService.getSentReminders(reminder).stream()
				.collect(Collectors.toMap(
						sent -> sent.getIdentity().getKey(),
						SentReminder::getCreationDate,
						(date1, date2) -> (date1.after(date2)? date1: date2)));
		
		List<CourseReminderSendRow> rows = new ArrayList<>(identites.size());
		for (Identity identity : identites) {
			CourseReminderSendRow row = new CourseReminderSendRow(identity, userPropertyHandlers, getLocale());
			Date sendDate = identityKeyToSendDate.get(identity.getKey());
			row.setSendDate(sendDate);
			rows.add(row);
		}
		
		flc.contextPut("showUnsent", Boolean.valueOf(!identites.isEmpty()));
		flc.contextPut("all", String.valueOf(identites.size()));
		int unsent = identites.size() - identityKeyToSendDate.size();
		if (unsent < 0) {
			unsent = 0;
		}
		flc.contextPut("unsent", String.valueOf(unsent));
		if (!readonly) {
			sendAllLink.setI18nKey("send.all", new String[] { String.valueOf(identites.size()) } );
			sendUnsentLink.setI18nKey("send.unsent", new String[] { String.valueOf(unsent) } );
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == sendAllLink) {
			fireEvent(ureq, new SendEvent(reminder, true));
		}
		super.formInnerEvent(ureq, source, event);
	}
	

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new SendEvent(reminder, false));
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static final class SendEvent extends Event {
		
		private static final long serialVersionUID = 7982338274757827457L;
		
		private final Reminder reminder;
		private final boolean resend;
		
		public SendEvent(Reminder reminder, boolean resend) {
			super("reminder-send");
			this.reminder = reminder;
			this.resend = resend;
		}
		
		public Reminder getReminder() {
			return reminder;
		}
		
		public boolean isResend() {
			return resend;
		}
		
	}

}
