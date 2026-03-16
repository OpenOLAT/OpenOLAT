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
public class InvitationEmail_3_TemplateStep extends BasicStep {
	
	private final InvitationVariables emailVar;
	
	public InvitationEmail_3_TemplateStep(UserRequest ureq, InvitationVariables emailVar) {
		super(ureq);
		this.emailVar = emailVar;
		setI18nTitleAndDescr("wizard.invitation.template.title", "wizard.invitation.template.title");
		setNextStep(new InvitationEmail_4_PreviewStep(ureq, emailVar));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new TemplateForInvitationEmailController(ureq, wControl, runContext, form, emailVar);
	}
}
