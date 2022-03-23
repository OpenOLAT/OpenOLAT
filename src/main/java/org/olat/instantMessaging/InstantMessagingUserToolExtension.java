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
package org.olat.instantMessaging;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 29.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessagingUserToolExtension extends UserToolExtension {
	
	@Override
	public boolean isShortCutOnly() {
		return true;
	}
	
	@Override
	public UserToolCategory getUserToolCategory() {
		return UserToolCategory.personal;
	}
	
	@Override
	public String getShortCutCssId() {
		return "o_navbar_imclient";
	}
	
	@Override
	public String getShortCutCssClass() {
		return null;
	}

	@Override
	public String getUniqueExtensionID() {
		return "org.olat.home.HomeMainController:org.olat.instantMessaging.ui.InstantMessagingMainController";
	}

	@Override
	public UserTool createUserTool(UserRequest ureq, WindowControl wControl, Locale locale) {
		if(ureq == null) return null;
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null || usess.getRoles().isGuestOnly() || usess.getRoles().isInvitee()) {
			return null;
		}
		return new InstantMessagingUserTool(wControl);
	}
}
