/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.commons.calendar.ui;

import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.springframework.beans.factory.annotation.Autowired;


public class CalendarEntryForm extends FormBasicController {

	public static final String SUBMIT_MULTI = "multi";
	public static final String SUBMIT_SINGLE = "single";

	public static final String RECURRENCE_NONE = "NONE";

	private StaticTextElement calendarName;
	private SingleSelection chooseCalendar;
	private TextElement subjectEl, descriptionEl, locationEl, liveStreamUrlEl;
	private FormLink colorLink;
	private FormLink colorResetLink;
	private MultipleSelectionElement liveStreamUrlTypeEl;
	private SingleSelection liveStreamUrlTemplateEl;
	private SelectionElement allDayEvent;
	
	private DateChooser begin, end;
	private SingleSelection classification;
	private SingleSelection chooseRecurrence;
	private DateChooser recurrenceEnd;
	private FormLink deleteEventButton;
	
	private CloseableCalloutWindowController calloutCtrl;
	private CalendarColorChooserController colorChooserCtrl;
	
	private KalendarEvent event;
	private KalendarRenderWrapper choosenWrapper;
	private List<KalendarRenderWrapper> writeableCalendars;
	private String colorCssClass;
	private final boolean readOnly, isNew;
	private final String caller;
	
	private String[] calendarKeys, calendarValues;
	private String[] keysRecurrence, valuesRecurrence;
	private String[] classKeys, classValues;
	
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private LiveStreamService liveStreamService;
	
	/**
	 * Display an event for modification or to add a new event.
	 * 
	 * @param name
	 * @param event
	 * @param choosenWrapper
	 * @param availableCalendars	At least one calendar must be editable if this is a new event.
	 * @param isNew		If it is a new event, display a list of calendars to choose from.
	 * @param caller 
	 * @param locale
	 */
	public CalendarEntryForm(UserRequest ureq, WindowControl wControl, KalendarEvent event, KalendarRenderWrapper choosenWrapper,
			Collection<KalendarRenderWrapper> availableCalendars, boolean isNew, String caller) {
		super(ureq, wControl);
		this.caller = caller;
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		this.event = event;
		this.choosenWrapper = choosenWrapper;
		readOnly = choosenWrapper == null
				? false : choosenWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;
		this.isNew = isNew;
		
		writeableCalendars = new ArrayList<>();
		for (Iterator<KalendarRenderWrapper> iter = availableCalendars.iterator(); iter.hasNext();) {
			KalendarRenderWrapper calendarRenderWrapper = iter.next();
			if (calendarRenderWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) {
				writeableCalendars.add(calendarRenderWrapper);
			}
		}
		
		calendarKeys = new String[writeableCalendars.size()];
		calendarValues = new String[writeableCalendars.size()];
		for (int i = 0; i < writeableCalendars.size(); i++) {
			KalendarRenderWrapper cw = writeableCalendars.get(i);
			calendarKeys[i] = cw.getKalendar().getCalendarID();
			calendarValues[i] = cw.getDisplayName();
		}

		keysRecurrence = new String[] {
				RECURRENCE_NONE,
				KalendarEvent.DAILY,
				KalendarEvent.WORKDAILY,
				KalendarEvent.WEEKLY,
				KalendarEvent.BIWEEKLY,
				KalendarEvent.MONTHLY,
				KalendarEvent.YEARLY
		};
		valuesRecurrence = new String[] {
				translate("cal.form.recurrence.none"),
				translate("cal.form.recurrence.daily"),
				translate("cal.form.recurrence.workdaily"),
				translate("cal.form.recurrence.weekly"),
				translate("cal.form.recurrence.biweekly"),
				translate("cal.form.recurrence.monthly"),
				translate("cal.form.recurrence.yearly")
		};
		
		// classification
		classKeys = new String[] {"0", "1", "2"};
		classValues = new String[] {
				getTranslator().translate("cal.form.class.private"),
				getTranslator().translate("cal.form.class.freebusy"),
				getTranslator().translate("cal.form.class.public")
		};
	
		initForm(ureq);
	}
	
