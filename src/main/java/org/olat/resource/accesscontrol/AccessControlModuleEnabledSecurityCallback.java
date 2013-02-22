/**
 * <a href=“http://www.openolat.org“>
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
 * 22.02.2013 by frentix GmbH, http://www.frentix.com
 * <p>
 **/
package org.olat.resource.accesscontrol;

import org.olat.core.CoreSpringFactory;
import org.olat.core.extensions.action.ActionExtensionSecurityCallback;
import org.olat.core.gui.UserRequest;

/**
 * @author Florian Gnaegi, frentix GmbH
 * 
 * This callback returns true when the access control module is enabled, false otherwise
 * 
 */
public class AccessControlModuleEnabledSecurityCallback implements
		ActionExtensionSecurityCallback {

	private final AccessControlModule acModule = CoreSpringFactory.getImpl(AccessControlModule.class);

	/**
	 * @see org.olat.core.extensions.action.ActionExtensionSecurityCallback#
	 * isAllowedToLaunchActionController(org.olat.core.gui.UserRequest)
	 */
	@Override
	public boolean isAllowedToLaunchActionController(UserRequest ureq) {
		return acModule.isEnabled();
	}

}
