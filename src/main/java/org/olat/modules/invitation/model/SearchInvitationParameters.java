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

import java.util.List;

import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 5 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchInvitationParameters {
	
	private Long identityKey;
	private InvitationStatusEnum status;
	
	private InvitationTypeEnum type;
	
	private String searchString;
	private List<UserPropertyHandler> userPropertyHandlers;

	public InvitationStatusEnum getStatus() {
		return status;
	}

	public void setStatus(InvitationStatusEnum status) {
		this.status = status;
	}

	public InvitationTypeEnum getType() {
		return type;
	}

	public void setType(InvitationTypeEnum type) {
		this.type = type;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public List<UserPropertyHandler> getUserPropertyHandlers() {
		return userPropertyHandlers;
	}

	public void setUserPropertyHandlers(List<UserPropertyHandler> userPropertyHandlers) {
		this.userPropertyHandlers = userPropertyHandlers;
	}
}
