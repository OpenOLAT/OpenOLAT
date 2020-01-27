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

package org.olat.course.nodes.projectbroker;

import java.io.File;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectEvent;
import org.olat.course.nodes.projectbroker.datamodel.Project.EventType;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * @author Christian Guretzki
 */

public class ProjectBrokerDropboxController extends DropboxController {

	private Project project;
	private ProjectBrokerModuleConfiguration moduleConfig;

	public ProjectBrokerDropboxController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, CourseNode node, UserCourseEnvironment userCourseEnv, boolean previewMode, Project project, ProjectBrokerModuleConfiguration moduleConfig) {
		super(ureq, wControl);
		this.config = config;
		this.node = node;
		this.userCourseEnv = userCourseEnv;
		this.project = project;
		this.moduleConfig = moduleConfig;
		init(ureq, wControl, previewMode, false);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (isDropboxAccessible(project, moduleConfig)) {
			super.event(ureq,  source,  event);
		} else {
			getLogger().debug("Dropbos is no longer accessible");
			showInfo("dropbox.is.not.accessible");
		}
	}
	
	private boolean isDropboxAccessible(Project project, ProjectBrokerModuleConfiguration moduleConfig) {
		if (moduleConfig.isProjectEventEnabled(EventType.HANDOUT_EVENT)) {
			ProjectEvent handoutEvent = project.getProjectEvent(EventType.HANDOUT_EVENT);
			Date now = new Date();
			if (handoutEvent.getStartDate() != null) {
				if (now.before(handoutEvent.getStartDate())) {
					return false;
				}
			}
			if (handoutEvent.getEndDate() != null) {
				if (now.after(handoutEvent.getEndDate())) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Return dropbox base-path. e.g. course/<COURSE_ID>/dropbox/<NODE_id>/<USER_NAME>
	 * @see org.olat.course.nodes.ta.DropboxController#getRelativeDropBoxFilePath(org.olat.core.id.Identity)
	 */
	@Override
	protected String getRelativeDropBoxFilePath(Identity identity) {
		return getDropboxBasePathForProject(this.project, userCourseEnv.getCourseEnvironment(), node) + File.separator + identity.getName();
	}

	/**
	 * Return dropbox base-path. e.g. course/<COURSE_ID>/dropbox/<NODE_id> 
	 * To have the path for certain user you must call method 'getRelativeDropBoxFilePath'  
	 * 
	 * @param project
	 * @param courseEnv
	 * @param cNode
	 * @return
	 */
	public static String getDropboxBasePathForProject(Project project, CourseEnvironment courseEnv, CourseNode cNode) {
		return getDropboxPathRelToFolderRoot(courseEnv, cNode) + File.separator + project.getKey() ;
	}
}
