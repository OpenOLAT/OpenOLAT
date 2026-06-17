/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
