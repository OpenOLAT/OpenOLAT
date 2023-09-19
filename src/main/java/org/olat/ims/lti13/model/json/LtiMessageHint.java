/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.lti13.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Initial date: 6 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LtiMessageHint {
	
	@JsonProperty("identityKey")
	private Long identityKey;
	@JsonProperty("repositoryEntryKey")
	private Long repositoryEntryKey;
	@JsonProperty("businessGroupKey")
	private Long businessGroupKey;
	@JsonProperty("messageType")
	private String messageType;
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}
	
	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}
	
	public void setRepositoryEntryKey(Long repositoryEntryKey) {
		this.repositoryEntryKey = repositoryEntryKey;
	}
	
	public Long getBusinessGroupKey() {
		return businessGroupKey;
	}
	
	public void setBusinessGroupKey(Long businessGroupKey) {
		this.businessGroupKey = businessGroupKey;
	}
	
	public String getMessageType() {
		return messageType;
	}
	
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
}
