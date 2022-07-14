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
package org.olat.group.ui.edit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMember_1a_LoginListStep;
import org.olat.course.member.wizard.ImportMember_1b_ChooseMemberStep;
import org.olat.course.member.wizard.InvitationContext;
import org.olat.course.member.wizard.InvitationFinishCallback;
import org.olat.course.member.wizard.Invitation_1_MailStep;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.MemberListSecurityCallback;
import org.olat.group.ui.main.MemberListSecurityCallbackFactory;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.modules.invitation.InvitationConfigurationPermission;
import org.olat.modules.invitation.InvitationModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMembersController extends BasicController {
	
	private Link invitationLink;
	private final Link addMemberLink;
	private final Link importMemberLink;
	private final Dropdown addMemberDropdown; 
	private final VelocityContainer mainVC;

	private final DisplayMemberSwitchForm dmsForm;
	private final MembershipConfigurationForm configForm;
	private MemberListController membersController;
	private StepsMainRunController invitationWizard;
	private StepsMainRunController importMembersWizard;
	
	private BusinessGroup businessGroup;
	
	@Autowired
	private InvitationModule invitationModule;
	@Autowired
	private BusinessGroupService businessGroupService;

	public BusinessGroupMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			BusinessGroup businessGroup, boolean readOnly) {
		super(ureq, wControl);
		
		this.businessGroup = businessGroup;
		
		mainVC = createVelocityContainer("tab_bgGrpMngmnt");
		putInitialPanel(mainVC);
		
		boolean hasWaitingList = businessGroup.getWaitingListEnabled().booleanValue();

		// Member Display Form, allows to enable/disable that others partips see
		// partips and/or owners
		// configure the form with checkboxes for owners and/or partips according
		// the booleans
		dmsForm = new DisplayMemberSwitchForm(ureq, getWindowControl(), true, true, hasWaitingList);
		dmsForm.setEnabled(!BusinessGroupManagedFlag.isManaged(businessGroup, BusinessGroupManagedFlag.display) && !readOnly);
		listenTo(dmsForm);
		// set if the checkboxes are checked or not.
		dmsForm.setDisplayMembers(businessGroup);
		mainVC.put("displayMembers", dmsForm.getInitialComponent());

		boolean managed = BusinessGroupManagedFlag.isManaged(businessGroup, BusinessGroupManagedFlag.membersmanagement);
		configForm = new MembershipConfigurationForm(ureq, getWindowControl(), managed, readOnly);
		listenTo(configForm);
		configForm.setMembershipConfiguration(businessGroup);
		mainVC.put("configMembers", configForm.getInitialComponent());
		
		SearchMembersParams searchParams = new SearchMembersParams(true, GroupRoles.coach, GroupRoles.participant, GroupRoles.waiting);
		MemberListSecurityCallback secCallback = MemberListSecurityCallbackFactory.getSecurityCallback(readOnly, !managed && !readOnly);
		membersController = new MemberListController(ureq, getWindowControl(), toolbarPanel, businessGroup, searchParams, secCallback);
		listenTo(membersController);
		
		membersController.reloadModel();

		mainVC.put("members", membersController.getInitialComponent());
		
		addMemberDropdown = new Dropdown("addmore", null, false, getTranslator());
		addMemberDropdown.setOrientation(DropdownOrientation.right);
		addMemberDropdown.setEmbbeded(true);
		addMemberDropdown.setButton(true);
		addMemberDropdown.setVisible(!managed && !readOnly);
		mainVC.put("addmore", addMemberDropdown);
		
		addMemberLink = LinkFactory.createButton("add.member", mainVC, this);
		addMemberLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		addMemberLink.setElementCssClass("o_sel_group_add_member");
		addMemberLink.setVisible(!managed && !readOnly);
		mainVC.put("addMembers", addMemberLink);
		
		importMemberLink = LinkFactory.createLink("import.member", mainVC, this);
		importMemberLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
		importMemberLink.setElementCssClass("o_sel_group_import_members");
		importMemberLink.setVisible(!managed);
		addMemberDropdown.addComponent(importMemberLink);
		
		if(isAllowedToInvite(ureq)) {
			invitationLink = LinkFactory.createLink("invitation.member", mainVC, this);
			invitationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			invitationLink.setElementCssClass("o_sel_course_invitations");
			invitationLink.setVisible(!managed);
			addMemberDropdown.addComponent(invitationLink);
		}
	}
	
	public BusinessGroup getGroup() {
		return businessGroup;
	}
	
	private boolean isAllowedToInvite(UserRequest ureq) {
		if(!invitationModule.isBusinessGroupInvitationEnabled()) {
			return false;
		}
		Roles roles = ureq.getUserSession().getRoles();
		return roles.isAdministrator() || roles.isGroupManager()
				|| (roles.isAuthor() && invitationModule.getBusinessGroupCoachPermission() == InvitationConfigurationPermission.allResources)
				|| (roles.isAuthor() && invitationModule.getBusinessGroupCoachPermission() == InvitationConfigurationPermission.perResource
						&& businessGroup.isInvitationByCoachWithAuthorRightsEnabled());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		 if (source == addMemberLink) {
			doChooseMembers(ureq);
		} else if (source == importMemberLink) {
			doImportMembers(ureq);
		} else if (source == invitationLink) {
			doInvitation(ureq);
		}
	}
	
	protected void updateBusinessGroup(BusinessGroup bGroup) {
		this.businessGroup = bGroup;
		
		boolean hasWaitingList = businessGroup.getWaitingListEnabled().booleanValue();	
		Boolean waitingFlag = (Boolean)mainVC.getContext().get("hasWaitingGrp");
		if(waitingFlag == null || waitingFlag.booleanValue() != hasWaitingList) {
			mainVC.contextPut("hasWaitingGrp", Boolean.valueOf(hasWaitingList));
			dmsForm.setWaitingListVisible(hasWaitingList);
		}
		
		membersController.reloadModel();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dmsForm) {
			if(event == Event.CHANGED_EVENT) {
				boolean ownersIntern = dmsForm.isDisplayOwnersIntern();
				boolean participantsIntern = dmsForm.isDisplayParticipantsIntern();
				boolean waitingIntern = dmsForm.isDisplayWaitingListIntern();
				boolean ownersPublic = dmsForm.isDisplayOwnersPublic();
				boolean participantsPublic = dmsForm.isDisplayParticipantsPublic();
				boolean waitingPublic = dmsForm.isDisplayWaitingListPublic();
				boolean download = dmsForm.isDownloadList();
				
				//changes are committed by the service
				businessGroup = businessGroupService.updateDisplayMembers(businessGroup,
						ownersIntern, participantsIntern, waitingIntern,
						ownersPublic, participantsPublic, waitingPublic,
						download);
				fireEvent(ureq, event);
				
				// notify current active users of this business group
				BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, businessGroup, null, getIdentity());
				// do loggin
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());
			}
		} else if (source == configForm) {
			if(event == Event.CHANGED_EVENT) {
				boolean allow = configForm.isAllowToLeaveBusinessGroup();
				businessGroup = businessGroupService.updateAllowToLeaveBusinessGroup(businessGroup, allow);
				fireEvent(ureq, event);
				
				// notify current active users of this business group
				BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, businessGroup, null, getIdentity());
				// do loggin
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());
			}
		} else if(source == importMembersWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importMembersWizard);
				importMembersWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					membersController.reloadModel();
				}
			}
		} else if(source == invitationWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(invitationWizard);
				invitationWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					membersController.reloadModel();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doChooseMembers(UserRequest ureq) {
		removeAsListenerAndDispose(importMembersWizard);

		MembersContext membersContext = MembersContext.valueOf(businessGroup);
		Step start = new ImportMember_1b_ChooseMemberStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(runContext);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_group_import_1_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void doImportMembers(UserRequest ureq) {
		removeAsListenerAndDispose(importMembersWizard);

		MembersContext membersContext = MembersContext.valueOf(businessGroup);
		Step start = new ImportMember_1a_LoginListStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(runContext);
			MembersByNameContext membersByNameContext = (MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY);
			if(!membersByNameContext.getNotFoundNames().isEmpty()) {
				String notFoundNames = membersByNameContext.getNotFoundNames().stream()
						.collect(Collectors.joining(", "));
				showWarning("user.notfound", notFoundNames);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.member"), "o_sel_group_import_logins_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void addMembers(StepsRunContext runContext) {
		Set<Identity> members = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
		
		MemberPermissionChangeEvent changes = (MemberPermissionChangeEvent)runContext.get("permissions");

		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.generateBusinessGroupMembershipChange(members);
		
		MailTemplate template = (MailTemplate)runContext.get("mailTemplate");
		MailPackage mailing = new MailPackage(template, getWindowControl().getBusinessControl().getAsString(), template != null);
		businessGroupService.updateMemberships(getIdentity(), allModifications, mailing);
		MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(), false, getLocale());
	}
	
	protected void doInvitation(UserRequest ureq) {
		removeAsListenerAndDispose(invitationWizard);

		InvitationContext invitationContext = InvitationContext.valueOf(businessGroup);
		Step start = new Invitation_1_MailStep(ureq, invitationContext);
		StepRunnerCallback finish = new InvitationFinishCallback(invitationContext);
		invitationWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("invitation.member"), "o_sel_course_member_invitation_wizard");
		listenTo(invitationWizard);
		getWindowControl().pushAsModalDialog(invitationWizard.getInitialComponent());
	}
}
