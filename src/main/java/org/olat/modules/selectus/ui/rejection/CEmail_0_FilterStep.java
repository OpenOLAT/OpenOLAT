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
 * Initial date: 12 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CEmail_0_FilterStep extends BasicStep {
	
	private final EmailVariables emailVar;
	
	public CEmail_0_FilterStep(UserRequest ureq, EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback) {
		super(ureq);
		this.emailVar = emailVar;
		setI18nTitleAndDescr("wizard.mail.filter.title", "wizard.mail.filter.title");
		setNextStep(new CEmail_1_SelectStep(ureq, emailVar, secCallback, false));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new RejectionFilterController(ureq, wControl, runContext, form, emailVar);
	}
}
