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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.CustomField;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectEvent;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerMailer;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;

/**
 * 
 * @author guretzki
 * 
 */
public class ProjectDetailsDisplayController extends BasicController {
	
	private final String CMD_OPEN_PROJECT_LEADER_DETAIL = "cmd_open_projectleader_detail";
	private VelocityContainer myContent;
	private Link editProjectButton;
	private Link deleteProjectButton;
	private DialogBoxController deleteConfirmController;
	private List<String> projectLeaderLinkNameList;
	private Link attachedFileLink;

	private Project project;
	private CourseEnvironment courseEnv;
	private CourseNode courseNode;
	private DialogBoxController deleteGroupConfirmController;
	private Link changeProjectStateToNotAssignButton;
	private Link changeProjectStateToAssignButton;
	private LockResult lock;

	private final ProjectBrokerMailer projectBrokerMailer;
	private final ProjectBrokerManager projectBrokerManager;
	private final ProjectGroupManager projectGroupManager;
	
	
	public ProjectDetailsDisplayController(UserRequest ureq, WindowControl wControl, Project project,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode,
			ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) {
		super(ureq, wControl);
		this.project = project;
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		this.courseNode = courseNode;
		
		projectBrokerMailer = CoreSpringFactory.getImpl(ProjectBrokerMailer.class);
		projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		
		// use property handler translator for translating of user fields
		setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));
		myContent = createVelocityContainer("projectdetailsdisplay");
		if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
			myContent.contextPut("keyMaxLabel", "detailsform.places.candidates.label");
		} else {
			myContent.contextPut("keyMaxLabel", "detailsform.places.label");
		}

		if ( projectGroupManager.isProjectManagerOrAdministrator(ureq, userCourseEnv, project) ) {
			myContent.contextPut("isProjectManager", true);
			editProjectButton = LinkFactory.createButtonSmall("edit.project.button", myContent, this);
			deleteProjectButton = LinkFactory.createButtonSmall("delete.project.button", myContent, this);
			if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
				// ProjectBroker run in accept-manually mode => add button to reset/set project-state
				if ( project.getState().equals(Project.STATE_ASSIGNED) ) {
					changeProjectStateToNotAssignButton = LinkFactory.createButtonSmall("change.project.state.not_assign.button", myContent, this);
				} else {
					changeProjectStateToAssignButton = LinkFactory.createButtonSmall("change.project.state.assign.button", myContent, this);					
				}
			}
		} else {
			myContent.contextPut("isProjectManager", false);
		}

		myContent.contextPut("title", project.getTitle());
		// account-Managers
		int i = 0;
		projectLeaderLinkNameList = new ArrayList<>();
		for (Identity identity : project.getProjectLeaders()) {
			String last = identity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
			String first= identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
			StringBuilder projectLeaderString = new StringBuilder();
			projectLeaderString.append(first);
			projectLeaderString.append(" ");
			projectLeaderString.append(last);
			String linkName = "projectLeaderLink_" + i;
			Link projectLeaderLink = LinkFactory.createCustomLink(linkName, CMD_OPEN_PROJECT_LEADER_DETAIL, projectLeaderString.toString() , Link.NONTRANSLATED, myContent, this);
			projectLeaderLink.setUserObject(identity);
			projectLeaderLink.setTarget("_blank");
			projectLeaderLinkNameList.add(linkName);
			i++;
		}
		myContent.contextPut("projectLeaderLinkNameList", projectLeaderLinkNameList);

		myContent.contextPut("description", project.getDescription());
		// Custom-fields
		List<CustomField> customFieldList = new ArrayList<>();
		int customFieldIndex = 0;
		for (Iterator<CustomField> iterator =  projectBrokerModuleConfiguration.getCustomFields().iterator(); iterator.hasNext();) {
			CustomField customField = iterator.next();
			getLogger().debug("customField=" + customField);
			String name = customField.getName();
			String value = project.getCustomFieldValue(customFieldIndex++);
			getLogger().debug("customField  name=" + name + "  value=" + value);
			customFieldList.add(new CustomField(name,value));
		}
		myContent.contextPut("customFieldList", customFieldList);
		
		// events
		List<ProjectEvent> eventList = new ArrayList<>();
		for (Project.EventType eventType : Project.EventType.values()) {
			if (projectBrokerModuleConfiguration.isProjectEventEnabled(eventType) ) {
				ProjectEvent projectEvent = project.getProjectEvent(eventType);
				eventList.add(projectEvent);
				getLogger().debug("eventList add event=" + projectEvent);
			}
		}		
		myContent.contextPut("eventList", eventList);
		
		String stateValue = getTranslator().translate(projectBrokerManager.getStateFor(project,ureq.getIdentity(),projectBrokerModuleConfiguration));
		myContent.contextPut("state", stateValue);
		if (project.getMaxMembers() == Project.MAX_MEMBERS_UNLIMITED) {
			myContent.contextPut("projectPlaces", this.getTranslator().translate("detailsform.unlimited.project.members") );
		} else {
			String placesValue = project.getSelectedPlaces() + " " + this.getTranslator().translate("detailsform.places.of") + " " + project.getMaxMembers();
			myContent.contextPut("projectPlaces", placesValue);
		}
		
		attachedFileLink = LinkFactory.createCustomLink("attachedFileLink", "cmd.download.attachment", project.getAttachmentFileName(), Link.NONTRANSLATED, myContent, this);
		attachedFileLink.setTarget("_blank");
		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteConfirmController) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				// ok group should be deleted, ask to delete group too
				deleteGroupConfirmController = this.activateYesNoDialog(ureq, null, translate("delete.group.confirm",project.getTitle()), deleteConfirmController);
			}
		} else if (source == deleteGroupConfirmController) {
			boolean deleteGroup;
			if (DialogBoxUIFactory.isOkEvent(event)) {	
				deleteGroup = true;
			} else {
				deleteGroup = false;
			}
			// send email before delete project with group
			projectBrokerMailer.sendProjectDeletedEmailToParticipants(ureq.getIdentity(), project, this.getTranslator());
			projectBrokerMailer.sendProjectDeletedEmailToManager(ureq.getIdentity(), project, this.getTranslator());
			//now send email to PB-accountmanager
			projectBrokerMailer.sendProjectDeletedEmailToAccountManagers(ureq.getIdentity(), project, courseEnv, courseNode, getTranslator());
			
			projectBrokerManager.deleteProject(project, deleteGroup, courseEnv, courseNode);
			projectGroupManager.sendGroupChangeEvent(project, courseEnv.getCourseResourceableId(), ureq.getIdentity());
			showInfo("project.deleted.msg", project.getTitle());
			fireEvent(ureq, new ProjectBrokerEditorEvent(project, ProjectBrokerEditorEvent.DELETED_PROJECT));
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}

	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ( projectBrokerManager.existsProject(project.getKey()) ) {
			if (source == editProjectButton) {
				fireEvent(ureq, new Event("switchToEditMode"));
			} else if (source == deleteProjectButton) {
				OLATResourceable projectOres = OresHelper.createOLATResourceableInstance(Project.class, project.getKey());
				lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(projectOres, ureq.getIdentity(), null, getWindow());
				if (lock.isSuccess()) {
					deleteConfirmController = activateOkCancelDialog(ureq, null, translate("delete.confirm",project.getTitle()), deleteConfirmController);
				} else if(lock.isDifferentWindows()) {
					showWarning("info.project.already.edit.same.user", project.getTitle());
				} else {
					showWarning("info.project.already.edit", project.getTitle());
				}
			} else if (event.getCommand().equals(CMD_OPEN_PROJECT_LEADER_DETAIL)){
				if (source instanceof Link) {
					Link projectLeaderLink = (Link)source;
					final Identity identity = (Identity)projectLeaderLink.getUserObject();
					ControllerCreator ctrlCreator = new ControllerCreator() {
						@Override
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							return new UserInfoMainController(lureq, lwControl, identity, true, false);
						}
					};
					// wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
					// open in new browser window
					this.openInNewBrowserWindow(ureq, layoutCtrlr);
				}
			} else if (source == attachedFileLink) {
				doFileDelivery(ureq, project, courseEnv, courseNode);
			} else if (source == changeProjectStateToNotAssignButton) {
				projectBrokerManager.setProjectState(project, Project.STATE_NOT_ASSIGNED);
				myContent.remove(changeProjectStateToNotAssignButton);
				changeProjectStateToAssignButton = LinkFactory.createButtonSmall("change.project.state.assign.button", myContent, this);					
			} else if (source == changeProjectStateToAssignButton) {
				projectBrokerManager.setProjectState(project, Project.STATE_ASSIGNED);
				myContent.remove(changeProjectStateToAssignButton);
				changeProjectStateToNotAssignButton = LinkFactory.createButtonSmall("change.project.state.not_assign.button", myContent, this);					
			}
		} else {
			this.showInfo("info.project.nolonger.exist", project.getTitle());
		}
	}

	@Override
	protected void doDispose() {
		// child controller sposed by basic controller
		if (lock != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
	}
	
	private void doFileDelivery(UserRequest ureq, final Project project, final CourseEnvironment courseEnv, final CourseNode cNode) {
		// Create a mapper to deliver the auto-download of the file. We have to
		// create a dedicated mapper here
		// and can not reuse the standard briefcase way of file delivering, some
		// very old fancy code
		// Mapper is cleaned up automatically by basic controller

		VFSContainer rootFolder = VFSManager.olatRootContainer(projectBrokerManager.getAttamchmentRelativeRootPath(project,courseEnv,cNode),null);
		VFSItem item = rootFolder.resolve(project.getAttachmentFileName());
		if(item instanceof VFSLeaf) {
			VFSLeaf attachment = (VFSLeaf)item;
			MediaResource resource = new VFSMediaResource(attachment);
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
}
