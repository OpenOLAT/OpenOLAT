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
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.ApplyToApplicationMainController.ApplyStep;

/**
 * 
 * Initial date: 24 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class HiddenInstructionsStep extends BasicStep implements ApplyStep {
	
	private Position preselectedPosition;
	private final Application application;
	private StepsRunContext currentRunContext;
	
	private InstructionsStepController stepController;

	public HiddenInstructionsStep(UserRequest ureq, Position preselectedPosition, Application app) {
		super(ureq);
		this.application = app;
		this.preselectedPosition = preselectedPosition;
		
		setI18nTitleAndDescr("wizard.instructions.title", "wizard.instructions.description");

		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		if(recruitingModule.isDataProtectionEnabled()) {
			setNextStep(new DataProtectionStep(ureq, preselectedPosition));
		} else {
			setNextStep(new EditPersonStep(ureq, preselectedPosition));
		}
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
		return new PrevNextFinishConfig(true, true, false);
	}

	public InstructionsStepController getStepController() {
		return stepController;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		form.setMultipartEnabled(true);
		this.currentRunContext = runContext;
		if(stepController == null) {
			runContext.put(WizardConstants.APPLICATION, application);
			runContext.put(WizardConstants.PRESELECTED_POSITION, preselectedPosition);
			stepController = new InstructionsStepController(ureq, wControl, runContext, true, form);
		}
		return stepController;
	}
}
