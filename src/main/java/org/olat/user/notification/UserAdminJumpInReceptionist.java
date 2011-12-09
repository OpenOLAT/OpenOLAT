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
package org.olat.user.notification;

import java.util.Locale;

import org.olat.admin.site.UserAdminSite;
import org.olat.core.dispatcher.jumpin.JumpInReceptionist;
import org.olat.core.dispatcher.jumpin.JumpInResult;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;

/**
 * 
 * Description:<br>
 * Implement a JumpInReceptionist to jump to the
 * UserAdminSite to see all new users.
 * <P>
 * Initial Date:  19 august 2009 <br>
 *
 * @author srosse
 */
public class UserAdminJumpInReceptionist implements JumpInReceptionist {
	
	private final Translator translator;
	
	public UserAdminJumpInReceptionist(Locale locale) {
		translator = Util.createPackageTranslator(this.getClass(), locale);
	}

	/**
	 * @return a title
	 */
	public String getTitle() {
		return translator.translate("notifications.header");
	}
	
	public OLATResourceable getOLATResourceable() {
		return new OLATResourceable() {
			public Long getResourceableId() {
				return new Long(0);
			}
			public String getResourceableTypeName() {
				return UserAdminSite.class.getName();
			}
		};
	}
	
	/**
	 * @return the key needed to activate the controller
	 */
	public JumpInResult createJumpInResult(UserRequest ureq, WindowControl wControl) {
		return new JumpInResult(null, "notifications");
	}

	public String extractActiveViewId(UserRequest ureq) {
		return null;
	}
}
