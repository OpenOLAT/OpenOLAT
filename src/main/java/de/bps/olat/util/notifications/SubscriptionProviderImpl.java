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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.util.notifications;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;

/**
 * 
 * Description:<br>
 * Managed different subscription sources.
 * 
 * <P>
 * Initial Date:  29.04.2009 <br>
 * @author bja
 */
public class SubscriptionProviderImpl implements SubscriptionProvider {

	private String caller;
	private KalendarRenderWrapper kalendarRenderWrapper;
	private SubscriptionContext subscriptionContext;
	private OLATResourceable course;
	private BusinessGroup businessGroup;
	
	public SubscriptionProviderImpl(KalendarRenderWrapper kalendarRenderWrapper) {
		this.kalendarRenderWrapper = kalendarRenderWrapper;
		this.caller = kalendarRenderWrapper.getKalendar().getType();
		this.subscriptionContext = setSubscriptionContext();
	}

	public SubscriptionProviderImpl(String caller, KalendarRenderWrapper kalendarRenderWrapper) {
		this.kalendarRenderWrapper = kalendarRenderWrapper;
		this.caller = caller;
		this.subscriptionContext = setSubscriptionContext();
	}
	
	public SubscriptionProviderImpl(KalendarRenderWrapper kalendarRenderWrapper, OLATResourceable course) {
		this.kalendarRenderWrapper = kalendarRenderWrapper;
		this.caller = kalendarRenderWrapper.getKalendar().getType();
		this.course = course;
		this.subscriptionContext = setSubscriptionContext();
	}
	
	private SubscriptionContext setSubscriptionContext() {
		SubscriptionContext subsContext = null;
		if (this.caller.equals(CalendarController.CALLER_COURSE) || this.caller.equals(CalendarManager.TYPE_COURSE)) {
			if (course != null) {
				subsContext = new SubscriptionContext(OresHelper.calculateTypeName(CalendarManager.class) + "." +  CalendarManager.TYPE_COURSE, course.getResourceableId(), CalendarController.ACTION_CALENDAR_COURSE);
			} else {
				Long courseId = this.kalendarRenderWrapper.getLinkProvider().getControler().getCourseID();
				if (courseId != null) {
					this.course = CourseFactory.loadCourse(courseId);
					subsContext = new SubscriptionContext(OresHelper.calculateTypeName(CalendarManager.class) + "." +  CalendarManager.TYPE_COURSE, this.kalendarRenderWrapper.getLinkProvider().getControler().getCourseID(), CalendarController.ACTION_CALENDAR_COURSE);
				}
			}
		}
		if (caller.equals(CalendarController.CALLER_COLLAB) || this.caller.equals(CalendarManager.TYPE_GROUP)) {
			Long resId = this.kalendarRenderWrapper.getKalendarConfig().getResId();
			if (resId == null) resId = Long.parseLong(this.kalendarRenderWrapper.getKalendar().getCalendarID());
			if (resId != null) {
				this.businessGroup = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(resId);
				if (businessGroup != null) {
					subsContext = new SubscriptionContext(OresHelper.calculateTypeName(CalendarManager.class) + "." +  CalendarManager.TYPE_GROUP, businessGroup.getResourceableId(), CalendarController.ACTION_CALENDAR_GROUP);
				}
			}
		}
		return subsContext;
	}

	public ContextualSubscriptionController getContextualSubscriptionController(UserRequest ureq, WindowControl wControl) {
		ContextualSubscriptionController csc = null;
		if (getSubscriptionContext() != null) {
			if ((caller.equals(CalendarController.CALLER_COURSE) || caller.equals(CalendarManager.TYPE_COURSE)) && course != null) {
				String businessPath = wControl.getBusinessControl().getAsString();
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(CalendarManager.class), String.valueOf(course.getResourceableId()), businessPath);
				csc = new ContextualSubscriptionController(ureq, wControl, getSubscriptionContext(), pdata);
			}
			if ((caller.equals(CalendarController.CALLER_COLLAB) || caller.equals(CalendarManager.TYPE_GROUP)) && businessGroup != null) {
				String businessPath = wControl.getBusinessControl().getAsString();
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(CalendarManager.class), String.valueOf(businessGroup.getResourceableId()), businessPath);
				csc = new ContextualSubscriptionController(ureq, wControl, getSubscriptionContext(), pdata);
			}
		}
		return csc;
	}

	public SubscriptionContext getSubscriptionContext() {
		return this.subscriptionContext;
	}

}
