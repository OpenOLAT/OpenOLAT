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
package org.olat.user.propertyhandlers;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.user.propertyhandlers.ui.UsrPropHandlerCfgController;

/**
 * 
 * Description:<br>
 * simple form-controller to inform the user about the special config for the
 * UserInterest-Property (which is not done here, but in the xml config)
 * 
 * <P>
 * Initial Date: 01.09.2011 <br>
 * 
 * @author strentini
 */
public class UserInterestsPropertyHandlerController extends FormBasicController implements UsrPropHandlerCfgController {

	public UserInterestsPropertyHandlerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(UserInterestsPropertyHandler.PACKAGE_UINTERESTS, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	public void setHandlerToConfigure(UserPropertyHandler handler) {
		// empty, since we do not actually configure anything...
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("info",null, translate("userinterests.configinfo",new String[]{UserInterestsPropertyHandler.USERINTERESTS_CONFIGURATION_FILE}), formLayout);
		uifactory.addFormSubmitButton("ok", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
