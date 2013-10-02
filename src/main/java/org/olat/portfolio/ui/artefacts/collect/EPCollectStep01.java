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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.portfolio.ui.artefacts.collect;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * Step which collects the tags. Presents a list of the 50 most used tags
 * 
 * <P>
 * Initial Date:  27.07.2010 <br>
 * @author rhaag
 */
public class EPCollectStep01 extends BasicStep {

	
	private AbstractArtefact artefact;

	public EPCollectStep01(UserRequest ureq, AbstractArtefact artefact) {
		super(ureq);
		this.artefact = artefact;
		setI18nTitleAndDescr("step1.description", "step1.short.descr");
		PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
		//signature > 0 means, collection wizard can be sure its from OLAT, < 0 means get an approval by user (the target value is the negative one)
		if (!portfolioModule.isCopyrightStepEnabled() && !portfolioModule.isReflexionStepEnabled()){
			// skip copyright AND reflexion step
			setNextStep(new EPCollectStep04(ureq));
		} else if (artefact.getSignature() > 0 || !portfolioModule.isCopyrightStepEnabled()){
			setNextStep(new EPCollectStep03(ureq, artefact));
		} else if (portfolioModule.isCopyrightStepEnabled() ){
			setNextStep(new EPCollectStep02(ureq, artefact));
		} 
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getInitialPrevNextFinishConfig()
	 */
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getStepController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.gui.control.generic.wizard.StepsRunContext, org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new EPCollectStepForm01(ureq, windowControl, form, stepsRunContext, artefact);
		return stepI;
	}
}
