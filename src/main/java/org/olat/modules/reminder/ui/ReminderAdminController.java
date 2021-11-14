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
package org.olat.modules.reminder.ui;

import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.reminder.ReminderInterval;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.model.SendTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderAdminController extends FormBasicController {
	
	private static final String[] enableKeys = new String[]{ "on" };
	
	private static final String[] intervalKeys = new String[]{
		ReminderInterval.every24.key(),
		ReminderInterval.every12.key(),
		ReminderInterval.every8.key(),
		ReminderInterval.every6.key(),
		ReminderInterval.every4.key(),
		ReminderInterval.every2.key(),
		ReminderInterval.every1.key()
	};
	
	private MultipleSelectionElement enableEl;
	private TextElement hoursEl, minutesEl;
	private SingleSelection timezoneEl;
	private SingleSelection intervalEl;
	private FormLayoutContainer timeLayout;
	
	private String[] intervalValues;
	
	@Autowired
	private ReminderModule reminderModule;
	
	public ReminderAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		intervalValues = new String[]{
				translate(ReminderInterval.every24.i18nKey()),
				translate(ReminderInterval.every12.i18nKey()),
				translate(ReminderInterval.every8.i18nKey()),
				translate(ReminderInterval.every6.i18nKey()),
				translate(ReminderInterval.every4.i18nKey()),
				translate(ReminderInterval.every2.i18nKey()),
				translate(ReminderInterval.every1.i18nKey())
		};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("reminder.admin.title");
		
		String[] enableValues = new String[]{ translate("on") };
		
		enableEl = uifactory.addCheckboxesHorizontal("enable.reminders", formLayout, enableKeys, enableValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.select(enableKeys[0], reminderModule.isEnabled());
		
		String interval = reminderModule.getInterval();
		intervalEl = uifactory.addDropdownSingleselect("interval", formLayout, intervalKeys, intervalValues, null);
		boolean found = false;
		if(StringHelper.containsNonWhitespace(interval)) {
			for(String intervalKey:intervalKeys) {
				if(intervalKey.equals(interval)) {
					intervalEl.select(intervalKey, true);
					found = true;
				}
			}
		}
		if(!found) {
			intervalEl.select(intervalKeys[0], true);
		}
		
		int hour = 9;
		int minute = 0;
		
		SendTime parsedTime = SendTime.parse(reminderModule.getDefaultSendTime());
		if(parsedTime.isValid()) {
			hour = parsedTime.getHour();
			minute = parsedTime.getMinute();
		}
		
		String timePage = velocity_root + "/time.html";
		timeLayout = FormLayoutContainer.createCustomFormLayout("send.time", getTranslator(), timePage);
		timeLayout.setRootForm(mainForm);
		formLayout.add(timeLayout);
		timeLayout.setLabel("default.send.time", null);
		
		String hourStr = (hour < 10 ? "0" : "") + hour;
		hoursEl = uifactory.addTextElement("hours", null, 2, hourStr, timeLayout);
		hoursEl.setDisplaySize(2);
		hoursEl.setDomReplacementWrapperRequired(false);

		String minuteStr = (minute < 10 ? "0" : "") + minute;
		minutesEl = uifactory.addTextElement("minutes", null, 2, minuteStr, timeLayout);
		minutesEl.setDisplaySize(2);
		minutesEl.setDomReplacementWrapperRequired(false);
		
		String[] timezoneKeys = TimeZone.getAvailableIDs();
		String[] timezoneValues = new String[timezoneKeys.length];
		for(int i=timezoneKeys.length; i-->0; ) {
			timezoneValues[i] = timezoneKeys[i] + " (" + TimeZone.getTimeZone(timezoneKeys[i]).getDisplayName(true, TimeZone.LONG, getLocale()) + ")";
		}
		timezoneEl = uifactory.addDropdownSingleselect("timezone", null, timeLayout, timezoneKeys, timezoneValues, null);
		timezoneEl.setEnabled(false);
		((AbstractComponent)timezoneEl.getComponent()).setDomReplacementWrapperRequired(false);
		TimeZone defaultTimeZone = reminderModule.getDefaultSendTimeZone();
		for(int i=timezoneKeys.length; i-->0; ) {
			if(defaultTimeZone.getID().equals(timezoneKeys[i])) {
				timezoneEl.select(defaultTimeZone.getID(), true);
			}
		}
		timeLayout.setVisible(reminderModule.isEnabled());
		
		/*
		enableSmsEl = uifactory.addCheckboxesHorizontal("enable.sms.reminders", formLayout, enableKeys, enableValues);
		enableSmsEl.addActionListener(FormEvent.ONCHANGE);
		enableSmsEl.select(enableKeys[0], reminderModule.isSmsEnabled());
		enableSmsEl.setVisible(reminderModule.isEnabled());
		*/
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			boolean enabled = enableEl.isAtLeastSelected(1);
			timeLayout.setVisible(enabled);
			intervalEl.setVisible(enabled);
			//enableSmsEl.setVisible(enabled);
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		hoursEl.clearError();
		minutesEl.clearError();
		boolean enabled = enableEl.isAtLeastSelected(1);
		if(enabled) {
			allOk &= validate(hoursEl, 23);
			allOk &= validate(minutesEl, 59);
		}
		
		return allOk;
	}
	
	private boolean validate(TextElement textEl, int max) {
		boolean allOk = true;
		
		String value = textEl.getValue();
		if(StringHelper.containsNonWhitespace(value)) {
			try {
				int val = Integer.parseInt(value);
				if(val < 0) {
					textEl.setErrorKey("text.element.error.minvalue", new String[]{ "0" });
				} else if(val > max) {
					textEl.setErrorKey("text.element.error.maxvalue", new String[]{ Integer.toString(max) });
				}
			} catch (NumberFormatException e) {
				textEl.setErrorKey("integer.element.int.error", null);
				allOk &= false;
			}
		} else {
			textEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		reminderModule.setEnabled(enabled);
		
		if(enabled) {
			String interval = intervalEl.getSelectedKey();

			String hoursStr = hoursEl.getValue();
			String minutesStr = minutesEl.getValue();
			String sendTime = Integer.parseInt(hoursStr) + ":" + Integer.parseInt(minutesStr);
			reminderModule.setScheduler(interval, sendTime);
			
			if(timezoneEl.isOneSelected()) {
				String timeZoneID = timezoneEl.getSelectedKey();
				TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
				reminderModule.setDefaultSendTimeZone(timeZone);
			}
		}
		
		/*
		boolean smsEnabled = enabled && enableSmsEl.isAtLeastSelected(1);
		reminderModule.setSmsEnabled(smsEnabled);
		*/
	}
}