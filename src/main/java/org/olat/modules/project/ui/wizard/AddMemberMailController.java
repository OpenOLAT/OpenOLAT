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
package org.olat.modules.project.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.mail.MailTemplate;
import org.olat.group.ui.wizard.BGMailTemplateController;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.manager.ProjectMailing;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 12 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class AddMemberMailController extends StepFormBasicController {
	
	private MailTemplate mailTemplate;
	private final BGMailTemplateController mailTemplateCtrl;
	
	@Autowired
	private ProjectMailing projectMailing;

	public AddMemberMailController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		
		ProjProject project = (ProjProject)runContext.get("project");
		ProjectBCFactory bcFactory = (ProjectBCFactory)runContext.get("bcFactory");
		
		mailTemplate = projectMailing.createMemberAddTemplate(getIdentity(), project, bcFactory);
		mailTemplateCtrl = new BGMailTemplateController(ureq, wControl, mailTemplate, false, true, false, false, rootForm);
		
		initForm (ureq);
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
			addToRunContext("memberAddTemplate", mailTemplate);
		} else {
			addToRunContext("memberAddTemplate", null);
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}