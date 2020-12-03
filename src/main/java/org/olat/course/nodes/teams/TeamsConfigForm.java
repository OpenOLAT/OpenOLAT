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
package org.olat.course.nodes.teams;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsConfigForm extends FormBasicController {
	
	private static final String[] accessKeys = new String[] { "start" };
	
	private MultipleSelectionElement accessEl;
	
	private final ModuleConfiguration config;

	public TeamsConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Course+element+BigBlueButton");
		
		String[] accessValues = new String[] { translate("vc.access.start") };
		
		boolean moderatorStart = config.getBooleanSafe(TeamsEditController.MODERATOR_START_MEETING, true);

		accessEl = uifactory.addCheckboxesVertical("vc.access.label", "vc.access.label", formLayout, accessKeys, accessValues, 1);
		accessEl.select(accessKeys[0], moderatorStart);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selectedKeys = accessEl.getSelectedKeys();
		config.setBooleanEntry(TeamsEditController.MODERATOR_START_MEETING, selectedKeys.contains(accessKeys[0]));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
}
