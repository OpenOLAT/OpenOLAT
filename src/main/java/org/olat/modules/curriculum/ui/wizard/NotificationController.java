/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.wizard;

import java.util.List;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailTemplate;
import org.olat.group.ui.wizard.BGMailTemplateController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.member.MembershipModification;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class NotificationController extends StepFormBasicController {

	private MailTemplate mailTemplate;
	private final MembersContext membersContext;
	private final BGMailTemplateController mailTemplateForm;

	public NotificationController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "notifications");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale(), getTranslator()));
		this.membersContext = membersContext;
		
		boolean mandatoryEmail = false;
		mailTemplate = findBestMailTemplate(membersContext);
		mailTemplateForm = new BGMailTemplateController(ureq, wControl, mailTemplate, false, true, false, mandatoryEmail, rootForm);
		
		initForm(ureq);
	}
	
	private MailTemplate findBestMailTemplate(MembersContext context) {
		CurriculumElement curriculumElement = context.getCurriculumElement();
		Curriculum curriculum = curriculumElement.getCurriculum();
		
		GroupMembershipStatus nextStatus = null;
		List<MembershipModification> modifications = membersContext.getModifications();
		if(modifications != null && !modifications.isEmpty()) {
			nextStatus = modifications.get(0).nextStatus();
		}
		
		MailTemplate template = null;
		if(nextStatus == GroupMembershipStatus.active) {
			template = CurriculumMailing.getMembershipAcceptedTemplate(curriculum, curriculumElement, getIdentity());
		} else if(context.getSelectedOffer() != null || nextStatus == GroupMembershipStatus.reservation) {
			template = CurriculumMailing.getMembershipBookedByAdminTemplate(curriculum, curriculumElement, getIdentity());
		} else {
			template = CurriculumMailing.getMembershipAcceptedTemplate(curriculum, curriculumElement, getIdentity());
		}
		return template;
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
			if(!mailTemplateForm.isMailContentDefault()) {
				mailTemplateForm.updateTemplateFromForm(mailTemplate);
			}
			membersContext.setMailTemplate(mailTemplate);
		} else {
			membersContext.setMailTemplate(null);
		}
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
