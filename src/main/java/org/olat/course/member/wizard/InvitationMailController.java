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
import org.olat.group.manager.BusinessGroupMailing;
import org.olat.group.manager.BusinessGroupMailing.MailType;
import org.olat.group.ui.wizard.BGMailTemplateController;
import org.olat.repository.RepositoryMailing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationMailController extends StepFormBasicController {
	
	private InvitationContext context;
	
	private MailTemplate mailTemplate;
	private BGMailTemplateController mailTemplateForm;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public InvitationMailController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, InvitationContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "mail_template");
		this.context = context;
		
		if(context.getRepoEntry() != null) {
			mailTemplate = RepositoryMailing.getInvitationTemplate(context.getRepoEntry(), getIdentity());
		} else if(context.getBusinessGroup() != null) {
			mailTemplate = BusinessGroupMailing.getDefaultTemplate(MailType.invitation, context.getBusinessGroup(), getIdentity());
		}
		
		boolean mandatoryEmail = context.hasInviteeOnly()
				|| businessGroupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		mailTemplateForm = new BGMailTemplateController(ureq, wControl, mailTemplate, false, true, false, mandatoryEmail, rootForm);
		
		initForm(ureq);
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
			context.setMailTemplate(mailTemplate);
		} else {
			context.setMailTemplate(null);
		}
		fireEvent (ureq, StepsEvent.INFORM_FINISHED);
	}
}
