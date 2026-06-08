/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AcademicalBackgroundStep extends BasicStep {

	private final Position preselectedPosition;
	private final RecruitingModule recruitingModule;
	
	public AcademicalBackgroundStep(UserRequest ureq, Position preselectedPosition) {
		super(ureq);
		setI18nTitleAndDescr("wizard.edit_background.title", "wizard.edit_background.description");
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		this.preselectedPosition = preselectedPosition;
		
		
		
		ProjectStep projectStep = new ProjectStep(ureq, preselectedPosition);
		if(projectStep.isEnabled()) {
			setNextStep(projectStep);
		} else {
			setNextStep(projectStep.nextStep());
		}
	}
	
	protected boolean isEnabled() {
		return recruitingModule.isApplicationAcademicalBackgroundEnabled(preselectedPosition);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		form.setMultipartEnabled(true);
		return new AcademicalBackgroundStepController(ureq, wControl, runContext, form);
	}
}
