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
package org.olat.modules.project.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.CalendarColors;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjAppointment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAppointmentContentEditForm extends FormBasicController {
	
	private static final String RECURRENCE_NONE = "none";

	private TextElement subjectEl;
	private TagSelection tagsEl;
	private FormToggle allDayEl;
	private DateChooser startEl;
	private DateChooser endEl;
	private SingleSelection recurrenceRuleEl;
	private DateChooser recurrenceEndEl;
	private TextElement locationEl;
	private ColorPickerElement colorPickerEl;
	private String color;
	private FormLink colorResetLink;
	private TextAreaElement descriptionEl;
	
	private final ProjAppointment appointment;
	private final boolean template;
	private final List<? extends TagInfo> projectTags;
	private Date startDate;

	@Autowired
	private CalendarManager calendarManager;

	public ProjAppointmentContentEditForm(UserRequest ureq, WindowControl wControl, Form mainForm,
			ProjAppointment appointment, boolean template, List<? extends TagInfo> projectTags, Date initialStartDate) {
		super(ureq, wControl, LAYOUT_CUSTOM, "appointment_edit", mainForm);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.appointment = appointment;
		this.template = template;
		this.projectTags = projectTags;
		this.startDate = !template? initialStartDate: null;
		
		initForm(ureq);
		updateAllDayUI();
		updateReccurenceUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		/*
		 * If you want to use this class as a replacement of CalendarEntryForm:
		 * - Move it to org.olat.commons.calendar.ui
		 * - Change ProjAppointment to KalendarEvent
		 * - Add further stuff from CalendarEntryForm: managed flags, read-only, live stream, ...
		 * - Make a subclass in projects to set the tags and the mapping from event to appointment
		 * - and much more ...
		 */
		
		String subject = appointment != null? appointment.getSubject(): null;
		subjectEl = uifactory.addTextElement("subject", "appointment.edit.subject", 256, subject, formLayout);
		subjectEl.setMandatory(true);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), projectTags);
		
		allDayEl = uifactory.addToggleButton("all.day", null, null, null, formLayout);
		allDayEl.addActionListener(FormEvent.ONCHANGE);
		if (appointment != null && appointment.isAllDay()) {
			allDayEl.toggleOn();
		}
		
		Date endDate = appointment != null? appointment.getEndDate(): null;
		if (endDate == null && startDate != null) {
			endDate = DateUtils.addHours(startDate, 1);
		}
		startEl = uifactory.addDateChooser("start", "appointment.edit.start", startDate, formLayout);
		startEl.setEnabled(!template);
		startEl.addActionListener(FormEvent.ONCHANGE);
		
		endEl = uifactory.addDateChooser("end", "appointment.edit.end", endDate, formLayout);
		endEl.setEnabled(!template);
		
		SelectionValues recurrenceSV = new SelectionValues();
		recurrenceSV.add(entry(RECURRENCE_NONE, translate("cal.form.recurrence.none")));
		recurrenceSV.add(entry(KalendarEvent.DAILY, translate("cal.form.recurrence.daily")));
		recurrenceSV.add(entry(KalendarEvent.WORKDAILY, translate("cal.form.recurrence.workdaily")));
		recurrenceSV.add(entry(KalendarEvent.WEEKLY, translate("cal.form.recurrence.weekly")));
		recurrenceSV.add(entry(KalendarEvent.BIWEEKLY, translate("cal.form.recurrence.biweekly")));
		recurrenceSV.add(entry(KalendarEvent.MONTHLY, translate("cal.form.recurrence.monthly")));
		recurrenceSV.add(entry(KalendarEvent.YEARLY, translate("cal.form.recurrence.yearly")));
		recurrenceRuleEl = uifactory.addDropdownSingleselect("recurrence.rule", "cal.form.recurrence", formLayout,
				recurrenceSV.keys(), recurrenceSV.values());
		recurrenceRuleEl.addActionListener(FormEvent.ONCHANGE);
		String recurrence = appointment != null? CalendarUtils.getRecurrence(appointment.getRecurrenceRule()): null;
		if (!StringHelper.containsNonWhitespace(recurrence)) {
			recurrence = RECURRENCE_NONE;
		}
		recurrenceRuleEl.select(recurrence, true);
	
		recurrenceEndEl = uifactory.addDateChooser("recurrence.end", "cal.form.recurrence.ends", null, formLayout);
		recurrenceEndEl.setMandatory(true);
		String recurrenceRule = appointment != null? appointment.getRecurrenceRule(): null;
		Date recurrenceEnd = calendarManager.getRecurrenceEndDate(recurrenceRule);
		if (recurrenceEnd != null) {
			recurrenceEndEl.setDate(recurrenceEnd);
		}
		
		String location =appointment != null?  appointment.getLocation(): null;
		locationEl = uifactory.addTextElement("location", "cal.form.location", 256, location, formLayout);

		colorPickerEl = uifactory.addColorPickerElement("color", "cal.form.event.color", formLayout, CalendarColors.getColorsList());
		colorPickerEl.addActionListener(FormEvent.ONCHANGE);
		if (appointment != null && appointment.getColor() != null && CalendarColors.getColorsList().contains(appointment.getColor())) {
			color = appointment.getColor();
		} else {
			color = null;
		}
		colorPickerEl.setCssPrefix("o_cal");
		colorResetLink = uifactory.addFormLink("reset", "cal.form.event.color.reset", "", formLayout, Link.BUTTON);
		updateColor();

		String description = appointment != null? appointment.getDescription(): null;
		descriptionEl = uifactory.addTextAreaElement("description", "cal.form.description", -1, 3, 40, true, false, description, formLayout);
	}
	
	private void updateAllDayUI() {
		boolean allDay = allDayEl.isOn();
		startEl.setDateChooserTimeEnabled(!allDay);
		endEl.setDateChooserTimeEnabled(!allDay);
	}
	
	private void updateReccurenceUI() {
		boolean reccurend = recurrenceRuleEl.isOneSelected() && !RECURRENCE_NONE.equals(recurrenceRuleEl.getSelectedKey());
		recurrenceEndEl.setVisible(reccurend );
	}

	private void updateColor() {
		colorResetLink.setVisible(color != null);
		if (color != null) {
			colorPickerEl.setColor(color);
		} else {
			colorPickerEl.setColor(CalendarColors.colorFromColorClass(ProjectUIFactory.COLOR_APPOINTMENT));
		}
	}

	private void syncEndDate() {
		Date newStartdDate = startEl.getDate();
		Date endDate = endEl.getDate();
		if (newStartdDate == null || startDate == null || endDate == null) {
			return;
		}
		
		Duration duration = Duration.between(DateUtils.toLocalDateTime(startDate), DateUtils.toLocalDateTime(newStartdDate));
		Date newEndDate = DateUtils.toDate(DateUtils.toLocalDateTime(endDate).plus(duration));
		endEl.setDate(newEndDate);
		startDate = newStartdDate;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == allDayEl) {
			updateAllDayUI();
		} else if (source == startEl) {
			syncEndDate();
		} else if (source == recurrenceRuleEl) {
			updateReccurenceUI();
		} else if (source == colorPickerEl) {
			color = colorPickerEl.getColor().getId();
			updateColor();
		} else if (source == colorResetLink) {
			color = null;
			updateColor();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		subjectEl.clearError();
		if (!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
			subjectEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		endEl.clearError();
		if (startEl.getDate() != null && endEl.getDate() != null && endEl.getDate().before(startEl.getDate())) {
			endEl.setErrorKey("cal.form.error.endbeforebegin");
			allOk &= false;
		}
		
		recurrenceEndEl.clearError();
		if (recurrenceEndEl.isVisible()) {
			if (recurrenceEndEl.getDate() == null) {
				recurrenceEndEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			} else if (recurrenceEndEl.getDate() != null && startEl.getDate() != null
					&& recurrenceEndEl.getDate().before(startEl.getDate())) {
				recurrenceEndEl.setErrorKey("cal.form.error.endbeforebegin");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public Date getStartDate() {
		return startEl.getDate();
	}
	
	public Date getEndDate() {
		return endEl.getDate();
	}
	
	public String getSubject() {
		return subjectEl.getValue();
	}
	
	public String getDescription() {
		return descriptionEl.getValue();
	}
	
	public String getLocation() {
		return locationEl.getValue();
	}
	
	public String getColor() {
		return color;
	}
	
	public boolean isAllDay() {
		return allDayEl.isOn();
	}
	
	public String getRecurrenceRule() {
		return recurrenceRuleEl.isOneSelected() && !RECURRENCE_NONE.equals(recurrenceRuleEl.getSelectedKey())
				? calendarManager.getRecurrenceRule(recurrenceRuleEl.getSelectedKey(), recurrenceEndEl.getDate())
				: null;
	}
	
	public List<String> getTagDisplayValues() {
		return tagsEl.getDisplayNames();
	}

}
