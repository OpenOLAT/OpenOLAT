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
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.course.nodes.cal;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.clone.CloneableController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.run.calendar.CourseCalendarSubscription;
import org.olat.course.run.userview.NodeEvaluation;

/**
 * 
 * <h3>Description:</h3>
 * This is wrapper around the WeeklyCalendarController.
 * <p>
 * Initial Date:  10 nov. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CourseCalendarController extends DefaultController implements CloneableController {

	private WeeklyCalendarController calendarController;
	private KalendarRenderWrapper courseKalendarWrapper;
	private CourseCalendarSubscription calendarSubscription;

	private NodeEvaluation nodeEvaluation;
	
	private OLATResourceable ores;
	private List<KalendarRenderWrapper> calendars;

	public CourseCalendarController(UserRequest ureq, WindowControl wControl, CourseCalendars myCal,
			CourseCalendarSubscription calendarSubscription, OLATResourceable course, NodeEvaluation ne) {
		super(wControl);
		this.ores = course;
		this.nodeEvaluation = ne;
		calendars = myCal.getCalendars();
		courseKalendarWrapper = myCal.getCourseKalendarWrapper();
		calendarController = new WeeklyCalendarController(ureq, wControl, calendars, WeeklyCalendarController.CALLER_COURSE,
				calendarSubscription, true);
		calendarController.setEnableRemoveFromPersonalCalendar(false);
		setInitialComponent(calendarController.getInitialComponent());
	}

	public CourseCalendarSubscription getCalendarSubscription() {
		return calendarSubscription;
	}

	public KalendarRenderWrapper getCourseKalendarWrapper() {
		return courseKalendarWrapper;
	}

	public OLATResourceable getOres() {
		return ores;
	}

	public void setFocus(Date date) {
		calendarController.setFocus(date);
	}
	
	public void setFocusOnEvent(String eventId) {
		calendarController.setFocusOnEvent(eventId);
	}

	public void event(UserRequest ureq, Component source, Event event) {
	// nothing to do
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof KalendarModifiedEvent) {
			CourseCalendars myCal = CourseCalendars.createCourseCalendarsWrapper(ureq, getWindowControl(), ores, nodeEvaluation);
			calendars = myCal.getCalendars();
			courseKalendarWrapper = myCal.getCourseKalendarWrapper();
			calendarController.setCalendars(calendars);
		}
	}

	protected void doDispose() {
		calendarController.dispose();
	}

	public Controller cloneController(UserRequest ureq, WindowControl wControl) {
		CourseCalendars myCal = new CourseCalendars(courseKalendarWrapper, calendars);
		CourseCalendarSubscription calSubscription = myCal.createSubscription(ureq);
		
		int weekOfYear = calendarController.getFocusWeekOfYear();
		int year = calendarController.getFocusYear();
		
		CourseCalendarController ctrl = new CourseCalendarController(ureq, wControl, myCal, calSubscription, ores, nodeEvaluation);
		ctrl.calendarController.setFocus(year, weekOfYear);
		return ctrl;
	}
}
