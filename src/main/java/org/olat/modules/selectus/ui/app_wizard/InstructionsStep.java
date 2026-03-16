/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
 * Description:<br>
 * This step schow some informations about the following steps.
 * 
 * <P>
 * Initial Date:  29 aug. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InstructionsStep extends BasicStep {
	
	private InstructionsStepController stepController;

	public InstructionsStep(UserRequest ureq, Position preselectedPosition) {
		super(ureq);
		
		setI18nTitleAndDescr("wizard.instructions.title", "wizard.instructions.description");

		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		if(recruitingModule.isDataProtectionEnabled()) {
			setNextStep(new DataProtectionStep(ureq, preselectedPosition));
		} else {
			setNextStep(new EditPersonStep(ureq, preselectedPosition));
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	public InstructionsStepController getStepController() {
		return stepController;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		stepController = new InstructionsStepController(ureq, wControl, runContext, false, form);
		return stepController;
	}
}
