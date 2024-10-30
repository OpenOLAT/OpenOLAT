/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.basesecurity;

import java.util.Collection;

import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 22 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationEmailDomainSearchParams {
	
	private Collection<Long> organisationKeys;
	private Collection<String> domains;
	private Boolean enabled;

	public Collection<Long> getOrganisationKeys() {
		return organisationKeys;
	}

	public void setOrganisations(Collection<? extends OrganisationRef> organisations) {
		organisationKeys = organisations != null? organisations.stream().map(OrganisationRef::getKey).toList(): null;
	}

	public Collection<String> getDomains() {
		return domains;
	}

	public void setDomains(Collection<String> domains) {
		this.domains = domains;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
}
