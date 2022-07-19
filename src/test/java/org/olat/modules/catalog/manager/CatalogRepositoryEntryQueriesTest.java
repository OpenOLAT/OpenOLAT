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
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.UserConstants;
import org.olat.core.util.DateUtils;
import org.olat.course.statistic.daily.DailyStat;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams.OrderBy;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryDataModel;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryDataModel.CatalogRepositoryEntryCols;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryLifecycleDAO reLifecycleDao;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private CatalogRepositoryEntryQueries sut;
	
	@Before
	@After
	public void enableFreeAccessMethod() {
		acService.enableMethod(FreeAccessMethod.class, true);
	}
	
	@Test
	public void shouldLoadRepositoryEntries() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
	}
	@Test
	public void shouldLoadRepositoryEntries_exclude_private() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		repositoryManager.setAccess(catalogItem.getRepositoryEntry(), false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_notInCatalog() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, null).stream()
				.forEach(offer -> {
					offer.setCatalogPublish(false);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_organisation() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null, null);
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOfferOrganisations(List.of(organisation));
		
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_not_in_period() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		setOfferValid(catalogItem, null, null);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, -2, 2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, -2, null);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, null, 2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, 2, 4);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, -4, -2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, 2, null);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		setOfferValid(catalogItem, null, -2);
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_status_period() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		setOfferValid(catalogItem, -2, 2);
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_status_no_period() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		setOfferValid(catalogItem, null, null);
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_no_offer() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, null).stream()
				.forEach(offer -> {
					acService.deleteOffer(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_guest_offer() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, null).stream()
				.forEach(offer -> {
					offer.setGuestAccess(true);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntries_exclude_disabled_method() {
		TestCatalogItem catalogItem = createCatalogItem();
		
		acService.enableMethod(FreeAccessMethod.class, false);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
	}
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess_exclude_private() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		repositoryManager.setAccess(catalogItem.getRepositoryEntry(), false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess_exclude_organisation() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null, null);
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOfferOrganisations(List.of(organisation));
		
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesOpenAccess_exclude_status() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		setOfferValid(catalogItem, null, null);
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).contains(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
		
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(), RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForOpenAccess_exclude_no_offer() {
		TestCatalogItem catalogItem = createOpenAccessCatalogItem();
		
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, null).stream()
				.forEach(offer -> {
					acService.deleteOffer(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(catalogItem.getSearchParams(), 0, -1)).doesNotContain(catalogItem.getRepositoryEntry());
	}

	@Test
	public void shouldLoadRepositoryEntriesForGuests() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).contains(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_private() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		repositoryManager.setAccess(repositoryEntry, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_status() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.review);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1);
		assertThat(repositoryEntries).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).contains(repositoryEntry);
		assertThat(repositoryEntries).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).contains(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).doesNotContain(repositoryEntry);
		
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_no_offer() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		acService.getOffers(repositoryEntry, true, false, null, null).stream()
				.forEach(offer -> {
					acService.deleteOffer(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntriesForGuests_exclude_no_guest_offer() {
		RepositoryEntry repositoryEntry = createRepositoryEntryForGuest();
		
		acService.getOffers(repositoryEntry, true, false, null, null).stream()
				.forEach(offer -> {
					offer.setGuestAccess(false);
					acService.save(offer);
				});
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(createGuestSearchParams(), 0, -1)).doesNotContain(repositoryEntry);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_RepositoryEntryKeys() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2));
		
		searchParams.setRepositoryEntries(List.of(catalogItem.getRepositoryEntry(0), catalogItem.getRepositoryEntry(1)));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_Author() {
		TestCatalogItem catalogItem = createCatalogItem(6);
		String authorName = random();
		
		// User first name
		Identity authorFirstName = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		authorFirstName.getUser().setProperty(UserConstants.FIRSTNAME, authorName);
		userManager.updateUserFromIdentity(authorFirstName);
		repositoryService.addRole(authorFirstName, catalogItem.getRepositoryEntry(0), GroupRoles.owner.name());
		// User last name
		Identity authorLastName = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		authorLastName.getUser().setProperty(UserConstants.LASTNAME, authorName);
		userManager.updateUserFromIdentity(authorLastName);
		repositoryService.addRole(authorLastName, catalogItem.getRepositoryEntry(1), GroupRoles.owner.name());
		// Identity name
		Identity identityName = JunitTestHelper.createAndPersistIdentityAsAuthor(authorName);
		repositoryService.addRole(identityName, catalogItem.getRepositoryEntry(2), GroupRoles.owner.name());
		// RepositoryEntry author
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(3), random(), null, null, null, authorName, null, null, null, null);
		// Not author role
		repositoryService.addRole(identityName, catalogItem.getRepositoryEntry(4), GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setAuthor(authorName.substring(1, authorName.length()-3));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						catalogItem.getRepositoryEntry(3))
				.doesNotContain(
						catalogItem.getRepositoryEntry(4),
						catalogItem.getRepositoryEntry(5));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_Status() {
		TestCatalogItem catalogItem = createCatalogItem(4);
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(0), RepositoryEntryStatusEnum.preparation);
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(1), RepositoryEntryStatusEnum.review);
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(2), RepositoryEntryStatusEnum.coachpublished);
		repositoryManager.setStatus(catalogItem.getRepositoryEntry(3), RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setStatus(List.of(RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(2),
						catalogItem.getRepositoryEntry(3));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_ResourceType() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		String resourceType0 = catalogItem.getRepositoryEntry(0).getOlatResource().getResourceableTypeName();
		String resourceType1 = catalogItem.getRepositoryEntry(1).getOlatResource().getResourceableTypeName();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.getIdentToResourceTypes().put("1", List.of(resourceType0));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0));
		
		searchParams.getIdentToResourceTypes().put("1", List.of(resourceType0, resourceType1));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1));
		
		searchParams.getIdentToResourceTypes().put("1", List.of(resourceType0, resourceType1));
		searchParams.getIdentToResourceTypes().put("2", List.of(resourceType1));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(1));
		
		searchParams.getIdentToResourceTypes().put("1", List.of(resourceType0));
		searchParams.getIdentToResourceTypes().put("2", List.of(resourceType1));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.isEmpty();
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_EducationalType() {
		TestCatalogItem catalogItem = createCatalogItem(4);
		RepositoryEntryEducationalType educationalType1 = repositoryManager.createEducationalType(random());
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(0), random(), null, null, null, null, null, null, null, null, null, null, null, null, null, educationalType1);
		RepositoryEntryEducationalType educationalType2 = repositoryManager.createEducationalType(random());
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(1), random(), null, null, null, null, null, null, null, null, null, null, null, null, null, educationalType2);
		RepositoryEntryEducationalType educationalTypeOther = repositoryManager.createEducationalType(random());
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(2), random(), null, null, null, null, null, null, null, null, null, null, null, null, null, educationalTypeOther);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.getIdentToEducationalTypeKeys().put("1", List.of(educationalType1.getKey()));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0));
		
		searchParams.getIdentToEducationalTypeKeys().put("1", List.of(educationalType1.getKey(), educationalType2.getKey()));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1));
		
		searchParams.getIdentToEducationalTypeKeys().put("1", List.of(educationalType1.getKey(), educationalType2.getKey()));
		searchParams.getIdentToEducationalTypeKeys().put("2", List.of(educationalType2.getKey()));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(1));
		
		searchParams.getIdentToEducationalTypeKeys().put("1", List.of(educationalType1.getKey()));
		searchParams.getIdentToEducationalTypeKeys().put("2", List.of(educationalType2.getKey()));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.isEmpty();
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_TaxonomyLevels() {
		TestCatalogItem catalogItem = createCatalogItem(5);
		
		Taxonomy taxonomy = taxonomyService.getTaxonomyList().get(0);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, taxonomyLevel1, taxonomy);
		TaxonomyLevel taxonomyLevel3 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(0), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(1), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1, taxonomyLevel2), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(2), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1, taxonomyLevel3), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(3), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel2), null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.getIdentToTaxonomyLevels().put("1", List.of(taxonomyLevel1));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						catalogItem.getRepositoryEntry(3));
		
		searchParams.getIdentToTaxonomyLevels().put("1", List.of(taxonomyLevel1, taxonomyLevel2));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						catalogItem.getRepositoryEntry(3));
		
		searchParams.getIdentToTaxonomyLevels().put("1", List.of(taxonomyLevel2));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(3));
		
		searchParams.getIdentToTaxonomyLevels().put("1", List.of(taxonomyLevel1));
		searchParams.getIdentToTaxonomyLevels().put("3", List.of(taxonomyLevel3));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(2));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_TaxonomyLevelChildren() {
		TestCatalogItem catalogItem = createCatalogItem(5);
		
		Taxonomy taxonomy = taxonomyService.getTaxonomyList().get(0);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, taxonomyLevel1, taxonomy);
		TaxonomyLevel taxonomyLevel3 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(0), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(1), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1, taxonomyLevel2), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(2), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel1, taxonomyLevel3), null);
		repositoryManager.setDescriptionAndName(catalogItem.getRepositoryEntry(3), random(), null, null, null, null, null, null, null, null, null, null, null, null, Set.of(taxonomyLevel2), null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setTaxonomyLevelChildren(true);
		searchParams.getIdentToTaxonomyLevels().put("1", List.of(taxonomyLevel1));
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						catalogItem.getRepositoryEntry(3));
		
		searchParams.setTaxonomyLevelChildren(false);
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_OpenAccess() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		TestCatalogItem openAccessCatalogItem = createOpenAccessCatalogItem();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		List<OrganisationRef> offerOrganisations = new ArrayList<>(2);
		offerOrganisations.addAll(searchParams.getOfferOrganisations());
		offerOrganisations.addAll(openAccessCatalogItem.getSearchParams().getOfferOrganisations());
		
		searchParams.setOpenAccess(null);
		searchParams.setOfferOrganisations(offerOrganisations);
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						openAccessCatalogItem.getRepositoryEntry(0));
		
		searchParams.setOpenAccess(Boolean.TRUE);
		searchParams.setOfferOrganisations(offerOrganisations);
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						openAccessCatalogItem.getRepositoryEntry(0));
		
		searchParams.setOpenAccess(Boolean.FALSE);
		searchParams.setOfferOrganisations(offerOrganisations);
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_ShowAcccessMethods() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		TestCatalogItem openAccessCatalogItem = createOpenAccessCatalogItem();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		List<OrganisationRef> offerOrganisations = new ArrayList<>(2);
		offerOrganisations.addAll(searchParams.getOfferOrganisations());
		offerOrganisations.addAll(openAccessCatalogItem.getSearchParams().getOfferOrganisations());
		
		searchParams.setOpenAccess(null);
		searchParams.setShowAccessMethods(null);
		searchParams.setOfferOrganisations(offerOrganisations);
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2),
						openAccessCatalogItem.getRepositoryEntry(0));
		
		searchParams.setShowAccessMethods(Boolean.FALSE);
		searchParams.setOfferOrganisations(offerOrganisations);
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						openAccessCatalogItem.getRepositoryEntry(0));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_filterBy_AcccessMethods() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		Offer offer = acService.getOffers(catalogItem.getRepositoryEntry(2), true, false, null, null).get(0);
		OfferAccess offerAccess = acService.getOfferAccess(offer, true).get(0);
		searchParams.setAccessMethods(List.of(offerAccess.getMethod()));
		
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1),
						catalogItem.getRepositoryEntry(2));
		
		acService.deleteOffer(offer);
		AccessMethod method = acService.getAvailableMethodsByType(TokenAccessMethod.class).get(0);
		Offer tokenOffer = acService.createOffer(catalogItem.getRepositoryEntry(2).getOlatResource(), random());
		acService.createOfferAccess(tokenOffer, method);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRepositoryEntries(searchParams, 0, -1))
				.containsExactlyInAnyOrder(
						catalogItem.getRepositoryEntry(0),
						catalogItem.getRepositoryEntry(1));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_Key() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		List<Long> sortedKeys = catalogItem.getRepositoryEntries().stream()
				.map(RepositoryEntry::getKey)
				.sorted()
				.collect(Collectors.toList());
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.key);
		searchParams.setOrderByAsc(false);
		List<RepositoryEntry> reverseRepositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(reverseRepositoryEntries.get(0).getKey()).isEqualTo(sortedKeys.get(2));
		assertThat(reverseRepositoryEntries.get(1).getKey()).isEqualTo(sortedKeys.get(1));
		assertThat(reverseRepositoryEntries.get(2).getKey()).isEqualTo(sortedKeys.get(0));
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_DisplayName() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, "Z1",null, null, null, null, null,  null, null, null, null, null, null, null, null, null);
		RepositoryEntry repositoryEntry2 = catalogItem.getRepositoryEntry(1);
		repositoryEntry2 = repositoryManager.setDescriptionAndName(repositoryEntry2, "2", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, "A49", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.displayName);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntry2,
				repositoryEntryA49,
				repositoryEntryZ1);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_ExternalId() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, null, null, null, null, null, "Z1", null, null, null);
		RepositoryEntry repositoryEntryNull = catalogItem.getRepositoryEntry(1);
		repositoryEntryNull = repositoryManager.setDescriptionAndName(repositoryEntryNull, null, null, null, null, null, null, null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, null, null, null, null, null, "A49", null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.externalId);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryA49,
				repositoryEntryZ1,
				repositoryEntryNull);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_ExternalRef() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, random(), "Z1", null, null, null, null, null, null, null, null, null, null, null, null, null);
		RepositoryEntry repositoryEntryNull = catalogItem.getRepositoryEntry(1);
		repositoryEntryNull = repositoryManager.setDescriptionAndName(repositoryEntryNull, random(), null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, random(), "A49", null, null, null, null, null, null, null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.externalRef);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryA49,
				repositoryEntryZ1,
				repositoryEntryNull);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_LifecycleLabel() {
		TestCatalogItem catalogItem = createCatalogItem(4);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create("Z1", null, false, null, null), null, null, null);
		RepositoryEntry repositoryEntryNull = catalogItem.getRepositoryEntry(1);
		repositoryEntryNull = repositoryManager.setDescriptionAndName(repositoryEntryNull, "B", null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, false, null, null), null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create("A49", null, false, null, null), null, null, null);
		RepositoryEntry repositoryEntryNoLifecycle = catalogItem.getRepositoryEntry(3);
		repositoryEntryNoLifecycle = repositoryManager.setDescriptionAndName(repositoryEntryNoLifecycle, "A", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.lifecycleLabel);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryA49,
				repositoryEntryZ1,
				repositoryEntryNoLifecycle,
				repositoryEntryNull);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_LifecycleSoftkey() {
		TestCatalogItem catalogItem = createCatalogItem(4);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, "Z1", false, null, null), null, null, null);
		RepositoryEntry repositoryEntryNull = catalogItem.getRepositoryEntry(1);
		repositoryEntryNull = repositoryManager.setDescriptionAndName(repositoryEntryNull, "B", null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, false, null, null), null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, "A49", false, null, null), null, null, null);
		RepositoryEntry repositoryEntryNoLifecycle = catalogItem.getRepositoryEntry(3);
		repositoryEntryNoLifecycle = repositoryManager.setDescriptionAndName(repositoryEntryNoLifecycle, "A", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.lifecycleSoftkey);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryA49,
				repositoryEntryZ1,
				repositoryEntryNoLifecycle,
				repositoryEntryNull);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_LifecycleStart() {
		TestCatalogItem catalogItem = createCatalogItem(4);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, true, DateUtils.addDays(new Date(), 2), null), null, null, null);
		RepositoryEntry repositoryEntryNull = catalogItem.getRepositoryEntry(1);
		repositoryEntryNull = repositoryManager.setDescriptionAndName(repositoryEntryNull, "B", null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, true, null, null), null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, true, DateUtils.addDays(new Date(), -2), null), null, null, null);
		RepositoryEntry repositoryEntryNoLifecycle = catalogItem.getRepositoryEntry(3);
		repositoryEntryNoLifecycle = repositoryManager.setDescriptionAndName(repositoryEntryNoLifecycle, "A", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.lifecycleStart);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryA49,
				repositoryEntryZ1,
				repositoryEntryNoLifecycle,
				repositoryEntryNull);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_LifecycleEnd() {
		TestCatalogItem catalogItem = createCatalogItem(4);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, true, null, DateUtils.addDays(new Date(), 2)), null, null, null);
		RepositoryEntry repositoryEntryNull = catalogItem.getRepositoryEntry(1);
		repositoryEntryNull = repositoryManager.setDescriptionAndName(repositoryEntryNull, "B", null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, true, null, null), null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, random(), null, null, null, null, null, null, null, null, null, null, reLifecycleDao.create(null, null, true, null, DateUtils.addDays(new Date(), -2)), null, null, null);
		RepositoryEntry repositoryEntryNoLifecycle = catalogItem.getRepositoryEntry(3);
		repositoryEntryNoLifecycle = repositoryManager.setDescriptionAndName(repositoryEntryNoLifecycle, "A", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.lifecycleEnd);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryA49,
				repositoryEntryZ1,
				repositoryEntryNoLifecycle,
				repositoryEntryNull);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_Location() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		RepositoryEntry repositoryEntryZ1 = catalogItem.getRepositoryEntry(0);
		repositoryEntryZ1 = repositoryManager.setDescriptionAndName(repositoryEntryZ1, random(), null, null, null, null, null, null, null, null, "Z1", null, null, null, null, null);
		RepositoryEntry repositoryEntryNull = catalogItem.getRepositoryEntry(1);
		repositoryEntryNull = repositoryManager.setDescriptionAndName(repositoryEntryNull, random(), null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		RepositoryEntry repositoryEntryA49 = catalogItem.getRepositoryEntry(2);
		repositoryEntryA49 = repositoryManager.setDescriptionAndName(repositoryEntryA49, random(), null, null, null, null, null, null, null, null, "A49", null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.location);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryA49,
				repositoryEntryZ1,
				repositoryEntryNull);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_PublishedDate() throws InterruptedException {
		TestCatalogItem catalogItem = createCatalogItem(3);
		RepositoryEntry repositoryEntryLastPublished = catalogItem.getRepositoryEntry(0);
		RepositoryEntry repositoryEntryFirstPublished = catalogItem.getRepositoryEntry(1);
		RepositoryEntry repositoryEntrySecondPublished = catalogItem.getRepositoryEntry(2);
		dbInstance.commitAndCloseSession();
		repositoryEntryFirstPublished = repositoryManager.setStatus(repositoryEntryFirstPublished, RepositoryEntryStatusEnum.preparation);
		repositoryEntryFirstPublished = repositoryManager.setStatus(repositoryEntryFirstPublished, RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		Thread.sleep(1000);
		repositoryEntrySecondPublished = repositoryManager.setStatus(repositoryEntrySecondPublished, RepositoryEntryStatusEnum.preparation);
		repositoryEntrySecondPublished = repositoryManager.setStatus(repositoryEntrySecondPublished, RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		Thread.sleep(1000);
		repositoryEntryLastPublished = repositoryManager.setStatus(repositoryEntryLastPublished, RepositoryEntryStatusEnum.preparation);
		repositoryEntryLastPublished = repositoryManager.setStatus(repositoryEntryLastPublished, RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.publishedDate);
		searchParams.setOrderByAsc(false);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryLastPublished,
				repositoryEntrySecondPublished,
				repositoryEntryFirstPublished);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_PopularCourse() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		RepositoryEntry repositoryEntryVeryPopular = catalogItem.getRepositoryEntry(0);
		RepositoryEntry repositoryEntryNotPopular = catalogItem.getRepositoryEntry(1);
		RepositoryEntry repositoryEntryPopular= catalogItem.getRepositoryEntry(2);
		addDailyStat(repositoryEntryVeryPopular, DateUtils.addDays(new Date(), -2), 10);
		addDailyStat(repositoryEntryVeryPopular, DateUtils.addDays(new Date(), -3), 10);
		addDailyStat(repositoryEntryVeryPopular, DateUtils.addDays(new Date(), -10), 10);
		addDailyStat(repositoryEntryNotPopular, DateUtils.addDays(new Date(), -4), 5);
		addDailyStat(repositoryEntryPopular, DateUtils.addDays(new Date(), -4), 20);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.popularCourses);
		searchParams.setOrderByAsc(false);
		List<RepositoryEntry> repositoryEntries = sut.loadRepositoryEntries(searchParams, 0, -1);
		
		assertThat(repositoryEntries).containsExactly(
				repositoryEntryVeryPopular,
				repositoryEntryPopular,
				repositoryEntryNotPopular);
	}
	
	private void addDailyStat(RepositoryEntry entry, Date day, int value) {
		DailyStat dailyStat = new DailyStat();
		dailyStat.setResId(entry.getKey());
		dailyStat.setBusinessPath(random());
		dailyStat.setDay(day);
		dailyStat.setValue(value);
		dbInstance.getCurrentEntityManager().persist(dailyStat);
	}
	
	@Test
	public void shouldLoadRepositoryEntries_orderBy_Random() {
		TestCatalogItem catalogItem = createCatalogItem(3);
		
		CatalogRepositoryEntrySearchParams searchParams = catalogItem.getSearchParams();
		searchParams.setOrderBy(OrderBy.random);
		sut.loadRepositoryEntries(searchParams, 0, -1);
		
		// Just a hql syntax test.
	}
	
	@Test
	public void shouldHaveOrderByForColumns() {
		for (CatalogRepositoryEntryCols col : CatalogRepositoryEntryDataModel.CatalogRepositoryEntryCols.values()) {
			if (col.sortable()) {
				OrderBy.valueOf(col.name());
				// Just no exception.
			}
		}
	}
	
	private TestCatalogItem createCatalogItem() {
		return createCatalogItem(1);
	}
	
	private TestCatalogItem createCatalogItem(int number) {
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null, null);
		List<Organisation> offerOrganisations = List.of(organisation);
		List<RepositoryEntry> repositoryEntries = new ArrayList<>(number);
		
		for (int i = 0; i < number; i++) {
			RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
			repositoryEntry = repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, null);
			repositoryEntry = repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
			repositoryEntries.add(repositoryEntry);
			
			Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), random());
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
		
		CatalogRepositoryEntrySearchParams searchParams = new CatalogRepositoryEntrySearchParams();
		searchParams.setOfferOrganisations(offerOrganisations);
		searchParams.setOfferValidAtNow(false);
		searchParams.setOfferValidAt(new Date());
		return new TestCatalogItem(repositoryEntries, searchParams);
	}
	
	private TestCatalogItem createOpenAccessCatalogItem() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), random());
		offer.setOpenAccess(true);
		offer.setCatalogPublish(true);
		offer = acService.save(offer);
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null, null);
		List<Organisation> offerOrganisations = List.of(organisation);
		acService.updateOfferOrganisations(offer, offerOrganisations);
		dbInstance.commitAndCloseSession();
		
		CatalogRepositoryEntrySearchParams searchParams = new CatalogRepositoryEntrySearchParams();
		searchParams.setOfferOrganisations(offerOrganisations);
		searchParams.setOfferValidAtNow(false);
		searchParams.setOfferValidAt(new Date());
		return new TestCatalogItem(List.of(repositoryEntry), searchParams);
	}
	
	private void setOfferValid(TestCatalogItem catalogItem, Integer fromAddDays, Integer toAddDays) {
		acService.getOffers(catalogItem.getRepositoryEntry(), true, false, null, null).stream()
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
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), random());
		offer.setGuestAccess(true);
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();
		
		return repositoryEntry;
	}
	
	private CatalogRepositoryEntrySearchParams createGuestSearchParams() {
		CatalogRepositoryEntrySearchParams searchParams = new CatalogRepositoryEntrySearchParams();
		searchParams.setGuestOnly(true);
		return searchParams;
	}

	private static final class TestCatalogItem {
		
		private final List<RepositoryEntry> repositoryEntries;
		private final CatalogRepositoryEntrySearchParams searchParams;
		
		public TestCatalogItem(List<RepositoryEntry> repositoryEntries, CatalogRepositoryEntrySearchParams searchParams) {
			this.repositoryEntries = repositoryEntries;
			this.searchParams = searchParams;
		}
		
		public RepositoryEntry getRepositoryEntry() {
			return repositoryEntries.get(0);
		}
		
		public RepositoryEntry getRepositoryEntry(int index) {
			return repositoryEntries.get(index);
		}

		public List<RepositoryEntry> getRepositoryEntries() {
			return repositoryEntries;
		}

		public CatalogRepositoryEntrySearchParams getSearchParams() {
			return searchParams;
		}
		
	}


}
