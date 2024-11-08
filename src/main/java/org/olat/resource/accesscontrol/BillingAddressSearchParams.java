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
package org.olat.resource.accesscontrol;

import java.util.Collection;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 1 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressSearchParams {

	private Collection<Long> billingAddressKeys;
	private Collection<Long> organisationKeys;
	private Collection<Long> identityKeys;
	private Boolean enabled;

	public Collection<Long> getBillingAddressKeys() {
		return billingAddressKeys;
	}

	public void setBillingAddressKeys(Collection<Long> billingAddressKeys) {
		this.billingAddressKeys = billingAddressKeys;
	}

	public void setBillingAddresses(Collection<BillingAddress> billingAddresses) {
		this.billingAddressKeys = billingAddresses != null? billingAddresses.stream().map(BillingAddress::getKey).toList(): null;
	}

	public Collection<Long> getOrganisationKeys() {
		return organisationKeys;
	}

	public void setOrganisations(Collection<? extends OrganisationRef> organisations) {
		organisationKeys = organisations != null? organisations.stream().map(OrganisationRef::getKey).toList(): null;
	}
	
	public Collection<Long> getIdentityKeys() {
		return identityKeys;
	}

	public void setIdentityKeys(Collection<? extends IdentityRef> identities) {
		identityKeys = identities != null? identities.stream().map(IdentityRef::getKey).toList(): null;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
}
