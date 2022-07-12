/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */

package org.olat.core.gui.control.navigation.callback;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.group.BusinessGroupService;
import org.springframework.beans.factory.annotation.Autowired;

public class GroupMemberOrManagerSecurityCallback implements SiteSecurityCallback {
	
	
	@Autowired
	private BusinessGroupService businessGroupService;


	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if (usess == null || usess.getRoles() == null || usess.getRoles().isGuestOnly()) {
			return false;
		}
		
		Roles roles = usess.getRoles();
		Identity ident = ureq.getIdentity();
		return roles.isAdministrator() 
				|| roles.isGroupManager() 
				|| (businessGroupService.findBusinessGroups(ident,1 , null).size() > 0);
	}
}
