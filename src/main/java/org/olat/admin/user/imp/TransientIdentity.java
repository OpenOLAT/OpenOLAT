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

/**
 * A transient implementation of Identity
 *
 * Initial date: 08.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientIdentity implements Identity, User {
	private static final long serialVersionUID = 1394807800521540930L;

	private String login;
	private String password;
	private String language;
	private Date expirationDate;
	
	private Map<String, String> properties = new HashMap<>();

	@Override
	public Date getCreationDate() {
		return null;
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
	public String getName() {
		return login;
	}
	
	public void setName(String name) {
		this.login = name;
	}
	
	@Override
	public String getFirstName() {
		return properties.get(UserConstants.FIRSTNAME);
	}

	@Override
	public String getLastName() {
		return properties.get(UserConstants.LASTNAME);
	}
	
	@Override
	public String getNickName() {
		return properties.get(UserConstants.NICKNAME);
	}

	@Override
	public String getEmail() {
		return properties.get(UserConstants.EMAIL);
	}

	@Override
	public String getInstitutionalEmail() {
		return properties.get(UserConstants.INSTITUTIONALEMAIL);
	}

	@Override
	public String getSmsTelMobile() {
		return properties.get(UserConstants.SMSTELMOBILE);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public User getUser() {
		return this;
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
		return expirationDate;
	}
	
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return super.equals(persistable);
	}

	@Override
	public Preferences getPreferences() {
		return null;
	}

	@Override
	public void setPreferences(Preferences prefs) {
		//
	}

	@Override
	public void setProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	@Override
	public String getProperty(String propertyName, Locale locale) {
		return properties.get(propertyName);
	}

	@Override
	public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
		//
	}

	@Override
	public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
		return null;
	}
}
