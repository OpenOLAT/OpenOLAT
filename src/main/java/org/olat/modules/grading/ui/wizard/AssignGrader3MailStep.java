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
package org.olat.modules.grading.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailTemplate;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AssignGrader3MailStep extends BasicStep {

	private final MailTemplate mailTemplate;
	private final AssignGrader assignGrader;
	
	public AssignGrader3MailStep(UserRequest ureq, AssignGrader assignGrader, MailTemplate mailTemplate) {
		super(ureq);
		this.mailTemplate = mailTemplate;
		this.assignGrader = assignGrader;
		setNextStep(NOSTEP);
		setI18nTitleAndDescr("import.mail.title", "import.mail.title");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		ContactList contacts = new ContactList(getTranslator().translate("assign.grader.contact"));
		contacts.add(assignGrader.getGrader());
		return new ImportGraderMailController(ureq, wControl, mailTemplate, contacts, form, runContext);
	}
}