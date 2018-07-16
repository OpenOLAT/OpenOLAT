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
package org.olat.repository.site;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.util.UserSession;
import org.olat.repository.manager.CatalogManager;

/**
 * <h3>Description:</h3>
 * Administrator callback allows to view/launch only for users with admin-role.
 * IMPORTANT: This Callback is also used for permissions in search!
 * 
 * Initial Date:  13.04.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class CatalogManagerSecurityCallback implements SiteSecurityCallback {
	
	private CatalogManager catalogManager;
	
	/**
	 * [used by Spring]
	 * @param catalogManager
	 */
	public void setCatalogManager(CatalogManager catalogManager) {
		this.catalogManager = catalogManager;
	}

	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null || ureq.getIdentity() == null
				|| usess.getRoles().isInvitee() || usess.getRoles().isGuestOnly()) {
			return false;
		}
		return usess.getRoles().isAdministrator()
				|| usess.getRoles().isLearnResourceManager()
				|| catalogManager.isOwner(ureq.getIdentity());
	}
}
