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
 * This step schow some informations about the following steps.
 * 
 * <P>
 * Initial Date:  29 aug. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DataProtectionStep extends BasicStep {
	
	private DataProtectionStepController stepController;

	public DataProtectionStep(UserRequest ureq, Position preselectedPosition) {
		super(ureq);
		
		setI18nTitleAndDescr("wizard.dataprotection.title", "wizard.dataprotection.description");
		setNextStep(new EditPersonStep(ureq, preselectedPosition));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	public DataProtectionStepController getStepController() {
		return stepController;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		stepController = new DataProtectionStepController(ureq, wControl, runContext, form);
		return stepController;
	}
}
