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
package org.olat.admin.user.imp;

import org.olat.admin.user.groups.GroupSearchController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.group.BusinessGroupModule;

/**
 * 
 * Description:<br>
 * have another step to define groups where new identities should be added to.
 * 
 * <P>
 * Initial Date:  09.05.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class ImportStep02 extends BasicStep {
	
	private final boolean mandatoryEmail;

	public ImportStep02(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("step2.description", "step2.short.description");

		mandatoryEmail = CoreSpringFactory.getImpl(BusinessGroupModule.class).isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		if(mandatoryEmail) {
			setNextStep(Step.NOSTEP);
		} else {
			setNextStep(new ImportStep03SendMail(ureq));
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getInitialPrevNextFinishConfig()
	 */
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		boolean next = !mandatoryEmail;
		boolean finish = mandatoryEmail;
		return new PrevNextFinishConfig(true, next, finish);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getStepController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.gui.control.generic.wizard.StepsRunContext, org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new GroupSearchController(ureq, windowControl, form, stepsRunContext, mandatoryEmail);
		return stepI;
	}
}