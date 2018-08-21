/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.generator.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.generator.QualityGenerator;

/**
 * 
 * Initial date: 14.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorEditController extends AbstractGeneratorEditController {
	
	private GeneratorConfigController configCtrl;
	private ProviderConfigController providerConfigCtrl;
	
	public GeneratorEditController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityGenerator generator, boolean validate) {
		super(ureq, wControl, secCallback, stackPanel, generator);
		initForm(ureq);
		
		if (validate) {
			validateFormLogic(ureq);
			validateBeforeActivation(ureq);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		configCtrl = new GeneratorConfigController(ureq, getWindowControl(), this.mainForm,
				secCallback, stackPanel, generator);
		listenTo(configCtrl);
		formLayout.add("config", configCtrl.getInitialFormItem());
		
		providerConfigCtrl = generatorService.getConfigController(ureq, getWindowControl(), this.mainForm, generator);
		listenTo(providerConfigCtrl);
		formLayout.add("providerConfig", providerConfigCtrl.getInitialFormItem());
		
		FormLayoutContainer buttonWrapperLayout = FormLayoutContainer.createDefaultFormLayout("buttonWrapper", getTranslator());
		formLayout.add("buttonWrapper", buttonWrapperLayout);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonWrapperLayout.add("button", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateUI();
	}

	@Override
	protected void updateUI() {
		configCtrl.setGenerator(generator);
		providerConfigCtrl.setReadOnly(generator.isEnabled());
	}

	boolean validateBeforeActivation(UserRequest ureq) {
		boolean allOk = true;
		allOk &= configCtrl.validateBeforeActivation(ureq);
		allOk &= providerConfigCtrl.validateBeforeActivation(ureq);
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Save is implemented in the sub forms.
	}

	@Override
	protected void doDispose() {
		//
	}

}
