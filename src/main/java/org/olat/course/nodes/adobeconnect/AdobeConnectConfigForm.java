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
package org.olat.course.nodes.adobeconnect;

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
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 3  juil. 2019<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectConfigForm extends FormBasicController {
	
	private static final String[] accessKeys = new String[] { "dates", "open", "start" };
	
	private MultipleSelectionElement accessEl;
	
	private final ModuleConfiguration config;
	
	@Autowired
	private AdobeConnectModule adobeConnectModule;

	public AdobeConnectConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] accessValues = new String[] { translate("vc.access.dates"), translate("vc.access.open"), translate("vc.access.start") };
		boolean onlyDates = config.getBooleanSafe(AdobeConnectEditController.ACCESS_BY_DATES, false);
		boolean guestAccess = config.getBooleanSafe(AdobeConnectEditController.GUEST_ACCESS_ALLOWED, false);
		boolean moderatorStart;
		if(adobeConnectModule.isCreateMeetingImmediately()) {
			moderatorStart = config.getBooleanSafe(AdobeConnectEditController.MODERATOR_START_MEETING, false);
		} else {
			moderatorStart = config.getBooleanSafe(AdobeConnectEditController.MODERATOR_START_MEETING, true);
		}
		accessEl = uifactory.addCheckboxesVertical("vc.access.label", "vc.access.label", formLayout, accessKeys, accessValues, 1);
		accessEl.select(accessKeys[0], onlyDates);
		accessEl.select(accessKeys[1], !guestAccess);
		accessEl.select(accessKeys[2], moderatorStart);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selectedKeys = accessEl.getSelectedKeys();
		config.setBooleanEntry(AdobeConnectEditController.ACCESS_BY_DATES, selectedKeys.contains(accessKeys[0]));
		config.setBooleanEntry(AdobeConnectEditController.GUEST_ACCESS_ALLOWED, !selectedKeys.contains(accessKeys[1]));
		config.setBooleanEntry(AdobeConnectEditController.MODERATOR_START_MEETING, selectedKeys.contains(accessKeys[2]));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
}
