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
package org.olat.modules.coach.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 02.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchCoachedIdentityParams {
	
	private String login;
	private Integer status; 
	private Long identityKey;
	private Map<String,String> userProperties;
	private List<OrganisationRef> organisations;
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Map<String, String> getUserProperties() {
		return userProperties;
	}
	
	public void setUserProperties(Map<String, String> userProperties) {
		this.userProperties = userProperties;
	}
	
	public boolean hasOrganisations() {
		return organisations != null && !organisations.isEmpty();
	}
	
	public List<OrganisationRef> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<? extends OrganisationRef> organisations) {
		if(organisations == null) {
			this.organisations = null;
		} else {
			this.organisations = new ArrayList<>(organisations);
		}
	}
}
