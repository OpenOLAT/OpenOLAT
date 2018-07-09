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

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderTo;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReminderController extends FormBasicController {

	private static final String SEND_NOW_KEY = "reminder.send.now";
	private static final String[] SEND_KEYS = new String[] {SEND_NOW_KEY};

	private MultipleSelectionElement sendSelectionEl;
	private DateChooser sendDateEl;
	private SingleSelection toEl;
	private TextElement subjectEl;
	private TextAreaElement bodyEl;
	private FormSubmit saveEl;
	private FormSubmit saveAndSendEl;
	
	private final QualityDataCollectionRef dataCollectionRef;
	private QualityReminder reminder;
	private QualitySecurityCallback secCallback;
	
	@Autowired
	private QualityService qualityService;
	
	public ReminderController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			QualityDataCollectionRef dataCollectionRef) {
		this(ureq, wControl, secCallback, null, dataCollectionRef);
	}

	public ReminderController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			QualityReminder reminder) {
		this(ureq, wControl, secCallback, reminder, null);
	}

	public ReminderController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			QualityReminder reminder, QualityDataCollectionRef dataCollectionRef) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.reminder = reminder;
		this.dataCollectionRef = dataCollectionRef;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		sendSelectionEl = uifactory.addCheckboxesVertical("reminder.send.selection", formLayout, SEND_KEYS,
				translateAll(getTranslator(), SEND_KEYS), 1);
		sendSelectionEl.addActionListener(FormEvent.ONCHANGE);
		sendSelectionEl.select(SEND_NOW_KEY, isNewReminder());
		
		sendDateEl = uifactory.addDateChooser("reminder.send.date", null, formLayout);
		sendDateEl.setDateChooserTimeEnabled(true);
		sendDateEl.setMandatory(true);
		
		toEl = uifactory.addRadiosVertical("reminder.to", formLayout, QualityReminderTo.getKeys(),
				QualityReminderTo.getValues(getTranslator()));
		toEl.select(QualityReminderTo.getKeys()[0], true);
		toEl.setMandatory(true);
		
		subjectEl= uifactory.addTextElement("reminder.subject", 500, "", formLayout);
		subjectEl.setMandatory(true);
		
		bodyEl = uifactory.addTextAreaElement("reminder.body", 12, 72, "", formLayout);
		bodyEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		saveEl = uifactory.addFormSubmitButton("save", buttonsCont);
		saveAndSendEl = uifactory.addFormSubmitButton("reminder.send.now.button", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		
		updateUI();
	}

	private void updateUI() {
		if (!isNewReminder()) {
			sendDateEl.setDate(reminder.getSendDate());
			subjectEl.setValue(reminder.getSubject());
			toEl.select(reminder.getTo().getKey(), true);
			subjectEl.setValue(reminder.getSubject());
			bodyEl.setValue(reminder.getBody());
		}

		boolean sendLater = !sendSelectionEl.getSelectedKeys().contains(SEND_NOW_KEY);
		sendDateEl.setVisible(sendLater);
		
		boolean canEditReminder = secCallback.canEditReminder(reminder);
		sendSelectionEl.setEnabled(canEditReminder);
		sendDateEl.setEnabled(canEditReminder);
		toEl.setEnabled(canEditReminder);
		subjectEl.setEnabled(canEditReminder);
		bodyEl.setEnabled(canEditReminder);
		saveEl.setVisible(canEditReminder && sendLater);
		saveAndSendEl.setVisible(canEditReminder && !sendLater);
	}

	private boolean isNewReminder() {
		return reminder == null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == sendSelectionEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		sendDateEl.clearError();
		if (sendDateEl.isVisible() && sendDateEl.getDate() == null) {
			sendDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		toEl.clearError();
		if (!toEl.isOneSelected()) {
			toEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		subjectEl.clearError();
		if (!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
			subjectEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		bodyEl.clearError();
		if (!StringHelper.containsNonWhitespace(bodyEl.getValue())) {
			bodyEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (reminder == null) {
			reminder = qualityService.createReminder(dataCollectionRef);
		}

		boolean sendLater = !sendSelectionEl.getSelectedKeys().contains(SEND_NOW_KEY);
		Date sendDate = sendLater? sendDateEl.getDate(): new Date();
		reminder.setSendDate(sendDate);
		QualityReminderTo to = QualityReminderTo.getEnum(toEl.getSelectedKey());
		reminder.setTo(to);
		reminder.setSubject(subjectEl.getValue());
		reminder.setBody(bodyEl.getValue());
		
		reminder = qualityService.saveReminder(reminder);
		
		if (!sendLater) {
			reminder = qualityService.sendReminder(reminder);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
