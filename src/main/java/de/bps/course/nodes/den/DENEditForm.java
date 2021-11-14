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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.ENCourseNode;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.DENCourseNode;

public class DENEditForm extends FormBasicController {
	
	private ModuleConfiguration moduleConfig;
	
	private MultipleSelectionElement enableCancelEnroll;
	private FormSubmit subm;
	
	/**
	 * Constructor of date enrollment creation and edit gui
	 * @param ureq
	 * @param wControl
	 * @param moduleConfig
	 */
	public DENEditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig) {
		super(ureq, wControl);
		this.moduleConfig = moduleConfig;
		
		initForm(this.flc, this, ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Boolean cancelEnrollEnabled = enableCancelEnroll.getSelectedKeys().size() == 1;
		moduleConfig.set(DENCourseNode.CONF_CANCEL_ENROLL_ENABLED, cancelEnrollEnabled);

		// Inform all listeners about the changed condition
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {

		Boolean initialCancelEnrollEnabled  = (Boolean) moduleConfig.get(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED);
		
		enableCancelEnroll = uifactory.addCheckboxesHorizontal("enableCancelEnroll", "form.enableCancelEnroll", formLayout, new String[] { "ison" }, new String[] { "" });
		enableCancelEnroll.select("ison", initialCancelEnrollEnabled);
		
		subm = new FormSubmit("subm", "submit");
		
		formLayout.add(subm);
	}
	
	/**
	 * @return ModuleConfiguration
	 */
	public ModuleConfiguration getModuleConfiguration() {
		return moduleConfig;
	}
	
}
