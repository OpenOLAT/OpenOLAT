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
package org.olat.course.nodes.livestream.ui;

import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 27 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamEventsEditController extends BasicController {
	
	private final VelocityContainer mainVC;
	private WeeklyCalendarController calendarCtrl;

	protected LiveStreamEventsEditController(UserRequest ureq, WindowControl wControl, OLATResource courseOres,
			CourseCalendars calendars) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("events_edit");
		
		calendarCtrl = new WeeklyCalendarController(ureq, wControl, calendars.getCalendars(),
				WeeklyCalendarController.CALLER_LIVE_STREAM, courseOres, false);
		listenTo(calendarCtrl);
		mainVC.put("viewer", calendarCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
