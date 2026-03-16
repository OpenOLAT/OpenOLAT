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
public class Contact3MailTemplateStep extends BasicStep {

	private final ContactMembersContext feedbacksContext;
	
	/**
	 * 
	 * @param ureq The user request
	 * @param feedbacksContext The context with its data
	 * @param first true if this step is the first one
	 */
	public Contact3MailTemplateStep(UserRequest ureq, ContactMembersContext feedbacksContext) {
		super(ureq);
		this.feedbacksContext = feedbacksContext;
		setI18nTitleAndDescr("wizard.template.title", "wizard.template.description");
		setNextStep(new Contact4PreviewStep(ureq, feedbacksContext));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new TemplateForContactEmailController(ureq, wControl, feedbacksContext, runContext, form);
	}
}
