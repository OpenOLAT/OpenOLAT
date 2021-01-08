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
package org.olat.admin.securitygroup.gui.multi;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMemberOverviewIdentitiesController;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UsersToGroupWizardStep01 extends BasicStep {
	
	private final boolean mandatoryEmail;
	private final MailTemplate mailDefaultTemplate;

	public UsersToGroupWizardStep01(UserRequest ureq, MailTemplate mailDefaultTemplate, boolean mandatoryEmail) {
		super(ureq);
		this.mandatoryEmail = mandatoryEmail;
		this.mailDefaultTemplate = mailDefaultTemplate;
		
		setI18nTitleAndDescr("import.title.select", null);
		
		if(mailDefaultTemplate == null) {
			setNextStep(Step.NOSTEP);
		} else {
			setNextStep(new UsersToGroupWizardStep02(ureq));
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		if(mailDefaultTemplate == null) {
			return new PrevNextFinishConfig(true, false, true);
		} else {
			return new PrevNextFinishConfig(true, true, !mandatoryEmail);
		}
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new ImportMemberOverviewIdentitiesController(ureq, wControl, form, stepsRunContext, ImportMemberByUsernamesController.RUN_CONTEXT_KEY, null);
	}
}
