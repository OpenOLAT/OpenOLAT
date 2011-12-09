/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.course.config.CourseConfig;

/**
 * Description:<br>
 * TODO: patrick Class Description for CourseEfficencyStatementController
 * <P>
 * Initial Date: Aug 12, 2005 <br>
 * 
 * @author patrick
 */
public class CourseCalendarConfigController extends BasicController implements ControllerEventListener {

	private CourseCalendarConfigForm calConfigForm;
	private VelocityContainer myContent;
	private CourseConfig courseConfig;
	private ILoggingAction loggingAction;

	/**
	 * @param course
	 * @param ureq
	 * @param wControl
	 */
	public CourseCalendarConfigController(UserRequest ureq, WindowControl wControl, CourseConfig courseConfig) {
		super(ureq, wControl);
		this.courseConfig = courseConfig;
		
		myContent = createVelocityContainer("CourseCalendar");
		calConfigForm = new CourseCalendarConfigForm(ureq, wControl, courseConfig.isCalendarEnabled());
		listenTo (calConfigForm);
		myContent.put("calendarForm", calConfigForm.getInitialComponent());
		//		
		putInitialPanel(myContent);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == calConfigForm) {
			if (event == Event.DONE_EVENT) {				
				courseConfig.setCalendarEnabled(calConfigForm.isCalendarEnabled());
				if (calConfigForm.isCalendarEnabled()) {
					loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_ENABLED;
				} else {
					loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_DISABLED;
				}
				this.fireEvent(ureq, Event.CHANGED_EVENT);				
			}
		}
	}

	protected void doDispose() {
		//
	}

	/**
	 * 
	 * @return Return the log message if any, else null.
	 */
	public ILoggingAction getLoggingAction() {
		return loggingAction;
	}


}

class CourseCalendarConfigForm extends FormBasicController {

	private SelectionElement isOn;
	private boolean calendarEnabled;

	/**
	 * @param name
	 * @param chatEnabled
	 */
	public CourseCalendarConfigForm(UserRequest ureq, WindowControl wControl, boolean calendarEnabled) {
		super(ureq, wControl);
		this.calendarEnabled = calendarEnabled;
		initForm (ureq);
	}

	/**
	 * @return if chat is enabled
	 */
	public boolean isCalendarEnabled() {
		return isOn.isSelected(0);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		isOn = uifactory.addCheckboxesVertical("isOn", "chkbx.calendar.onoff", formLayout, new String[] {"xx"}, new String[] {""}, null, 1);
		isOn.select("xx", calendarEnabled);
		
		uifactory.addFormSubmitButton("save", "save", formLayout);

	}

	@Override
	protected void doDispose() {
		// 
	}

}
