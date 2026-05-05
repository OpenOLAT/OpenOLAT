/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.model.Application;

/**
 * 
 * Initial date: 10 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectStepController extends StepFormBasicController {
	
	private final ProjectController projectController;

	public ProjectStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		projectController = new ProjectController(ureq, wControl, rootForm, app, null, false, false, true);
		listenTo(projectController);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	@Override
	public FormItem getStepFormItem() {
		return projectController.getInitialFormItem();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return projectController.validateFormLogic(ureq);
	}

	@Override
	public void back() {
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		projectController.commitChanges(app);
		super.back();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		projectController.commitChanges(app);
		logAudit("Apply background: " + app.toString(), null);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
