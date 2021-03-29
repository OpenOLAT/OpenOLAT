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
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * @author guretzki
 * 
 */
public class ProjectDetailsPanelController extends BasicController {
	
	private StackedPanel detailsPanel;
	private ProjectEditDetailsFormController editController;
	private ProjectDetailsDisplayController runController;

	private Project project;
	private UserCourseEnvironment userCourseEnv;
	private CourseNode courseNode;
	private ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;

	private boolean newCreatedProject;
	private VelocityContainer editVC;
	private LockResult lock;
	
	private final ProjectBrokerManager projectBrokerManager;
	private final ProjectGroupManager projectGroupManager;
	
	
	public ProjectDetailsPanelController(UserRequest ureq, WindowControl wControl, Project project,
			boolean newCreatedProject, UserCourseEnvironment userCourseEnv, CourseNode courseNode,
			ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) {
		super(ureq, wControl);
		this.project = project;
		this.userCourseEnv =  userCourseEnv;
		this.courseNode = courseNode;
		this.projectBrokerModuleConfiguration = projectBrokerModuleConfiguration;
		this.newCreatedProject = newCreatedProject;
		
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);

		detailsPanel = new SimpleStackedPanel("projectdetails_panel");
		runController = new ProjectDetailsDisplayController(ureq, wControl, project, userCourseEnv, courseNode, projectBrokerModuleConfiguration);
		listenTo(runController);
		detailsPanel.setContent(runController.getInitialComponent());

		editVC = createVelocityContainer("editProject");
		if ( newCreatedProject && projectGroupManager.isProjectManagerOrAdministrator(ureq, userCourseEnv, project) ) {
			openEditController(ureq);
		} 

		putInitialPanel(detailsPanel);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if ((source == runController) && event.getCommand().equals("switchToEditMode")) {
			if (newCreatedProject) {
				newCreatedProject = false;
			}
			if (editController != null) editController.doDispose();
			openEditController(ureq);
		} else if ((source == editController) && event == Event.DONE_EVENT) {
			// switch back from edit mode to display-mode
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
			detailsPanel.popContent();
			removeAsListenerAndDispose(runController);
			runController = new ProjectDetailsDisplayController(ureq, this.getWindowControl(), project, userCourseEnv, courseNode, projectBrokerModuleConfiguration);
			listenTo(runController);
			detailsPanel.setContent(runController.getInitialComponent());
			if (newCreatedProject){
				fireEvent(ureq, new ProjectBrokerEditorEvent(project, ProjectBrokerEditorEvent.CREATED_NEW_PROJECT));
			} else {
				fireEvent(ureq, new ProjectBrokerEditorEvent(project, ProjectBrokerEditorEvent.CHANGED_PROJECT));				
			}
		} else if (source == runController) {
			// go back to project-list, pass event
			fireEvent(ureq, event);
		} else if ((source == editController) && (event == Event.CANCELLED_EVENT)) {
			if (newCreatedProject) {
				// from cancelled and go back to project-list
				fireEvent(ureq, new ProjectBrokerEditorEvent(project, ProjectBrokerEditorEvent.CANCEL_NEW_PROJECT));
			}
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
	}

	private void openEditController(UserRequest ureq) {
		if ( projectBrokerManager.existsProject(project.getKey()) ) {
			OLATResourceable projectOres = OresHelper.createOLATResourceableInstance(Project.class, project.getKey());
			this.lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(projectOres, ureq.getIdentity(), null, getWindow());
			if (lock.isSuccess()) {
				editController = new ProjectEditDetailsFormController(ureq, getWindowControl(), project,
						userCourseEnv.getCourseEnvironment(), courseNode, projectBrokerModuleConfiguration,
						newCreatedProject);
				editController.addControllerListener(this);
				editVC.put("editController", editController.getInitialComponent());
				detailsPanel.pushContent(editVC);
			} else if(lock.isDifferentWindows()) {
				showInfo("info.project.already.edit.same.user", project.getTitle());
			} else {
				showInfo("info.project.already.edit", project.getTitle());
			}
		} else {
			showInfo("info.project.nolonger.exist", project.getTitle());
			//fire event to update project list
			ProjectBroker projectBroker = project.getProjectBroker();
			OLATResourceableDeletedEvent delEv = new OLATResourceableDeletedEvent(projectBroker);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(delEv, projectBroker);			
		}
	}

	@Override
	protected void doDispose() {
		// child controller sposed by basic controller
		if (lock != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
	}
	
}
