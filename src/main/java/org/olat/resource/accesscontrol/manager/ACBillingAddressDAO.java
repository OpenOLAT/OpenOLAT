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
package org.olat.resource.accesscontrol.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.BillingAddressSearchParams;
import org.olat.resource.accesscontrol.model.BillingAddressImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 30 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ACBillingAddressDAO {
	
	@Autowired
	private DB dbInstance;
	
	public BillingAddress create(Organisation organisation, Identity identity) {
		BillingAddressImpl billingAddress = new BillingAddressImpl();
		billingAddress.setCreationDate(new Date());
		billingAddress.setLastModified(billingAddress.getCreationDate());
		billingAddress.setOrganisation(organisation);
		billingAddress.setIdentity(identity);
		billingAddress.setEnabled(true);
		dbInstance.getCurrentEntityManager().persist(billingAddress);
		return billingAddress;
	}
	
	public BillingAddress update(BillingAddress billingAddress) {
		if (billingAddress instanceof BillingAddressImpl impl) {
			impl.setLastModified(new Date());
			return dbInstance.getCurrentEntityManager().merge(billingAddress);
		}
		return billingAddress;
	}
	
	public void delete(BillingAddress billingAddress) {
		String query = "delete from acbillingaddress billingaddress where billingaddress.key = :billingAddressKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("billingAddressKey", billingAddress.getKey())
				.executeUpdate();
	}
	
	public List<BillingAddress> loadBillingAddresses(BillingAddressSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select billingaddress");
		sb.append("  from acbillingaddress billingaddress");
		if (searchParams.getOrganisationKeys() != null && !searchParams.getOrganisationKeys().isEmpty()) {
			sb.and().append("billingaddress.organisation.key in :organisationKeys");
		}
		if (searchParams.getIdentityKeys() != null && !searchParams.getIdentityKeys().isEmpty()) {
			sb.and().append("billingaddress.identity.key in :identityKeys");
		}
		if (searchParams.getEnabled() != null) {
			sb.and().append("billingaddress.enabled = ").append(searchParams.getEnabled());
		}
		
		TypedQuery<BillingAddress> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BillingAddress.class);
		
		if (searchParams.getOrganisationKeys() != null && !searchParams.getOrganisationKeys().isEmpty()) {
			query.setParameter("organisationKeys", searchParams.getOrganisationKeys());
		}
		if (searchParams.getIdentityKeys() != null && !searchParams.getIdentityKeys().isEmpty()) {
			query.setParameter("identityKeys", searchParams.getIdentityKeys());
		}
		
		return query.getResultList();
	}

}
