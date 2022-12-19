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
package org.olat.modules.invitation.restapi;

import org.olat.basesecurity.Invitation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Initial date: 16 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "invitationVO")
public class InvitationVO {
	
	private Long key;
	private String firstName;
	private String lastName;
	private String email;
	private Long identityKey;

	private String status;
	private Boolean registration;
	
	private String token;
	private String url;

	public InvitationVO() {
		//make JAXB happy
	}
	
	public static InvitationVO valueOf(Invitation invitation, String url) {
		InvitationVO vo = new InvitationVO();
		vo.setKey(invitation.getKey());
		vo.setFirstName(invitation.getFirstName());
		vo.setLastName(invitation.getLastName());
		vo.setEmail(invitation.getMail());
		if(invitation.getIdentity() != null) {
			vo.setIdentityKey(invitation.getIdentity().getKey());
		}
		vo.setStatus(invitation.getStatus() == null ? null : invitation.getStatus().name());
		vo.setRegistration(Boolean.valueOf(invitation.isRegistration()));
		vo.setToken(invitation.getToken());
		vo.setUrl(url);
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getRegistration() {
		return registration;
	}

	public void setRegistration(Boolean registration) {
		this.registration = registration;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "InvitationVO[key=" + key + "]";
	}
	
	@Override
	public int hashCode() {
		return key == null ? 51315 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof InvitationVO invitation) {
			return key != null && key.equals(invitation.getKey());
		}
		return false;
	}
}
