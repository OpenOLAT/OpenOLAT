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
package org.olat.modules.catalog.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.model.RepositoryEntryInfos;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private CatalogQueries sut;
	
	@Before
	@After
	public void enableFreeAccessMethod() {
		acService.enableMethod(FreeAccessMethod.class, true);
	}
	
	@Test
	public void shouldLoadRepositoryEntries() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
	}
	@Test
	public void shouldLoadRepositoryEntries_exclude_private() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		repositoryManager.setAccess(catalogItem.getRepositoryEntry(), false, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_notInCatalog() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, false, null, null).stream()
				.forEach(offer -> {
					offer.setCatalogPublish(false);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_organisation() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null, null, JunitTestHelper.getDefaultActor());
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOfferOrganisations(List.of(organisation));
		
		assertThat(sut.loadRepositoryEntries(searchParams)).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_not_in_period() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		setOfferValid(catalogItem, null, null);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, -2, 2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, -2, null);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, null, 2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, 1, 1);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, 2, 4);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, -4, -2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, 2, null);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, null, -2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_status_period() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		setOfferValid(catalogItem, -2, 2);
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_status_no_period() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		setOfferValid(catalogItem, null, null);
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_no_offer() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, false, null, null).stream()
				.forEach(offer -> {
					acService.deleteOffer(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_guest_offer() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, false, null, null).stream()
				.forEach(offer -> {
					offer.setGuestAccess(true);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_disabled_method() {
		TestCatalogItem catalogItem = createCatalogItem(true);
		
		acService.enableMethod(FreeAccessMethod.class, false);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess_exclude_private() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		repositoryManager.setAccess(catalogItem.getRepositoryEntry(), false, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess_webPublish() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setWebPublish(true);
		
		assertThat(sut.loadRepositoryEntries(searchParams)).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, false, null, null).stream()
		.forEach(offer -> {
			offer.setCatalogWebPublish(true);
			acService.save(offer);
		});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(searchParams)).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess_exclude_organisation() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null,
				null, JunitTestHelper.getDefaultActor());
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOfferOrganisations(List.of(organisation));
		
		assertThat(sut.loadRepositoryEntries(searchParams)).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess_exclude_status() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		setOfferValid(catalogItem, null, null);
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForOpenAccess_exclude_no_offer() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, false, null, null).stream()
				.forEach(offer -> {
					acService.deleteOffer(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(catalogItem.getRepositoryEntry());
	}

	@Test
	public void shouldLoadRepositoryEntriesForGuests() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).contains(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_private() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		repositoryManager.setAccess(repositoryEntry, false, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_status() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		List<RepositoryEntryInfos> repositoryEntries = sut.loadRepositoryEntries(createGuestSearchParams());
		assertThat(repositoryEntries).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).contains(repositoryEntry);
		assertThat(repositoryEntries).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_no_offer() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		acService.getOffers(repositoryEntry, true, false, null, false, null, null).stream()
				.forEach(offer -> {
					acService.deleteOffer(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_no_guest_offer() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		acService.getOffers(repositoryEntry, true, false, null, false, null, null).stream()
				.forEach(offer -> {
					offer.setGuestAccess(false);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams())).map(RepositoryEntryInfos::entry).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_ResourceKeys() {
		TestCatalogItem catalogItem = createCatalogItem(3, true);
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2));
		
		searchParams.setResourceKeys(List.of(
				catalogItem.getRepositoryEntry(0).getOlatResource().getKey(),
				catalogItem.getRepositoryEntry(1).getOlatResource().getKey()));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_ResourceType() {
		TestCatalogItem catalogItem = createCatalogItem(3, true);
		String resourceType0 = catalogItem.getRepositoryEntry(0).getOlatResource().getResourceableTypeName();
		String resourceType1 = catalogItem.getRepositoryEntry(1).getOlatResource().getResourceableTypeName();
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setLauncherResourceTypes(List.of(resourceType0));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0));
		
		searchParams.setLauncherResourceTypes(List.of(resourceType0, resourceType1));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_EducationalType() {
		TestCatalogItem catalogItem = createCatalogItem(4, true);
		RepositoryEntryEducationalType educationalType1 = repositoryManager.createEducationalType(random());
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(0), random(), null, null, null, null, null, null, null, null, null, null, null, null, null, educationalType1);
		RepositoryEntryEducationalType educationalType2 = repositoryManager.createEducationalType(random());
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(1), random(), null, null, null, null, null, null, null, null, null, null, null, null, null, educationalType2);
		RepositoryEntryEducationalType educationalTypeOther = repositoryManager.createEducationalType(random());
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(2), random(), null, null, null, null, null, null, null, null, null, null, null, null, null, educationalTypeOther);
		dbInstance.commitAndCloseSession();
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setLauncherEducationalTypeKeys(List.of(educationalType1.getKey()));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0));
		
		searchParams.setLauncherEducationalTypeKeys(List.of(educationalType1.getKey(), educationalType2.getKey()));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_TaxonomyLevels() {
		TestCatalogItem catalogItem = createCatalogItem(5, true);
		
		Taxonomy taxonomy = taxonomyService.getTaxonomyList().get(0);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, taxonomyLevel1, taxonomy);
		TaxonomyLevel taxonomyLevel3 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(0), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(1), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1, taxonomyLevel2), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(2), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1, taxonomyLevel3), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(3), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel2), null);
		dbInstance.commitAndCloseSession();
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setLauncherTaxonomyLevels(List.of(taxonomyLevel1));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						catalogItem.getRepositoryEntry(3));
		
		searchParams.setLauncherTaxonomyLevels(List.of(taxonomyLevel1, taxonomyLevel2));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						catalogItem.getRepositoryEntry(3));
		
		searchParams.setLauncherTaxonomyLevels(List.of(taxonomyLevel2));
		assertThat(sut.loadRepositoryEntries(searchParams))
				.map(RepositoryEntryInfos::entry)
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(3));
	}
	
	@Test
	public void shouldLoadCurriculumElements() {
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_notInCatalog() {
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		acService.findOfferByResource(catalogItem.getCurriculumElement().getResource(), true, null, null).stream()
				.forEach(offer -> {
					offer.setCatalogPublish(false);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_organisation() {
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		Organisation organisation = organisationService.createOrganisation(random(), null, random(),
				null, null, JunitTestHelper.getDefaultActor());
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOfferOrganisations(List.of(organisation));
		
		assertThat(sut.loadCurriculumElements(searchParams)).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_not_in_period() {
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		setOfferValid(catalogItem, null, null);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, -2, 2);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, -2, null);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, null, 2);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, 1, 1);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, 2, 4);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, -4, -2);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, 2, null);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		setOfferValid(catalogItem, null, -2);
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_status_period() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		setOfferValid(catalogItem, -2, 2);
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.preparation, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.provisional, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.confirmed, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.active, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.cancelled, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.finished, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.deleted, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_status_no_period() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		setOfferValid(catalogItem, null, null);
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.preparation, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.provisional, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.confirmed, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.active, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).contains(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.cancelled, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.finished, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
		
		curriculumService.updateCurriculumElementStatus(doer, catalogItem.getCurriculumElement(), CurriculumElementStatus.deleted, false, null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_no_offer() {
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		acService.findOfferByResource(catalogItem.getCurriculumElement().getResource(), true, null, null).stream()
				.forEach(offer -> {
					acService.deleteOffer(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_guest_offer() {
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		acService.findOfferByResource(catalogItem.getCurriculumElement().getResource(), true, null, null).stream()
				.forEach(offer -> {
					offer.setGuestAccess(true);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_exclude_disabled_method() {
		TestCatalogItem catalogItem = createCatalogItem(false);
		
		acService.enableMethod(FreeAccessMethod.class, false);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadCurriculumElements(catalogItem.getSearchParams())).doesNotContain(catalogItem.getCurriculumElement());
	}
	
	@Test
	public void shouldLoadCurriculumElements_filterBy_ResourceKey() {
		TestCatalogItem catalogItem = createCatalogItem(3, false);
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0),
						catalogItem.getCurriculumElement(1),
						catalogItem.getCurriculumElement(2));
		
		searchParams.setResourceKeys(List.of(
				catalogItem.getCurriculumElement(0).getResource().getKey(),
				catalogItem.getCurriculumElement(1).getResource().getKey()));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0),
						catalogItem.getCurriculumElement(1));
	}
	
	@Test
	public void shouldLoadCurriculumElements_filterBy_ResourceType() {
		TestCatalogItem catalogItem = createCatalogItem(3, false);
		String resourceType0 = catalogItem.getCurriculumElement(0).getResource().getResourceableTypeName();
		String resourceType1 = catalogItem.getCurriculumElement(1).getResource().getResourceableTypeName();
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setLauncherResourceTypes(List.of(resourceType0));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0));
		
		searchParams.setLauncherResourceTypes(List.of(resourceType0, resourceType1));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0),
						catalogItem.getCurriculumElement(1));
	}
	
	@Test
	public void shouldLoadCurriculumElements_filterBy_EducationalType() {
		TestCatalogItem catalogItem = createCatalogItem(4, false);
		RepositoryEntryEducationalType educationalType1 = repositoryManager.createEducationalType(random());
		catalogItem.getCurriculumElement(0).setEducationalType(educationalType1);
		curriculumService.updateCurriculumElement(catalogItem.getCurriculumElement(0));
		RepositoryEntryEducationalType educationalType2 = repositoryManager.createEducationalType(random());
		catalogItem.getCurriculumElement(1).setEducationalType(educationalType2);
		curriculumService.updateCurriculumElement(catalogItem.getCurriculumElement(1));
		RepositoryEntryEducationalType educationalTypeOther = repositoryManager.createEducationalType(random());
		catalogItem.getCurriculumElement(2).setEducationalType(educationalTypeOther);
		curriculumService.updateCurriculumElement(catalogItem.getCurriculumElement(2));
		dbInstance.commitAndCloseSession();
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setLauncherEducationalTypeKeys(List.of(educationalType1.getKey()));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0));
		
		searchParams.setLauncherEducationalTypeKeys(List.of(educationalType1.getKey(), educationalType2.getKey()));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0),
						catalogItem.getCurriculumElement(1));
	}
	
	@Test
	public void shouldLoadCurriculumElements_filterBy_TaxonomyLevels() {
		TestCatalogItem catalogItem = createCatalogItem(5, false);
		
		Taxonomy taxonomy = taxonomyService.getTaxonomyList().get(0);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, taxonomyLevel1, taxonomy);
		TaxonomyLevel taxonomyLevel3 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		
		curriculumService.updateTaxonomyLevels(catalogItem.getCurriculumElement(0), List.of(taxonomyLevel1), null);
		curriculumService.updateTaxonomyLevels(catalogItem.getCurriculumElement(1), List.of(taxonomyLevel1, taxonomyLevel2), null);
		curriculumService.updateTaxonomyLevels(catalogItem.getCurriculumElement(2), List.of(taxonomyLevel1, taxonomyLevel3), null);
		curriculumService.updateTaxonomyLevels(catalogItem.getCurriculumElement(3), List.of(taxonomyLevel2), null);
		dbInstance.commitAndCloseSession();
		
		CatalogEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setLauncherTaxonomyLevels(List.of(taxonomyLevel1));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0),
						catalogItem.getCurriculumElement(1),
						catalogItem.getCurriculumElement(2),
						catalogItem.getCurriculumElement(3));
		
		searchParams.setLauncherTaxonomyLevels(List.of(taxonomyLevel1, taxonomyLevel2));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(0),
						catalogItem.getCurriculumElement(1),
						catalogItem.getCurriculumElement(2),
						catalogItem.getCurriculumElement(3));
		
		searchParams.setLauncherTaxonomyLevels(List.of(taxonomyLevel2));
		assertThat(sut.loadCurriculumElements(searchParams))
				.containsExactlyInAnyOrder(
						catalogItem.getCurriculumElement(1),
						catalogItem.getCurriculumElement(3));
	}
	
	private TestCatalogItem createCatalogItem(boolean repositoryEntryResource) {
		return createCatalogItem(1, repositoryEntryResource);
	}
	
	private TestCatalogItem createCatalogItem(int number, boolean repositoryEntryResource) {
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null,
				null, JunitTestHelper.getDefaultActor());
		List<Organisation> offerOrganisations = List.of(organisation);
		List<RepositoryEntry> repositoryEntries = new ArrayList<>(number);
		List<CurriculumElement> curriculumElements = new ArrayList<>(number);
		
		for (int i = 0; i < number; i++) {
			OLATResource olatResource;
			if (repositoryEntryResource) {
				RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
				olatResource = repositoryEntry.getOlatResource();
				repositoryEntry = repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime,
						false, false, false, false, null);
				repositoryEntry = repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
				repositoryEntries.add(repositoryEntry);
			} else {
				Curriculum curriculum = curriculumService.createCurriculum(null, random(), null, true, organisation);
				CurriculumElementType elementType = curriculumService.getDefaultCurriculumElementType();
				CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(),
						CurriculumElementStatus.active, null, null, null, elementType, CurriculumCalendars.enabled,
						CurriculumLectures.enabled, CurriculumLearningProgress.enabled, curriculum);
				if (curriculumElement instanceof CurriculumElementImpl impl) {
					impl.setResource(createOlatResource()); // random types to test filter
					curriculumElement = curriculumService.updateCurriculumElement(impl);
				}
				
				olatResource = curriculumElement.getResource();
				curriculumElements.add(curriculumElement);
			}
			
			Offer offer = acService.createOffer(olatResource, random());
			offer.setCatalogPublish(true);
			offer.setValidFrom(DateUtils.addDays(new Date(), -10));
			offer.setValidTo(DateUtils.addDays(new Date(), 10));
			offer = acService.save(offer);
			AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
			OfferAccess offerAccess = acService.createOfferAccess(offer, method);
			acService.saveOfferAccess(offerAccess);
			acService.updateOfferOrganisations(offer, offerOrganisations);
		}
		dbInstance.commitAndCloseSession();
		
		CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
		searchParams.setOfferOrganisations(offerOrganisations);
		searchParams.setOfferValidAtNow(false);
		searchParams.setOfferValidAt(new Date());
		return new TestCatalogItem(repositoryEntries, curriculumElements, searchParams);
	}
	
	private TestCatalogItem createOpenAccessCatalogItem() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), random());
		offer.setOpenAccess(true);
		offer.setCatalogPublish(true);
		offer = acService.save(offer);
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null,
				null, JunitTestHelper.getDefaultActor());
		List<Organisation> offerOrganisations = List.of(organisation);
		acService.updateOfferOrganisations(offer, offerOrganisations);
		dbInstance.commitAndCloseSession();
		
		CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
		searchParams.setOfferOrganisations(offerOrganisations);
		searchParams.setOfferValidAtNow(false);
		searchParams.setOfferValidAt(new Date());
		return new TestCatalogItem(List.of(repositoryEntry), List.of(), searchParams);
	}
	
	private void setOfferValid(TestCatalogItem catalogItem, Integer fromAddDays, Integer toAddDays) {
		acService.findOfferByResource(catalogItem.getResource(), true, null, null).stream()
				.forEach(offer -> {
					if (fromAddDays != null) {
						offer.setValidFrom(DateUtils.addDays(catalogItem.getSearchParams().getOfferValidAt(), fromAddDays.intValue()));
					} else {
						offer.setValidFrom(null);
					}
					if (toAddDays != null) {
						offer.setValidTo(DateUtils.addDays(catalogItem.getSearchParams().getOfferValidAt(), toAddDays.intValue()));
					} else {
						offer.setValidTo(null);
					}
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
	}
	
	private RepositoryEntry createRepositoryEntryForGuest() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), random());
		offer.setGuestAccess(true);
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();
		
		return repositoryEntry;
	}
	
	private CatalogEntrySearchParams createGuestSearchParams() {
		CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
		searchParams.setGuestOnly(true);
		return searchParams;
	}
	
	private OLATResource createOlatResource() {
		OLATResourceManager resourceManager = OLATResourceManager.getInstance();
		String resourceName = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resourceName, CodeHelper.getForeverUniqueID());
		OLATResource r =  resourceManager.createOLATResourceInstance(ores);
		resourceManager.saveOLATResource(r);
		return r;
	}

	private static final class TestCatalogItem {
		
		private final List<RepositoryEntry> repositoryEntries;
		private final List<CurriculumElement> curriculumElements;
		private final CatalogEntrySearchParams searchParams;
		
		public TestCatalogItem(List<RepositoryEntry> repositoryEntries, List<CurriculumElement> curriculumElements, CatalogEntrySearchParams searchParams) {
			this.repositoryEntries = repositoryEntries;
			this.curriculumElements = curriculumElements;
			this.searchParams = searchParams;
		}
		
		public RepositoryEntry getRepositoryEntry() {
			return repositoryEntries.get(0);
		}
		
		public RepositoryEntry getRepositoryEntry(int index) {
			return repositoryEntries.get(index);
		}
		
		public CurriculumElement getCurriculumElement() {
			return curriculumElements.get(0);
		}
		
		public CurriculumElement getCurriculumElement(int index) {
			return curriculumElements.get(index);
		}
		
		public OLATResource getResource() {
			if (repositoryEntries != null && !repositoryEntries.isEmpty()) {
				return repositoryEntries.get(0).getOlatResource();
			}
			return getCurriculumElement().getResource();
		}
		
		public CatalogEntrySearchParams getSearchParams() {
			return searchParams;
		}
		
	}


}
