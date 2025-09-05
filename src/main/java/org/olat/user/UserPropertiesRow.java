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
package org.olat.user;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityNames;
import org.olat.basesecurity.model.QueryUserHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * This is to build table and reduced the memory needed by IdentityImpl.
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserPropertiesRow {
	
	private final Long identityKey;
	private final String externalId;
	private final String[] identityProps;
	
	/**
	 * 
	 * @param identityKey The identity key
	 * @param identityName The identity name
	 * @param userPropertyHandlers The handlers (must match exactly the identities properties array)
	 * @param identityProps The raw user properties
	 * @param locale The locale
	 */
	public UserPropertiesRow(Long identityKey, String externalId, List<UserPropertyHandler> userPropertyHandlers, String[] identityProps, Locale locale) {
		this.externalId = externalId;
		this.identityKey = identityKey;
		this.identityProps = identityProps;
		if(identityProps != null) {
			QueryUserHelper user = new QueryUserHelper();
			for(int i=userPropertyHandlers.size(); i-->0; ) {
				user.setUserProperty(identityProps[i]);
				identityProps[i] = userPropertyHandlers.get(i).getUserProperty(user, locale);
			}
		}
	}
	
	/**
	 * @param identity The identity
	 * @param userPropertyHandlers The handlers which genegrates the properties array
	 * @param locale The locale
	 */
	public UserPropertiesRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		identityProps = new String[userPropertyHandlers.size()];
		if(identity == null) {
			identityKey = null;
			externalId = null;
		} else {
			externalId = identity.getExternalId();
			identityKey = identity.getKey();
			for(int i=userPropertyHandlers.size(); i-->0; ) {
				identityProps[i] = userPropertyHandlers.get(i).getUserProperty(identity.getUser(), locale);
			}
		}
	}
	
	/**
	 * Copy the properties array
	 * 
	 * @param row The user properties
	 */
	protected UserPropertiesRow(UserPropertiesRow row) {
		identityProps = row.identityProps;
		identityKey = row.identityKey;
		externalId = row.externalId;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public String getIdentityExternalId() {
		return externalId;
	}
	
	public String[] getIdentityProps() {
		return identityProps;
	}
	
	public String getIdentityProp(int index) {
		return identityProps[index];
	}
	
	public String getIdentityProp(String property, List<UserPropertyHandler> userPropertyHandlers) {
		int numOfHandlers = userPropertyHandlers.size();
		for(int i=0; i<numOfHandlers; i++) {
			if(property.equals(userPropertyHandlers.get(i).getName()) && i<identityProps.length) {
				return getIdentityProp(i);
			}
		}
		
		return null;
	}
	
	public IdentityNames getIdentityNames(List<UserPropertyHandler> userPropertyHandlers) {
		String firstName = null;
		String lastName = null;
		
		int numOfHandlers = userPropertyHandlers.size();
		for(int i=0; i<numOfHandlers; i++) {
			if(UserConstants.FIRSTNAME.equals(userPropertyHandlers.get(i).getName()) && i<identityProps.length) {
				firstName = getIdentityProp(i);
			} else if(UserConstants.LASTNAME.equals(userPropertyHandlers.get(i).getName()) && i<identityProps.length) {
				lastName = getIdentityProp(i);
			}
		}
		return new UserNames(getIdentityKey(), firstName, lastName);
	}
	
	public record UserNames(Long key, String firstName, String lastName) implements IdentityNames {
		@Override
		public Long getKey() {
			return key();
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getFirstName() {
			return firstName();
		}

		@Override
		public String getLastName() {
			return lastName();
		}
	}
}
