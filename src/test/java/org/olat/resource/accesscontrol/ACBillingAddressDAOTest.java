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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.manager.ACBillingAddressDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ACBillingAddressDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private ACBillingAddressDAO sut;

	@Test
	public void shouldCreate() {
		Organisation organisation = JunitTestHelper.getDefaultOrganisation();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		
		BillingAddress billingAddress = sut.create(organisation,identity);
		dbInstance.commitAndCloseSession();
		
		assertThat(billingAddress.getCreationDate()).isNotNull();
		assertThat(billingAddress.getLastModified()).isNotNull();
		assertThat(billingAddress.isEnabled()).isTrue();
		assertThat(billingAddress.getOrganisation()).isEqualTo(organisation);
		assertThat(billingAddress.getIdentity()).isEqualTo(identity);
	}
	
	@Test
	public void shouldDeleteByKey() {
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null, null);
		BillingAddress billingaAddress1 = sut.create(organisation, null);
		BillingAddress billingaAddress2 = sut.create(organisation, null);
		BillingAddress billingaAddress3 = sut.create(organisation, null);
		dbInstance.commitAndCloseSession();
		
		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		searchParams.setOrganisations(List.of(organisation));
		List<BillingAddress> billingaAddresses = sut.loadBillingAddresses(searchParams);
		assertThat(billingaAddresses).containsExactlyInAnyOrder(billingaAddress1, billingaAddress2, billingaAddress3);
		
		sut.delete(billingaAddress1);
		dbInstance.commitAndCloseSession();
		
		billingaAddresses = sut.loadBillingAddresses(searchParams);
		assertThat(billingaAddresses).containsExactlyInAnyOrder(billingaAddress2, billingaAddress3);
	}
	
	@Test
	public void shouldFiler_organisations() {
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null, null);
		Organisation organisation3 = organisationService.createOrganisation(random(), null, null, null, null);
		BillingAddress billingaAddress11 = sut.create(organisation1, null);
		BillingAddress billingaAddress12 = sut.create(organisation1, null);
		BillingAddress billingaAddress21 = sut.create(organisation2, null);
		sut.create(organisation3, null);
		dbInstance.commitAndCloseSession();
		
		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		searchParams.setOrganisations(List.of(organisation1, organisation2));
		List<BillingAddress> billingaAddresses = sut.loadBillingAddresses(searchParams);
		
		assertThat(billingaAddresses).containsExactlyInAnyOrder(billingaAddress11, billingaAddress12, billingaAddress21);
	}
	
	@Test
	public void shouldFiler_identities() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		BillingAddress billingaAddress1 = sut.create(null, identity1);
		BillingAddress billingaAddress2 = sut.create(null, identity1);
		BillingAddress billingaAddress3 = sut.create(null, identity2);
		sut.create(null, identity3);
		dbInstance.commitAndCloseSession();
		
		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		searchParams.setIdentityKeys(List.of(identity1, identity2));
		List<BillingAddress> billingaAddresses = sut.loadBillingAddresses(searchParams);
		assertThat(billingaAddresses).containsExactlyInAnyOrder(billingaAddress1, billingaAddress2, billingaAddress3);
	}
	
	@Test
	public void shouldFiler_enabled() {
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null, null);
		BillingAddress billingaAddress1 = sut.create(organisation, null);
		BillingAddress billingaAddress2 = sut.create(organisation, null);
		billingaAddress2.setEnabled(false);
		billingaAddress2 = sut.update(billingaAddress2);
		dbInstance.commitAndCloseSession();
		
		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		searchParams.setOrganisations(List.of(organisation));
		assertThat(sut.loadBillingAddresses(searchParams)).containsExactlyInAnyOrder(billingaAddress1, billingaAddress2);
		
		searchParams.setEnabled(Boolean.TRUE);
		assertThat(sut.loadBillingAddresses(searchParams)).containsExactlyInAnyOrder(billingaAddress1);
		
		searchParams.setEnabled(Boolean.FALSE);
		assertThat(sut.loadBillingAddresses(searchParams)).containsExactlyInAnyOrder(billingaAddress2);
	}
	
}
