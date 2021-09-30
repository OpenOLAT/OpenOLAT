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
package org.olat.admin.user.imp;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 23.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UpdateIdentity implements Identity {
	
	private static final long serialVersionUID = 8918783456443529334L;

	private final UpdateUser userWrapper;
	private final Identity identity;
	
	private final String password;
	private final String language;
	private final Date expirationDate;
	
	public UpdateIdentity(Identity identity, String password, String language, Date expirationDate) {
		this.identity = identity;
		this.password = password;
		this.language = language;
		this.expirationDate = expirationDate;
		this.userWrapper = new UpdateUser(identity.getUser());
	}
	
	public String getPassword() {
		return password;
	}

	public String getLanguage() {
		return language;
	}

	public Identity getIdentity() {
		return getIdentity(false);
	}
	
	public Identity getIdentity(boolean transferNewProperties) {
		if(transferNewProperties) {
			User user = identity.getUser();
			if(StringHelper.containsNonWhitespace(language)) {
				user.getPreferences().setLanguage(language);
			}
			
			Map<String,String> updatedProperties = userWrapper.getUpdatedProperties();
			for(Map.Entry<String, String> entry:updatedProperties.entrySet()) {
				String propertyName = entry.getKey();
				String propertyValue = entry.getValue();
				user.setProperty(propertyName, propertyValue);
			}
		}
		return identity;
	}

	@Override
	public Long getKey() {
		return identity.getKey();
	}

	@Override
	public Date getCreationDate() {
		return identity.getCreationDate();
	}

	@Override
	public String getName() {
		return identity.getName();
	}
	
	@Override
	public String getExternalId() {
		return identity.getExternalId();
	}

	@Override
	public User getUser() {
		return userWrapper;
	}

	@Override
	public Date getLastLogin() {
		return identity.getLastLogin();
	}

	@Override
	public Integer getStatus() {
		return identity.getStatus();
	}
	
	@Override
	public Date getInactivationDate() {
		return identity.getInactivationDate();
	}

	@Override
	public Date getReactivationDate() {
		return identity.getReactivationDate();
	}
	
	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}
	
	@Override
	public Date getDeletionEmailDate() {
		return identity.getDeletionEmailDate();
	}

	@Override
	public int hashCode() {
		return identity.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return identity.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return identity.equalsByPersistableKey(persistable);
	}
	
	private static class UpdateUser implements User {

		private static final long serialVersionUID = -7755595504039649174L;
		
		private final User user;
		private Map<String,String> updatedProperties = new HashMap<>();
		
		public UpdateUser(User user) {
			this.user = user;
		}
		
		public Map<String,String> getUpdatedProperties() {
			return updatedProperties;
		}
		
		@Override
		public Long getKey() {
			return user.getKey();
		}
		
		@Override
		public String getFirstName() {
			return updatedProperties.get(UserConstants.FIRSTNAME);
		}

		@Override
		public String getLastName() {
			return updatedProperties.get(UserConstants.LASTNAME);
		}

		@Override
		public String getNickName() {
			return updatedProperties.get(UserConstants.NICKNAME);
		}

		@Override
		public String getEmail() {
			return updatedProperties.get(UserConstants.EMAIL);
		}

		@Override
		public String getInstitutionalEmail() {
			return updatedProperties.get(UserConstants.INSTITUTIONALEMAIL);
		}

		@Override
		public String getSmsTelMobile() {
			return updatedProperties.get(UserConstants.SMSTELMOBILE);
		}

		@Override
		public Date getCreationDate() {
			return user.getCreationDate();
		}

		@Override
		public Preferences getPreferences() {
			return user.getPreferences();
		}

		@Override
		public void setPreferences(Preferences prefs) {
			//
		}

		@Override
		public void setProperty(String propertyName, String propertyValue) {
			String currentProperty = user.getProperty(propertyName, null);
			if(currentProperty == null
					|| (currentProperty != null && !currentProperty.equals(propertyValue))) {
				updatedProperties.put(propertyName, propertyValue);
			}
		}

		@Override
		public String getProperty(String propertyName, Locale locale) {
			if(updatedProperties.containsKey(propertyName)) {
				return updatedProperties.get(propertyName);
			}
			return user.getProperty(propertyName, locale);
		}

		@Override
		public String getProperty(String propertyName) {
			if(updatedProperties.containsKey(propertyName)) {
				return updatedProperties.get(propertyName);
			}
			return user.getProperty(propertyName);
		}

		@Override
		public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
			//
		}

		@Override
		public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
			return null;
		}

		@Override
		public int hashCode() {
			return user.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return user.equals(obj);
		}

		@Override
		public boolean equalsByPersistableKey(Persistable persistable) {
			return user.equalsByPersistableKey(persistable);
		}
	}
}
