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
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Initial date: 13 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CustomStepController extends StepFormBasicController {

	private final CustomAttributesController customAttributesCtrl;
	
	public CustomStepController(UserRequest ureq, WindowControl wControl, Tab tab, Position position, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		TabConfiguration tabConfiguration = position.getTabConfiguration(tab);
		customAttributesCtrl = new CustomAttributesController(ureq, wControl, rootForm, app, tabConfiguration, false, false, true);
		listenTo(customAttributesCtrl);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	@Override
	public FormItem getStepFormItem() {
		return customAttributesCtrl.getInitialFormItem();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return customAttributesCtrl.validateFormLogic(ureq);
	}
	
	@Override
	public void back() {
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		customAttributesCtrl.commitChanges(app);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		customAttributesCtrl.commitChanges(app);

		logAudit("Apply custom datas: " + app.toString(), null);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
