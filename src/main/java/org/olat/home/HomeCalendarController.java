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
package org.olat.home;

import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class HomeCalendarController extends BasicController implements Activateable2, GenericEventListener {
	
	private UserSession userSession;
	private CalendarController calendarController;

	@Autowired
	private HomeCalendarManager homeCalendarManager;
	
	public HomeCalendarController(UserRequest ureq, WindowControl windowControl) {
		super(ureq, windowControl);
		this.userSession = ureq.getUserSession();
		
		Identity identity = ureq.getIdentity();
		userSession.getSingleUserEventCenter().registerFor(this, identity, OresHelper.lookupType(CalendarManager.class));
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, OresHelper.lookupType(CalendarManager.class));
		
		OLATResourceable callerOres = OresHelper.createOLATResourceableInstanceWithoutCheck(identity.getName(), identity.getKey());
		List<KalendarRenderWrapper> calendars = homeCalendarManager.getListOfCalendarWrappers(ureq, windowControl);
		calendarController = new WeeklyCalendarController(ureq, windowControl, calendars,
				CalendarController.CALLER_HOME, callerOres, true);
		calendarController.setDifferentiateManagedEvent(true);
		listenTo(calendarController);

		putInitialPanel(calendarController.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty() && calendarController instanceof Activateable2) {
			((Activateable2)calendarController).activate(ureq, entries, state);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof CalendarGUIModifiedEvent) {
			List<KalendarRenderWrapper> calendars = homeCalendarManager.getListOfCalendarWrappers(ureq, getWindowControl());
			calendarController.setCalendars(calendars);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		// remove from event bus
		userSession.getSingleUserEventCenter().deregisterFor(this, OresHelper.lookupType(CalendarManager.class));
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(CalendarManager.class));
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if (event instanceof CalendarGUIModifiedEvent) {
			if (calendarController != null) {
				// could theoretically be disposed 
				calendarController.setDirty();
			}
		}
	}
}
