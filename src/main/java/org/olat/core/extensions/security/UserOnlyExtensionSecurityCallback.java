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
package org.olat.core.extensions.security;

import org.olat.core.extensions.action.ActionExtensionSecurityCallback;
import org.olat.core.gui.UserRequest;

/**
 * Description:<br>
 * hide extensions for guests
 * 
 * <P>
 * Initial Date:  09.09.2011 <br>
 * @author Sergio Trentini, sergio.trentini@frentix.com http://www.frentix.com
 */
public class UserOnlyExtensionSecurityCallback implements ActionExtensionSecurityCallback {

	/**
	 * 
	 * @see org.olat.core.extensions.action.ActionExtensionSecurityCallback#isAllowedToLaunchActionController(org.olat.core.gui.UserRequest)
	 */
	@Override
	public boolean isAllowedToLaunchActionController(UserRequest ureq) {
		if(ureq == null || ureq.getUserSession() == null || ureq.getUserSession().getRoles() == null) {
			return false;
		}
		return !ureq.getUserSession().getRoles().isGuestOnly();
	}

}
