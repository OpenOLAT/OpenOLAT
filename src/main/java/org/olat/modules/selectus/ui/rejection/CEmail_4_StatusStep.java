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
 * Initial date: 13 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CEmail_4_StatusStep extends BasicStep {
	
	private final EmailVariables emailVar;
	private final RecruitingPositionSecurityCallback secCallback;
	
	public CEmail_4_StatusStep(UserRequest ureq, EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback) {
		super(ureq);
		this.emailVar = emailVar;
		this.secCallback = secCallback;
		setI18nTitleAndDescr("wizard.mail.status.title", "wizard.mail.status.title");
		setNextStep(new CEmail_5_PreviewStep(ureq, emailVar));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new StatusController(ureq, wControl, runContext, form, emailVar, secCallback);
	}

}
