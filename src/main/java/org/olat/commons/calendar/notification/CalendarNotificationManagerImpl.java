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
package org.olat.commons.calendar.notification;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarNotificationManager;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("calendarNotificationManager")
public class CalendarNotificationManagerImpl implements CalendarNotificationManager {
	
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	
	@Override
	public SubscriptionContext getSubscriptionContext(KalendarRenderWrapper kalendarRenderWrapper) {
		String caller = kalendarRenderWrapper.getKalendar().getType();
		SubscriptionContext subsContext = null;
		if (caller.equals(CalendarController.CALLER_COURSE) || caller.equals(CalendarManager.TYPE_COURSE)) {
			Long courseId = Long.valueOf(kalendarRenderWrapper.getCalendarKey().getCalendarId());
			subsContext = new SubscriptionContext(OresHelper.calculateTypeName(CalendarManager.class) + "." +  CalendarManager.TYPE_COURSE, courseId, CalendarController.ACTION_CALENDAR_COURSE);
		}
		if (caller.equals(CalendarController.CALLER_COLLAB) || caller.equals(CalendarManager.TYPE_GROUP)) {
			BusinessGroup businessGroup = getBusinessGroup(kalendarRenderWrapper);
			if (businessGroup != null) {
				subsContext = new SubscriptionContext(OresHelper.calculateTypeName(CalendarManager.class) + "." +  CalendarManager.TYPE_GROUP, businessGroup.getResourceableId(), CalendarController.ACTION_CALENDAR_GROUP);
			}
		}
		return subsContext;
	}

	@Override
	public SubscriptionContext getSubscriptionContext(KalendarRenderWrapper kalendarRenderWrapper, OLATResourceable course) {
		String caller = kalendarRenderWrapper.getKalendar().getType();
		
		SubscriptionContext subsContext = null;
		if (caller.equals(CalendarController.CALLER_COURSE) || caller.equals(CalendarManager.TYPE_COURSE)) {
			subsContext = new SubscriptionContext(OresHelper.calculateTypeName(CalendarManager.class) + "." +  CalendarManager.TYPE_COURSE, course.getResourceableId(), CalendarController.ACTION_CALENDAR_COURSE);
		} else if (caller.equals(CalendarController.CALLER_COLLAB) || caller.equals(CalendarManager.TYPE_GROUP)) {
			BusinessGroup businessGroup = getBusinessGroup(kalendarRenderWrapper);
			if (businessGroup != null) {
				subsContext = new SubscriptionContext(OresHelper.calculateTypeName(CalendarManager.class) + "." +  CalendarManager.TYPE_GROUP, businessGroup.getResourceableId(), CalendarController.ACTION_CALENDAR_GROUP);
			}
		}
		return subsContext;
	}

	@Override
	public BusinessGroup getBusinessGroup(KalendarRenderWrapper kalendarRenderWrapper) {
		String caller = kalendarRenderWrapper.getKalendar().getType();
		if (caller.equals(CalendarController.CALLER_COLLAB) || caller.equals(CalendarManager.TYPE_GROUP)) {
			Long resId = Long.parseLong(kalendarRenderWrapper.getKalendar().getCalendarID());
			if (resId != null) {
				BusinessGroup businessGroup = businessGroupDao.load(resId);
				if (businessGroup != null) {
					return businessGroup;
				}
			}
		}
		return null;

	}
	
	
}
