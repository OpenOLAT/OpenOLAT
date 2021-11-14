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
package org.olat.course.member.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.mail.MailTemplate;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.BusinessGroupMailing;
import org.olat.group.manager.BusinessGroupMailing.MailType;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.group.ui.wizard.BGMailTemplateController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryMailing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberMailController extends StepFormBasicController {
	
	private MailTemplate mailTemplate;
	private final BGMailTemplateController mailTemplateForm;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;

	public ImportMemberMailController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry,
			Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "mail_template");
		
		MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)runContext.get("permissions");
		boolean mandatoryEmail = businessGroupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		if(mandatoryEmail && e != null) {
			boolean includeParticipantsOrTutorsRights = hasParticipantOrTutorsRightsChanges(e);
			if(!includeParticipantsOrTutorsRights) {
				mandatoryEmail = false;//only mandatory for participants and tutors
			}
		}
		
		MailType defaultGroupType = BusinessGroupMailing.getDefaultTemplateType(e);
		RepositoryMailing.Type defaultRepoType = RepositoryMailing.getDefaultTemplateType(e);

		if(defaultGroupType != null && e.getGroups().size() == 1) {
			BusinessGroupShort group = e.getGroups().get(0);
			mailTemplate = BusinessGroupMailing.getDefaultTemplate(defaultGroupType, group, getIdentity());
		} else if(defaultRepoType != null) {
			mailTemplate = RepositoryMailing.getDefaultTemplate(defaultRepoType, repoEntry, getIdentity());
		} else if(hasCourseRights(e)) {
			mailTemplate = RepositoryMailing.createAddParticipantMailTemplate(repoEntry, getIdentity());
		} else if(hasCurriculumRights(e)) {
			CurriculumElement curriculumElement = e.getRootCurriculumElement();
			Curriculum curriculum = curriculumElement.getCurriculum();
			mailTemplate = CurriculumMailing.getDefaultMailTemplate(curriculum, curriculumElement, getIdentity());
		} else {
			mailTemplate = BusinessGroupMailing.getDefaultTemplate(MailType.addParticipant, null, getIdentity());
		}
		mailTemplateForm = new BGMailTemplateController(ureq, wControl, mailTemplate, false, true, false, mandatoryEmail, rootForm);
		
		initForm (ureq);
	}
	
	private boolean hasCurriculumRights(MemberPermissionChangeEvent e) {
		return e != null && e.getCurriculumChanges() != null && !e.getCurriculumChanges().isEmpty();
	}
	
	private boolean hasCourseRights(MemberPermissionChangeEvent e) {
		return e != null && ((e.getRepoOwner() != null && e.getRepoOwner().booleanValue())
				|| (e.getRepoParticipant() != null && e.getRepoParticipant().booleanValue())
				|| (e.getRepoTutor() != null && e.getRepoTutor().booleanValue()));
	}
	
	private boolean hasParticipantOrTutorsRightsChanges(MemberPermissionChangeEvent e) {
		if((e.getRepoParticipant() != null && e.getRepoParticipant().booleanValue())
				|| (e.getRepoTutor() != null && e.getRepoTutor().booleanValue())){
			return true;
		}
		
		List<BusinessGroupMembershipChange> groupChanges = e.getGroupChanges();
		for(BusinessGroupMembershipChange change:groupChanges) {
			if(change.getParticipant() != null && change.getParticipant().booleanValue()) {
				return true;
			}
			if(change.getTutor() != null && change.getTutor().booleanValue()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("template", mailTemplateForm.getInitialFormItem());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= mailTemplateForm.validateFormLogic(ureq);
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mailTemplateForm.sendMailSwitchEnabled()) {
			if(!mailTemplateForm.isDefaultTemplate()) {
				mailTemplateForm.updateTemplateFromForm(mailTemplate);
			}
			addToRunContext("mailTemplate", mailTemplate);
		} else {
			addToRunContext("mailTemplate", null);
		}
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}