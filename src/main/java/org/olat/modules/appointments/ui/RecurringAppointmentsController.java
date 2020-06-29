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
package org.olat.modules.appointments.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Topic;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RecurringAppointmentsController extends FormBasicController {
	
	private TextElement locationEl;
	private TextElement maxParticipationsEl;
	private DateChooser recurringFirstEl;
	private MultipleSelectionElement recurringDaysOfWeekEl;
	private DateChooser recurringLastEl;

	private final Topic topic;
	
	@Autowired
	private AppointmentsService appointmentsService;

	public RecurringAppointmentsController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		this.topic = topic;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		locationEl = uifactory.addTextElement("appointment.location", 128, null, formLayout);
		locationEl.setHelpTextKey("appointment.init.value", null);
		
		maxParticipationsEl = uifactory.addTextElement("appointment.max.participations", 5, null, formLayout);
		maxParticipationsEl.setHelpTextKey("appointment.init.value", null);
		
		recurringFirstEl = uifactory.addDateChooser("appointments.recurring.first", null, formLayout);
		recurringFirstEl.setDateChooserTimeEnabled(true);
		recurringFirstEl.setSecondDate(true);
		recurringFirstEl.setSameDay(true);
		recurringFirstEl.setMandatory(true);
		
		DayOfWeek[] dayOfWeeks = DayOfWeek.values();
		KeyValues dayOfWeekKV = new KeyValues();
		for (int i = 0; i < dayOfWeeks.length; i++) {
			dayOfWeekKV.add(entry(dayOfWeeks[i].name(), dayOfWeeks[i].getDisplayName(TextStyle.FULL_STANDALONE, getLocale())));
		}
		recurringDaysOfWeekEl = uifactory.addCheckboxesHorizontal("appointments.recurring.days.of.week", formLayout,
				dayOfWeekKV.keys(), dayOfWeekKV.values());
		recurringDaysOfWeekEl.setMandatory(true);
		
		recurringLastEl = uifactory.addDateChooser("appointments.recurring.last", null, formLayout);
		recurringLastEl.setMandatory(true);
		
		// Buttons
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		maxParticipationsEl.clearError();
		String maxParticipationsValue = maxParticipationsEl.getValue();
		if (maxParticipationsEl.isVisible() && StringHelper.containsNonWhitespace(maxParticipationsValue)) {
			try {
				int value = Integer.parseInt(maxParticipationsValue);
				if (value < 1) {
					maxParticipationsEl.setErrorKey("error.positiv.number", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				maxParticipationsEl.setErrorKey("error.positiv.number", null);
				allOk &= false;
			}
		}
		
		recurringFirstEl.clearError();
		recurringDaysOfWeekEl.clearError();
		recurringLastEl.clearError();
		if (recurringFirstEl.getDate() == null || recurringFirstEl.getSecondDate() == null) {
			recurringFirstEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (recurringFirstEl.getDate().after(recurringFirstEl.getSecondDate())) {
			recurringFirstEl.setErrorKey("error.start.after.end", null);
			allOk &= false;
		}
		
		if (!recurringDaysOfWeekEl.isAtLeastSelected(1)) {
			recurringDaysOfWeekEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (recurringLastEl.getDate() == null) {
			recurringLastEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (recurringFirstEl.getDate() != null && recurringLastEl.getDate() != null
				&& recurringFirstEl.getDate().after(recurringLastEl.getDate())) {
			recurringLastEl.setErrorKey("error.first.after.start", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSaveReccuringAppointments();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void doSaveReccuringAppointments() {
		Date firstStart = recurringFirstEl.getDate();
		Date firstEnd = recurringFirstEl.getSecondDate();
		
		Date last = recurringLastEl.getDate();
		last = DateUtils.setTime(last, 23, 59, 59);
		
		Collection<DayOfWeek> daysOfWeek = recurringDaysOfWeekEl.getSelectedKeys().stream()
				.map(DayOfWeek::valueOf)
				.collect(Collectors.toList());
		
		List<Date> starts = DateUtils.getDaysInRange(firstStart, last, daysOfWeek);
		for (Date start : starts) {
			Appointment appointment = appointmentsService.createUnsavedAppointment(topic);
			
			appointment.setStart(start);
			
			Date end = DateUtils.copyTime(start, firstEnd);
			appointment.setEnd(end);
			
			String location = locationEl.getValue();
			appointment.setLocation(location);
			
			if (maxParticipationsEl.isVisible()) {
				String maxParticipationsValue = maxParticipationsEl.getValue();
				Integer maxParticipations = StringHelper.containsNonWhitespace(maxParticipationsValue)
						? Integer.valueOf(maxParticipationsValue)
						: null;
				appointment.setMaxParticipations(maxParticipations);
			}
			
			appointmentsService.saveAppointment(appointment);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

}
