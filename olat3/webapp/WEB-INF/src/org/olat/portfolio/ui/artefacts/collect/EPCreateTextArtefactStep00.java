/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.portfolio.ui.artefacts.collect;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * TODO: rhaag Class Description for EPCreateTextArtefactStep00
 * 
 * <P>
 * Initial Date: 01.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCreateTextArtefactStep00 extends BasicStep {

	private AbstractArtefact artefact;
	private VFSContainer vfsTemp;

	public EPCreateTextArtefactStep00(UserRequest ureq, AbstractArtefact artefact, VFSContainer vfsTemp) {
		super(ureq);
		this.vfsTemp = vfsTemp;
		this.artefact = artefact;
		setI18nTitleAndDescr("step0.text.description", "step0.text.short.descr");
		setNextStep(new EPCollectStep00(ureq, artefact));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new EPCreateTextArtefactStepForm00(ureq, windowControl, form, stepsRunContext,
				FormBasicController.LAYOUT_DEFAULT, null, artefact, vfsTemp);
		return stepI;
	}

}
