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
package org.olat.basesecurity.model;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;

/**
 * This is an helper class to use with UserPropertyHandler
 * if the values are directly retrieved from the database without
 * using the IdentityImpl implementation.
 * 
 * Initial date: 2 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QueryUserHelper implements User {

	private static final long serialVersionUID = 1898321948345225158L;
	private String userProperty;
	
	public void setUserProperty(String property) {
		this.userProperty = property;
	}

	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public Long getKey() {
		return null;
	}

	@Override
	public String getFirstName() {
		return null;
	}

	@Override
	public String getLastName() {
		return null;
	}

	@Override
	public String getNickName() {
		return null;
	}

	@Override
	public String getEmail() {
		return null;
	}

	@Override
	public String getInstitutionalEmail() {
		return null;
	}

	@Override
	public String getSmsTelMobile() {
		return null;
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
		//
	}

	@Override
	public String getProperty(String propertyName, Locale locale) {
		return getProperty(propertyName);
	}

	@Override
	public String getProperty(String propertyName) {
		return userProperty;
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
	public boolean equalsByPersistableKey(Persistable persistable) {
		return false;
	}
}
