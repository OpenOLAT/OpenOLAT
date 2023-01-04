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
package org.olat.repository.manager;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Check if the query with the sort argument are "playable" but don't
 * check if the order by goes in the right direction.
 * 
 * Initial date: 04.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryMyCourseQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private RepositoryEntryMyCourseQueries repositoryEntryMyCourseViewQueries;
	@Autowired
	private ACService acService;
	@Autowired
	private OrganisationService organisationService;
	
	@Before
	@After
	public void setUp() {
		acService.enableMethod(FreeAccessMethod.class, true);
	}
	@Test
	public void searchViews() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-1-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchMyRepositoryEntryViewParams params
			= new SearchMyRepositoryEntryViewParams(id, roles);
		params.setMarked(Boolean.TRUE);
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, 10);
		Assert.assertNotNull(views);
		
	}
	
	/**
	 * Check only the syntax of the order by in the query.
	 */
	@Test
	public void searchViews_orderBy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-2-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchMyRepositoryEntryViewParams params
			= new SearchMyRepositoryEntryViewParams(id, roles);
		params.setMarked(Boolean.TRUE);
		
		for(OrderBy orderBy:OrderBy.values()) {
			params.setOrderBy(orderBy);
			params.setOrderByAsc(true);
			List<RepositoryEntryMyView> viewAsc = repositoryEntryMyCourseViewQueries.searchViews(params, 0, 10);
			Assert.assertNotNull(viewAsc);
			params.setOrderByAsc(false);
			List<RepositoryEntryMyView> viewDesc = repositoryEntryMyCourseViewQueries.searchViews(params, 0, 10);
			Assert.assertNotNull(viewDesc);
		}
	}
	
	/**
	 * Check only the syntax of the filter statements..
	 */
	@Test
	public void searchViews_filter() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-2-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchMyRepositoryEntryViewParams params
			= new SearchMyRepositoryEntryViewParams(id, roles);
		params.setMarked(Boolean.TRUE);
		
		for(Filter filter:Filter.values()) {
			params.setFilters(Arrays.asList(filter));
			List<RepositoryEntryMyView> viewAsc = repositoryEntryMyCourseViewQueries.searchViews(params, 0, 10);
			Assert.assertNotNull(viewAsc);
		}
	}

	
	/**
	 * Check the search parameter mandatory membership.
	 */
	@Test
	public void searchViews_membershipMandatory() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-3-");
		RepositoryEntry reMember = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reMember, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, reMember, GroupRoles.participant.name());
		RepositoryEntry reNotMember = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotMember, RepositoryEntryStatusEnum.published);
		
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		params.setMembershipMandatory(true);

		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reMember, views));
		Assert.assertFalse(contains(reNotMember, views));
	}
	
	/**
	 * Check the search parameter id.
	 */
	@Test
	public void searchViews_idAndRefs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-3-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(re, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		RepositoryEntry otherRe = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(otherRe, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		params.setIdAndRefs(re.getKey().toString());

		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(re, views));
		Assert.assertFalse(contains(otherRe, views));
	}
	
	/**
	 * Check the search parameter resource types.
	 */
	@Test
	public void searchViews_resourceTypes() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-3-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(re, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		RepositoryEntry otherRe = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(otherRe, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, otherRe, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		List<String> types = new ArrayList<>();
		types.add(re.getOlatResource().getResourceableTypeName());
		params.setResourceTypes(types);
		
		// check with a type
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(re, views));
		Assert.assertFalse(contains(otherRe, views));
		
		// add an other type
		params.addResourceTypes(otherRe.getOlatResource().getResourceableTypeName());
		
		// check with a type
		List<RepositoryEntryMyView> viewsMore = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(re, viewsMore));
		Assert.assertTrue(contains(otherRe, viewsMore));
	}
	
	/**
	 * Check the visibility of entries as a simple user.
	 */
	@Test
	public void searchViews_status_asUsers() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertFalse(contains(reReview, views));
		Assert.assertFalse(contains(reCoachPublished, views));
		Assert.assertFalse(contains(rePublished, views));
		Assert.assertFalse(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
	}
	
	/**
	 * Check the visibility of entries per status as participant.
	 */
	@Test
	public void searchViews_status_asParticipant() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		repositoryEntryRelationDao.addRole(id, rePreparation, GroupRoles.participant.name());
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		repositoryEntryRelationDao.addRole(id, reReview, GroupRoles.participant.name());
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		repositoryEntryRelationDao.addRole(id, reCoachPublished, GroupRoles.participant.name());
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, rePublished, GroupRoles.participant.name());
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		repositoryEntryRelationDao.addRole(id, reClosed, GroupRoles.participant.name());
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		repositoryEntryRelationDao.addRole(id, reTrash, GroupRoles.participant.name());
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		repositoryEntryRelationDao.addRole(id, reDeleted, GroupRoles.participant.name());
		
		dbInstance.commitAndCloseSession();
		
		// as members
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		params.setMembershipMandatory(true);
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(rePreparation, views));
		Assert.assertTrue(contains(reReview, views));
		Assert.assertTrue(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
		
		// as
		SearchMyRepositoryEntryViewParams paramsForAll = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());

		List<RepositoryEntryMyView> viewsForAll = repositoryEntryMyCourseViewQueries.searchViews(paramsForAll, 0, -1);
		Assert.assertTrue(contains(rePreparation, viewsForAll));
		Assert.assertTrue(contains(reReview, viewsForAll));
		Assert.assertTrue(contains(reCoachPublished, viewsForAll));
		Assert.assertTrue(contains(rePublished, viewsForAll));
		Assert.assertTrue(contains(reClosed, viewsForAll));
		Assert.assertFalse(contains(reTrash, viewsForAll));
		Assert.assertFalse(contains(reDeleted, viewsForAll));
	}
	
	/**
	 * Check the visibility of entries per status as coach.
	 */
	@Test
	public void searchViews_status_asCoaches() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-6-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, rePreparation, GroupRoles.coach.name());
		repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reReview, GroupRoles.coach.name());
		repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reCoachPublished, GroupRoles.coach.name());
		repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, rePublished, GroupRoles.coach.name());
		repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reClosed, GroupRoles.coach.name());
		repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reTrash, GroupRoles.coach.name());
		repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reDeleted, GroupRoles.coach.name());
		repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		
		dbInstance.commitAndCloseSession();
		
		// as coaches
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		params.setMembershipMandatory(true);
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(rePreparation, views));
		Assert.assertTrue(contains(reReview, views));
		Assert.assertTrue(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
		
		// with mandatory membership 
		SearchMyRepositoryEntryViewParams paramsForAll = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());

		List<RepositoryEntryMyView> viewsForAll = repositoryEntryMyCourseViewQueries.searchViews(paramsForAll, 0, -1);
		Assert.assertTrue(contains(rePreparation, viewsForAll));
		Assert.assertTrue(contains(reReview, viewsForAll));
		Assert.assertTrue(contains(reCoachPublished, viewsForAll));
		Assert.assertTrue(contains(rePublished, viewsForAll));
		Assert.assertTrue(contains(reClosed, viewsForAll));
		Assert.assertFalse(contains(reTrash, viewsForAll));
		Assert.assertFalse(contains(reDeleted, viewsForAll));
	}
	
	/**
	 * Check if entries with the review are visible to coaches.
	 */
	@Test
	public void searchViews_review_asCoaches() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-7-");
		RepositoryEntry reNotCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotCoach, RepositoryEntryStatusEnum.review);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reCoach, RepositoryEntryStatusEnum.review);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reCoach, views));
		Assert.assertFalse(contains(reNotCoach, views));
	}
	
	/**
	 * Check if entries with the coach published status are visible to coaches.
	 */
	@Test
	public void searchViews_coachPublished_asCoaches() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-8-");
		RepositoryEntry reNotCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotCoach, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reCoach, RepositoryEntryStatusEnum.coachpublished);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reCoach, views));
		Assert.assertFalse(contains(reNotCoach, views));
	}
	
	/**
	 * Check if entries with the published status are visible to coaches.
	 */
	@Test
	public void searchViews_published_asCoaches() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-9-");
		RepositoryEntry reNotCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotCoach, RepositoryEntryStatusEnum.published);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reCoach, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reCoach, views));
		Assert.assertFalse(contains(reNotCoach, views));
	}
	
	/**
	 * Check if entries with the closed status are visible to coaches.
	 */
	@Test
	public void searchViews_closed_asCoaches() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-10-");
		RepositoryEntry reNotCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotCoach, RepositoryEntryStatusEnum.closed);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reCoach, RepositoryEntryStatusEnum.closed);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reCoach, views));
		Assert.assertFalse(contains(reNotCoach, views));
	}
	
	/**
	 * Check if entries with the trash status are visible to coaches.
	 */
	@Test
	public void searchViews_trash_asCoaches() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-10-");
		RepositoryEntry reNotCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotCoach, RepositoryEntryStatusEnum.trash);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reCoach, RepositoryEntryStatusEnum.trash);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(reCoach, views));
		Assert.assertFalse(contains(reNotCoach, views));
	}
	
	@Test
	public void searchViews_offer_bookable() {
		AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Organisation> reOrgs = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		List<Organisation> offerOrganisations = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		
		// Not bookable
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		re = repositoryManager.setStatus(re,  RepositoryEntryStatusEnum.published);
		re = repositoryManager.setAccess(re, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, reOrgs);
		Offer offer = acService.createOffer(re.getOlatResource(), random());
		OfferAccess offerAccess = acService.createOfferAccess(offer, method);
		acService.saveOfferAccess(offerAccess);
		acService.updateOfferOrganisations(offer, offerOrganisations);
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.userRoles());
		params.setOfferOrganisations(offerOrganisations);
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).isEmpty();
		
		// Bookable
		re = repositoryManager.setAccess(re, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, reOrgs);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).extracting(RepositoryEntryMyView::getKey).containsExactlyInAnyOrder(re.getKey());
	}
	
	@Test
	public void searchViews_offer_status() {
		AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Organisation> reOrgs = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		List<Organisation> offerOrganisations = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		Date now = new Date();
		Date inPast = DateUtils.addDays(now, -2);
		Date inFuture = DateUtils.addDays(now, 2);
		
		// Offer without date has to be published
		RepositoryEntry rePreparation = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.preparation, inPast, inFuture);
		RepositoryEntry reReview = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.review, inPast, inFuture);
		RepositoryEntry reCoachPublished = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.coachpublished, inPast, inFuture);
		RepositoryEntry rePublished = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, inPast, inFuture);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.closed, inPast, inFuture);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.trash, inPast, inFuture);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.deleted, inPast, inFuture);
		// Offer with date has to be prepared to published
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.preparation, null, null);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.review, null, null);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.coachpublished, null, null);
		RepositoryEntry rePublishedNoDates = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, null, null);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.closed, null, null);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.trash, null, null);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.deleted, null, null);
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.userRoles());
		params.setOfferOrganisations(offerOrganisations);
		params.setOfferValidAt(now);
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).extracting(RepositoryEntryMyView::getKey).containsExactlyInAnyOrder(
				rePreparation.getKey(),
				reReview.getKey(),
				reCoachPublished.getKey(),
				rePublished.getKey(),
				rePublishedNoDates.getKey());
	}
	
	@Test
	public void searchViews_offer_organisation() {
		AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Organisation> reOrgs = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), organisation1, null);
		Organisation otherOganisation = organisationService.createOrganisation(random(), null, random(), null, null);
		
		RepositoryEntry reOfferOrg1 = createReOffer(method, reOrgs, singletonList(organisation1), RepositoryEntryStatusEnum.published, null, null);
		RepositoryEntry reOfferOrg2 = createReOffer(method, reOrgs, singletonList(organisation2), RepositoryEntryStatusEnum.published, null, null);
		createReOffer(method, reOrgs, singletonList(otherOganisation), RepositoryEntryStatusEnum.published, null, null);
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.userRoles());
		params.setOfferOrganisations(List.of(organisation1, organisation2));
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).extracting(RepositoryEntryMyView::getKey).containsExactlyInAnyOrder(
				reOfferOrg1.getKey(),
				reOfferOrg2.getKey());
	}
	
	@Test
	public void searchViews_offer_period() {
		AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Organisation> reOrgs = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		List<Organisation> offerOrganisations = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		Date now = new Date();
		Date inPast = DateUtils.addDays(now, -2);
		Date inFuture = DateUtils.addDays(now, 2);
		
		RepositoryEntry reNoDates = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, null, null);
		RepositoryEntry reFromInPast = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, inPast, null);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, inFuture, null);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, null, inPast);
		RepositoryEntry reToInFuture = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, null, inFuture);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, DateUtils.addDays(inPast, -2), inPast);
		RepositoryEntry reToInRange = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, inPast, inFuture);
		createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, inFuture, DateUtils.addDays(inFuture, 2));
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.userRoles());
		params.setOfferOrganisations(offerOrganisations);
		params.setOfferValidAt(now);
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).extracting(RepositoryEntryMyView::getKey).containsExactlyInAnyOrder(
				reNoDates.getKey(),
				reFromInPast.getKey(),
				reToInFuture.getKey(),
				reToInRange.getKey());
	}
	
	@Test
	public void searchViews_offer_method() {
		AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Organisation> reOrgs = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		List<Organisation> offerOrganisations = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		RepositoryEntry re = createReOffer(method, reOrgs, offerOrganisations, RepositoryEntryStatusEnum.published, null, null);
		dbInstance.commitAndCloseSession();
		
		// Method enabled
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.userRoles());
		params.setOfferOrganisations(offerOrganisations);
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).extracting(RepositoryEntryMyView::getKey).containsExactlyInAnyOrder(re.getKey());
		
		// Method disabled
		acService.enableMethod(FreeAccessMethod.class, false);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).isEmpty();
		
		// Method enabled
		acService.enableMethod(FreeAccessMethod.class, true);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).extracting(RepositoryEntryMyView::getKey).containsExactlyInAnyOrder(re.getKey());
	}
	
	private RepositoryEntry createReOffer(AccessMethod method, List<Organisation> reOrgs,
			List<Organisation> offerOrganisations, RepositoryEntryStatusEnum status, Date validFrom, Date validTo) {
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry();
		rePreparation = repositoryManager.setStatus(rePreparation, status);
		rePreparation = repositoryManager.setAccess(rePreparation, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, reOrgs);
		Offer offer = acService.createOffer(rePreparation.getOlatResource(), random());
		offer.setValidFrom(validFrom);
		offer.setValidTo(validTo);
		OfferAccess offerAccess = acService.createOfferAccess(offer, method);
		acService.saveOfferAccess(offerAccess);
		acService.updateOfferOrganisations(offer, offerOrganisations);
		return rePreparation;
	}
	
	@Test
	public void searchViews_openaccess() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<Organisation> offerOrganisations = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		dbInstance.commitAndCloseSession();
		
		// Open access enabled, repository entry public visible, status published
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), random());
		offer.setOpenAccess(true);
		offer = acService.save(offer);
		acService.updateOfferOrganisations(offer, offerOrganisations);
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.userRoles());
		params.setOfferOrganisations(offerOrganisations);
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).contains(repositoryEntry.getKey());
		
		// repository entry not public visible
		repositoryManager.setAccess(repositoryEntry, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).doesNotContain(repositoryEntry.getKey());
		
		// repository entry not published
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).doesNotContain(repositoryEntry.getKey());

		// Open access not enabled
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		offer.setOpenAccess(false);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).doesNotContain(repositoryEntry.getKey());
	}
	
	@Test
	public void searchViews_openaccess_organisations() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Organisation> reOrgs = singletonList(organisationService.createOrganisation(random(), null, random(), null, null));
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), organisation1, null);
		Organisation otherOganisation = organisationService.createOrganisation(random(), null, random(), null, null);
		
		RepositoryEntry reOfferOrg1 = createReOpenAccess(reOrgs, singletonList(organisation1));
		RepositoryEntry reOfferOrg2 = createReOpenAccess(reOrgs, singletonList(organisation2));
		createReOpenAccess(reOrgs, singletonList(otherOganisation));
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.userRoles());
		params.setOfferOrganisations(List.of(organisation1, organisation2));
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		
		assertThat(views).extracting(RepositoryEntryMyView::getKey).containsExactlyInAnyOrder(
				reOfferOrg1.getKey(),
				reOfferOrg2.getKey());
	}
	
	private RepositoryEntry createReOpenAccess(List<Organisation> reOrgs, List<Organisation> offerOrganisations) {
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry();
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.published);
		rePreparation = repositoryManager.setAccess(rePreparation, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, reOrgs);
		Offer offer = acService.createOffer(rePreparation.getOlatResource(), random());
		offer.setOpenAccess(true);
		offer = acService.save(offer);
		acService.updateOfferOrganisations(offer, offerOrganisations);
		return rePreparation;
	}
	
	@Test
	public void searchViews_guest() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		// Guest enabled, repository entry public visible, status published
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), random());
		offer.setGuestAccess(true);
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, Roles.guestRoles());
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).contains(repositoryEntry.getKey());
		
		// repository entry not public visible
		repositoryManager.setAccess(repositoryEntry, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).doesNotContain(repositoryEntry.getKey());
		
		// repository entry not published
		repositoryManager.setAccess(repositoryEntry, true, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, false, false, null);
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.coachpublished);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).doesNotContain(repositoryEntry.getKey());

		// guest not enabled
		repositoryManager.setStatus(repositoryEntry, RepositoryEntryStatusEnum.published);
		offer.setGuestAccess(false);
		dbInstance.commitAndCloseSession();
		
		views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		assertThat(views).extracting(RepositoryEntryMyView::getKey).doesNotContain(repositoryEntry.getKey());
	}
	
	@Test
	public void searchViews_assessment_values() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-11-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(re, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		
		AssessmentEntry rootAE = assessmentService.getOrCreateAssessmentEntry(id, null, re, random(), Boolean.TRUE, null);
		rootAE.setPassed(Boolean.TRUE);
		rootAE.setScore(BigDecimal.valueOf(0.9));
		rootAE.setCompletion(Double.valueOf(0.8));
		rootAE = assessmentService.updateAssessmentEntry(rootAE);
		AssessmentEntry childAE = assessmentService.getOrCreateAssessmentEntry(id, null, re, random(), Boolean.FALSE, null);
		childAE.setPassed(Boolean.TRUE);
		childAE.setScore(BigDecimal.valueOf(0.3));
		childAE.setCompletion(Double.valueOf(0.2));
		childAE = assessmentService.updateAssessmentEntry(childAE);
		dbInstance.closeSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		params.setIdAndRefs(re.getKey().toString());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		RepositoryEntryMyView view = views.get(0);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(view.getPassed()).isTrue();
		softly.assertThat(view.getScore()).isEqualTo(0.9f, Offset.offset(0.001f));
		softly.assertThat(view.getCompletion()).isEqualTo(0.8, Offset.offset(0.001));
		softly.assertAll();
	}
	
	private final boolean contains(RepositoryEntry re, List<RepositoryEntryMyView> views) {
		for(RepositoryEntryMyView view:views) {
			if(re.getKey().equals(view.getKey())) {
				return true;
			}
		}
		return false;
	}
}
