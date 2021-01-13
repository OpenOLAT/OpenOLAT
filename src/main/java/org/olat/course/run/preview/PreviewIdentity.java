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

package org.olat.course.run.preview;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.CodeHelper;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
public final class PreviewIdentity implements Identity, User {

	private static final long serialVersionUID = 6582855975941440446L;
	
	private final Map<String, String> data = new HashMap<>();
	private final Long key;
	private Map<String, String> envAttrs;
	{
		data.put(UserConstants.FIRSTNAME, "Jane");
		data.put(UserConstants.LASTNAME, "Doe");
		data.put(UserConstants.EMAIL, "jane.doe@testmail.com");
	}
	
	public PreviewIdentity() {
		key = CodeHelper.getRAMUniqueID();
	}

	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public String getExternalId() {
		return null;
	}

	@Override
	public String getName() {
		return "JaneDoe";
	}
	
	@Override
	public String getNickName() {
		return "u" + getKey();
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
	public String getFirstName() {
		return data.get(UserConstants.FIRSTNAME);
	}

	@Override
	public String getLastName() {
		return data.get(UserConstants.LASTNAME);
	}

	@Override
	public String getSmsTelMobile() {
		return data.get(UserConstants.SMSTELMOBILE);
	}

	@Override
	public User getUser() {
		return this;
	}

	@Override
	public String getProperty(String propertyName, Locale locale) {					
		return data.get(propertyName);
	}

	@Override
	public void setProperty(String propertyName, String propertyValue) {
		data.put(propertyName, propertyValue);
	}

	@Override
	public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
		this.envAttrs = identEnvAttribs;
	}	

	@Override
	public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
		String retVal = null;
		retVal = data.get(propertyName);
		if(retVal== null && this.envAttrs != null){
			retVal = envAttrs.get(propertyName);
		}
		return retVal;
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
	public Date getCreationDate() {
		return new Date();
	}

	@Override
	public Date getLastLogin() {
		return new Date();
	}

	@Override
	public Integer getStatus() {
		return Identity.STATUS_ACTIV;
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

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}