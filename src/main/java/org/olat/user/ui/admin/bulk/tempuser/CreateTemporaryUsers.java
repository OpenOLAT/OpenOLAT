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
package org.olat.user.ui.admin.bulk.tempuser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Organisation;
import org.olat.core.id.UserConstants;
import org.olat.core.util.crypto.PasswordGenerator;
import org.olat.login.LoginModule;

/**
 * 
 * Initial date: 14 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateTemporaryUsers {
	
	private int numberOfUsers;
	private String usernamePrefix;
	private String firstNamePrefix;
	private String lastNamePrefix;
	private Date expirationDate;
	private Organisation organisation;
	private final List<TransientIdentity> proposedIdentities = new ArrayList<>();
	private List<TransientIdentity> validatedIdentities = new ArrayList<>();
	
	public CreateTemporaryUsers(Organisation organisation) {
		this.organisation = organisation;
	}
	
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public List<TransientIdentity> getProposedIdentities() {
		return proposedIdentities;
	}
	
	public List<TransientIdentity> getValidatedIdentities() {
		return validatedIdentities;
	}
	
	public void setValidatedIdentities(List<TransientIdentity> identities) {
		if(identities == null) {
			validatedIdentities = new ArrayList<>(1);
		} else {
			validatedIdentities = new ArrayList<>(identities);
		}
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}

	public String getUsernamePrefix() {
		return usernamePrefix;
	}

	public void setUsernamePrefix(String usernamePrefix) {
		this.usernamePrefix = usernamePrefix;
	}

	public String getFirstNamePrefix() {
		return firstNamePrefix;
	}

	public void setFirstNamePrefix(String firstNamePrefix) {
		this.firstNamePrefix = firstNamePrefix;
	}

	public String getLastNamePrefix() {
		return lastNamePrefix;
	}

	public void setLastNamePrefix(String lastNamePrefix) {
		this.lastNamePrefix = lastNamePrefix;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	public void generateUsers() {
		proposedIdentities.clear();
		if(numberOfUsers <= 0) {
			return;
		}
		
		int passwordLength = passwordLength();
		int numberOfCharacters = Integer.toString(numberOfUsers).length();
		for(int i=1; i<=numberOfUsers; i++) {
			String num = getNumber(i, numberOfCharacters);
			
			TransientIdentity identity = new TransientIdentity();
			identity.setName(usernamePrefix.concat(num));
			identity.setProperty(UserConstants.NICKNAME, usernamePrefix);
			identity.setProperty(UserConstants.FIRSTNAME, firstNamePrefix.concat(num));
			identity.setProperty(UserConstants.LASTNAME, lastNamePrefix.concat(num));
			identity.setExpirationDate(getExpirationDate());
			identity.setPassword(PasswordGenerator.generatePassword(passwordLength));
			proposedIdentities.add(identity);
		}
	}
	
	private int passwordLength() {
		LoginModule loginModule = CoreSpringFactory.getImpl(LoginModule.class);
		int minLength = Math.max(6, loginModule.getPasswordMinLength()) + 2;
		return Math.min(minLength, loginModule.getPasswordMaxLength());
	}
	
	private String getNumber(int i, int numberOfCharacters) {
		String potentielName = Integer.toString(i);
		if(potentielName.length() < numberOfCharacters) {
			for(int j=potentielName.length(); j<numberOfCharacters; j++) {
				potentielName = "0".concat(potentielName);
			}
		}
		return potentielName;
	}
}
