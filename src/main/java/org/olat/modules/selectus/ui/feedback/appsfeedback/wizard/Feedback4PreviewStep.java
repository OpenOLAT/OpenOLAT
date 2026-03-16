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
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Feedback4PreviewStep extends BasicStep {
	
	private final FeedbackMembersContext feedbacksContext;
	
	public Feedback4PreviewStep(UserRequest ureq, FeedbackMembersContext feedbacksContext) {
		super(ureq);
		this.feedbacksContext = feedbacksContext;
		
		setI18nTitleAndDescr("wizard.preview.title", "wizard.preview.description");
		setNextStep(new Feedback5ConfirmationStep(ureq, feedbacksContext));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		if(feedbacksContext.isSendMail()) {
			return new PreviewEmailsController(ureq, wControl, runContext, form, feedbacksContext);
		}
		return new NoPreviewController(ureq, wControl, runContext, form);
	}

}
