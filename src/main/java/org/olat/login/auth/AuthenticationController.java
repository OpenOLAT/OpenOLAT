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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.login.auth;

import java.util.Locale;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  04.08.2004
 *
 * @author Mike Stock
 */
public abstract class AuthenticationController extends BasicController {
	
	@Autowired
	protected UserDeletionManager userDeletionManager;
	

	public AuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	public AuthenticationController(UserRequest ureq, WindowControl wControl, Translator translator) {
		super(ureq, wControl, translator);
	}

	/**
	 * Called if the user was successfully authenticated.
	 * @param ureq
	 * @param identity
	 */
	public void authenticated(UserRequest ureq, Identity identity) {
		identity = userDeletionManager.setIdentityAsActiv(identity);
		fireEvent(ureq, new AuthenticationEvent(identity));
	}
	
	/**
	 * Called if the Controller is to change the locale on the fly.
	 * @param newLocale
	 */
	public abstract void changeLocale(Locale newLocale);
	
}
