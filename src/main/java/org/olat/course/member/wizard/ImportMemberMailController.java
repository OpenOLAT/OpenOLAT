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

import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailTemplateForm;
import org.olat.course.member.MemberPermissionChangeEvent;
import org.olat.group.BusinessGroupModule;
import org.olat.group.model.BusinessGroupMembershipChange;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberMailController extends StepFormBasicController {
	
	private final MailTemplate mailTemplate;
	private final MailTemplateForm mailTemplateForm;

	public ImportMemberMailController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "mail_template");
		
		boolean mandatoryEmail = CoreSpringFactory.getImpl(BusinessGroupModule.class).isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		if(mandatoryEmail) {
			MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)runContext.get("permissions");
			boolean includeParticipantsRights = hasParticipantRightsChanges(e);
			if(!includeParticipantsRights) {
				mandatoryEmail = false;//only mandatory for participants
			}
		}
		
		mailTemplate = new TestMailTemplate();
		mailTemplateForm = new MailTemplateForm(ureq, wControl, mailTemplate, mandatoryEmail, rootForm);
		
		initForm (ureq);
	}
	
	private boolean hasParticipantRightsChanges(MemberPermissionChangeEvent e) {
		if(e.getRepoParticipant() != null && e.getRepoParticipant().booleanValue()) {
			return true;
		}
		
		List<BusinessGroupMembershipChange> groupChanges = e.getGroupChanges();
		for(BusinessGroupMembershipChange change:groupChanges) {
			if(change.getParticipant() != null && change.getParticipant().booleanValue()) {
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
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk =  mailTemplateForm.validateFormLogic(ureq);
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mailTemplateForm.sendMailSwitchEnabled()) {
			mailTemplateForm.updateTemplateFromForm(mailTemplate);
			addToRunContext("mailTemplate", mailTemplate);
		} else {
			addToRunContext("mailTemplate", null);
		}
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private static class TestMailTemplate extends MailTemplate {
		public TestMailTemplate() {
			super("", "", null);
		}
		
		
		@Override
		public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
			//
		}
	}
}