/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.util.notifications;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;

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
	private ICourse course;
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
	
	public SubscriptionProviderImpl(KalendarRenderWrapper kalendarRenderWrapper, ICourse course) {
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
				this.businessGroup = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(resId, true);
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
