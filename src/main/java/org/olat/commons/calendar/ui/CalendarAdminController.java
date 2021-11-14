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
package org.olat.commons.calendar.ui;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarAdminController extends FormBasicController {
	
	private MultipleSelectionElement enableEl, enablePersonalCalendarEl,
		enableGroupCalendarEl, enableCourseToolEl, enableCourseElementEl;
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	
	@Autowired
	private CalendarModule calendarModule;
	
	public CalendarAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarModule.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("calendar.admin");
		setFormDescription("calendar.admin.description");
		
		//
		enableEl = uifactory.addCheckboxesHorizontal("enable", "calendar.enable", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.select("on", calendarModule.isEnabled());
		
		enablePersonalCalendarEl = uifactory.addCheckboxesHorizontal("enable.personal", "calendar.enable.personal", formLayout, onKeys, onValues);
		enablePersonalCalendarEl.addActionListener(FormEvent.ONCHANGE);
		enablePersonalCalendarEl.select("on", calendarModule.isEnablePersonalCalendar());
		
		enableGroupCalendarEl = uifactory.addCheckboxesHorizontal("enable.group", "calendar.enable.group", formLayout, onKeys, onValues);
		enableGroupCalendarEl.addActionListener(FormEvent.ONCHANGE);
		enableGroupCalendarEl.select("on", calendarModule.isEnableGroupCalendar());
		
		enableCourseToolEl = uifactory.addCheckboxesHorizontal("enable.course.tool", "calendar.enable.course.tool", formLayout, onKeys, onValues);
		enableCourseToolEl.addActionListener(FormEvent.ONCHANGE);
		enableCourseToolEl.select("on", calendarModule.isEnableCourseToolCalendar());
		
		enableCourseElementEl = uifactory.addCheckboxesHorizontal("enable.course.element", "calendar.enable.course.element", formLayout, onKeys, onValues);
		enableCourseElementEl.addActionListener(FormEvent.ONCHANGE);
		enableCourseElementEl.select("on", calendarModule.isEnableCourseElementCalendar());
		updateEnableElements();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			calendarModule.setEnabled(enableEl.isAtLeastSelected(1));
			CollaborationToolsFactory.getInstance().initAvailableTools();
			updateEnableElements();
		} else if(enablePersonalCalendarEl == source) {
			calendarModule.setEnablePersonalCalendar(enablePersonalCalendarEl.isAtLeastSelected(1));
		} else if(enableGroupCalendarEl == source) {
			calendarModule.setEnableGroupCalendar(enableGroupCalendarEl.isAtLeastSelected(1));
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} else if(enableCourseToolEl == source) {
			calendarModule.setEnableCourseToolCalendar(enableCourseToolEl.isAtLeastSelected(1));
		} else if(enableCourseElementEl == source) {
			calendarModule.setEnableCourseElementCalendar(enableCourseElementEl.isAtLeastSelected(1));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void updateEnableElements() {
		boolean enableOptions = enableEl.isAtLeastSelected(1);
		enablePersonalCalendarEl.setEnabled(enableOptions);
		enableGroupCalendarEl.setEnabled(enableOptions);
		enableCourseToolEl.setEnabled(enableOptions);
		enableCourseElementEl.setEnabled(enableOptions);
	}
}
