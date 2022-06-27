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
	public void shouldGetAccessMethodForResources_filterByOrganisation_() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AccessMethod method = sut.getAvailableMethods().get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, organisation1, null);
		Organisation organisation4 = organisationService.createOrganisation(random(), null, null, null, null);
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
	
}
