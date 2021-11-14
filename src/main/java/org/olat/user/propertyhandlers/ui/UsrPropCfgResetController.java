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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This controller provides the reset-gui which allows the user to reset the
 * UserProperty-Configuration to several PreSets defined in "_static"
 * 
 * <P>
 * Initial Date: 26.08.2011 <br>
 * 
 * @author strentini
 */
public class UsrPropCfgResetController extends FormBasicController {

	private static final String PRESET_KEY_OLATDEF = "upc.reset.config.olatdefault";
	private static final String PRESET_KEY_MINIMAL = "upc.reset.config.minimal";
	private static final String PRESET_KEY_SCHOOL = "upc.reset.config.school";
	private static final String PRESET_KEY_BUSINESS = "upc.reset.config.business";

	private SingleSelection resetDropdown;

	@Autowired
	private UsrPropCfgManager usrPropCfgMng;

	public UsrPropCfgResetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] values = {
				translate(PRESET_KEY_OLATDEF), translate(PRESET_KEY_MINIMAL),
				translate(PRESET_KEY_SCHOOL), translate(PRESET_KEY_BUSINESS)
		};
		String[] keys = { PRESET_KEY_OLATDEF, PRESET_KEY_MINIMAL, PRESET_KEY_SCHOOL, PRESET_KEY_BUSINESS };

		resetDropdown = uifactory.addDropdownSingleselect("upc.reset.configs", formLayout, keys, values, null);
		uifactory.addStaticTextElement("reset.note", null, translate("upc.reset.configs.note"), formLayout);
		uifactory.addFormSubmitButton("ok", formLayout);
	}

	/**
	 * form is ok<br/>
	 * load the selected preset.properties file and reset userProperties.<br />
	 * show dialog on error
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		String propertyFileName = "_static/" + resetDropdown.getSelectedKey() + ".properties";
		try(InputStream is = this.getClass().getResourceAsStream(propertyFileName)) {
			Properties p = new Properties();
			p.load(is);
			usrPropCfgMng.resetToPresetConfig(p);
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (IOException e) {
			DialogBoxController dBox = null;
			List<String> buttonLabels = Collections.singletonList(translate("ok"));
			activateGenericDialog(ureq, "ERROR", "Sorry, Preset-File was not found: " + propertyFileName, buttonLabels, dBox);
		}  catch(Exception ex){
			DialogBoxController dBox = null;
			List<String> buttonLabels = Collections.singletonList(translate("ok"));
			StringBuilder sbError = new StringBuilder();
			sbError.append("Sorry, there was an error while loading the preset file: ");
			sbError.append(propertyFileName);
			sbError.append("<br /><br />");
			sbError.append(ex.getClass().getSimpleName()).append("      ").append(ex.getMessage());
			activateGenericDialog(ureq, "ERROR",sbError.toString(), buttonLabels, dBox);
		}
	}
}
