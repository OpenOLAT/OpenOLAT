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

package org.olat.admin.site;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;

/**
 * Initial Date:  Jan 16, 2006
 * @author Florian Gnaegi d
 */
public class UserAdminSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	public UserAdminSiteDef() {
		//
	}

	@Override
	public SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if(ureq == null) return null;
		
		UserSession usess = ureq.getUserSession();
		if(usess.getRoles() == null) {
			return null;
		}
		if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			return new UserAdminSite(this, ureq.getLocale());
		} 
		
		Roles roles = usess.getRoles();
		if (roles.isAdministrator() || roles.isUserManager() || roles.isRolesManager()) {
			// only open for olat-usermanagers
			return new UserAdminSite(this, ureq.getLocale());
		} 
		return null;
	}
}

