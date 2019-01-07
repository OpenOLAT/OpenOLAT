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
package org.olat.modules.edusharing.manager;

import java.util.ArrayList;
import java.util.List;

import org.edu_sharing.webservices.types.KeyValue;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.EdusharingModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 7 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class EdusharingUserFactory {
	
	@Autowired
	private EdusharingModule edusharingModule;
	
	String getUserIdentifier(Identity identity) {
		String identifier;
		
		switch (edusharingModule.getUserIdentifierKey()) {
		case "username":
			identifier = identity.getName();
			break;
		case "email": 
			identifier = identity.getUser().getEmail();
			break;
		default:
			identifier = null;
		}
		
		return StringHelper.blankIfNull(identifier);
	}
	
	/**
	 * Key are configured in edu-sharing configuration: edu-sharing-sso-context.xml
	 *
	 * @return
	 */
	List<KeyValue> getSSOData(Identity identity) {
		List<KeyValue> ssoData = new ArrayList<>();
		
		// mandatory
		KeyValue userId = new KeyValue();
		userId.setKey(edusharingModule.getAuthKeyUseriId());
		userId.setValue(getUserIdentifier(identity));
		ssoData.add(userId);
		
		// optional
		User user = identity.getUser();
		
		if (StringHelper.containsNonWhitespace(edusharingModule.getAuthKeyLastname())
				&& StringHelper.containsNonWhitespace(user.getLastName())) {
			KeyValue lastname = new KeyValue();
			lastname.setKey(edusharingModule.getAuthKeyLastname());
			lastname.setValue(user.getLastName());
			ssoData.add(lastname);
		}
		
		if (StringHelper.containsNonWhitespace(edusharingModule.getAuthKeyFirstname())
				&& StringHelper.containsNonWhitespace(user.getFirstName())) {
			KeyValue firstname = new KeyValue();
			firstname.setKey(edusharingModule.getAuthKeyFirstname());
			firstname.setValue(user.getFirstName());
			ssoData.add(firstname);
		}

		if (StringHelper.containsNonWhitespace(edusharingModule.getAuthKeyEmail())
				&& StringHelper.containsNonWhitespace(user.getEmail())) {
			KeyValue email = new KeyValue();
			email.setKey(edusharingModule.getAuthKeyEmail());
			email.setValue(user.getEmail());
			ssoData.add(email);
		}

		if (StringHelper.containsNonWhitespace(edusharingModule.getAuthAffiliationId())) {
			KeyValue affiliationId = new KeyValue();
			affiliationId.setKey("affiliation");
			affiliationId.setValue(edusharingModule.getAuthAffiliationId());
			ssoData.add(affiliationId);
		}

		if (StringHelper.containsNonWhitespace(edusharingModule.getAuthAffiliationName())) {
			KeyValue affiliationName = new KeyValue();
			affiliationName.setKey("affiliationname");
			affiliationName.setValue(edusharingModule.getAuthAffiliationName());
			ssoData.add(affiliationName);
		}
		
		return ssoData;
	}

}
