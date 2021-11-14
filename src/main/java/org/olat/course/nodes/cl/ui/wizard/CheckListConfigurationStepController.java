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
package org.olat.course.nodes.cl.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.cl.ui.CheckListConfigurationController;
import org.olat.course.nodes.cl.ui.CheckListEditController;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 13.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListConfigurationStepController extends StepFormBasicController {

	private final GeneratorData data;
	private final CheckListConfigurationController configController;
	
	public CheckListConfigurationStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "checklist_config");
		setTranslator(Util.createPackageTranslator(CheckListEditController.class, getLocale(), getTranslator()));

		data = (GeneratorData)getFromRunContext("data");
		NodeAccessType nodeAccessType = (NodeAccessType)getFromRunContext("nodeAccessType");
		ModuleConfiguration config = data.getModuleConfiguration();
		configController = new CheckListConfigurationController(ureq, wControl, config, nodeAccessType, data, rootForm);
		listenTo(configController);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("configuration.template");
		setFormDescription("configuration.template.description");

		formLayout.add("config", configController.getInitialFormItem());
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(configController);
        super.doDispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
