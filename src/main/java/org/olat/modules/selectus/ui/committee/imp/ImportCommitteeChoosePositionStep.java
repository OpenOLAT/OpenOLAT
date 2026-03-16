/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.imp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 29 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCommitteeChoosePositionStep extends BasicStep {
	
	private final ImportCommitteeMembers importCommittee;
	
	public ImportCommitteeChoosePositionStep(UserRequest ureq, ImportCommitteeMembers importCommittee) {
		super(ureq);
		this.importCommittee = importCommittee;
		setI18nTitleAndDescr("wizard.choose.position.title", "wizard.choose.position.title");
		setNextStep(new ImportCommitteeStep(ureq, importCommittee));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, false, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form) {
		return new ChoosePositionController(ureq, wControl, runContext, form, importCommittee);
	}
}
