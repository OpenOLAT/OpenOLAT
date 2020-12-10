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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.author;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.member.wizard.ImportMember_1a_LoginListStep;
import org.olat.course.member.wizard.ImportMember_1b_ChooseMemberStep;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.MemberListSecurityCallbackFactory;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.group.ui.main.MemberRow;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The members list specific to the repository entries (except courses which
 * have a specialized one).
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryMembersController extends AbstractMemberListController {
	
	private final SearchMembersParams params;
	private FormLink addMemberLink;
	private FormLink importMemberLink; 
	private StepsMainRunController importMembersWizard;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	public RepositoryMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry repoEntry) {
		super(ureq, wControl, repoEntry, null, "all_member_list", MemberListSecurityCallbackFactory.adminRights(), stackPanel,
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(),
						Util.createPackageTranslator(AbstractMemberListController.class, ureq.getLocale())));

		params = new SearchMembersParams(true, GroupRoles.owner, GroupRoles.coach, GroupRoles.participant, GroupRoles.waiting);
		reloadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		boolean managed = RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement);
		addMemberLink = uifactory.addFormLink("add.member", formLayout, Link.BUTTON);
		addMemberLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		addMemberLink.setVisible(!managed);

		importMemberLink = uifactory.addFormLink("import.member", formLayout, Link.BUTTON);
		importMemberLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
		importMemberLink.setVisible(!managed);
	}

	@Override
	protected SearchMembersParams getSearchParams() {
		return params;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addMemberLink) {
			doChooseMembers(ureq);
		} else if (source == importMemberLink) {
			doImportMembers(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if(source == importMembersWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importMembersWizard);
				importMembersWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadModel();
				}
			}
		} else {
			super.event(ureq, source, event);
		}
	}
	
	@Override
	protected void doOpenAssessmentTool(UserRequest ureq, MemberRow member) {
		//
	}
	
	private void doChooseMembers(UserRequest ureq) {
		removeAsListenerAndDispose(importMembersWizard);

		MembersContext membersContext = MembersContext.valueOf(repoEntry, false);
		Step start = new ImportMember_1b_ChooseMemberStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_group_import_1_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void doImportMembers(UserRequest ureq) {
		removeAsListenerAndDispose(importMembersWizard);

		MembersContext membersContext = MembersContext.valueOf(repoEntry, false);
		Step start = new ImportMember_1a_LoginListStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			if(runContext.containsKey("notFounds")) {
				showWarning("user.notfound", runContext.get("notFounds").toString());
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.member"), "o_sel_group_import_logins_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	protected void addMembers(UserRequest ureq, StepsRunContext runContext) {
		Roles roles = ureq.getUserSession().getRoles();
		
		@SuppressWarnings("unchecked")
		List<Identity> members = (List<Identity>)runContext.get("members");
		MailTemplate template = (MailTemplate)runContext.get("mailTemplate");
		MemberPermissionChangeEvent changes = (MemberPermissionChangeEvent)runContext.get("permissions");
		
		//commit changes to the repository entry
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(template, result, getWindowControl().getBusinessControl().getAsString(), template != null);
		List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.getRepoChanges();
		repositoryManager.updateRepositoryEntryMemberships(getIdentity(), roles, repoEntry, repoChanges, reMailing);

		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.getGroupChanges();
		MailPackage bgMailing = new MailPackage(template, result, getWindowControl().getBusinessControl().getAsString(), template != null);
		businessGroupService.updateMemberships(getIdentity(), allModifications, bgMailing);
		boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
		MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, getLocale());
		
		//commit all changes to the curriculum memberships
		MailPackage curMailing = new MailPackage(template, result, getWindowControl().getBusinessControl().getAsString(), template != null);
		List<CurriculumElementMembershipChange> curriculumChanges = changes.generateCurriculumElementMembershipChange(members);
		curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, curriculumChanges, curMailing);
	}
}
