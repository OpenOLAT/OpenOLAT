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
package org.olat.core.gui.control.navigation.callback;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteViewSecurityCallback;
import org.olat.core.util.UserSession;

/**
 * Guest can see the site, but only registred can launch it
 * 
 * 
 * Initial date: 25.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RegistredUserGuestTeaserSecurityCallback implements SiteViewSecurityCallback {

	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		return usess != null && usess.getRoles() != null && !usess.getRoles().isInvitee() && !usess.getRoles().isGuestOnly();
	}

	@Override
	public boolean isAllowedToViewSite(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		return usess != null && usess.getRoles() != null && !usess.getRoles().isInvitee();
	}
}
