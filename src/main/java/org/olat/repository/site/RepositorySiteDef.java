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

package org.olat.repository.site;

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
 * Description:<br>
 * Site for the learn resources
 * 
 * <P>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Felix Jost
 */
public class RepositorySiteDef extends AbstractSiteDefinition implements SiteDefinition {

	/**
	 * Site is normally only open to authors. Configured via security callback
	 */
	@Override
	public SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			return new RepositorySite(this, ureq.getLocale());
		} 
		
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null) {
			return null;
		}
		
		Roles roles = usess.getRoles();
		if(roles.isAdministrator() || roles.isAuthor() || roles.isLearnResourceManager()) {
			// only for authors and institutional resource managers
			return new RepositorySite(this, ureq.getLocale());
		}
		return null;
	}
}

