/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.cns.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSConfigsController extends FormBasicController {

	private TextElement requiredSelectionsEl;

	private final ModuleConfiguration moduleConfig;

	public CNSConfigsController(UserRequest ureq, WindowControl wControl, CNSCourseNode courseNode) {
		super(ureq, wControl);
		moduleConfig = courseNode.getModuleConfiguration();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.config");
		
		requiredSelectionsEl = uifactory.addTextElement("config.required.selections", 10,
				moduleConfig.getStringValue(CNSCourseNode.CONFIG_KEY_REQUIRED_SELECTIONS), formLayout);
		requiredSelectionsEl.setMandatory(true);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", "save", buttonCont);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		requiredSelectionsEl.clearError();
		if (StringHelper.containsNonWhitespace(requiredSelectionsEl.getValue())) {
			try {
				int meetingDeletionDays = Integer.parseInt(requiredSelectionsEl.getValue());
				if (meetingDeletionDays < 0) {
					requiredSelectionsEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				requiredSelectionsEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		} else {
			requiredSelectionsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		moduleConfig.setStringValue(CNSCourseNode.CONFIG_KEY_REQUIRED_SELECTIONS, requiredSelectionsEl.getValue());
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

}
