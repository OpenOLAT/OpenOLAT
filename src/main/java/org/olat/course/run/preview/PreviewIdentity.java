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
* <p>
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
import org.olat.core.logging.AssertException;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
final class PreviewIdentity implements Identity {

	/**
	 * @see org.olat.core.id.Identity#getName()
	 */
	public String getName() {
		return "JaneDoe";
	}

	/**
	 * @see org.olat.core.id.Identity#getUser()
	 */
	public User getUser() {
		return new User(){
			Map<String, String> data = new HashMap<String, String>();
			private Map<String, String> envAttrs;
			{
				data.put(UserConstants.FIRSTNAME, "Jane");
				data.put(UserConstants.LASTNAME, "Doe");
				data.put(UserConstants.EMAIL, "jane.doe@testmail.com");
			}
			
			public Long getKey() {
				// TODO Auto-generated method stub
				return null;
			}
			@SuppressWarnings("unused")
			public boolean equalsByPersistableKey(Persistable persistable) {
				// TODO Auto-generated method stub
				return false;
			}
		
			public Date getLastModified() {
				// TODO Auto-generated method stub
				return null;
			}
		
			public Date getCreationDate() {
				// TODO Auto-generated method stub
				return null;
			}
			@SuppressWarnings("unused")
			public void setProperty(String propertyName, String propertyValue) {
				// TODO Auto-generated method stub
				
			}
			@SuppressWarnings("unused")
			public void setPreferences(Preferences prefs) {
				// TODO Auto-generated method stub
				
			}
			@SuppressWarnings("unused")
			public String getProperty(String propertyName, Locale locale) {					
				return data.get(propertyName);
			}

			public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
				this.envAttrs = identEnvAttribs;
			}	

			public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
				String retVal = null;
				retVal = data.get(propertyName);
				if(retVal== null && this.envAttrs != null){
					retVal = envAttrs.get(propertyName);
				}
				return retVal;
			}
			
			public Preferences getPreferences() {
				// TODO Auto-generated method stub
				return null;
			}
		
		};
	}

	/**
	 * @see org.olat.core.commons.persistence.Auditable#getCreationDate()
	 */
	public Date getCreationDate() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.core.commons.persistence.Auditable#getLastModified()
	 */
	public Date getLastModified() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.core.commons.persistence.Persistable#getKey()
	 */
	public Long getKey() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.core.commons.persistence.Persistable#equalsByPersistableKey(org.olat.core.commons.persistence.Persistable)
	 */
	public boolean equalsByPersistableKey(Persistable persistable) {
		throw new AssertException("unsupported");
	}

	public Date getLastLogin() {
		throw new AssertException("unsupported");
	}

	public void setLastLogin(Date loginDate) {
		throw new AssertException("unsupported");
	}
	
	public Integer getStatus() {
		throw new AssertException("unsupported");
	}

	public void setStatus(Integer newStatus) {
		throw new AssertException("unsupported");
	}

	public Date getDeleteEmailDate() {
		throw new AssertException("unsupported");
	}

	public void setDeleteEmailDate(Date newDeleteEmail) {
		throw new AssertException("unsupported");
	}

	public void setName(String loginName) {
		
	}

}