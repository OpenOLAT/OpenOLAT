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
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * <h3>Description:</h3>
 * <p>
 * Metadata step of the library publish process.
 * <p>
 * Initial Date:  Sep 24, 2009 <br>
 * @author twuersch, timo.wuersch@frentix.com, www.frentix.com
 */
public class MetadataAcceptStep extends BasicStep {
	private final String filename;

	public MetadataAcceptStep(UserRequest ureq, String filename) {
		super(ureq);
		this.filename = filename;
		setI18nTitleAndDescr("acceptstep.metadata.title", "acceptstep.metadata.description");
		setNextStep(new DestinationAcceptStep(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		stepsRunContext.put(MetadataAcceptStepController.STEPS_RUN_CONTEXT_FILENAME_KEY, filename);
		
		return new MetadataAcceptStepController(ureq, windowControl, stepsRunContext, form);
	}
}
