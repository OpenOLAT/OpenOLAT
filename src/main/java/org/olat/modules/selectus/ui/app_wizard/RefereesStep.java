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
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RefereesStep extends BasicStep {

	private final Position preselectedPosition;
	private final RecruitingModule recruitingModule;
	
	public RefereesStep(UserRequest ureq, Position preselectedPosition) {
		super(ureq);
		this.preselectedPosition = preselectedPosition;

		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		
		setI18nTitleAndDescr("wizard.referees.title", "wizard.referees.description");
		setNextStep(new ReviewAndSubmitStep(ureq));
	}
	
	protected boolean isEnabled() {
		return preselectedPosition != null
				&& recruitingModule.isReferenceEnabled()
				&& (
						preselectedPosition.isRefereeRecommendationEnabled() && (preselectedPosition.getMaxReferees() == null || preselectedPosition.getMaxReferees().longValue() > 0)
						||
						(recruitingModule.isReferenceConsentEnabled() && preselectedPosition.isExpertRecommendationEnabled())
						||
						(recruitingModule.isReferenceExpertsBlackListEnabled() && preselectedPosition.isExpertRecommendationEnabled())
					);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form) {
		return new RefereesStepController(ureq, wControl, runContext, form);
	}
}
