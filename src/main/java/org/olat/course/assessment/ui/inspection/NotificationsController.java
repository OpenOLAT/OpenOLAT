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
package org.olat.course.assessment.ui.inspection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.MailTemplate;
import org.olat.group.ui.wizard.BGMailTemplateController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 */
public class NotificationsController extends StepFormBasicController {

	private MailTemplate mailTemplate;
	private final CreateInspectionContext context;
	
	private final BGMailTemplateController mailTemplateCtrl;
	
	@Autowired
	private UserManager userManager;
	
	public NotificationsController(UserRequest ureq, WindowControl wControl,
			CreateInspectionContext context, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.context = context;
		
		mailTemplate = getTemplate();
		mailTemplateCtrl = new BGMailTemplateController(ureq, wControl, mailTemplate, false, true, false, false, rootForm);
		
		initForm(ureq);
	}
	
	private MailTemplate getTemplate() {
		Formatter format = Formatter.getInstance(getLocale());
		
		String[] args = new String[] {
				context.getCourseEntry().getDisplayname(),
				context.getCourseNode().getShortTitle(),
				format.formatDateAndTime(context.getStartDate()),
				format.formatDateAndTime(context.getEndDate()),
				BusinessControlFactory.getInstance().getURLFromBusinessPathString("[RepositoryEntry:" + context.getCourseEntry().getKey() + "]"),
				userManager.getUserDisplayName(getIdentity()),
				userManager.getUserDisplayEmail(getIdentity(), getLocale())	
		};

		String subject = translate("inspection.invitation.subject", args);
		String body = translate("inspection.invitation.body", args);

		return new InspectionMailTemplate(subject, body, context);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("template", mailTemplateCtrl.getInitialFormItem());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= mailTemplateCtrl.validateFormLogic(ureq);
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(mailTemplateCtrl.sendMailSwitchEnabled()) {
			if(!mailTemplateCtrl.isMailContentDefault()) {
				mailTemplateCtrl.updateTemplateFromForm(mailTemplate);
			}
			context.setMailTemplate(mailTemplate);
		} else {
			context.setMailTemplate(null);
		}
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
