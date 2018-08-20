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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorEditController extends FormBasicController implements TooledController {

	private Link enableLink;
	private Link disableLink;
	
	private GeneratorConfigController configCtrl;
	private ProviderConfigController providerConfigCtrl;
	private CloseableModalController cmc;
	private GeneratorEnableConfirmationController enableConfirmationCtrl;
	private GeneratorDisableConfirmationController disableConfirmationCtrl;
	
	private final QualitySecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	private QualityGenerator generator;
	
	@Autowired
	private QualityGeneratorService generatorService;

	public GeneratorEditController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityGenerator generator) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.generator = generator;
		initForm(ureq);
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

	private void updateUI() {
		configCtrl.setGenerator(generator);
		providerConfigCtrl.setReadOnly(generator.isEnabled());
		initTools();
	}
	
	@Override
	public void initTools() {
		stackPanel.removeAllTools();
		
		if (secCallback.canActivateGenerators()) {
			String enabled = generator.isEnabled()? "enabled": "disabled";
			
			Dropdown enableDropdown = new Dropdown("generator.enable.dropdown", "generator." + enabled + ".hover", false, getTranslator());
			enableDropdown.setIconCSS("o_icon o_icon-fw o_icon_qual_gen_" + enabled);
			enableDropdown.setOrientation(DropdownOrientation.normal);
		
			enableLink = LinkFactory.createToolLink("generator.enabled", translate("generator.enabled.hover"), this);
			enableLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_enabled");
			enableLink.setVisible(!generator.isEnabled());
			enableDropdown.addComponent(enableLink);
			
			disableLink = LinkFactory.createToolLink("generator.disabled", translate("generator.disabled.hover"), this);
			disableLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_disabled");
			disableLink.setVisible(generator.isEnabled());
			enableDropdown.addComponent(disableLink);
			
			stackPanel.addTool(enableDropdown, Align.left);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == enableLink) {
			doConfirmEnableGenerator(ureq);;
		} else if (source == disableLink) {
			doConfirmDisableGenerator(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == enableConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				Date fromDate = enableConfirmationCtrl.getFromDate();
				doEnableGenerator(fromDate);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == disableConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				doDisabledGenerator();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(disableConfirmationCtrl);
		removeAsListenerAndDispose(enableConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		disableConfirmationCtrl = null;
		enableConfirmationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Save is implemented in the sub forms.
	}

	private void doConfirmEnableGenerator(UserRequest ureq) {
		if (!validateBeforeActivation(ureq)) return;
		
		enableConfirmationCtrl = new GeneratorEnableConfirmationController(ureq, getWindowControl(), generator);
		listenTo(enableConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				enableConfirmationCtrl.getInitialComponent(), true, translate("generator.enable.confirm.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private boolean validateBeforeActivation(UserRequest ureq) {
		boolean allOk = true;
		allOk &= configCtrl.validateBeforeActivation(ureq);
		allOk &= providerConfigCtrl.validateBeforeActivation(ureq);
		return allOk;
	}

	private void doEnableGenerator(Date fromDate) {
		generator = generatorService.loadGenerator(generator);
		generator.setEnabled(true);
		generator.setLastRun(fromDate);
		generator = generatorService.updateGenerator(generator);
		updateUI();	
	}
	
	private void doConfirmDisableGenerator(UserRequest ureq) {
		disableConfirmationCtrl = new GeneratorDisableConfirmationController(ureq, getWindowControl(), generator);
		listenTo(disableConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				disableConfirmationCtrl.getInitialComponent(), true, translate("generator.disable.confirm.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDisabledGenerator() {
		generator = generatorService.loadGenerator(generator);
		generator.setEnabled(false);
		generator = generatorService.updateGenerator(generator);
		updateUI();
	}

	@Override
	protected void doDispose() {
		//
	}

}
