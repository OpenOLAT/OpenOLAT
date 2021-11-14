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
package org.olat.modules.lecture.ui.coach;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyAbsenceNoticesController extends BasicController {
	
	private final AbsenceNoticeSearchParameters searchParams = new AbsenceNoticeSearchParameters();
	
	private final VelocityContainer mainVC;
	
	private final AbsenceNoticesListController noticesListCtlr;

	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param currentDate An optional date to focus on
	 * @param profiledIdentity An optional user to focus on
	 * @param secCallback The security callback of the user
	 */
	public DailyAbsenceNoticesController(UserRequest ureq, WindowControl wControl, Date currentDate, Identity profiledIdentity, LecturesSecurityCallback secCallback) {
		super(ureq, wControl);
		searchParams.addTypes(AbsenceNoticeType.values());
		searchParams.setStartDate(CalendarUtils.startOfDay(currentDate));
		searchParams.setEndDate(CalendarUtils.endOfDay(currentDate));
		searchParams.setViewAs(getIdentity(), ureq.getUserSession().getRoles(), secCallback.viewAs());
		if(profiledIdentity != null) {
			searchParams.setParticipant(profiledIdentity);
		}
		noticesListCtlr = new AbsenceNoticesListController(ureq, getWindowControl(),
				currentDate, secCallback, true, "daily");
		listenTo(noticesListCtlr);
		
		mainVC = createVelocityContainer("daily_absences");
		mainVC.put("noticesList", noticesListCtlr.getInitialComponent());
		
		putInitialPanel(mainVC);
		setCurrentDate(currentDate);
	}
	
	public void setCurrentDate(Date date) {
		searchParams.setStartDate(CalendarUtils.startOfDay(date));
		searchParams.setEndDate(CalendarUtils.endOfDay(date));
		noticesListCtlr.setCurrentDate(date);
		noticesListCtlr.loadModel(searchParams);
	}
	
	public void reloadModel() {
		noticesListCtlr.reloadModel();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(noticesListCtlr == source) {
			if(event instanceof SelectLectureIdentityEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	
}
