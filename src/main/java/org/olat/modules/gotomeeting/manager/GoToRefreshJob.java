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
package org.olat.modules.gotomeeting.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.util.StringHelper;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.GoToMeetingModule;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 14 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToRefreshJob extends JobWithDB {

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		GoToMeetingModule meetingModule = CoreSpringFactory.getImpl(GoToMeetingModule.class);
		if(meetingModule.isEnabled()) {
			GoToMeetingManager meetingManager = CoreSpringFactory.getImpl(GoToMeetingManager.class);
			List<GoToOrganizer> organizers = meetingManager.getOrganizers();
			for(GoToOrganizer organizer:organizers) {
				processOrganizer(organizer);
			}
		}
	}
	
	private void processOrganizer(GoToOrganizer organizer) {
		if(!StringHelper.containsNonWhitespace(organizer.getRefreshToken())
				|| organizer.getRenewRefreshDate() == null) return;

		Date date = organizer.getRenewRefreshDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, -2);
		if(new Date().after(cal.getTime())) {
			CoreSpringFactory.getImpl(GoToMeetingManager.class).refreshToken(organizer);
		}
	}
}
