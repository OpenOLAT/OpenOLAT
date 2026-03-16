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
public class Contact2OverviewStep extends BasicStep {

	private final boolean first;
	private final ContactMembersContext feedbacksContext;
	
	public Contact2OverviewStep(UserRequest ureq, ContactMembersContext feedbacksContext, boolean first) {
		super(ureq);
		this.first = first;
		this.feedbacksContext = feedbacksContext;
		setI18nTitleAndDescr("wizard.overview.title", "wizard.overview.title");
		setNextStep(new Contact3MailTemplateStep(ureq, feedbacksContext));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(!first, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new OverviewMembersToContactController(ureq, wControl, runContext, form, feedbacksContext);
	}
}
