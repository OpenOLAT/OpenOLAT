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
package org.olat.course.nodes.members;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 30.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersPrintController extends BasicController {
	
	private final String avatarBaseURL;
	private final VelocityContainer mainVC;
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public MembersPrintController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, String avatarBaseURL, List<UserPropertyHandler> userPropertyHandlers,
			List<Member> owners, List<Member> coaches, List<Member> participants) {
		super(ureq, wControl);
		this.avatarBaseURL = avatarBaseURL;
		this.userPropertyHandlers = userPropertyHandlers;
		
		mainVC = createVelocityContainer("print");
		mainVC.contextPut("courseTitle", courseEnv.getCourseTitle());
		mainVC.contextPut("avatarBaseURL", avatarBaseURL);
		mainVC.contextPut("userPropertyHandlers", userPropertyHandlers);
		
		List<Member> members = new ArrayList<>();
		if(owners != null && owners.size() > 0) {
			members.addAll(owners);
		}
		if(coaches != null && coaches.size() > 0) {
			members.addAll(coaches);
		}
		if(participants != null && participants.size() > 0) {
			members.addAll(participants);
		}
		initFormMemberList("members", members);

		MainPanel mainPanel = new MainPanel("membersPrintPanel");
		mainPanel.setContent(mainVC);
		putInitialPanel(mainPanel);
	}
	
	private void initFormMemberList(String name, List<Member> members) {
		VelocityContainer listVC = createVelocityContainer("printList");
		listVC.contextPut("avatarBaseURL", avatarBaseURL);
		listVC.contextPut("members", members);
		listVC.contextPut("userPropertyHandlers", userPropertyHandlers);
		mainVC.put(name, listVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}
