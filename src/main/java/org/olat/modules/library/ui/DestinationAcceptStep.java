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
package org.olat.modules.library.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * <h3>Description:</h3>
 * <p>
 * Destination selection step of the library publish process.
 * <p>
 * Initial Date:  Sep 24, 2009 <br>
 * @author twuersch, timo.wuersch@frentix.com, www.frentix.com
 */
public class DestinationAcceptStep extends BasicStep {

	private final boolean isApprovalEnabled;

	public DestinationAcceptStep(UserRequest ureq) {
		super(ureq);
		// default value should be true, because if this constructor is used that means that approval is needed from review
		this.isApprovalEnabled = true;
		setI18nTitleAndDescr("acceptstep.destination.title", "acceptstep.destination.description");
		setNextStep(new NotificationAcceptStep(ureq));
	}

	public DestinationAcceptStep(UserRequest ureq, boolean isApprovalEnabled) {
		super(ureq);
		this.isApprovalEnabled = isApprovalEnabled;
		setI18nTitleAndDescr("acceptstep.destination.title", "acceptstep.destination.description");
		if (isApprovalEnabled) {
			setNextStep(new NotificationAcceptStep(ureq));
		} else {
			setNextStep(Step.NOSTEP);
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, isApprovalEnabled, !isApprovalEnabled);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new DestinationAcceptStepController(ureq, windowControl, stepsRunContext, form);
	}
}
