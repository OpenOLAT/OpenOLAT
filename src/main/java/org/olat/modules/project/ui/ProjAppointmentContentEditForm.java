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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.CalendarColors;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagRef;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
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
	private static final String CMD_COLOR = "color";

	private TextElement subjectEl;
	private TagSelection tagsEl;
	private FormToggle allDayEl;
	private DateChooser startEl;
	private DateChooser endEl;
	private SingleSelection recurrenceRuleEl;
	private DateChooser recurrenceEndEl;
	private TextElement locationEl;
	private DropdownItem colorEl;
	private TextAreaElement descriptionEl;
	
	private final ProjAppointment appointment;
	private final List<? extends TagInfo> projectTags;
	private final Collection<? extends TagRef> artefactTags;

	@Autowired
	private CalendarManager calendarManager;

	public ProjAppointmentContentEditForm(UserRequest ureq, WindowControl wControl, Form mainForm,
			ProjAppointment appointment, List<? extends TagInfo> projectTags, Collection<? extends TagRef> artefactTags) {
		super(ureq, wControl, LAYOUT_CUSTOM, "appointment_edit", mainForm);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.appointment = appointment;
		this.projectTags = projectTags;
		this.artefactTags = artefactTags;
		
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
		 * - and much moore ...
		 */
		
		
		subjectEl = uifactory.addTextElement("subject", "appointment.edit.subject", 256, appointment.getSubject(), formLayout);
		subjectEl.setMandatory(true);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), projectTags, artefactTags);
		
		allDayEl = uifactory.addToggleButton("all.day", null, "&nbsp;&nbsp;", formLayout, null, null);
		allDayEl.addActionListener(FormEvent.ONCHANGE);
		if (appointment.isAllDay()) {
			allDayEl.toggleOn();
		}
		
		Date startDate = appointment.getStartDate();
		Date endDate = appointment.getEndDate();
		if (startDate == null && endDate == null) {
			startDate = new Date();
			endDate = DateUtils.addHours(startDate, 1);
		}
		startEl = uifactory.addDateChooser("start", "appointment.edit.start", startDate, formLayout);
		startEl.setMandatory(true);
		
		endEl = uifactory.addDateChooser("end", "appointment.edit.end", endDate, formLayout);
		endEl.setMandatory(true);
		
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
		String recurrence = CalendarUtils.getRecurrence(appointment.getRecurrenceRule());
		if (!StringHelper.containsNonWhitespace(recurrence)) {
			recurrence = RECURRENCE_NONE;
		}
		recurrenceRuleEl.select(recurrence, true);
	
		recurrenceEndEl = uifactory.addDateChooser("recurrence.end", "cal.form.recurrence.ends", null, formLayout);
		recurrenceEndEl.setMandatory(true);
		Date recurrenceEnd = calendarManager.getRecurrenceEndDate(appointment.getRecurrenceRule());
		if (recurrenceEnd != null) {
			recurrenceEndEl.setDate(recurrenceEnd);
		}
		
		locationEl = uifactory.addTextElement("location", "cal.form.location", 256, appointment.getLocation(), formLayout);
		
		colorEl = uifactory.addDropdownMenu("color", "", "cal.form.event.color", formLayout, getTranslator());
		colorEl.setElementCssClass("o_proj_color");
		colorEl.setOrientation(DropdownOrientation.normal);
		colorEl.addActionListener(FormEvent.ONCHANGE);
		for (String color : CalendarColors.getColorClasses()) {
			FormLink colorLink = uifactory.addFormLink(color, CMD_COLOR, "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
			if (color.equals(appointment.getColor())){
				colorLink.setIconLeftCSS("o_cal_color_element o_cal_colorchooser_selected " + color);
			} else {
				colorLink.setIconLeftCSS("o_cal_color_element " + color);
			}
			colorLink.setUserObject(color);
			colorEl.addElement(colorLink);
		}
		updateColorUI("o_cal_" + appointment.getColor());
		
		descriptionEl = uifactory.addTextAreaElement("description", "cal.form.description", -1, 3, 40, true, false, appointment.getDescription(), formLayout);
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
	
	private void updateColorUI(String colorCss) {
		if (CalendarColors.colorClassExists(colorCss)) {
			colorEl.setIconCSS("o_cal_color_element " + colorCss);
			colorEl.setUserObject(colorCss);
		} else {
			colorEl.setIconCSS("o_cal_color_element " + ProjectUIFactory.COLOR_APPOINTMENT);
			colorEl.setUserObject(null);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == allDayEl) {
			updateAllDayUI();
		} else if (source == recurrenceRuleEl) {
			updateReccurenceUI();
		} else if (source instanceof FormLink link) {
			if (CMD_COLOR.equals(link.getCmd())) {
				String colorCss = (String)link.getUserObject();
				updateColorUI(colorCss);
			}
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
		
		startEl.clearError();
		if (startEl.getDate() == null) {
			startEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		endEl.clearError();
		if (endEl.getDate() == null) {
			endEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (startEl.getDate() != null && endEl.getDate().before(startEl.getDate())) {
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
		if (colorEl.getUserObject() instanceof String color) {
			return color.substring(6, color.length());
		}
		return null;
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
