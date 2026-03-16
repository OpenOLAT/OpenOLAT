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
 * Initial date: 6 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Contact1SelectStep extends BasicStep {
	
	private final ContactMembersContext feedbacksContext;
	
	public Contact1SelectStep(UserRequest ureq, ContactMembersContext feedbacksContext) {
		super(ureq);
		this.feedbacksContext = feedbacksContext;
		setI18nTitleAndDescr("wizard.select.title", "wizard.select.title");
		setNextStep(new Contact2OverviewStep(ureq, feedbacksContext, false));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new SelectMembersToContactController(ureq, wControl, runContext, form, feedbacksContext);
	}
}