	protected void setEntry(KalendarEvent kalendarEvent) {
		// subject
		if (readOnly && kalendarEvent.getClassification() == KalendarEvent.CLASS_X_FREEBUSY) {
			subjectEl.setValue(getTranslator().translate("cal.form.subject.hidden"));
		} else {
			subjectEl.setValue(kalendarEvent.getSubject());
		}
		// location
		if (readOnly && kalendarEvent.getClassification() == KalendarEvent.CLASS_X_FREEBUSY) {
			locationEl.setValue(getTranslator().translate("cal.form.location.hidden"));
		} else {
			locationEl.setValue(kalendarEvent.getLocation());
		}
	
		doUpdateColor(kalendarEvent);
		
		begin.setDate(kalendarEvent.getBegin());
		end.setDate(kalendarEvent.getEnd());
		boolean allDay = kalendarEvent.isAllDayEvent();
		allDayEvent.select("xx", allDay);
		end.setDateChooserTimeEnabled(!allDay);
		begin.setDateChooserTimeEnabled(!allDay);
		
		switch (kalendarEvent.getClassification()) {
			case KalendarEvent.CLASS_PRIVATE: classification.select("0", true); break;
			case KalendarEvent.CLASS_X_FREEBUSY: classification.select("1", true); break;
			case KalendarEvent.CLASS_PUBLIC: classification.select("2", true); break;
			default: classification.select("0", true);
		}
		
		if(StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			chooseRecurrence.setVisible(false);
		} else {
			String recurrence = CalendarUtils.getRecurrence(kalendarEvent.getRecurrenceRule());
			if(recurrence != null && !recurrence.equals("") && !recurrence.equals(RECURRENCE_NONE)) {
				chooseRecurrence.select(recurrence, true);
				Date recurEnd = calendarManager.getRecurrenceEndDate(kalendarEvent.getRecurrenceRule());
				if(recurEnd != null) {
					recurrenceEnd.setDate(recurEnd);
				}
			} else {
				chooseRecurrence.select(RECURRENCE_NONE, true);
			}
		}
		
		updateLiveStreamUI(kalendarEvent);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		begin.clearError();
		if (begin.getDate() == null) {
			begin.setErrorKey("cal.form.error.date", null);
			allOk &= false;
		} else if(!validateFormItem(begin)) {
			allOk &= false;
		}
		
		end.clearError();
		if (end.getDate() == null) {
			end.setErrorKey("cal.form.error.date", null);
			allOk &= false;
		} else if(!validateFormItem(end)) {
			allOk &= false;
		} else if (begin.getDate() != null && end.getDate().before(begin.getDate())) {
			end.setErrorKey("cal.form.error.endbeforebegin", null);
			allOk &= false;
		}
		
		boolean hasEnd = !chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE);
		recurrenceEnd.clearError();
		if (hasEnd && recurrenceEnd.getDate() == null) {
			recurrenceEnd.setErrorKey("cal.form.error.date", null);
			allOk &= false;
		}
		
		if (hasEnd && recurrenceEnd.getDate() != null && begin.getDate() != null
				&& recurrenceEnd.getDate().before(begin.getDate())) {
			recurrenceEnd.setErrorKey("cal.form.error.endbeforebegin", null);
			allOk &= false;
		}
		
