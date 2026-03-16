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

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.ApplyToApplicationMainController.ApplyStep;

/**
 * Description:<br>
 * This step choose or select the position.
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AddApplicationInitialStep extends BasicStep implements ApplyStep {
	
	private boolean canChangePreselection = true;
	private Position preselectedPosition;
	private final Application application;
	private StepsRunContext currentRunContext;
	private ChoosePositionStepController stepController;

	public AddApplicationInitialStep(UserRequest ureq, Position preselectedPosition, Application app) {
		super(ureq);
		this.application = app;
		this.preselectedPosition = preselectedPosition;
		
		setI18nTitleAndDescr("wizard.choose_position.title", "wizard.choose_position.description");
		setNextStep(new InstructionsStep(ureq, preselectedPosition));
	}

	@Override
	public Application getApplication() {
		return currentRunContext != null ? (Application)currentRunContext.get(WizardConstants.APPLICATION) : application;
	}

	@Override
	public Position getPreselectedPosition() {
		return preselectedPosition;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	public ChoosePositionStepController getStepController() {
		return stepController;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		form.setMultipartEnabled(true);
		this.currentRunContext = runContext;
		
		if(stepController == null) {
			runContext.put(WizardConstants.APPLICATION, application);
			runContext.put(WizardConstants.PRESELECTED_POSITION, preselectedPosition);
			
			stepController = new ChoosePositionStepController(ureq, wControl, runContext, form);
			if(preselectedPosition != null) {
				stepController.selectPosition(preselectedPosition, canChangePreselection);
			}
		}
		return stepController;
	}
}
