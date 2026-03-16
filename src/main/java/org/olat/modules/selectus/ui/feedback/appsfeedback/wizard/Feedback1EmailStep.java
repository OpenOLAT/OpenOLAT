/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 23 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Feedback1EmailStep extends BasicStep {
	
	private final FeedbackMembersContext feedbacksContext;
	
	public Feedback1EmailStep(UserRequest ureq, FeedbackMembersContext feedbacksContext) {
		super(ureq);
		this.feedbacksContext = feedbacksContext;
		setI18nTitleAndDescr("wizard.email.title", "wizard.email.description");
		setNextStep(new Feedback2OverviewStep(ureq, feedbacksContext));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new EmailStepController(ureq, wControl, feedbacksContext, runContext, form);
	}
}
