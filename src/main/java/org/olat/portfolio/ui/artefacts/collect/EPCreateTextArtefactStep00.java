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
import org.olat.portfolio.model.structel.PortfolioStructure;

/**
 * Initial Date: 01.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCreateTextArtefactStep00 extends BasicStep {

	private final AbstractArtefact artefact;
	private final VFSContainer vfsTemp;
	private final PortfolioStructure preSelectedStruct;

	public EPCreateTextArtefactStep00(UserRequest ureq, AbstractArtefact artefact, PortfolioStructure preSelectedStruct,
			VFSContainer vfsTemp) {
		super(ureq);
		this.vfsTemp = vfsTemp;
		this.artefact = artefact;
		this.preSelectedStruct = preSelectedStruct;
		setI18nTitleAndDescr("step0.text.description", "step0.text.short.descr");
		setNextStep(new EPCollectStep00(ureq, artefact));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		if(preSelectedStruct != null) {
			stepsRunContext.put("preSelectedStructure", preSelectedStruct);
		}
		
		return new EPCreateTextArtefactStepForm00(ureq, windowControl, form, stepsRunContext,
				FormBasicController.LAYOUT_DEFAULT, null, artefact, vfsTemp);
	}
}
