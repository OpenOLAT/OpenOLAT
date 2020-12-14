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
package org.olat.modules.teams.ui;

import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
import org.olat.commons.calendar.ui.components.FullCalendarViews;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingsCalendarController extends FormBasicController {
	
	@Autowired
	private TeamsService teamsService;
	
	public TeamsMeetingsCalendarController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "meetings_calendar");		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Kalendar calendar = new Kalendar("Teams.OnlineMeetings", "Teams.OnlineMeetings");
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(calendar, "Teams Online-Meetings", "teams.calendar");
		calendarWrapper.setPrivateEventsVisible(true);
		calendarWrapper.setCssClass("o_cal_blue"); 
		calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);

		List<TeamsMeeting> meetings = teamsService.getAllMeetings();
		// Add events to the calendar
		for (TeamsMeeting meeting : meetings) {
			if (meeting.isPermanent()) {
				continue;
			}

			// Create a calendar event with the room name as subject
			String eventId = "teams.meeting." + meeting.getKey();
			KalendarEvent newEvent = new KalendarEvent(eventId, null, meeting.getSubject(), meeting.getStartDate(), meeting.getEndDate());
			newEvent.setDescription(meeting.getSubject());
			calendar.addEvent(newEvent);	
		}

		Translator calTrans = Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator());
		List<KalendarRenderWrapper> calendarWrappers = List.of(calendarWrapper);
		FullCalendarElement calComp = new FullCalendarElement(ureq, "calComp", calendarWrappers, calTrans);
		calComp.setView(FullCalendarViews.timeGridWeek);
		formLayout.add(calComp);
	}
	
	
	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do		
	}

	@Override
	protected void doDispose() {
		// nothing to do
	}

}
