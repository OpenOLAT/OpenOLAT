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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 *
 * @author guretzki
 * 
 */

public class ProjectController extends BasicController {
	
	private VelocityContainer contentVC;

	private TabbedPane myTabbedPane;

	private ProjectDetailsPanelController detailsController;

	private ProjectFolderController projectFolderController;

	private ProjectGroupController projectGroupController;

	private Link backLink;

	private ProjectGroupManager projectGroupManager;

	
	public ProjectController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode, Project project, 
			boolean newCreatedProject, ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) { 
		super(ureq, wControl);
		
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
			
		contentVC = createVelocityContainer("project");
		contentVC.contextPut("menuTitle", courseNode.getShortTitle());

		if (!newCreatedProject) {
			backLink = LinkFactory.createLinkBack(contentVC, this);
		}
		myTabbedPane = new TabbedPane("projectTabbedPane", ureq.getLocale());		
		detailsController = new ProjectDetailsPanelController(ureq, wControl, project, newCreatedProject, userCourseEnv, courseNode, projectBrokerModuleConfiguration);
		detailsController.addControllerListener(this);
		myTabbedPane.addTab(translate("tab.project.details"), detailsController.getInitialComponent());
		projectFolderController = new ProjectFolderController( ureq, wControl, userCourseEnv, courseNode, false, project);
		myTabbedPane.addTab(translate("tab.project.folder"), projectFolderController.getInitialComponent());
		if (projectGroupManager.isProjectManagerOrAdministrator(ureq, userCourseEnv, project)) {
			projectGroupController = new ProjectGroupController(ureq, wControl, userCourseEnv, project, projectBrokerModuleConfiguration);
			myTabbedPane.addTab(translate("tab.project.members"), projectGroupController.getInitialComponent());
		}
		contentVC.put("projectTabbedPane", myTabbedPane);
		putInitialPanel(contentVC);
	}
	
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			fireEvent(ureq,Event.BACK_EVENT);
		}
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		getLogger().debug("event" + event );
		if ((source == detailsController) && (event == Event.CHANGED_EVENT)) {
			if (backLink == null) {
				backLink = LinkFactory.createLinkBack(contentVC, this);
			}
		}
		// pass event
		fireEvent(urequest, event);
	}

	@Override
	protected void doDispose() {
		
	}

}
