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
package org.olat.course.member;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DBFactory;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMember_1_MemberStep;
import org.olat.course.member.wizard.InvitationContext;
import org.olat.course.member.wizard.InvitationFinishCallback;
import org.olat.course.member.wizard.Invitation_1_MailStep;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.course.member.wizard.MembersContext;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.DedupMembersConfirmationController;
import org.olat.group.ui.main.MemberListSecurityCallback;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.invitation.InvitationModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The members overview.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersOverviewController extends BasicController implements Activateable2 {
	
	private static final String SEG_ALL_MEMBERS = "All";
	
	private Link overrideLink;
	private Link unOverrideLink;
	private Link invitationLink;
	private final Link dedupLink;
	private final Link addMemberLink;
	private final Dropdown moreDropdown;
	private final Dropdown addMemberDropdown; 
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private CourseMemberListController memberListCtrl;
	
	private CloseableModalController cmc;
	private StepsMainRunController invitationWizard;
	private StepsMainRunController importMembersWizard;
	private DedupMembersConfirmationController dedupCtrl;
	
	private final boolean managed;
	private boolean overrideManaged = false;
	private final RepositoryEntry repoEntry;
	private final UserCourseEnvironment coachCourseEnv;
	private final MemberListSecurityCallback secCallback;
	
	@Autowired
	private InvitationModule invitationModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public MembersOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry repoEntry, UserCourseEnvironment coachCourseEnv, MemberListSecurityCallback secCallback,
			boolean canInvite) {
		super(ureq, wControl);
		this.repoEntry = repoEntry;
		this.toolbarPanel = toolbarPanel;
		this.coachCourseEnv = coachCourseEnv;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("members_overview");

		memberListCtrl = updateMembers(ureq);
		
		managed = RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement);
		if(managed &&  isAllowedToOverrideManaged(ureq)) {
			overrideLink = LinkFactory.createButton("override.member", mainVC, this);
			overrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
			
			unOverrideLink = LinkFactory.createButton("unoverride.member", mainVC, this);
			unOverrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
			unOverrideLink.setVisible(false);
		}
		
		addMemberLink = LinkFactory.createButton("add.member", mainVC, this);
		addMemberLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		addMemberLink.setElementCssClass("o_sel_course_add_member");
		addMemberLink.setVisible(!managed && !coachCourseEnv.isCourseReadOnly());
		mainVC.put("addMembers", addMemberLink);
		
		addMemberDropdown = new Dropdown("addmore", null, false, getTranslator());
		addMemberDropdown.setOrientation(DropdownOrientation.right);
		addMemberDropdown.setElementCssClass("o_sel_add_more");
		addMemberDropdown.setEmbbeded(true);
		addMemberDropdown.setButton(true);
		addMemberDropdown.setVisible(!managed && !coachCourseEnv.isCourseReadOnly());
		mainVC.put("addmore", addMemberDropdown);
		
		if(invitationModule.isCourseInvitationEnabled() && canInvite) {
			invitationLink = LinkFactory.createLink("invitation.member", mainVC, this);
			invitationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			invitationLink.setElementCssClass("o_sel_course_invitations");
			invitationLink.setVisible(!managed && !coachCourseEnv.isCourseReadOnly());
			addMemberDropdown.addComponent(invitationLink);
		} else {
			addMemberDropdown.setVisible(false);
		}

		moreDropdown = new Dropdown("more", null, false, getTranslator());
		moreDropdown.setCarretIconCSS("o_icon o_icon_commands");
		moreDropdown.setOrientation(DropdownOrientation.right);
		moreDropdown.setButton(true);
		moreDropdown.setVisible(!managed && !coachCourseEnv.isCourseReadOnly());
		mainVC.put("more", moreDropdown);
		
		dedupLink = LinkFactory.createLink("dedup.members", mainVC, this);
		dedupLink.setIconLeftCSS("o_icon o_icon-fw o_icon_cleanup");
		dedupLink.setVisible(!managed && !coachCourseEnv.isCourseReadOnly());
		moreDropdown.addComponent(dedupLink);
		
		putInitialPanel(mainVC);
	}
	
	protected boolean isAllowedToOverrideManaged(UserRequest ureq) {
		if(repoEntry != null) {
			Roles roles = ureq.getUserSession().getRoles();
			return roles.isAdministrator() && repositoryService.hasRoleExpanded(getIdentity(), repoEntry,
					OrganisationRoles.administrator.name());
		}
		return false;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		updateMembers(ureq).activate(ureq, entries, null);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == addMemberLink) {
			doImportMembers(ureq);
		} else if (source == invitationLink) {
			doInvitation(ureq);
		} else if (source == dedupLink) {
			doDedupMembers(ureq);
		} else if (source == overrideLink) {
			doOverrideManagedResource(ureq);
		} else if (source == unOverrideLink) {
			doUnOverrideManagedResource(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == importMembersWizard || source == invitationWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				cleanUp();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					if(memberListCtrl != null) {
						memberListCtrl.reloadModel();
					}
				}
			}
		} else if(source == dedupCtrl) {
			if(event == Event.DONE_EVENT) {
				dedupMembers(ureq, dedupCtrl.isDedupCoaches(), dedupCtrl.isDedupParticipants());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(importMembersWizard);
		removeAsListenerAndDispose(invitationWizard);
		removeAsListenerAndDispose(dedupCtrl);
		removeAsListenerAndDispose(cmc);
		importMembersWizard = null;
		invitationWizard = null;
		dedupCtrl = null;
		cmc = null;
	}
	
	protected void reloadMembers() {
		if(memberListCtrl != null) {
			memberListCtrl.reloadModel();
		}
	}
	
	private void doOverrideManagedResource(UserRequest ureq) {
		overrideManagedResource(ureq, true);
	}
	
	private void doUnOverrideManagedResource(UserRequest ureq) {
		overrideManagedResource(ureq, false);
	}
	
	private void overrideManagedResource(UserRequest ureq, boolean override) {
		overrideManaged = override;

		overrideLink.setVisible(!overrideManaged);
		unOverrideLink.setVisible(overrideManaged);
		
		addMemberLink.setVisible(overrideManaged);
		dedupLink.setVisible(overrideManaged);
		mainVC.setDirty(true);
		
		if(memberListCtrl != null) {
			memberListCtrl.overrideManaged(ureq, overrideManaged);
		}
	}
	
	private void doImportMembers(UserRequest ureq) {
		removeAsListenerAndDispose(importMembersWizard);

		MembersContext membersContext = MembersContext.valueOf(repoEntry, overrideManaged);
		Step start = new ImportMember_1_MemberStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			MembersByNameContext membersByNameContext = (MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY);
			if(!membersByNameContext.getNotFoundNames().isEmpty()) {
				String notFoundNames = membersByNameContext.getNotFoundNames().stream()
						.collect(Collectors.joining(", "));
				showWarning("user.notfound", notFoundNames);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_course_member_import_logins_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	protected void addMembers(UserRequest ureq, StepsRunContext runContext) {
		Set<Identity> members = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
		
		MemberPermissionChangeEvent changes = (MemberPermissionChangeEvent)runContext.get("permissions");
		
		MailTemplate template = (MailTemplate)runContext.get("mailTemplate");
		//commit changes to the repository entry
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(template, result, getWindowControl().getBusinessControl().getAsString(), template != null);
		
		Roles roles = ureq.getUserSession().getRoles();
		List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.generateRepositoryChanges(members);
		repositoryManager.updateRepositoryEntryMemberships(getIdentity(), roles, repoEntry, repoChanges, reMailing);

		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.generateBusinessGroupMembershipChange(members);
		
		MailPackage mailing = new MailPackage(template, result, getWindowControl().getBusinessControl().getAsString(), template != null);
		businessGroupService.updateMemberships(getIdentity(), allModifications, mailing);
		
		//commit all changes to the curriculum memberships
		List<CurriculumElementMembershipChange> curriculumChanges = changes.generateCurriculumElementMembershipChange(members);
		curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, curriculumChanges, mailing);
		
		boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
		MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, getLocale());
		
		switchToAllMembers(ureq);
	}
	
	protected void doInvitation(UserRequest ureq) {
		removeAsListenerAndDispose(invitationWizard);

		InvitationContext invitationContext = InvitationContext.valueOf(repoEntry, overrideManaged);
		Step start = new Invitation_1_MailStep(ureq, invitationContext);
		StepRunnerCallback finish = new InvitationFinishCallback(invitationContext);
		invitationWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("invitation.member"), "o_sel_course_member_invitation_wizard");
		listenTo(invitationWizard);
		getWindowControl().pushAsModalDialog(invitationWizard.getInitialComponent());
	}
	
	protected void doDedupMembers(UserRequest ureq) {
		int numOfDuplicate = businessGroupService.countDuplicateMembers(repoEntry, true, true);
		if(numOfDuplicate <= 0) {
			showInfo("dedup.members.notfound");
		} else {
			dedupCtrl = new DedupMembersConfirmationController(ureq, getWindowControl(), numOfDuplicate);
			listenTo(dedupCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), dedupCtrl.getInitialComponent(),
					true, translate("dedup.members"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	protected void dedupMembers(UserRequest ureq, boolean coaches, boolean participants) {
		businessGroupService.dedupMembers(ureq.getIdentity(), repoEntry, coaches, participants);
		showInfo("dedup.done");
		if(memberListCtrl != null) {
			memberListCtrl.reloadModel();
		}
	}
	
	public void switchToAllMembers(UserRequest ureq) {
		DBFactory.getInstance().commit();//make sure all is on the DB before reloading
		memberListCtrl.switchToAllMembers(ureq);
	}
	
	private CourseMemberListController updateMembers(UserRequest ureq) {
		if(memberListCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(SEG_ALL_MEMBERS, 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			SearchMembersParams searchParams = new SearchMembersParams(GroupRoles.owner, GroupRoles.coach, GroupRoles.participant);
			memberListCtrl = new CourseMemberListController(ureq, bwControl, toolbarPanel, repoEntry, coachCourseEnv, secCallback, searchParams, null);
			listenTo(memberListCtrl);
		}
		
		memberListCtrl.overrideManaged(ureq, overrideManaged);
		mainVC.put("membersCmp", memberListCtrl.getInitialComponent());
		addToHistory(ureq, memberListCtrl);
		return memberListCtrl;
	}
}
