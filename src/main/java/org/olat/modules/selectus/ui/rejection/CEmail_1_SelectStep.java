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
public class CEmail_1_SelectStep extends BasicStep {
	
	private final boolean first;
	private final EmailVariables emailVar;
	private final RecruitingPositionSecurityCallback secCallback;
	
	public CEmail_1_SelectStep(UserRequest ureq, EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback, boolean first) {
		super(ureq);
		this.first = first;
		this.emailVar = emailVar;
		this.secCallback = secCallback;
		setI18nTitleAndDescr("wizard.mail.select.title", "wizard.mail.select.title");
		setNextStep(new CEmail_2_OverviewStep(ureq, emailVar, secCallback));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(!first, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new SelectForEmailController(ureq, wControl, runContext, form, emailVar, secCallback);
	}
}
