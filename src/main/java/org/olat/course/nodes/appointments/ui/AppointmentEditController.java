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
package org.olat.course.nodes.appointments.ui;

import java.util.Collections;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.ParticipationSearchParams;
import org.olat.course.nodes.appointments.Topic;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentEditController extends FormBasicController {
	
	private DateChooser startEl;
	private DateChooser endEl;
	private TextElement locationEl;
	private TextElement maxParticipationsEl;
	private TextElement detailsEl;
	
	private Topic topic;
	private Appointment appointment;
	
	@Autowired
	private AppointmentsService appointmentsService;

	public AppointmentEditController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		this.topic = topic;
		initForm(ureq);
	}

	public AppointmentEditController(UserRequest ureq, WindowControl wControl, Appointment appointment) {
		super(ureq, wControl);
		this.appointment = appointment;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Date start = appointment != null? appointment.getStart(): null;
		startEl = uifactory.addDateChooser("appointment.start", start, formLayout);
		startEl.setDateChooserTimeEnabled(true);
		startEl.setMandatory(true);
		startEl.addActionListener(FormEvent.ONCHANGE);
		
		Date end = appointment != null? appointment.getEnd(): null;
		endEl = uifactory.addDateChooser("appointment.end", end, formLayout);
		endEl.setDateChooserTimeEnabled(true);
		endEl.setMandatory(true);
		
		String location = appointment != null? appointment.getLocation(): null;
		locationEl = uifactory.addTextElement("appointment.location", 128, location, formLayout);
		
		String maxParticipations = appointment != null && appointment.getMaxParticipations() != null
				? appointment.getMaxParticipations().toString()
				: null;
		maxParticipationsEl = uifactory.addTextElement("appointment.max.participations", 5, maxParticipations,
				formLayout);
		
		String details = appointment == null ? "" : appointment.getDetails();
		detailsEl = uifactory.addTextAreaElement("appointment.details", "appointment.details", 2000, 4, 72, false,
				false, details, formLayout);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		String changeI18n = appointment == null? "add.appointment.button": "edit.appointment.button";
		uifactory.addFormSubmitButton(changeI18n, buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == startEl) {
			if (startEl.getDate() != null && endEl.getDate() == null) {
				endEl.setDate(startEl.getDate());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		startEl.clearError();
		endEl.clearError();
		if (startEl.getDate() == null) {
			startEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		if (endEl.getDate() == null) {
			endEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (startEl.getDate() != null && endEl.getDate() != null) {
			Date start = startEl.getDate();
			Date end = endEl.getDate();
			if(end.before(start)) {
				endEl.setErrorKey("error.start.after.end", null);
				allOk &= false;
			}
		}
		
		maxParticipationsEl.clearError();
		String maxParticipationsValue = maxParticipationsEl.getValue();
		if (StringHelper.containsNonWhitespace(maxParticipationsValue)) {
			try {
				int value = Integer.parseInt(maxParticipationsValue);
				if (value < 1) {
					maxParticipationsEl.setErrorKey("error.positiv.number", null);
					allOk &= false;
				} else {
					ParticipationSearchParams params = new ParticipationSearchParams();
					params.setAppointments(Collections.singletonList(appointment));
					Long count = appointmentsService.getParticipationCount(params);
					if (count.doubleValue() > value) {
						maxParticipationsEl.setErrorKey("error.too.much.participations",
								new String[] { count.toString() });
						allOk &= false;
					}
				}
			} catch (NumberFormatException e) {
				maxParticipationsEl.setErrorKey("error.positiv.number", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (appointment == null) {
			appointment = appointmentsService.createUnsavedAppointment(topic);
		}
		
		Date start = startEl.getDate();
		appointment.setStart(start);
		
		Date end = endEl.getDate();
		appointment.setEnd(end);
		
		String location = locationEl.getValue();
		appointment.setLocation(location);
		
		String details = detailsEl.getValue();
		appointment.setDetails(details);
		
		String maxParticipationsValue = maxParticipationsEl.getValue();
		Integer maxParticipations = StringHelper.containsNonWhitespace(maxParticipationsValue)
				? Integer.valueOf(maxParticipationsValue)
				: null;
		appointment.setMaxParticipations(maxParticipations);
		
		appointmentsService.saveAppointment(appointment);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
