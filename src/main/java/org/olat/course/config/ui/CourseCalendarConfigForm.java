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
package org.olat.course.config.ui;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;

/**
 * 
 * Initial date: 06.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseCalendarConfigForm extends FormBasicController {

	private SelectionElement isOn;
	private final boolean editable;
	private final boolean calendarEnabled;
	
	private final OLATResourceable courseOres;
	private CourseConfig courseConfig;

	/**
	 * @param name
	 * @param chatEnabled
	 */
	public CourseCalendarConfigForm(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		this.courseOres = OresHelper.clone(courseOres);
		this.courseConfig = courseConfig;
		this.editable = editable;
		this.calendarEnabled = courseConfig.isCalendarEnabled();
		initForm (ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("org.olat.course.config.ui","course-calendar.html","help.hover.coursecal");
		
		isOn = uifactory.addCheckboxesVertical("isOn", "chkbx.calendar.onoff", formLayout, new String[] {"xx"}, new String[] {""}, 1);
		isOn.select("xx", calendarEnabled);
		isOn.setEnabled(editable);
		
		if(editable) {
			uifactory.addFormSubmitButton("save", "save", formLayout);
		}
	}

	@Override
	protected void doDispose() {
		// 
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		boolean enable = isOn.isSelected(0);
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setCalendarEnabled(enable);
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(),true);
		
		ILoggingAction loggingAction = enable ?
				LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_ENABLED :
				LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_DISABLED;

		ThreadLocalUserActivityLogger.log(loggingAction, getClass());
        CoordinatorManager.getInstance().getCoordinator().getEventBus()
        	.fireEventToListenersOf(new KalendarModifiedEvent(), OresHelper.lookupType(CalendarManager.class));
        CoordinatorManager.getInstance().getCoordinator().getEventBus()
        	.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.calendar, course.getResourceableId()), course);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}