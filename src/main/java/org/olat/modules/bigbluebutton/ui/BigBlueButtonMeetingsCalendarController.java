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
package org.olat.modules.bigbluebutton.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2020<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingsCalendarController extends FormBasicController {
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;

	// TODO: refactor to enum and replace in CalendarColorChooserController
	private static final String[] colors = new String[]{
			"o_cal_green", "o_cal_lime", "o_cal_blue", "o_cal_orange", "o_cal_fuchsia",
			"o_cal_red", "o_cal_rebeccapurple", "o_cal_navy", "o_cal_olive",
			"o_cal_maroon", "o_cal_grey"
		};

	
	public BigBlueButtonMeetingsCalendarController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "meetings_calendar");		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates();
		if (templates == null || templates.isEmpty()) {
			return;
		}
		
		List<KalendarRenderWrapper> calendarWrappers = new ArrayList<>();
		Map<Long, Kalendar> calendars = new HashMap<>();
		
		// Create a template calendar for each template / room. This is all in-memory, no ics files
		for (int i = 0; i < templates.size(); i++) {
			BigBlueButtonMeetingTemplate template = templates.get(i);
			Kalendar calendar = new Kalendar("BBB.Template." + template.getKey(), "BBB.Template");
			KalendarRenderWrapper calRenderWrapper = new KalendarRenderWrapper(calendar, template.getName(), "bbb.calendar." + template.getKey());
			calRenderWrapper.setPrivateEventsVisible(true);
			// Each template calendar has it's own color
			calRenderWrapper.setCssClass(colors[i % colors.length]); 
			calRenderWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			
			calendarWrappers.add(calRenderWrapper);
			calendars.put(template.getKey(), calendar);
		}
		
		//TODO: implement getAllMeetings with lower date boundary to scale for the future when we have many old meetings
		// when available, fetch all meetings -1 week and from the future
		List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getAllMeetings();
		// Add bbb events to template calendars
		for (BigBlueButtonMeeting meeting : meetings) {
			if (meeting.isPermanent()) {
				continue;
			}
			BigBlueButtonMeetingTemplate template = meeting.getTemplate();
			if (template == null) {
				continue;
			}
			// Create a calendar event with the room name as subject
			String eventId = CodeHelper.getGlobalForeverUniqueID();
			KalendarEvent newEvent = new KalendarEvent(eventId, null, template.getName(), meeting.getStartDate(), meeting.getEndDate());
			newEvent.setDescription(meeting.getName());
			// Add to template calendar
			calendars.get(meeting.getTemplate().getKey()).addEvent(newEvent);	
		}
		
		
		Translator calTrans = Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator());
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
