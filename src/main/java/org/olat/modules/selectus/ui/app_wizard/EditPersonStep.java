/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.model.Position;

/**
 * Description:<br>
 * This step choose or select the position.
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EditPersonStep extends BasicStep {

	public EditPersonStep(UserRequest ureq, Position preselectedPosition) {
		super(ureq);
		
		setI18nTitleAndDescr("wizard.edit_person.title", "wizard.edit_person.description");

		AcademicalBackgroundStep nextStep = new AcademicalBackgroundStep(ureq, preselectedPosition);
		if(nextStep.isEnabled()) {
			setNextStep(nextStep);
		} else {
			setNextStep(nextStep.nextStep());
		}
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		form.setMultipartEnabled(true);
		return new EditPersonStepController(ureq, wControl, runContext, form);
	}
}
