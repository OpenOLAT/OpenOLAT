/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.mail.EmailVariables;

/**
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CEmail_3_TemplateStep extends BasicStep {
	
	private final EmailVariables emailVar;
	
	public CEmail_3_TemplateStep(UserRequest ureq, EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback) {
		super(ureq);
		this.emailVar = emailVar;
		setI18nTitleAndDescr("wizard.mail.template.title", "wizard.mail.template.title");
		setNextStep(new CEmail_4_StatusStep(ureq, emailVar, secCallback));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new TemplateForEmailController(ureq, wControl, runContext, form, emailVar);
	}
}
