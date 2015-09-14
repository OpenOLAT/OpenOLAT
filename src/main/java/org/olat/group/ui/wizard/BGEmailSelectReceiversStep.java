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
package org.olat.group.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGEmailSelectReceiversStep extends BasicStep {
	
	
	public BGEmailSelectReceiversStep(UserRequest ureq, List<BusinessGroup> groups) {
		super(ureq);
		
		setI18nTitleAndDescr("email.select.receiver.title", "email.select.receiver.description");

		setNextStep(new BGEmailCompositionStep(ureq, groups));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		form.setMultipartEnabled(true);
		BGEmailSelectReceiversStepController selectForm
			= new BGEmailSelectReceiversStepController(ureq, windowControl, form, stepsRunContext);
		return selectForm;
	}
}
