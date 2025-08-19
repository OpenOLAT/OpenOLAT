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
package org.olat.course.nodes.projectbroker;

import org.olat.admin.securitygroup.gui.multi.UsersToGroupWizardStep01;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.member.MemberSearchConfig;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMemberByUsernamesInternalController;

/**
 * 
 * Initial date: 19 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersToGroupWizardStep00 extends BasicStep {

	private final MemberSearchConfig config;

	/**
	 * the email step only appears if you give a mail template
	 * @param ureq
	 * @param mailDefaultTempl
	 * @param mandatoryEmail
	 */
	public MembersToGroupWizardStep00(UserRequest ureq, MailTemplate mailDefaultTemplate, MemberSearchConfig config, boolean mandatoryEmail) {
		super(ureq);
		this.config = config;
		setI18nTitleAndDescr("import.title", null);
		setNextStep(new UsersToGroupWizardStep01(ureq, mailDefaultTemplate, mandatoryEmail));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new ImportMemberByUsernamesInternalController(ureq, wControl, form, stepsRunContext, ImportMemberByUsernamesController.RUN_CONTEXT_KEY, config);
	}
}
