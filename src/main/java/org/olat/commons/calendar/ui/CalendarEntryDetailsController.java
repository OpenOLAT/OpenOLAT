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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIDeleteEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;


public class CalendarEntryDetailsController extends FormBasicController {

	private FormLink deleteEventButton;

	private final KalendarRenderWrapper calendarWrapper;
	private final Collection<KalendarRenderWrapper> availableCalendars;
	private final boolean isNew;
	private Boolean linksOpen = Boolean.FALSE;
	private final String caller;
	private KalendarEvent kalendarEvent;
	private CalendarEntryForm eventForm;
	private CloseableModalController cmc;
	private ConfirmDeleteController deleteCtr;
	private ConfirmUpdateController updateCtr;
	private CalendarEntryLinksController calendarEntryLinksCtrl;
	private final List<KalendarEventLink> links;


	@Autowired
	private CalendarManager calendarManager;

	public CalendarEntryDetailsController(UserRequest ureq, KalendarEvent kalendarEvent, KalendarRenderWrapper calendarWrapper,
			List<KalendarRenderWrapper> availableCalendars, boolean isNew, String caller, WindowControl wControl) {
		super(ureq, wControl, "calendar_entry");
		setTranslator(Util.createPackageTranslator(CalendarModule.class, getLocale(), getTranslator()));

		this.availableCalendars = availableCalendars;
		this.kalendarEvent = kalendarEvent;
		this.isNew = isNew;
		this.caller = caller;
		this.calendarWrapper = calendarWrapper;
		this.links = new ArrayList<>(kalendarEvent.getKalendarEventLinks());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		eventForm = new CalendarEntryForm(ureq, getWindowControl(), kalendarEvent, calendarWrapper, availableCalendars, isNew, caller, mainForm);
		listenTo(eventForm);
		formLayout.add("calendar_entry_form", eventForm.getInitialFormItem());

		calendarEntryLinksCtrl = new CalendarEntryLinksController(ureq, getWindowControl(),
				kalendarEvent, calendarWrapper, availableCalendars, mainForm, caller, eventForm, isNew);
		listenTo(calendarEntryLinksCtrl);
		formLayout.add("calendar_entry_links", calendarEntryLinksCtrl.getInitialFormItem());
		flc.contextPut("linksOpen", linksOpen);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		buttonLayout.setElementCssClass("o_sel_cal_buttons");
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton(CalendarEntryForm.SUBMIT_SINGLE, "cal.form.submitSingle", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());

		if(!isNew) {
			deleteEventButton = uifactory.addFormLink("delete", "cal.edit.delete", null, buttonLayout, Link.BUTTON);
			deleteEventButton.setElementCssClass("o_sel_cal_delete pull-right");
			deleteEventButton.setIconLeftCSS("o_icon o_icon_deleted");
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		kalendarEvent.setKalendarEventLinks(links);
		eventForm.setEntry(kalendarEvent);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String linksOpenVal = ureq.getParameter("referenceOpen");
			if (StringHelper.containsNonWhitespace(linksOpenVal)) {
				linksOpen = Boolean.valueOf(linksOpenVal);
				flc.contextPut("linksOpen", linksOpen);
			}
		} else if (source == deleteEventButton) {
			doConfirmDelete(ureq);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteCtr) {
			if(event instanceof CalendarGUIDeleteEvent deleteEvent) {
				doDelete(deleteEvent);
				cmc.deactivate();
				cleanUp();

				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == eventForm) {
			if (event == Event.DONE_EVENT) {
				doSave(ureq);
			} else if("delete".equals(event.getCommand())) {
				doConfirmDelete(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				eventForm.setEntry(kalendarEvent);
				// user canceled, finish workflow
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(source == updateCtr) {
			if(event instanceof CalendarGUIUpdateEvent updateEvent) {
				doUpdate(updateEvent);
				cmc.deactivate();
				cleanUp();

				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		}  else if(cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteCtr);
		removeAsListenerAndDispose(updateCtr);
		removeAsListenerAndDispose(cmc);
		updateCtr = null;
		deleteCtr = null;
		cmc = null;
	}

	private void doConfirmDelete(UserRequest ureq) {
		deleteCtr = new ConfirmDeleteController(ureq, getWindowControl(), kalendarEvent);
		listenTo(deleteCtr);

		String title = translate("cal.edit.delete");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCtr.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doDelete(CalendarGUIDeleteEvent event) {
		switch (event.getCascade()) {
			case all -> calendarManager.removeEventFrom(kalendarEvent.getCalendar(), kalendarEvent);
			case once -> {
				if (kalendarEvent instanceof KalendarRecurEvent kalendarRecurEvent) {
					calendarManager.removeOccurenceOfEvent(kalendarEvent.getCalendar(), kalendarRecurEvent);
				}
			}
			case future -> {
				if (kalendarEvent instanceof KalendarRecurEvent kalendarRecurEvent) {
					calendarManager.removeFutureOfEvent(kalendarEvent.getCalendar(), kalendarRecurEvent);
				}
			}
		}
	}

	private void doSave(UserRequest ureq) {
		List<CalendarEntryLinkRow> calendarEntryLinkRows = calendarEntryLinksCtrl.getCalendarEntryLinkRows();

		// update kalendarEventLinks by given name values
		for (CalendarEntryLinkRow calendarEntryLinkRow : calendarEntryLinkRows) {
			kalendarEvent.getKalendarEventLinks()
					.stream()
					.filter(k -> k.getId().equals(calendarEntryLinkRow.getKey()))
					.findFirst().ifPresent(kalendarEventLink -> kalendarEventLink.setDisplayName(calendarEntryLinkRow.getLinkTitleEl().getValue()));
		}

		// ok, save edited entry
		kalendarEvent = eventForm.getUpdatedKalendarEvent();

		if (isNew) {
			boolean doneSuccessfully = true;
			// this is a new event, add event to calendar
			String calendarID = eventForm.getChoosenKalendarID();
			for (KalendarRenderWrapper calendarWrapper : availableCalendars) {
				if (!calendarWrapper.getKalendar().getCalendarID().equals(calendarID)) {
					continue;
				}
				Kalendar cal = calendarWrapper.getKalendar();
				boolean result = calendarManager.addEventTo(cal, kalendarEvent);
				if (!result) {
					// if one failed => done not successfully
					doneSuccessfully = false;
				}
			}
			reportSaveStatus(ureq, doneSuccessfully);
		} else if(kalendarEvent instanceof KalendarRecurEvent kalendarRecurEvent && !StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			updateCtr = new ConfirmUpdateController(ureq, getWindowControl(), kalendarRecurEvent);
			listenTo(updateCtr);

			String title = translate("cal.edit.update");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), updateCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else {
			// this is an existing event, so we get the previousely assigned calendar from the event
			Kalendar cal = kalendarEvent.getCalendar();
			boolean doneSuccessfully = calendarManager.updateEventFrom(cal, kalendarEvent);
			reportSaveStatus(ureq, doneSuccessfully);
		}
	}

	private void doUpdate(CalendarGUIUpdateEvent event) {
		switch (event.getCascade()) {
			case all -> calendarManager.updateEventFrom(kalendarEvent.getCalendar(), kalendarEvent);
			case once -> {
				if (kalendarEvent instanceof KalendarRecurEvent kalendarRecurEvent) {
					kalendarEvent = calendarManager.createKalendarEventRecurringOccurence(kalendarRecurEvent);
					calendarManager.addEventTo(kalendarRecurEvent.getCalendar(), kalendarEvent);
				}
			}
		}
	}

	private void reportSaveStatus(UserRequest ureq, boolean doneSuccessfully) {
		// check if event is still available
		if (doneSuccessfully) {
			// saving was ok, finish workflow
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			showError("cal.error.save");
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}

	public KalendarEvent getKalendarEvent() {
		return kalendarEvent;
	}
}
