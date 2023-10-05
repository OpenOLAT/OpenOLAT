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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.login.performx;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.login.auth.AuthenticationProvider;

/**
 * 
 * Initial date: 06.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PerformXAuthenticationProvider extends AuthenticationProvider {
	
	public PerformXAuthenticationProvider(String name, String clazz, boolean enabled, boolean isDefault, String iconCssClass) {
		super(name, clazz, enabled, isDefault, iconCssClass);
	}

	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		return super.createController(lureq, lwControl);
	}

	@Override
	public boolean isDefault() {
		return super.isEnabled();
	}
}
