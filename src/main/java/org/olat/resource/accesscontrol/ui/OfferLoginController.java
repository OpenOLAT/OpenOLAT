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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.OfferAccess;

/**
 * 
 * Initial date: 22 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OfferLoginController extends AbstractAccessController {

	public OfferLoginController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl, link);
		init(ureq);
	}

	@Override
	protected Controller createDetailsController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		return new LoginButtonController(ureq, wControl);
	}
	
	public static class LoginButtonController extends FormBasicController implements FormController {

		public LoginButtonController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "offer_login_button");
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			uifactory.addFormSubmitButton("login.or.register", formLayout);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, OffersController.LOGIN_EVENT);
		}
	}

}
