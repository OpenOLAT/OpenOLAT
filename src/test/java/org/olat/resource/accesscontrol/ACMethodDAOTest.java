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
package org.olat.resource.accesscontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Apr 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ACMethodDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private ACMethodDAO sut;

	@Test
	public void shouldGetAccessMethodForResources_filterByOrganisation() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AccessMethod method = sut.getAvailableMethods().get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, organisation1, null, JunitTestHelper.getDefaultActor());
		Organisation organisation4 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OLATResource olatResource = repositoryEntry.getOlatResource();
		Offer  offer1 = acService.save(acService.createOffer(olatResource, random()));
		sut.save(acService.createOfferAccess(offer1, method));
		dbInstance.commitAndCloseSession();

		List<Long> resourceKeys = List.of(olatResource.getKey());
		List<Organisation> organisations = List.of(organisation1, organisation2);
		
		// Offer has no organisation
		List<OLATResourceAccess> resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, organisations);
		assertThat(resourceAccesses).hasSize(1);
		assertThat(resourceAccesses.get(0).getMethods()).hasSize(1);
		
		// Offer in organisation
		acService.updateOfferOrganisations(offer1, List.of(organisation1, organisation4));
		dbInstance.commitAndCloseSession();
		resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, organisations);
		assertThat(resourceAccesses).hasSize(1);
		assertThat(resourceAccesses.get(0).getMethods()).hasSize(1);
		acService.updateOfferOrganisations(offer1, List.of(organisation2, organisation4));
		dbInstance.commitAndCloseSession();
		resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, organisations);
		assertThat(resourceAccesses).hasSize(1);
		assertThat(resourceAccesses.get(0).getMethods()).hasSize(1);
		
		// Offer in other organisation
		acService.updateOfferOrganisations(offer1, List.of(organisation4));
		dbInstance.commitAndCloseSession();
		resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, organisations);
		assertThat(resourceAccesses).isEmpty();
	}

	@Test
	public void shouldGetAccessMethodForResources_offerInMultipleMatchingOrgs_noDuplicates() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AccessMethod method = sut.getAvailableMethods().get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OLATResource olatResource = repositoryEntry.getOlatResource();
		Offer offer = acService.save(acService.createOffer(olatResource, random()));
		sut.save(acService.createOfferAccess(offer, method));
		acService.updateOfferOrganisations(offer, List.of(organisation1, organisation2));
		dbInstance.commitAndCloseSession();

		List<Long> resourceKeys = List.of(olatResource.getKey());
		List<Organisation> organisations = List.of(organisation1, organisation2);
		List<OLATResourceAccess> resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, organisations);

		assertThat(resourceAccesses).hasSize(1);
		assertThat(resourceAccesses.get(0).getMethods()).hasSize(1);
	}

	@Test
	public void shouldGetAccessMethodForResources_multipleOffersOnSameResource_filterPerOffer() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<AccessMethod> methods = sut.getAvailableMethods();
		AccessMethod method1 = methods.get(0);
		AccessMethod method2 = methods.size() > 1 ? methods.get(1) : methods.get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation3 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OLATResource olatResource = repositoryEntry.getOlatResource();
		Offer offer1 = acService.save(acService.createOffer(olatResource, random()));
		sut.save(acService.createOfferAccess(offer1, method1));
		acService.updateOfferOrganisations(offer1, List.of(organisation1));
		Offer offer2 = acService.save(acService.createOffer(olatResource, random()));
		sut.save(acService.createOfferAccess(offer2, method2));
		acService.updateOfferOrganisations(offer2, List.of(organisation3));
		dbInstance.commitAndCloseSession();

		List<Long> resourceKeys = List.of(olatResource.getKey());
		List<Organisation> organisations = List.of(organisation1);
		List<OLATResourceAccess> resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, organisations);

		assertThat(resourceAccesses).hasSize(1);
		assertThat(resourceAccesses.get(0).getMethods()).hasSize(1);
		assertThat(resourceAccesses.get(0).getMethods().get(0).getMethod()).isEqualTo(method1);
	}

	@Test
	public void shouldGetAccessMethodForResources_nullOrganisations_noFiltering() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AccessMethod method = sut.getAvailableMethods().get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OLATResource olatResource = repositoryEntry.getOlatResource();
		Offer offer = acService.save(acService.createOffer(olatResource, random()));
		sut.save(acService.createOfferAccess(offer, method));
		acService.updateOfferOrganisations(offer, List.of(organisation1));
		dbInstance.commitAndCloseSession();

		List<Long> resourceKeys = List.of(olatResource.getKey());
		List<OLATResourceAccess> resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, null);

		assertThat(resourceAccesses).hasSize(1);
	}

	@Test
	public void shouldGetAccessMethodForResources_emptyOrganisations_noFiltering() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AccessMethod method = sut.getAvailableMethods().get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OLATResource olatResource = repositoryEntry.getOlatResource();
		Offer offer = acService.save(acService.createOffer(olatResource, random()));
		sut.save(acService.createOfferAccess(offer, method));
		acService.updateOfferOrganisations(offer, List.of(organisation1));
		dbInstance.commitAndCloseSession();

		List<Long> resourceKeys = List.of(olatResource.getKey());
		List<OLATResourceAccess> resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, List.of());

		assertThat(resourceAccesses).hasSize(1);
	}

	@Test
	public void shouldGetAccessMethodForResources_multipleResources_filterByOrg() {
		RepositoryEntry repositoryEntry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry repositoryEntry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		AccessMethod method = sut.getAvailableMethods().get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation3 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OLATResource resource1 = repositoryEntry1.getOlatResource();
		OLATResource resource2 = repositoryEntry2.getOlatResource();
		Offer offer1 = acService.save(acService.createOffer(resource1, random()));
		sut.save(acService.createOfferAccess(offer1, method));
		acService.updateOfferOrganisations(offer1, List.of(organisation1));
		Offer offer2 = acService.save(acService.createOffer(resource2, random()));
		sut.save(acService.createOfferAccess(offer2, method));
		acService.updateOfferOrganisations(offer2, List.of(organisation3));
		dbInstance.commitAndCloseSession();

		List<Long> resourceKeys = List.of(resource1.getKey(), resource2.getKey());
		List<Organisation> organisations = List.of(organisation1);
		List<OLATResourceAccess> resourceAccesses = sut.getAccessMethodForResources(resourceKeys, null, null, true, null, organisations);

		assertThat(resourceAccesses).hasSize(1);
		assertThat(resourceAccesses.get(0).getResource().getKey()).isEqualTo(resource1.getKey());
	}

}
