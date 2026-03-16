/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.model.mail.InvitationVariables;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationEmail_2_OverviewStep extends BasicStep {
	
	private final InvitationVariables emailVar;
	
	public InvitationEmail_2_OverviewStep(UserRequest ureq, InvitationVariables emailVar) {
		super(ureq);
		this.emailVar = emailVar;
		setI18nTitleAndDescr("wizard.invitation.overview.title", "wizard.mail.invitation.title");
		setNextStep(new InvitationEmail_3_TemplateStep(ureq, emailVar));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new ReviewInvitationEmailController(ureq, wControl, emailVar, runContext, form);
	}
}
