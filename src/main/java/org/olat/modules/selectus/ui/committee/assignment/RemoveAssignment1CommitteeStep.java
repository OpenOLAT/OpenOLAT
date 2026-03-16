/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.assignment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 28 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemoveAssignment1CommitteeStep extends BasicStep {
	
	private final AssignmentsData data;
	
	public RemoveAssignment1CommitteeStep(UserRequest ureq, AssignmentsData data) {
		super(ureq);
		this.data = data;
		
		setI18nTitleAndDescr("wizard.committee.title", "wizard.committee.title");
		setNextStep(new RemoveAssignment2NotificationStep(ureq, data));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext context, Form form) {
		return new CommitteeListController(ureq, wControl, context, form, data, true);
	}

}
