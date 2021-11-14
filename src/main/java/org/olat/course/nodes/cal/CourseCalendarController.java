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

package org.olat.course.nodes.cal;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.run.calendar.CourseCalendarSubscription;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * <h3>Description:</h3>
 * This is wrapper around the WeeklyCalendarController.
 * <p>
 * Initial Date:  10 nov. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CourseCalendarController extends DefaultController {

	private final WeeklyCalendarController calendarController;
	private KalendarRenderWrapper courseKalendarWrapper;
	private CourseCalendarSubscription calendarSubscription;

	private CalSecurityCallback secCallback;
	
	private UserCourseEnvironment courseEnv;
	private List<KalendarRenderWrapper> calendars;

	public CourseCalendarController(UserRequest ureq, WindowControl wControl, CourseCalendars myCal,
			UserCourseEnvironment courseEnv, CalSecurityCallback secCallback) {
		super(wControl);
		this.courseEnv = courseEnv;
		this.secCallback = secCallback;
		calendars = myCal.getCalendars();
		courseKalendarWrapper = myCal.getCourseKalendarWrapper();
		calendarController = new WeeklyCalendarController(ureq, wControl, calendars, WeeklyCalendarController.CALLER_COURSE,
				courseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource(), false);
		calendarController.setDifferentiateManagedEvent(CourseCalendars.needToDifferentiateManagedEvents(calendars));
		setInitialComponent(calendarController.getInitialComponent());
	}	

	public CourseCalendarSubscription getCalendarSubscription() {
		return calendarSubscription;
	}

	public KalendarRenderWrapper getCourseKalendarWrapper() {
		return courseKalendarWrapper;
	}

	public void setFocus(Date date) {
		calendarController.setFocus(date);
	}
	
	public void setFocusOnEvent(String eventId, String recurrenceId) {
		calendarController.setFocusOnEvent(eventId, recurrenceId);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
	// nothing to do
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof CalendarGUIModifiedEvent) {
			CourseCalendars myCal = CourseCalendars.createCourseCalendarsWrapper(ureq, getWindowControl(), courseEnv, secCallback);
			calendars = myCal.getCalendars();
			courseKalendarWrapper = myCal.getCourseKalendarWrapper();
			calendarController.setCalendars(calendars);
		}
	}

	@Override
	protected void doDispose() {
		calendarController.dispose();
        super.doDispose();
	}
}
