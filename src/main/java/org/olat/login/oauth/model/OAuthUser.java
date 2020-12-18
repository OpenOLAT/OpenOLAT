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
package org.olat.login.oauth.model;

import org.olat.core.id.UserConstants;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthUser {
	
	private String id;
	private String nickName;
	private String email;
	private String firstName;
	private String lastName;
	private String institutionalUserIdentifier;
	private String institutionalName;
	private String lang;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getInstitutionalUserIdentifier() {
		return institutionalUserIdentifier;
	}

	public void setInstitutionalUserIdentifier(String institutionalUserIdentifier) {
		this.institutionalUserIdentifier = institutionalUserIdentifier;
	}

	public String getInstitutionalName() {
		return institutionalName;
	}

	public void setInstitutionalName(String institutionalName) {
		this.institutionalName = institutionalName;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getProperty(String propName) {
		switch(propName) {
			case UserConstants.EMAIL: return email;
			case UserConstants.FIRSTNAME: return firstName;
			case UserConstants.LASTNAME: return lastName;
			case UserConstants.INSTITUTIONALUSERIDENTIFIER: return institutionalUserIdentifier;
			case UserConstants.INSTITUTIONALNAME: return institutionalName;
			default: return null;
		}	
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("oauthUser[id=").append(id == null ? "null" : id).append(";")
		  .append("email=").append(email == null ? "null" : email).append(";")
		  .append("firstName=").append(firstName == null ? "null" : firstName).append(";")
		  .append("lastName=").append(lastName == null ? "null" : lastName).append("]");
		return sb.toString();
	}
}
