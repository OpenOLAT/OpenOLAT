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

import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.EdusharingModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
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
	JsonNode getUserProfile(Identity identity) {
		ObjectNode userProfile = objectMapper.createObjectNode();
		
		// optional
		User user = identity.getUser();
		
		if (StringHelper.containsNonWhitespace(user.getLastName())) {
			userProfile.put("lastName", user.getLastName());
		}
		
		if (StringHelper.containsNonWhitespace(user.getFirstName())) {
			userProfile.put("firstName", user.getFirstName());
		}
		
		if (StringHelper.containsNonWhitespace(user.getEmail())) {
			userProfile.put("email", user.getEmail());
		}
		
		if (StringHelper.containsNonWhitespace(edusharingModule.getAuthAffiliationId())) {
			userProfile.put("primaryAffiliation", edusharingModule.getAuthAffiliationId());
		}
		
		return userProfile;
	}

}
