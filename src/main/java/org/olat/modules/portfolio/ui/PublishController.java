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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioElementType;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.ui.event.AccessRightsEvent;
import org.olat.modules.portfolio.ui.renderer.PortfolioRendererHelper;
import org.olat.modules.portfolio.ui.wizard.AccessRightsContext;
import org.olat.modules.portfolio.ui.wizard.AddMember_1_ChooseMemberStep;
import org.olat.modules.portfolio.ui.wizard.AddMember_1_CourseMemberChoiceStep;
import org.olat.modules.portfolio.ui.wizard.AddMember_3_ChoosePermissionStep;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublishController extends BasicController implements TooledController {
	
	private Link addInvitationLink;
	private Link addAccessRightsLink;
	private Link addCoachAccessRightsLink;
	private Link addParticipantAccessRightsLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;

	private CloseableModalController cmc;
	private StepsMainRunController addMembersWizard;
	private AccessRightsEditController editAccessRightsCtrl;
	private InvitationEditRightsController addInvitationCtrl;
	private InvitationEmailController addInvitationEmailCtrl;
	
	private int counter;
	private Binder binder;
	private RepositoryEntry entry;
	private PortfolioElementRow binderRow;
	private final BinderConfiguration config;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PortfolioService portfolioService;
	
	public PublishController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl);
		this.binder = binder;
		this.config = config;
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		
		if(binder.getEntry() != null) {
			entry = binder.getEntry();
		}
		
		mainVC = createVelocityContainer("publish");
		mainVC.contextPut("binderTitle", StringHelper.escapeHtml(binder.getTitle()));
		
		binderRow = new PortfolioElementRow(binder, null);
		mainVC.contextPut("binderRow", binderRow);
		putInitialPanel(mainVC);
		reloadData();
	}
	
	@Override
	public void initTools() {
		if(secCallback.canEditAccessRights(binder)) {
			Dropdown accessDropdown = new Dropdown("access.rights", "access.rights", false, getTranslator());
			accessDropdown.setIconCSS("o_icon o_icon-fw o_icon_new_portfolio");
			accessDropdown.setElementCssClass("o_sel_pf_access");
			accessDropdown.setOrientation(DropdownOrientation.right);
			
			if(entry != null) {
				addCoachAccessRightsLink = LinkFactory.createToolLink("add.course.coach", translate("add.course.coach"), this);
				addCoachAccessRightsLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_user_vip");
				addCoachAccessRightsLink.setElementCssClass("o_sel_pf_access_course_coach");
				accessDropdown.addComponent(addCoachAccessRightsLink);
				
				addParticipantAccessRightsLink = LinkFactory.createToolLink("add.course.participant", translate("add.course.participant"), this);
				addParticipantAccessRightsLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_group");
				addParticipantAccessRightsLink.setElementCssClass("o_sel_pf_access_course_participant");
				accessDropdown.addComponent(addParticipantAccessRightsLink);
			}
			
			addAccessRightsLink = LinkFactory.createToolLink("add.member", translate("add.member"), this);
			addAccessRightsLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_user");
			addAccessRightsLink.setElementCssClass("o_sel_pf_access_member");
			accessDropdown.addComponent(addAccessRightsLink);
			
			if(loginModule.isInvitationEnabled()) {
				addInvitationLink = LinkFactory.createToolLink("add.invitation", translate("add.invitation"), this);
				addInvitationLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_user_anonymous");
				addInvitationLink.setElementCssClass("o_sel_pf_access_invitation");
				accessDropdown.addComponent(addInvitationLink);
			}
			
			
			
			stackPanel.addTool(accessDropdown, Align.right);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void reloadData() {
		binderRow.getChildren().clear();
		binderRow.getAccessRights().clear();
		
		List<AccessRights> rights = portfolioService.getAccessRights(binder);
		boolean canEditBinderAccessRights = secCallback.canEditAccessRights(binder);
		for(AccessRights right:rights) {
			if(right.getSectionKey() == null && right.getPageKey() == null) {
				if(PortfolioRoles.invitee.equals(right.getRole())) {
					continue;//only access
				}
				
				Link editLink = null;
				if(canEditBinderAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
					String id = "edit_" + (counter++);
					editLink = LinkFactory.createLink(id, id, "edit_access", "edit", getTranslator(), mainVC, this, Link.LINK);
				}
				binderRow.getAccessRights().add(new AccessRightsRow(binder, right, editLink));
			}
		}
		
		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(binder, getIdentity());
		Map<Section,AssessmentSection> sectionToAssessmentSectionMap = new HashMap<>();
		for(AssessmentSection assessmentSection:assessmentSections) {
			sectionToAssessmentSectionMap.put(assessmentSection.getSection(), assessmentSection);
		}

		//sections
		List<Section> sections = portfolioService.getSections(binder);
		Map<Long,PortfolioElementRow> sectionMap = new HashMap<>();
		for(Section section:sections) {
			boolean canEditSectionAccessRights = secCallback.canEditAccessRights(section);
			boolean canViewSectionAccessRights = secCallback.canViewAccessRights(section);
			if(canEditSectionAccessRights || canViewSectionAccessRights) {
				PortfolioElementRow sectionRow = new PortfolioElementRow(section, sectionToAssessmentSectionMap.get(section));
				binderRow.getChildren().add(sectionRow);
				sectionMap.put(section.getKey(), sectionRow);	
	
				for(AccessRights right:rights) {
					if(section.getKey().equals(right.getSectionKey()) && right.getPageKey() == null) {
						Link editLink = null;
						if(canEditSectionAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
							String id = "edit_" + (counter++);
							editLink = LinkFactory.createLink(id, id, "edit_access", "edit", getTranslator(), mainVC, this, Link.LINK);
							sectionRow.getAccessRights().add(new AccessRightsRow(section, right, editLink));
						}
					}
				}
			}
		}
		
		//pages
		List<Page> pages = portfolioService.getPages(binder, null);
		for(Page page:pages) {
			boolean canEditPageAccessRights = secCallback.canEditAccessRights(page);
			boolean canViewPageAccessRights = secCallback.canViewAccessRights(page);
			if(canEditPageAccessRights || canViewPageAccessRights) {
				Section section = page.getSection();
				PortfolioElementRow sectionRow = sectionMap.get(section.getKey());
				if(sectionRow == null) {
					logError("Section not found: " + section.getKey() + " of page: " + page.getKey(), null);
					continue;
				}
				
				PortfolioElementRow pageRow = new PortfolioElementRow(page, null);
				sectionRow.getChildren().add(pageRow);
	
				for(AccessRights right:rights) {
					if(page.getKey().equals(right.getPageKey())) {
						Link editLink = null;
						if(canEditPageAccessRights && !PortfolioRoles.owner.equals(right.getRole())) {
							String id = "edit_" + (counter++);
							editLink = LinkFactory.createLink(id, id, "edit_access", "edit", getTranslator(), mainVC, this, Link.LINK);
							pageRow.getAccessRights().add(new AccessRightsRow(page, right, editLink));
						}
					}
				}
			}
		}
		
		mainVC.setDirty(true);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addAccessRightsLink == source) {
			doAddAccessRights(ureq);
		} else if(addInvitationLink == source) {
			doAddInvitationEmail(ureq);
		} else if(addCoachAccessRightsLink == source) {
			doAddCoachAccessRights(ureq);
		} else if(addParticipantAccessRightsLink == source) {
			doAddParticipantAccessRights(ureq);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("edit_access".equals(cmd)) {
				AccessRightsRow row = (AccessRightsRow)link.getUserObject();
				if(PortfolioRoles.invitee.name().equals(row.getRole())
						|| PortfolioRoles.readInvitee.name().equals(row.getRole())) {
					doEditInvitation(ureq, row.getIdentity());
				} else {
					doEditAccessRights(ureq, row.getElement(), row.getIdentity());
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addMembersWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadData();
				}
				cleanUp();
			}
		} else if(addInvitationEmailCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				String email = addInvitationEmailCtrl.getEmail();
				Identity invitee = addInvitationEmailCtrl.getInvitee();
				cmc.deactivate();
				cleanUp();
				
				if(event == Event.DONE_EVENT) {
					if(invitee != null) {
						doAddInvitation(ureq, invitee);
					} else {
						doAddInvitation(ureq, email);
					}
				}
			}
		} else if(addInvitationCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadData();
				}
				cleanUp();
			}
		} else if(editAccessRightsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				List<AccessRightChange> changes = editAccessRightsCtrl.getChanges();
				List<Identity> identities = Collections.singletonList(editAccessRightsCtrl.getMember());
				portfolioService.changeAccessRights(identities, changes);
				reloadData();
			} else if(AccessRightsEvent.REMOVE_ALL_RIGHTS.equals(event.getCommand())) {
				portfolioService.removeAccessRights(binder, editAccessRightsCtrl.getMember(),
						PortfolioRoles.coach, PortfolioRoles.reviewer, PortfolioRoles.invitee, PortfolioRoles.readInvitee);
				reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addInvitationEmailCtrl);
		removeAsListenerAndDispose(editAccessRightsCtrl);
		removeAsListenerAndDispose(addInvitationCtrl);
		removeAsListenerAndDispose(addMembersWizard);
		removeAsListenerAndDispose(cmc);
		addInvitationEmailCtrl = null;
		editAccessRightsCtrl = null;
		addInvitationCtrl = null;
		addMembersWizard = null;
		cmc = null;
	}
	
	private void doAddInvitationEmail(UserRequest ureq) {
		if(guardModalController(addInvitationEmailCtrl)) return;
		
		addInvitationEmailCtrl = new InvitationEmailController(ureq, getWindowControl(), binder);
		listenTo(addInvitationEmailCtrl);
		
		String title = translate("add.invitation");
		cmc = new CloseableModalController(getWindowControl(), null, addInvitationEmailCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddInvitation(UserRequest ureq, String email) {
		if(guardModalController(addInvitationCtrl)) return;
		
		addInvitationCtrl = new InvitationEditRightsController(ureq, getWindowControl(), binder, email, null);
		listenTo(addInvitationCtrl);
		
		String title = translate("add.invitation");
		cmc = new CloseableModalController(getWindowControl(), null, addInvitationCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddInvitation(UserRequest ureq, Identity invitee) {
		removeAsListenerAndDispose(addMembersWizard);
		
		Roles inviteeRoles = securityManager.getRoles(invitee);
		if(inviteeRoles.isInvitee()) {
			doAddInvitation(ureq, invitee.getUser().getEmail());
		} else {
		
			Step start = new AddMember_3_ChoosePermissionStep(ureq, binder, invitee);
			StepRunnerCallback finish = (uureq, wControl, runContext) -> {
				AccessRightsContext rightsContext = (AccessRightsContext)runContext.get("rightsContext");
				MailTemplate mailTemplate = (MailTemplate)runContext.get("mailTemplate");
				addMembers(rightsContext, mailTemplate);
				return StepsMainRunController.DONE_MODIFIED;
			};
			
			addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
					translate("add.member"), "o_sel_course_member_import_1_wizard");
			listenTo(addMembersWizard);
			getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
		}
	}
	
	private void doEditInvitation(UserRequest ureq, Identity invitee) {
		if(guardModalController(addInvitationCtrl)) return;

		addInvitationCtrl = new InvitationEditRightsController(ureq, getWindowControl(), binder, invitee);
		listenTo(addInvitationCtrl);
		
		String title = translate("add.invitation");
		cmc = new CloseableModalController(getWindowControl(), null, addInvitationCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddCoachAccessRights(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizard);

		Step start = new AddMember_1_CourseMemberChoiceStep(ureq, binder, entry, GroupRoles.coach);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			AccessRightsContext rightsContext = (AccessRightsContext)runContext.get("rightsContext");
			MailTemplate mailTemplate = (MailTemplate)runContext.get("mailTemplate");
			addMembers(rightsContext, mailTemplate);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.course.coach"), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizard);
		getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
	}
	
	private void doAddParticipantAccessRights(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizard);

		Step start = new AddMember_1_CourseMemberChoiceStep(ureq, binder, entry, GroupRoles.participant);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			AccessRightsContext rightsContext = (AccessRightsContext)runContext.get("rightsContext");
			MailTemplate mailTemplate = (MailTemplate)runContext.get("mailTemplate");
			addMembers(rightsContext, mailTemplate);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.course.participant"), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizard);
		getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
	}
	
	private void doAddAccessRights(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizard);

		Step start = new AddMember_1_ChooseMemberStep(ureq, binder);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			AccessRightsContext rightsContext = (AccessRightsContext)runContext.get("rightsContext");
			MailTemplate mailTemplate = (MailTemplate)runContext.get("mailTemplate");
			addMembers(rightsContext, mailTemplate);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		addMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizard);
		getWindowControl().pushAsModalDialog(addMembersWizard.getInitialComponent());
		
	}
	
	private void doEditAccessRights(UserRequest ureq, PortfolioElement element, Identity member) {
		if(guardModalController(editAccessRightsCtrl)) return;
		
		boolean canEdit = secCallback.canEditAccessRights(element);
		editAccessRightsCtrl = new AccessRightsEditController(ureq, getWindowControl(), binder, member, canEdit);
		listenTo(editAccessRightsCtrl);
		
		String title = translate("edit.access.rights");
		cmc = new CloseableModalController(getWindowControl(), null, editAccessRightsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void addMembers(AccessRightsContext rightsContext, MailTemplate mailTemplate) {
		List<Identity> identities = rightsContext.getIdentities();
		List<AccessRightChange> changes = rightsContext.getAccessRightChanges();
		portfolioService.changeAccessRights(identities, changes);
		
		if(mailTemplate != null) {
			sendInvitation(identities, mailTemplate);
		}
		reloadData();
	}
	
	private void sendInvitation(List<Identity> identities, MailTemplate mailTemplate) {
		ContactList contactList = new ContactList("Invitation");
		contactList.addAllIdentites(identities);

		boolean success = false;
		try {
			MailContext context = new MailContextImpl(binder, null, getWindowControl().getBusinessControl().getAsString()); 
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(getIdentity());
			bundle.setContactList(contactList);
			bundle.setContent(mailTemplate.getSubjectTemplate(), mailTemplate.getBodyTemplate());
			MailerResult result = mailManager.sendMessage(bundle);
			success = result.isSuccessful();
		} catch (Exception e) {
			logError("Error on sending invitation mail to contactlist, invalid address.", e);
		}
		if (success) {
			showInfo("invitation.mail.success");
		}	else {
			showError("invitation.mail.failure");			
		}
	}
	
	public class AccessRightsRow {
		
		private final AccessRights rights;
		private final PortfolioElement element;
		private String fullName;
		private Link editLink;
		
		public AccessRightsRow(PortfolioElement element, AccessRights rights, Link editLink) {
			this.rights = rights;
			this.editLink = editLink;
			this.element = element;
			
			if(rights.getInvitation() == null) {
				fullName = userManager.getUserDisplayName(rights.getIdentity());
			} else {
				Invitation invitation = rights.getInvitation();
				fullName = userManager.getUserDisplayName(invitation.getFirstName(), invitation.getLastName()) + " :: " + invitation.getKey();
			}
			
			if(editLink != null) {
				editLink.setUserObject(this);
			}
		}
		
		public String getRole() {
			return rights.getRole().name();
		}
		
		public Identity getIdentity() {
			return rights.getIdentity();
		}
		
		public PortfolioElement getElement() {
			return element;
		}
		
		public String getFullName() {
			return fullName;
		}
		
		public String getCssClass() {
			if(PortfolioRoles.reviewer.equals(rights.getRole())) {
				return "o_icon o_icon_reviewer o_icon-fw";
			}
			return "o_icon o_icon_user o_icon-fw";
		}

		public Link getEditLink() {
			return editLink;
		}
		
		public String getExplanation() {
			String explanation = null;
			if(PortfolioRoles.owner.equals(rights.getRole())) {
				explanation = translate("access.rights.owner.long");
			} else if(PortfolioRoles.coach.equals(rights.getRole())) {
				explanation = translate("access.rights.coach.long");
			} else if(PortfolioRoles.reviewer.equals(rights.getRole())) {
				explanation = translate("access.rights.reviewer.long");
			} else if(PortfolioRoles.readInvitee.equals(rights.getRole())) {
				explanation = translate("access.rights.invitee.long");
			}
			return explanation;
		}
	}

	public class PortfolioElementRow {
		
		private final PortfolioElement element;
		private List<PortfolioElementRow> children;
		private List<AccessRightsRow> accessRights = new ArrayList<>();
		
		private final AssessmentSection assessmentSection;
		
		public PortfolioElementRow(PortfolioElement element, AssessmentSection assessmentSection) {
			this.element = element;
			this.assessmentSection = assessmentSection;
		}
		
		public boolean isAssessable() {
			return config.isAssessable();
		}
		
		
		public String getTitle() {
			return element.getTitle();
		}
		
		public String getCssClassStatus() {
			if(element.getType() == PortfolioElementType.section) {
				Section section = (Section)element;
				return section.getSectionStatus() == null
					? SectionStatus.notStarted.iconClass() : section.getSectionStatus().iconClass();
			}
			return "";
		}
		
		public String getFormattedResult() {
			if(element.getType() == PortfolioElementType.section) {
				return PortfolioRendererHelper.getFormattedResult(assessmentSection, getTranslator());
			}
			return "";
		}
		
		public List<AccessRightsRow> getAccessRights() {
			return accessRights;
		}
		
		public List<PortfolioElementRow> getChildren() {
			if(children == null) {
				children = new ArrayList<>();
			}
			return children;
		}
	}
}
