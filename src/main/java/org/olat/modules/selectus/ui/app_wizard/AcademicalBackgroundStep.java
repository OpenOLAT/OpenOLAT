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
