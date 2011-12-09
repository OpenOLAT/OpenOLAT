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

package org.olat.commons.calendar;

import org.olat.core.logging.LogDelegator;
import org.olat.core.util.notifications.NotificationsUpgrade;
import org.olat.core.util.notifications.Publisher;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * Upgrade publisher of calendars
 * 
 * <P>
 * Initial Date:  7 jan. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CalendarNotificationsUpgrade extends LogDelegator implements NotificationsUpgrade {

	@Override
	public Publisher ugrade(Publisher publisher) {
		String type = publisher.getResName();
		String businessPath = publisher.getBusinessPath();
		if(businessPath != null && businessPath.startsWith("[")) return null;
		
		if("CalendarManager.course".equals(type)) {
			try {
				ICourse course = CourseFactory.loadCourse(publisher.getResId());
				RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
				businessPath = "[RepositoryEntry:" + re.getKey() + "]";
			} catch (Exception e) {
				//if something went wrong, like error while loading course...
				logError("error while processing resid: "+publisher.getResId(), e);
			}
		}	else if("CalendarManager.group".equals(type)) {
			businessPath = "[BusinessGroup:" + publisher.getResId()+ "][action.calendar.group:0]";
		}
	
		if(businessPath != null) {
			publisher.setBusinessPath(businessPath);
			return publisher;
		}
		return null;
	}

	@Override
	public String getType() {
		return "CalendarManager";
	}
}