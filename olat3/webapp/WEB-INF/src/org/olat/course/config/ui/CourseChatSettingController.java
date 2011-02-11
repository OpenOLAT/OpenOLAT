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
* <p>
*/ 

package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.config.CourseConfig;

/**
 * Description: <br>
 * Initial Date: Jun 16, 2005 <br>
 * @author patrick
 */
public class CourseChatSettingController extends BasicController implements ControllerEventListener {

	private CourseChatSettingsForm chatForm;
	private VelocityContainer myContent;
	private CourseConfig courseConfig;

	/**
	 * @param course
	 * @param ureq
	 * @param wControl
	 */
	public CourseChatSettingController(UserRequest ureq, WindowControl wControl, CourseConfig courseConfig) {
		super(ureq, wControl);
		this.courseConfig = courseConfig;
		
		myContent = createVelocityContainer("CourseChat");
		chatForm = new CourseChatSettingsForm(ureq, wControl, courseConfig.isChatEnabled());
		listenTo (chatForm);
		myContent.put("chatForm", chatForm.getInitialComponent());
		//		
		putInitialPanel(myContent);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == chatForm) {
			if (event == Event.DONE_EVENT) {				
				courseConfig.setChatIsEnabled(chatForm.chatIsEnabled());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

}