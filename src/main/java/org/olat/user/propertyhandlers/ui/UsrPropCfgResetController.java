/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.user.propertyhandlers.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This controller provides the reset-gui which allows the user to reset the
 * UserProperty-Configuration to one of two presets computed by the UsrPropCfgManager
 * 
 * <P>
 * Initial Date: 26.08.2011 <br>
 * 
 * @author strentini
 */
public class UsrPropCfgResetController extends FormBasicController {

	private static final String PRESET_KEY_DEFAULT = "upc.reset.config.default";
	private static final String PRESET_KEY_MINIMAL = "upc.reset.config.minimal";

	private SingleSelection resetDropdown;

	@Autowired
	private UsrPropCfgManager usrPropCfgMng;

	public UsrPropCfgResetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] values = { translate(PRESET_KEY_DEFAULT), translate(PRESET_KEY_MINIMAL) };
		String[] keys = { PRESET_KEY_DEFAULT, PRESET_KEY_MINIMAL };

		resetDropdown = uifactory.addDropdownSingleselect("upc.reset.configs", formLayout, keys, values, null);
		uifactory.addStaticTextElement("reset.note", null, translate("upc.reset.configs.note"), formLayout);
		uifactory.addFormSubmitButton("ok", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (PRESET_KEY_MINIMAL.equals(resetDropdown.getSelectedKey())) {
			usrPropCfgMng.resetToMinimal(getIdentity());
		} else {
			usrPropCfgMng.resetToSystemDefault(getIdentity());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
