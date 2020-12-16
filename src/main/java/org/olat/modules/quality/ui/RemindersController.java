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

import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.security.DataCollectionSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RemindersController extends FormBasicController {

	private DateChooser announcementCoachTopicEl;
	private DateChooser announcementCoachContextEl;
	private DateChooser invitationEl;
	private DateChooser reminder1El;
	private DateChooser reminder2El;
	private FormLayoutContainer buttonLayout;
	
	private DataCollectionSecurityCallback secCallback;
	private QualityDataCollection dataCollection;
	
	private QualityReminder announcementCoachTopic;
	private QualityReminder announcementCoachContext;
	private QualityReminder invitation;
	private QualityReminder reminder1;
	private QualityReminder reminder2;
	
	@Autowired
	private QualityService qualityService;

	public RemindersController(UserRequest ureq, WindowControl wControl, DataCollectionSecurityCallback secCallback,
			QualityDataCollection dataCollection) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.dataCollection = dataCollection;
		Map<QualityReminderType, QualityReminder> typeToReminder = qualityService.loadReminders(dataCollection).stream()
				.collect(Collectors.toMap(QualityReminder::getType, Function.identity()));
		this.announcementCoachTopic = typeToReminder.get(QualityReminderType.ANNOUNCEMENT_COACH_TOPIC);
		this.announcementCoachContext = typeToReminder.get(QualityReminderType.ANNOUNCEMENT_COACH_CONTEXT);
		this.invitation = typeToReminder.get(QualityReminderType.INVITATION);
		this.reminder1 = typeToReminder.get(QualityReminderType.REMINDER1);
		this.reminder2 = typeToReminder.get(QualityReminderType.REMINDER2);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (announcementCoachContext != null) {
			Date announcementCoachContextDate = announcementCoachContext.isSent()
					? announcementCoachContext.getSendDone()
					: announcementCoachContext.getSendPlaned();
			announcementCoachContextEl = uifactory.addDateChooser("reminder.announcement.coach.context.date",
					announcementCoachContextDate, formLayout);
			announcementCoachContextEl.setDateChooserTimeEnabled(true);
			announcementCoachContextEl.setEnabled(false);
		}
		
		if (announcementCoachTopic != null) {
			Date announcementCoachTopicDate = announcementCoachTopic.isSent()
					? announcementCoachTopic.getSendDone()
					: announcementCoachTopic.getSendPlaned();
			announcementCoachTopicEl = uifactory.addDateChooser("reminder.announcement.coach.topic.date",
					announcementCoachTopicDate, formLayout);
			announcementCoachTopicEl.setDateChooserTimeEnabled(true);
			announcementCoachTopicEl.setEnabled(false);
		}
		
		Date invitationDate = invitation != null? invitation.getSendPlaned(): null;
		invitationEl = uifactory.addDateChooser("reminder.invitation.date", invitationDate, formLayout);
		invitationEl.setDateChooserTimeEnabled(true);
		
		Date reminder1Date = reminder1 != null? reminder1.getSendPlaned(): null;
		reminder1El = uifactory.addDateChooser("reminder.reminder1.date", reminder1Date, formLayout);
		reminder1El.setDateChooserTimeEnabled(true);
		
		Date reminder2Date = reminder2 != null? reminder2.getSendPlaned(): null;
		reminder2El = uifactory.addDateChooser("reminder.reminder2.date", reminder2Date, formLayout);
		reminder2El.setDateChooserTimeEnabled(true);
		
		buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		
		updateUI();
	}

	protected void onChanged(QualityDataCollection dataCollection, DataCollectionSecurityCallback secCallback) {
		this.dataCollection = dataCollection;
		this.secCallback = secCallback;
		updateUI();
	}
	
	private void updateUI() {
		boolean canEditInvitation = secCallback.canEditReminder(invitation);
		invitationEl.setEnabled(canEditInvitation);
		boolean canEditReminder1 = secCallback.canEditReminder(reminder1);
		reminder1El.setEnabled(canEditReminder1);
		boolean canEditReminder2 = secCallback.canEditReminder(reminder2);
		reminder2El.setEnabled(canEditReminder2);
		buttonLayout.setVisible(canEditInvitation || canEditReminder1 || canEditReminder2);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		invitation = save(QualityReminderType.INVITATION, invitationEl);
		reminder1 = save(QualityReminderType.REMINDER1, reminder1El);
		reminder2 = save(QualityReminderType.REMINDER2, reminder2El);
		updateUI();
	}
	
	private QualityReminder save(QualityReminderType type, DateChooser reminderEl) {
		QualityReminder savedReminder = null;
		if (reminderEl.isEnabled()) {
			QualityReminder reminder = qualityService.loadReminder(dataCollection, type);
			if (reminderEl.getDate() != null) {
				if (reminder != null) {
					savedReminder = qualityService.updateReminderDatePlaned(reminder, reminderEl.getDate());
				} else {
					savedReminder = qualityService.createReminder(dataCollection, reminderEl.getDate(), type);
				}
			} else {
				qualityService.deleteReminder(reminder);
			}
		}
		return savedReminder;
	}

	@Override
	protected void doDispose() {
		//
	}

}
