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
package org.olat.modules.invitation.site;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.modules.invitation.InvitationModule;

/**
 * 
 * Initial date: 12 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationsSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null || !usess.getRoles().isInvitee()) {
			return null;
		}

		// these are the minimal security
		Roles roles = usess.getRoles();
		if(roles.isInvitee()) {
			return new InvitationsSite(this, ureq.getLocale());
		}	
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		InvitationModule module = CoreSpringFactory.getImpl(InvitationModule.class);
		return (module.isBusinessGroupInvitationEnabled() || module.isCourseInvitationEnabled() || module.isPortfolioInvitationEnabled())
				&& super.isEnabled();
	}
}
