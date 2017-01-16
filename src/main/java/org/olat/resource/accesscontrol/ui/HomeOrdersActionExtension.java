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
package org.olat.resource.accesscontrol.ui;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.admin.user.tools.UserToolImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.UserSession;
import org.olat.resource.accesscontrol.AccessControlModule;

/**
 * 
 * Initial date: 09.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HomeOrdersActionExtension extends UserToolExtension  {
	
	private final AccessControlModule acModule;
	
	public HomeOrdersActionExtension(AccessControlModule acModule) {
		this.acModule = acModule;
	}
	
	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		return new OrdersController(ureq, wControl);
	}
	
	@Override
	public UserTool createUserTool(UserRequest ureq, WindowControl wControl, Locale locale) {
		if(ureq == null) return null;
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null || usess.getRoles().isGuestOnly() || usess.getRoles().isInvitee()) {
			return null;
		}
		return new UserToolImpl(this, wControl, locale);
	}

	@Override
	public boolean isEnabled() {
		return acModule.isEnabled() && (acModule.isHomeOverviewEnabled() || acModule.isPaypalEnabled());
	}

}
