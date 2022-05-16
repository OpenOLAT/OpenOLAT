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
import static org.olat.test.JunitTestHelper.createRandomResource;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.manager.ACOfferToOrganisationDAO;
import org.olat.resource.accesscontrol.model.OfferToOrganisationImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Apr 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ACOfferToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACFrontendManager acFrontendManager;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private ACOfferToOrganisationDAO sut;
	
	@Test
	public void shouldCreateRelation() {
		Offer offer = acFrontendManager.createOffer(createRandomResource(), random());
		offer = acFrontendManager.save(offer);
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null,null);
		dbInstance.commitAndCloseSession();
		
		OfferToOrganisation offerToOrganisation = sut.createRelation(offer, organisation);
		dbInstance.commitAndCloseSession();
		
		assertThat(((OfferToOrganisationImpl)offerToOrganisation).getCreationDate()).isNotNull();
		assertThat(((OfferToOrganisationImpl)offerToOrganisation).getLastModified()).isNotNull();
		assertThat(offerToOrganisation.getOffer()).isEqualTo(offer);
		assertThat(offerToOrganisation.getOrganisation()).isEqualTo(organisation);
	}
	
	@Test
	public void shouldLoadRelationsByOfferOrgOrganistion() {
		Offer offer1 = acFrontendManager.createOffer(createRandomResource(), random());
		offer1 = acFrontendManager.save(offer1);
		Offer offer2 = acFrontendManager.createOffer(createRandomResource(), random());
		offer2 = acFrontendManager.save(offer2);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		OfferToOrganisation offerToOrganisation11 = sut.createRelation(offer1, organisation1);
		OfferToOrganisation offerToOrganisation12 = sut.createRelation(offer1, organisation2);
		sut.createRelation(offer2, organisation2);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(offer1, null)).hasSize(2).containsExactlyInAnyOrder(offerToOrganisation11, offerToOrganisation12);
		assertThat(sut.loadRelations(null, organisation1)).hasSize(1).containsExactlyInAnyOrder(offerToOrganisation11);
		assertThat(sut.loadRelations(offer1, organisation2)).hasSize(1).containsExactlyInAnyOrder(offerToOrganisation12);
	}
	
	@Test
	public void shouldLoadRelationsByOffers() {
		Offer offer1 = acFrontendManager.createOffer(createRandomResource(), random());
		offer1 = acFrontendManager.save(offer1);
		Offer offer2 = acFrontendManager.createOffer(createRandomResource(), random());
		offer2 = acFrontendManager.save(offer2);
		Offer offer3 = acFrontendManager.createOffer(createRandomResource(), random());
		offer3 = acFrontendManager.save(offer3);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		OfferToOrganisation offerToOrganisation11 = sut.createRelation(offer1, organisation1);
		OfferToOrganisation offerToOrganisation12 = sut.createRelation(offer1, organisation2);
		OfferToOrganisation offerToOrganisation21 = sut.createRelation(offer2, organisation1);
		sut.createRelation(offer3, organisation2);
		dbInstance.commitAndCloseSession();
		
		List<OfferToOrganisation> relations = sut.loadRelations(List.of(offer1,  offer2));
		
		assertThat(relations).containsExactlyInAnyOrder(
				offerToOrganisation11,
				offerToOrganisation12,
				offerToOrganisation21
				);
	}
	
	@Test
	public void shouldLoadOrganisationsByOffer() {
		Offer offer1 = acFrontendManager.createOffer(createRandomResource(), random());
		offer1 = acFrontendManager.save(offer1);
		Offer offer2 = acFrontendManager.createOffer(createRandomResource(), random());
		offer2 = acFrontendManager.save(offer2);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		sut.createRelation(offer1, organisation1);
		sut.createRelation(offer1, organisation2);
		sut.createRelation(offer2, organisation2);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadOrganisations(offer1)).containsExactlyInAnyOrder(organisation1, organisation2);
		assertThat(sut.loadOrganisations(offer2)).containsExactlyInAnyOrder(organisation2);
	}
	
	@Test
	public void shouldDeleteReleation() {
		Offer offer1 = acFrontendManager.createOffer(createRandomResource(), random());
		offer1 = acFrontendManager.save(offer1);
		Offer offer2 = acFrontendManager.createOffer(createRandomResource(), random());
		offer2 = acFrontendManager.save(offer2);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		OfferToOrganisation offerToOrganisation11 = sut.createRelation(offer1, organisation1);
		OfferToOrganisation offerToOrganisation12 = sut.createRelation(offer1, organisation2);
		OfferToOrganisation offerToOrganisation21 = sut.createRelation(offer2, organisation2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(offerToOrganisation12);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(offer1, null)).containsExactlyInAnyOrder(offerToOrganisation11);
		assertThat(sut.loadRelations(offer2, null)).containsExactlyInAnyOrder(offerToOrganisation21);
	}
	
	@Test
	public void shouldDeleteByOffer() {
		Offer offer1 = acFrontendManager.createOffer(createRandomResource(), random());
		offer1 = acFrontendManager.save(offer1);
		Offer offer2 = acFrontendManager.createOffer(createRandomResource(), random());
		offer2 = acFrontendManager.save(offer2);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		sut.createRelation(offer1, organisation1);
		sut.createRelation(offer1, organisation2);
		OfferToOrganisation offerToOrganisation21 = sut.createRelation(offer2, organisation2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(offer1);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(offer1, null)).isEmpty();
		assertThat(sut.loadRelations(offer2, null)).containsExactlyInAnyOrder(offerToOrganisation21);
	}

}
