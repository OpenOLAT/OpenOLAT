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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorEditController extends FormBasicController {
	
	private GeneratorConfigController configCtrl;
	private ProviderConfigController providerConfigCtrl;

	private TooledStackedPanel stackPanel;
	
	private GeneratorSecurityCallback secCallback;
	private QualityGenerator generator;
	
	@Autowired
	private QualityGeneratorService generatorService;
	
	public GeneratorEditController(UserRequest ureq, WindowControl wControl, GeneratorSecurityCallback secCallback2,
			TooledStackedPanel stackPanel, QualityGenerator generator, boolean validate) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.secCallback = secCallback2;
		this.stackPanel = stackPanel;
		this.generator = generator;
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

	public void onChanged(QualityGenerator generator, GeneratorSecurityCallback secCallback) {
		this.generator = generator;
		this.secCallback = secCallback;
		configCtrl.onChanged(generator, secCallback);
		updateUI();
	}
	
	private void updateUI() {
		providerConfigCtrl.setReadOnly(!secCallback.canEditGenerator());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof GeneratorEvent) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
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

}