		return allOk;
	}

	/**
	 * Get event with updated values.
	 * @return
	 */
	public KalendarEvent getUpdatedKalendarEvent() {
		// subject
		event.setSubject(subjectEl.getValue());
		// description
		event.setDescription(descriptionEl.getValue());
		// location
		event.setLocation(locationEl.getValue());
		// color
		String color = StringHelper.containsNonWhitespace(colorCssClass)
				? colorCssClass.substring(6, colorCssClass.length())
				: null;
		event.setColor(color);

		// date / time
		event.setBegin(begin.getDate());
		event.setEnd(end.getDate());
		event.setLastModified(new Date().getTime());
		if(event.getCreated() == 0) {
			event.setCreated(new Date().getTime());
		}
		
		// allday event?
		event.setAllDayEvent(allDayEvent.isSelected(0));

		// classification
		switch (classification.getSelected()) {
			case 0: event.setClassification(KalendarEvent.CLASS_PRIVATE); break;
			case 1: event.setClassification(KalendarEvent.CLASS_X_FREEBUSY); break;
			case 2: event.setClassification(KalendarEvent.CLASS_PUBLIC); break;
			default: throw new OLATRuntimeException("getSelected() in KalendarEntryForm.classification returned weitrd value", null);
		}

		// recurrence
		if (chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE)) {
			event.setRecurrenceRule(null);
		} else {
			String rrule = calendarManager.getRecurrenceRule(chooseRecurrence.getSelectedKey(), recurrenceEnd.getDate());
			event.setRecurrenceRule(rrule);
		}
		
		// Live Stream
		event.setLiveStreamUrlTemplateKey(null);
		if (liveStreamUrlTemplateEl.isVisible() && liveStreamUrlTemplateEl.isEnabled() && liveStreamUrlTemplateEl.isOneSelected()) {
			event.setLiveStreamUrlTemplateKey(Long.valueOf(liveStreamUrlTemplateEl.getSelectedKey()));
			event.setLiveStreamUrl(getLiveStreamUrlFromSelection());
		}
		if (liveStreamUrlEl.isVisible() && liveStreamUrlEl.isEnabled()) {
			String liveStreamUrl = StringHelper.containsNonWhitespace(liveStreamUrlEl.getValue())
					? liveStreamUrlEl.getValue()
					: null;
			event.setLiveStreamUrl(liveStreamUrl);
		}

		return event;
	}


	public String getChoosenKalendarID() {
		if (chooseCalendar == null) {
			return choosenWrapper.getKalendar().getCalendarID();
		}
		return chooseCalendar.getSelectedKey();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_cal_entry_form");
		
		setFormTitle("cal.form.title");
		setFormContextHelp("manual_user/personal/Calendar/");
		
		chooseCalendar = uifactory.addDropdownSingleselect("cal.form.chooseCalendar", formLayout, calendarKeys, calendarValues, null);
		if(choosenWrapper != null) {
			chooseCalendar.select(choosenWrapper.getKalendar().getCalendarID(), true);
		} else {
			chooseCalendar.select(calendarKeys[0], true);
		}
		chooseCalendar.addActionListener(FormEvent.ONCHANGE);
		chooseCalendar.setVisible(isNew);
		if(event.getManagedFlags() != null && event.getManagedFlags().length > 0) {
			chooseCalendar.setEnabled(false);
		}
		
		String calName = choosenWrapper == null ? "" : StringHelper.escapeHtml(choosenWrapper.getDisplayName());
		calendarName = uifactory.addStaticTextElement("calendarname", "cal.form.calendarname", calName, formLayout);
		calendarName.setVisible(!isNew);
		
		boolean fb = readOnly && event.getClassification() == KalendarEvent.CLASS_X_FREEBUSY;
		String subject = fb ? translate("cal.form.subject.hidden") : event.getSubject();
		if(subject != null && subject.length() > 64) {
			subjectEl = uifactory.addTextAreaElement("subject", "cal.form.subject", -1, 3, 40, true, false, subject, formLayout);
		} else {
			subjectEl = uifactory.addTextElement("subject", "cal.form.subject", 255, subject, formLayout);
		}
		subjectEl.setMandatory(true);
		subjectEl.setNotEmptyCheck("cal.form.error.mandatory");
		subjectEl.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.subject));
		subjectEl.setElementCssClass("o_sel_cal_subject");
		
		String description = event.getDescription();
		descriptionEl = uifactory.addTextAreaElement("description", "cal.form.description", -1, 3, 40, true, false, description, formLayout);
		descriptionEl.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.description));
		descriptionEl.setElementCssClass("o_sel_cal_description");
		
		String location = fb ? translate("cal.form.location.hidden") : event.getLocation();
		if(location != null && location.length() > 64) {
			locationEl = uifactory.addTextAreaElement("location", "cal.form.location", -1, 3, 40, true, false, location, formLayout);
		} else {
			locationEl = uifactory.addTextElement("location", "cal.form.location", 255, location, formLayout);
		}
		locationEl.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.location));
		locationEl.setElementCssClass("o_sel_cal_location");
		
		FormLayoutContainer colorLinks = FormLayoutContainer.createCustomFormLayout("color.links", getTranslator(), velocity_root + "/event_color.html");
		colorLinks.setRootForm(mainForm);
		colorLinks.setLabel("cal.form.event.color", null);
		formLayout.add(colorLinks);
		
		colorLink = uifactory.addFormLink("color", "color", "", "", colorLinks, Link.NONTRANSLATED);
		colorLink.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.color));
		
		colorResetLink = uifactory.addFormLink("cal.form.event.color.reset", colorLinks, Link.BUTTON_XSMALL);
		doUpdateColor(event);
		
		boolean managedDates = CalendarManagedFlag.isManaged(event, CalendarManagedFlag.dates);
		allDayEvent = uifactory.addCheckboxesHorizontal("allday", "cal.form.allday", formLayout, new String[]{"xx"}, new String[]{null});
		allDayEvent.select("xx", event.isAllDayEvent());
		allDayEvent.addActionListener(FormEvent.ONCHANGE);
		allDayEvent.setEnabled(!managedDates);
		allDayEvent.setElementCssClass("o_sel_cal_all_day");
		
		begin = uifactory.addDateChooser("begin", "cal.form.begin", null, formLayout);
		begin.setDisplaySize(21);
		begin.setDateChooserTimeEnabled(!event.isAllDayEvent());
		begin.setValidDateCheck("form.error.date");
		begin.setMandatory(true);
		begin.setDate(event.getBegin());
		begin.setEnabled(!managedDates);
		begin.setElementCssClass("o_sel_cal_begin");
		
		end = uifactory.addDateChooser("end", "cal.form.end", null, formLayout);
		end.setDisplaySize(21);
		end.setDateChooserTimeEnabled(!event.isAllDayEvent());
		end.setValidDateCheck("form.error.date");
		end.setMandatory(true);
		end.setDate(event.getEnd());
		end.setEnabled(!managedDates);
		end.setElementCssClass("o_sel_cal_end");
		
		chooseRecurrence = uifactory.addDropdownSingleselect("cal.form.recurrence", formLayout, keysRecurrence, valuesRecurrence, null);
		String currentRecur = CalendarUtils.getRecurrence(event.getRecurrenceRule());
		boolean rk = currentRecur != null && !currentRecur.equals("");
		chooseRecurrence.select(rk ? currentRecur:RECURRENCE_NONE, true);
		chooseRecurrence.addActionListener(FormEvent.ONCHANGE);
		chooseRecurrence.setEnabled(!managedDates);
		chooseRecurrence.setVisible(!StringHelper.containsNonWhitespace(event.getRecurrenceID()));
		
		recurrenceEnd = uifactory.addDateChooser("recurrence", "cal.form.recurrence.end", null, formLayout);
		recurrenceEnd.setDisplaySize(21);
		recurrenceEnd.setDateChooserTimeEnabled(false);
		recurrenceEnd.setMandatory(true);
		recurrenceEnd.setElementCssClass("o_sel_cal_until");
		Date recurEnd = calendarManager.getRecurrenceEndDate(event.getRecurrenceRule());
		if(recurEnd != null) {
			recurrenceEnd.setDate(recurEnd);
		}
		recurrenceEnd.setEnabled(!managedDates);
		recurrenceEnd.setVisible(!chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE));
		
		liveStreamUrlTypeEl = uifactory.addCheckboxesHorizontal("cal.live.stream.url.type", formLayout, emptyStrings(), emptyStrings());
		liveStreamUrlTypeEl.addActionListener(FormEvent.ONCHANGE);
		
		liveStreamUrlTemplateEl = uifactory.addDropdownSingleselect("cal.live.stream.url.template", formLayout, emptyStrings(), emptyStrings());
		liveStreamUrlTemplateEl.addActionListener(FormEvent.ONCHANGE);
		
		liveStreamUrlEl = uifactory.addTextElement("cal.live.stream.url", 2000, event.getLiveStreamUrl(), formLayout);
		updateLiveStreamUI(event);
		
		classification = uifactory.addRadiosVertical("classification", "cal.form.class", formLayout, classKeys, classValues);
		classification.setHelpUrlForManualPage("manual_user/personal/Calendar/#visibility");
		//classification.setHelpTextKey("cal.form.class.hover", null);
		classification.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.classification));
		switch (event.getClassification()) {
			case KalendarEvent.CLASS_PRIVATE: classification.select("0", true); break;
			case KalendarEvent.CLASS_X_FREEBUSY: classification.select("1", true); break;
			case KalendarEvent.CLASS_PUBLIC: classification.select("2", true); break;
			default: classification.select("0", true);
		}

		StringBuilder buf = new StringBuilder();
		if (event.getCreated() != 0) {
			buf.append(StringHelper.formatLocaleDateTime(event.getCreated(), getTranslator().getLocale()));
			if (event.getCreatedBy() != null && !event.getCreatedBy().equals("")) {
				buf.append(" ");
				buf.append(getTranslator().translate("cal.form.created.by"));
				buf.append(" ");
				buf.append(StringHelper.escapeHtml(event.getCreatedBy()));
			} 
		} else {
			buf.append("-");
		}
		uifactory.addStaticTextElement("cal.form.created.label", buf.toString(), formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton(SUBMIT_SINGLE, "cal.form.submitSingle", buttonLayout);
		
		if (readOnly) {
			flc.setEnabled(false);
		}  else if(!isNew) {
			deleteEventButton = uifactory.addFormLink("delete", "cal.edit.delete", null, buttonLayout, Link.BUTTON);
			deleteEventButton.setElementCssClass("o_sel_cal_delete");
		}
	}

	private void updateLiveStreamUI(KalendarEvent kalendarEvent) {
		boolean isLiveStream = CalendarController.CALLER_LIVE_STREAM.equals(caller);
		boolean liveStreamNotManaged = !CalendarManagedFlag.isManaged(kalendarEvent, CalendarManagedFlag.liveStreamUrl);
		
		liveStreamUrlTypeEl.setVisible(isLiveStream && liveStreamNotManaged);
		liveStreamUrlTemplateEl.setVisible(isLiveStream && liveStreamNotManaged);
		
		liveStreamUrlEl.setValue(kalendarEvent.getLiveStreamUrl());

		boolean liveStreamUrlManually = true;
		if (liveStreamUrlTypeEl.isVisible()) {
			List<UrlTemplate> urlTemplates = liveStreamService.getAllUrlTemplates();
			if (urlTemplates.isEmpty()) {
				liveStreamUrlTypeEl.setVisible(false);
				liveStreamUrlTemplateEl.setVisible(false);
				return;
			}
			
			SelectionValues typeKV = new SelectionValues();
			typeKV.add(SelectionValues.entry("key", translate("cal.live.stream.url.type.manually")));
			SelectionValues urlTemplateKV = new SelectionValues();
			liveStreamUrlTypeEl.setKeysAndValues(typeKV.keys(), typeKV.values());
			
			urlTemplates.forEach(urlTemplate -> urlTemplateKV.add(SelectionValues.entry(urlTemplate.getKey().toString(), urlTemplate.getName())));
			urlTemplateKV.sort(SelectionValues.VALUE_ASC);
			liveStreamUrlTemplateEl.setKeysAndValues(urlTemplateKV.keys(), urlTemplateKV.values(), null);
			liveStreamUrlTemplateEl.select(liveStreamUrlTemplateEl.getKey(0), true);
			
			Long urlTemplateKey = kalendarEvent.getLiveStreamUrlTemplateKey();
			if (isValidUrlTemplateKey(urlTemplateKey)) {
				liveStreamUrlTemplateEl.select(urlTemplateKey.toString(), true);
				liveStreamUrlManually = false;
			} else if (!StringHelper.containsNonWhitespace(kalendarEvent.getLiveStreamUrl())) {
				liveStreamUrlTemplateEl.select(liveStreamUrlTemplateEl.getKey(0), true);
				liveStreamUrlManually = false;
			}
			liveStreamUrlTypeEl.select(liveStreamUrlTypeEl.getKey(0), liveStreamUrlManually);
		}
		liveStreamUrlTemplateEl.setVisible(!liveStreamUrlManually);
		liveStreamUrlEl.setVisible(isLiveStream && liveStreamUrlManually);
		liveStreamUrlEl.setEnabled(liveStreamNotManaged);
	}

	private boolean isValidUrlTemplateKey(Long urlTemplateKey) {
		return urlTemplateKey != null 
				&& Arrays.stream(liveStreamUrlTemplateEl.getKeys()).anyMatch(key -> key.equals(urlTemplateKey.toString()));
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent e) {
		if (source == colorLink) {
			doChooseColor(ureq);
		} else if (source == colorResetLink) {
			doSetColor(null);
		} else if (source == chooseCalendar) {
			doSetColor(this.colorCssClass);
		} else if (source == chooseRecurrence) {
			recurrenceEnd.setVisible(!chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE));
		} else if(allDayEvent == source) {
			boolean allDay = allDayEvent.isSelected(0);
			begin.setDateChooserTimeEnabled(!allDay);
			end.setDateChooserTimeEnabled(!allDay);
		} else if (source == liveStreamUrlTypeEl) {
			boolean manually = liveStreamUrlTypeEl.isAtLeastSelected(1);
			liveStreamUrlTemplateEl.setVisible(!manually);
			liveStreamUrlEl.setVisible(manually);
		} else if (source == liveStreamUrlTemplateEl) {
			doSyncLiveStreamUrl();
		} else if(deleteEventButton == source) {
			fireEvent(ureq, new Event("delete"));
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(colorChooserCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSetColor(colorChooserCtrl.getChoosenColor());
			}
			calloutCtrl.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(colorChooserCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		colorChooserCtrl = null;
		calloutCtrl = null;
	}

	private void doSyncLiveStreamUrl() {
		String liveStreamUrl = getLiveStreamUrlFromSelection();
		liveStreamUrlEl.setValue(liveStreamUrl);
	}

	private String getLiveStreamUrlFromSelection() {
		Long key = Long.valueOf(liveStreamUrlTemplateEl.getSelectedKey());
		UrlTemplate urlTemplate = liveStreamService.getUrlTemplate(key);
		String url = liveStreamService.concatUrls(urlTemplate);
		return StringHelper.containsNonWhitespace(url) ? url : null;
	}
	
	private void doChooseColor(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(colorChooserCtrl);
		
		colorChooserCtrl = new CalendarColorChooserController(ureq, getWindowControl(), colorCssClass);
		listenTo(colorChooserCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				colorChooserCtrl.getInitialComponent(), colorLink.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doUpdateColor(KalendarEvent kalendarEvent) {
		String color = StringHelper.containsNonWhitespace(kalendarEvent.getColor())
				? "o_cal_".concat(kalendarEvent.getColor())
				: null;
		doSetColor(color);
	}
	
	private void doSetColor(String choosenColor) {
		this.colorCssClass = choosenColor;
		
		boolean canReset = !CalendarManagedFlag.isManaged(event, CalendarManagedFlag.color)
				&& StringHelper.containsNonWhitespace(choosenColor);
		colorResetLink.setVisible(canReset);
		
		String colorIcon = null;
		if (CalendarColorChooserController.colorExists(choosenColor)) {
			colorIcon = "o_cal_color_element ".concat(choosenColor);
		} else if (choosenWrapper != null) {
			colorIcon = "o_cal_color_element ".concat(choosenWrapper.getCssClass());
		} else {
			String kalendarID = getChoosenKalendarID();
			Optional<KalendarRenderWrapper> selectedWrapper = writeableCalendars.stream()
					.filter(cal -> cal.getKalendar().getCalendarID().equals(kalendarID))
					.findFirst();
			if (selectedWrapper.isPresent()) {
				colorIcon = "o_cal_color_element ".concat(selectedWrapper.get().getCssClass());
			}
		}
		
		colorLink.setIconLeftCSS(colorIcon);
	}

}
