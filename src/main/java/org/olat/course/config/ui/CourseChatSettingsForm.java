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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;


/**
 * Description: <br>
 * 
 * Initial Date: Jun 16, 2005 <br>
 * @author patrick
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseChatSettingsForm extends FormBasicController {

	private SelectionElement isOn;
	private final boolean chatEnabled;
	private final boolean editable;
	private final OLATResourceable courseOres;

	public CourseChatSettingsForm(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		this.editable = editable;
		this.courseOres = OresHelper.clone(courseOres);
		chatEnabled = courseConfig.isChatEnabled();
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("org.olat.course.config.ui","course-chat.html","help.hover.course-chat");

		isOn = uifactory.addCheckboxesVertical("isOn", "chkbx.chat.onoff", formLayout, new String[] {"xx"}, new String[] {""}, 1);
		isOn.select("xx", chatEnabled);
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
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setChatIsEnabled(isOn.isSelected(0));
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		if (courseConfig.isChatEnabled()) {
	  		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_ENABLED, getClass());
	  	} else {
	  		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_DISABLED, getClass());
	  	}

		fireEvent (ureq, Event.CHANGED_EVENT);
	}
}