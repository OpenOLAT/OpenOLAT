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

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.Project.EventType;
import org.olat.course.nodes.projectbroker.datamodel.ProjectEvent;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 *
 * @author guretzki
 *   
 * 
 */

public class ProjectFolderController extends BasicController {

	private ModuleConfiguration config;
	private boolean hasDropbox, hasReturnbox;
	private VelocityContainer content;
	private DropboxController dropboxController;
	private Controller dropboxEditController;
	private ReturnboxController returnboxController;
	private MSCourseNodeRunController scoringController;

	private final ProjectGroupManager projectGroupManager;
	
	
	public ProjectFolderController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNode courseNode, boolean previewMode, Project project) { 
		super(ureq, wControl);
		config = courseNode.getModuleConfiguration();
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		
		ProjectBrokerModuleConfiguration moduleConfig = new ProjectBrokerModuleConfiguration(config);
		
		content = createVelocityContainer("folder");
		boolean isProjectManagerOrAdministrator = projectGroupManager.isProjectManagerOrAdministrator(ureq, userCourseEnv, project);
		if (projectGroupManager.isProjectParticipant(ureq.getIdentity(), project)
			  || isProjectManagerOrAdministrator ) {
			content.contextPut("isParticipant", true);
			readConfig(config);
			if (!hasDropbox && !hasReturnbox ) {
				// nothing to show => Show text message no folder
				content.contextPut("noFolder", Boolean.TRUE);
			} else {
				getLogger().debug("isDropboxAccessible(project, moduleConfig)=" + isDropboxAccessible(project, moduleConfig));
				if (isProjectManagerOrAdministrator) {
					dropboxEditController = new ProjectBrokerDropboxScoringViewController(project, ureq, wControl, courseNode, userCourseEnv); 
					content.put("dropboxController", dropboxEditController.getInitialComponent());
					content.contextPut("hasDropbox", Boolean.TRUE);
				} else {
					if (hasDropbox) {
						if (isDropboxAccessible(project, moduleConfig)) {
							dropboxController = new ProjectBrokerDropboxController(ureq, wControl, config, courseNode, userCourseEnv, previewMode, project, moduleConfig);
							content.put("dropboxController", dropboxController.getInitialComponent());
							content.contextPut("hasDropbox", Boolean.TRUE);
						} else {
							content.contextPut("hasDropbox", Boolean.FALSE);
							content.contextPut("DropboxIsNotAccessible", Boolean.TRUE);
						}
					}
					if (hasReturnbox) {
						if (!isProjectManagerOrAdministrator) {
							returnboxController = new ProjectBrokerReturnboxController(ureq, wControl, courseNode, userCourseEnv, previewMode,project);
							content.put("returnboxController", returnboxController.getInitialComponent());
							content.contextPut("hasReturnbox", Boolean.TRUE);
						}
					}		
				}

			}
			// push title 
			content.contextPut("menuTitle", courseNode.getShortTitle());
			content.contextPut("displayTitle", courseNode.getLongTitle());
		} else {
			content.contextPut("isParticipant", false);
		}
		putInitialPanel(content);
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

	private void readConfig(ModuleConfiguration modConfig) {
		Boolean bValue = (Boolean)modConfig.get(ProjectBrokerCourseNode.CONF_DROPBOX_ENABLED);
		hasDropbox = (bValue != null) ? bValue.booleanValue() : false;
		bValue = (Boolean)modConfig.get(ProjectBrokerCourseNode.CONF_RETURNBOX_ENABLED);
		hasReturnbox = (bValue != null) ? bValue.booleanValue() : false;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
	}
	
	@Override
	protected void doDispose() {
		if (dropboxController != null) {
			dropboxController.dispose();
			dropboxController = null;
		}
		if (dropboxEditController != null) {
			dropboxEditController.dispose();
			dropboxEditController = null;			
		}
		if (scoringController != null) {
			scoringController.dispose();
			scoringController = null;
		}
		if (returnboxController != null) {
			returnboxController.dispose();
			returnboxController = null;
		}
        super.doDispose();
	}
}
