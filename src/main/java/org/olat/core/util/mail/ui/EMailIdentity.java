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
package org.olat.core.util.mail.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;

public class EMailIdentity implements Identity {

	private static final long serialVersionUID = -2899896628137672419L;
	private final String email;
	private final User user;
	private final Locale locale;

	public EMailIdentity(String email, Locale locale) {
		this.email = email;
		user = new EMailUser(email);
		this.locale = locale;
	}

	@Override
	public Long getKey() {
		return null;
	}
	
	@Override
	public String getExternalId() {
		return null;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return this == persistable;
	}

	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public String getName() {
		return email;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Date getLastLogin() {
		return null;
	}

	@Override
	public Integer getStatus() {
		return null;
	}
	
	@Override
	public Date getInactivationDate() {
		return null;
	}
	
	@Override
	public Date getReactivationDate() {
		return null;
	}
	
	@Override
	public Date getExpirationDate() {
		return null;
	}

	private class EMailUser implements User, ModifiedInfo {

		private static final long serialVersionUID = 7260225880639460228L;
		private final EMailPreferences prefs = new EMailPreferences();
		private Map<String, String> data = new HashMap<>();

		public EMailUser(String email) {
			data.put(UserConstants.FIRSTNAME, "");
			data.put(UserConstants.LASTNAME, "");
			data.put(UserConstants.EMAIL, email);
		}

		@Override
		public Long getKey() {
			return null;
		}

		@Override
		public boolean equalsByPersistableKey(Persistable persistable) {
			return this == persistable;
		}

		@Override
		public String getFirstName() {
			return data.get(UserConstants.FIRSTNAME);
		}

		@Override
		public String getLastName() {
			return data.get(UserConstants.LASTNAME);
		}
		
		@Override
		public String getNickName() {
			return data.get(UserConstants.NICKNAME);
		}

		@Override
		public String getEmail() {
			return data.get(UserConstants.EMAIL);
		}

		@Override
		public String getInstitutionalEmail() {
			return data.get(UserConstants.INSTITUTIONALEMAIL);
		}

		@Override
		public String getSmsTelMobile() {
			return data.get(UserConstants.SMSTELMOBILE);
		}

		@Override
		public Date getLastModified() {
			return null;
		}

		@Override
		public void setLastModified(Date date) {
			//
		}

		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public void setProperty(String propertyName, String propertyValue) {
			//
		}

		@Override
		public void setPreferences(Preferences prefs) {
			//
		}

		@Override
		public String getProperty(String propertyName, Locale propLocale) {
			return data.get(propertyName);
		}

		@Override
		public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {/**/
		}

		@Override
		public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale propLocale) {
			return data.get(propertyName);
		}

		@Override
		public Preferences getPreferences() {
			return prefs;
		}
	}

	private class EMailPreferences implements Preferences {
		private static final long serialVersionUID = 7039109437910126584L;

		@Override
		public String getLanguage() {
			return locale.getLanguage();
		}

		@Override
		public void setLanguage(String l) {
			//
		}

		@Override
		public String getNotificationInterval() {
			return null;
		}

		@Override
		public void setNotificationInterval(String notificationInterval) {/* */
		}

		@Override
		public String getReceiveRealMail() {
			return "true";
		}

		@Override
		public void setReceiveRealMail(String receiveRealMail) {
			//
		}

		@Override
		public boolean getInformSessionTimeout() {
			return false;
		}

		@Override
		public void setInformSessionTimeout(boolean b) {/* */
		}

		@Override
		public boolean getPresenceMessagesPublic() {
			return false;
		}

		@Override
		public void setPresenceMessagesPublic(boolean b) {/* */
		}
	}
}