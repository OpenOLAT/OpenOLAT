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
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class CopyEventToCalendarController extends FormBasicController {
	
	private static final String[] copy = new String[] {"copy"};

	private final KalendarEvent kalendarEvent;
	private final Collection<KalendarRenderWrapper> calendars;
	private List<MultipleSelectionElement> copyEls;
	
	@Autowired
	private CalendarManager calendarManager;

	public CopyEventToCalendarController(UserRequest ureq, WindowControl wControl,
			KalendarEvent kalendarEvent, Collection<KalendarRenderWrapper> calendars) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));
		
		this.calendars = calendars;
		this.kalendarEvent = kalendarEvent;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("cal.copy.title");
		
		copyEls = new ArrayList<>(calendars.size());
		for (KalendarRenderWrapper calendarWrapper : calendars) {
			String calId = calendarWrapper.getKalendar().getCalendarID();
			String value = calendarWrapper.getDisplayName();
			MultipleSelectionElement copyEl = uifactory
					.addCheckboxesHorizontal("cal_" + calId, null, formLayout, copy, new String[]{ value });
			copyEl.setUserObject(calendarWrapper);
			if (calendarWrapper.getKalendar().getCalendarID().equals(kalendarEvent.getCalendar().getCalendarID())) {
				// this is the calendar, the event comes from
				copyEl.select(copy[0], true);
				copyEl.setEnabled(false);
			} else {
				copyEl.setEnabled(calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (MultipleSelectionElement copyEl : copyEls) {
			if(copyEl.isEnabled() && copyEl.isAtLeastSelected(1)) {
				KalendarRenderWrapper calendarWrapper = (KalendarRenderWrapper)copyEl.getUserObject();
				Kalendar cal = calendarWrapper.getKalendar();
				KalendarEvent clonedKalendarEvent = (KalendarEvent)XStreamHelper.xstreamClone(kalendarEvent);
				if (clonedKalendarEvent.getKalendarEventLinks().size() > 0) {
					clonedKalendarEvent.setKalendarEventLinks(new ArrayList<KalendarEventLink>());
				}
				calendarManager.addEventTo(cal, clonedKalendarEvent);
			}		
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}