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
package org.olat.modules.invitation.model;

import java.util.HashMap;
import java.util.Map;

import org.olat.modules.invitation.InvitationAdditionalInfos;

/**
 * 
 * Initial date: 18 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationAdditionalInfosImpl implements InvitationAdditionalInfos {
	
	private Map<String,String> userAttributes;

	@Override
	public Map<String, String> getUserAttributes() {
		if(userAttributes == null) {
			userAttributes = new HashMap<>();
		}
		return userAttributes;
	}

	public void setUserAttributes(Map<String, String> userAttributes) {
		this.userAttributes = userAttributes;
	}
}
