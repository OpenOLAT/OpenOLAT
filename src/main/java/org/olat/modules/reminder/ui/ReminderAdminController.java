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
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
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
	
	private MultipleSelectionElement enableEl;
	private IntegerElement hoursEl, minutesEl;
	private SingleSelection timezoneEl;
	private FormLayoutContainer timeLayout;
	
	@Autowired
	private ReminderModule reminderModule;
	
	public ReminderAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("reminder.admin.title");
		
		String[] enableValues = new String[]{ translate("on") };
		
		enableEl = uifactory.addCheckboxesHorizontal("enable.reminders", formLayout, enableKeys, enableValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.select(enableKeys[0], reminderModule.isEnabled());
		
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
		hoursEl = uifactory.addIntegerElement("hours", null, hour, timeLayout);
		hoursEl.setDisplaySize(2);
		((AbstractComponent)hoursEl.getComponent()).setDomReplacementWrapperRequired(false);
		minutesEl = uifactory.addIntegerElement("minutes", null, minute, timeLayout);
		minutesEl.setDisplaySize(2);
		((AbstractComponent)minutesEl.getComponent()).setDomReplacementWrapperRequired(false);
		String[] timezoneKeys = TimeZone.getAvailableIDs();
		String[] timezoneValues = new String[timezoneKeys.length];
		for(int i=timezoneKeys.length; i-->0; ) {
			timezoneValues[i] = timezoneKeys[i] + " (" + TimeZone.getTimeZone(timezoneKeys[i]).getDisplayName(true, TimeZone.LONG, getLocale()) + ")";
		}
		timezoneEl = uifactory.addDropdownSingleselect("timezone", timeLayout, timezoneKeys, timezoneValues, null);
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
			//enableSmsEl.setVisible(enabled);
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		reminderModule.setEnabled(enabled);
		
		if(enabled) {
			int hours = hoursEl.getIntValue();
			int minutes = minutesEl.getIntValue();
			String sendTime = hours + ":" + minutes;
			reminderModule.setDefaultSendTime(sendTime);
			
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