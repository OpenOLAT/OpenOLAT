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

import java.util.List;
import java.util.UUID;

import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesMoveEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.admin.securitygroup.gui.WaitingGroupController;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerMailer;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.BGMailHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author guretzki
 * 
 */
public class ProjectGroupController extends BasicController {

	private GroupController projectLeaderController;
	private GroupController projectMemberController;
	private WaitingGroupController projectCandidatesController;

	private Project project;

	private ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private ProjectBrokerManager projectBrokerManager;
	@Autowired
	private ProjectGroupManager projectGroupManager;
	@Autowired
	private ProjectBrokerMailer projectBrokerMailer;

	/**
	 * @param ureq
	 * @param wControl
	 * @param hpc
	 */
	public ProjectGroupController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, Project project, ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) {
		super(ureq, wControl);
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		this.project = project;
		this.projectBrokerModuleConfiguration = projectBrokerModuleConfiguration;
		
		VelocityContainer myContent = createVelocityContainer("projectgroup_management");

		// Project Leader Management
		BusinessGroup projectGroup = project.getProjectGroup();
		Group group = businessGroupService.getGroup(projectGroup);
		
		boolean mayModifyMembers = !userCourseEnv.isCourseReadOnly();
		projectLeaderController = new GroupController(ureq, getWindowControl(), mayModifyMembers, true, true, false, true, false, group, GroupRoles.coach.name());
		listenTo(projectLeaderController);
		myContent.put("projectLeaderController", projectLeaderController.getInitialComponent());
		MailTemplate leaderAddUserMailTempl = BGMailHelper.createAddParticipantMailTemplate(projectGroup, ureq.getIdentity());
		projectLeaderController.setAddUserMailTempl(leaderAddUserMailTempl,false);
		MailTemplate leaderAremoveUserMailTempl = BGMailHelper.createRemoveParticipantMailTemplate(projectGroup, ureq.getIdentity());
		projectLeaderController.setRemoveUserMailTempl(leaderAremoveUserMailTempl,false);

		// Project Member Management
		projectMemberController = new GroupController(ureq, getWindowControl(), mayModifyMembers, false, true, false, true, false, group, GroupRoles.participant.name());
		listenTo(projectMemberController);
		myContent.put("projectMemberController", projectMemberController.getInitialComponent());
		// add mail templates used when adding and removing users
		MailTemplate partAddUserMailTempl = projectBrokerMailer.createAddParticipantMailTemplate(project, ureq.getIdentity(), this.getTranslator());
		projectMemberController.setAddUserMailTempl(partAddUserMailTempl,false);
		MailTemplate partRemoveUserMailTempl = projectBrokerMailer.createRemoveParticipantMailTemplate(project, ureq.getIdentity(), this.getTranslator());
		projectMemberController.setRemoveUserMailTempl(partRemoveUserMailTempl,false);

		// Project Candidates Management
		if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
			projectCandidatesController = new WaitingGroupController(ureq, getWindowControl(), mayModifyMembers, false, true, true, false, project.getCandidateGroup());
			listenTo(projectCandidatesController);
			myContent.contextPut("isProjectCandidatesListEmpty", projectGroupManager.isCandidateListEmpty(project.getCandidateGroup()) );
			myContent.put("projectCandidatesController", projectCandidatesController.getInitialComponent());
			// add mail templates used when adding and removing users
			MailTemplate waitAddUserMailTempl = projectBrokerMailer.createAddCandidateMailTemplate(project, ureq.getIdentity(), this.getTranslator());
			projectCandidatesController.setAddUserMailTempl(waitAddUserMailTempl,false);
			MailTemplate waitRemoveUserMailTempl = projectBrokerMailer.createRemoveAsCandiadateMailTemplate(project, ureq.getIdentity(), this.getTranslator());
			projectCandidatesController.setRemoveUserMailTempl(waitRemoveUserMailTempl,false);
			MailTemplate waitTransferUserMailTempl = projectBrokerMailer.createAcceptCandiadateMailTemplate(project, ureq.getIdentity(), this.getTranslator());
			projectCandidatesController.setTransferUserMailTempl(waitTransferUserMailTempl);
		}
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if ( projectBrokerManager.existsProject( project.getKey() ) ) {
			if (source == projectLeaderController) {
				handleProjectLeaderGroupEvent(urequest, event);
			} else if (source == projectMemberController) {
				handleProjectMemberGroupEvent(urequest, event);
			} else if (source == projectCandidatesController) {
				handleCandidateGroupEvent(urequest, event);
			}
		} else {
			this.showInfo("info.project.nolonger.exist", project.getTitle());
		}
	}

	private void handleCandidateGroupEvent(UserRequest urequest, Event event) {
		if (event instanceof IdentitiesAddEvent) {
			IdentitiesAddEvent identitiesAddEvent = (IdentitiesAddEvent)event;
			List<Identity> addedIdentities = projectGroupManager.addCandidates(identitiesAddEvent.getAddIdentities(), project);
			identitiesAddEvent.setIdentitiesAddedEvent(addedIdentities);
			fireEvent(urequest, Event.CHANGED_EVENT );			
		} else if (event instanceof IdentitiesRemoveEvent) {
			projectGroupManager.removeCandidates(((IdentitiesRemoveEvent)event).getRemovedIdentities(), project);
			fireEvent(urequest, Event.CHANGED_EVENT );
		} else if (event instanceof IdentitiesMoveEvent) {
			final IdentitiesMoveEvent identitiesMoveEvent = (IdentitiesMoveEvent) event;
			//OLAT-6342: check identity not in group first!
			List<Identity> moveIdents = identitiesMoveEvent.getChosenIdentities();
			BusinessGroupAddResponse response = projectGroupManager.acceptCandidates(moveIdents, project, urequest.getIdentity(),
					projectBrokerModuleConfiguration.isAutoSignOut(), projectBrokerModuleConfiguration.isAcceptSelectionManually());
			identitiesMoveEvent.setMovedIdentities(response.getAddedIdentities());
			identitiesMoveEvent.setNotMovedIdentities(response.getIdentitiesAlreadyInGroup());
			// send mail for all of them
			MailTemplate mailTemplate = identitiesMoveEvent.getMailTemplate();
			if (mailTemplate != null) {
				MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
				String metaId = UUID.randomUUID().toString().replace("-", "");
				MailerResult result = new MailerResult();
				MailBundle[] bundles = mailManager.makeMailBundles(context, identitiesMoveEvent.getMovedIdentities(), mailTemplate, null, metaId, result);
				result.append(mailManager.sendMessage(bundles));
				if(mailTemplate.getCpfrom()) {
					MailBundle ccBundle = mailManager.makeMailBundle(context, urequest.getIdentity(), mailTemplate, null, metaId, result);
					result.append(mailManager.sendMessage(ccBundle));
				}
				
				Roles roles = urequest.getUserSession().getRoles();
				boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
				MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, urequest.getLocale());
			}
			fireEvent(urequest, Event.CHANGED_EVENT );		
			// Participant and waiting-list were changed => reload both
			projectMemberController.reloadData();
			projectCandidatesController.reloadData(); // Do only reload data in case of IdentitiesMoveEvent (IdentitiesAddEvent and reload data resulting in doublicate values)
		}
	}

	private void handleProjectMemberGroupEvent(UserRequest urequest, Event event) {
		if (event instanceof IdentitiesAddEvent) {
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent)event;
			BusinessGroupAddResponse response = businessGroupService.addParticipants(urequest.getIdentity(), urequest.getUserSession().getRoles(),
					identitiesAddedEvent.getAddIdentities(), project.getProjectGroup(), null);
			identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
			identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
			identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
			getLogger().info("Add users as project-members");
			fireEvent(urequest, Event.CHANGED_EVENT );			
		} else if (event instanceof IdentitiesRemoveEvent) {
			businessGroupService.removeParticipants(urequest.getIdentity(), ((IdentitiesRemoveEvent) event).getRemovedIdentities(), project.getProjectGroup(), null);
			getLogger().info("Remove users as account-managers");
			fireEvent(urequest, Event.CHANGED_EVENT );
		}
	}

	private void handleProjectLeaderGroupEvent(UserRequest urequest, Event event) {
		if (event instanceof IdentitiesAddEvent) {
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent)event;
			BusinessGroupAddResponse response = businessGroupService.addOwners(urequest.getIdentity(), urequest.getUserSession().getRoles(), identitiesAddedEvent.getAddIdentities(), project.getProjectGroup(), null);
			identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
			identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
			identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
			getLogger().info("Add users as project-leader");
			fireEvent(urequest, Event.CHANGED_EVENT );			
		} else if (event instanceof IdentitiesRemoveEvent) {
			businessGroupService.removeOwners(urequest.getIdentity(), ((IdentitiesRemoveEvent) event).getRemovedIdentities(), project.getProjectGroup());
			getLogger().info("Remove users as account-managers");
			fireEvent(urequest, Event.CHANGED_EVENT );
		}
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// child controller disposed by basic controller
	}
	
}
