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
package org.olat.modules.gotomeeting.model;

/**
 * {
 *  "access_token":"RlUe11faKeyCWxZToK3nk0uTKAL",
 *  "expires_in":"30758399",
 *  "refresh_token":"d1cp20yB3hrFAKeTokenTr49EZ34kTvNK",
 *  "organizer_key":"8439885694023999999",
 *  "account_key":"9999982253621659654",
 *  "account_type":"",
 *  "firstName":"Mahar",
 *  "lastName":"Singh",
 *  "email":"mahar.singh@singhSong.com",
 *  "platform":"GLOBAL",
 *  "version":"2",
 * }
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToOrganizerG2T {
	
	private String accessToken;
	private Long expiresIn;
	private String refreshToken;
	private String organizerKey;
	private String accountKey;
	private String accountType;
	private String firstName;
	private String lastName;
	private String email;
	private String platform;
	private String version;
	
	public GoToOrganizerG2T() {
		//
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public Long getExpiresIn() {
		return expiresIn;
	}
	
	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}
	
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public String getOrganizerKey() {
		return organizerKey;
	}
	
	public void setOrganizerKey(String organizerKey) {
		this.organizerKey = organizerKey;
	}
	
	public String getAccountKey() {
		return accountKey;
	}
	
	public void setAccountKey(String accountKey) {
		this.accountKey = accountKey;
	}
	
	public String getAccountType() {
		return accountType;
	}
	
	public void setAccountType(String accountType) {
		this.accountType = accountType;
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
	
	public String getPlatform() {
		return platform;
	}
	
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}
