/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.List;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskPortraitController extends BasicController {

	private final MapperKey avatarMapperKey;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MapperService mapperService;
	
	public TaskPortraitController(UserRequest ureq, WindowControl wControl, Task task) {
		super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));

		avatarMapperKey =  mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));
		
		VelocityContainer mainVC = createVelocityContainer("task_portrait");
		
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(List.of(task.getIdentity()));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "task_identity", mainVC, null, avatarMapperKey);
		usersPortraitCmp.setSize(PortraitSize.medium);
		usersPortraitCmp.setMaxUsersVisible(1);
		usersPortraitCmp.setUsers(portraitUsers);
		
		mainVC.put("task_portrait", usersPortraitCmp);
		mainVC.contextPut("task_fullname", userManager.getUserDisplayName(task.getIdentity()));		
		mainVC.contextPut("taskName", task.getTaskName());		

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
