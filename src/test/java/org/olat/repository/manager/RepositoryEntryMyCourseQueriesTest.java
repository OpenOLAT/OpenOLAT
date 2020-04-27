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

import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Offset;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
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
		repositoryManager.setAccess(reMember, RepositoryEntryStatusEnum.published, false, false);
		repositoryEntryRelationDao.addRole(id, reMember, GroupRoles.participant.name());
		RepositoryEntry reNotMember = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reNotMember, RepositoryEntryStatusEnum.published, false, false);
		
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
		repositoryManager.setAccess(re, RepositoryEntryStatusEnum.published, false, false);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		RepositoryEntry otherRe = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(otherRe, RepositoryEntryStatusEnum.published, false, false);
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
		repositoryManager.setAccess(re, RepositoryEntryStatusEnum.published, false, false);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		RepositoryEntry otherRe = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(otherRe, RepositoryEntryStatusEnum.published, false, false);
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
	 * Check the visibility of entries with the published and flags all users / guests
	 * set.
	 */
	@Test
	public void searchViews_asUsers() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-4-");
		RepositoryEntry reNotAllUsers = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reNotAllUsers, RepositoryEntryStatusEnum.published, false, false);
		RepositoryEntry reAllUsers = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reAllUsers, RepositoryEntryStatusEnum.published, true, true);
		
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reAllUsers, views));
		Assert.assertFalse(contains(reNotAllUsers, views));
	}
	
	/**
	 * Check the visibility of entries as a simple user.
	 */
	@Test
	public void searchViews_status_asUsers() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertFalse(contains(reReview, views));
		Assert.assertFalse(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
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
		repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, false, false);
		repositoryEntryRelationDao.addRole(id, rePreparation, GroupRoles.participant.name());
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, false, false);
		repositoryEntryRelationDao.addRole(id, reReview, GroupRoles.participant.name());
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, false, false);
		repositoryEntryRelationDao.addRole(id, reCoachPublished, GroupRoles.participant.name());
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, false, false);
		repositoryEntryRelationDao.addRole(id, rePublished, GroupRoles.participant.name());
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, false, false);
		repositoryEntryRelationDao.addRole(id, reClosed, GroupRoles.participant.name());
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, false, false);
		repositoryEntryRelationDao.addRole(id, reTrash, GroupRoles.participant.name());
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, false, false);
		repositoryEntryRelationDao.addRole(id, reDeleted, GroupRoles.participant.name());
		
		dbInstance.commitAndCloseSession();
		
		// as members
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		params.setMembershipMandatory(true);
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertFalse(contains(reReview, views));
		Assert.assertFalse(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
		
		// as
		SearchMyRepositoryEntryViewParams paramsForAll = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());

		List<RepositoryEntryMyView> viewsForAll = repositoryEntryMyCourseViewQueries.searchViews(paramsForAll, 0, -1);
		Assert.assertFalse(contains(rePreparation, viewsForAll));
		Assert.assertFalse(contains(reReview, viewsForAll));
		Assert.assertFalse(contains(reCoachPublished, viewsForAll));
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
		repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reReview, GroupRoles.coach.name());
		repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reCoachPublished, GroupRoles.coach.name());
		repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, rePublished, GroupRoles.coach.name());
		repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reClosed, GroupRoles.coach.name());
		repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reTrash, GroupRoles.coach.name());
		repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		repositoryEntryRelationDao.addRole(id, reDeleted, GroupRoles.coach.name());
		
		dbInstance.commitAndCloseSession();
		
		// as coaches
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		params.setMembershipMandatory(true);
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertFalse(contains(reReview, views));
		Assert.assertTrue(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
		
		// with mandatory membership 
		SearchMyRepositoryEntryViewParams paramsForAll = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());

		List<RepositoryEntryMyView> viewsForAll = repositoryEntryMyCourseViewQueries.searchViews(paramsForAll, 0, -1);
		Assert.assertFalse(contains(rePreparation, viewsForAll));
		Assert.assertFalse(contains(reReview, viewsForAll));
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
		repositoryManager.setAccess(reNotCoach, RepositoryEntryStatusEnum.review, false, false);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reCoach, RepositoryEntryStatusEnum.review, false, false);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(reCoach, views));
		Assert.assertFalse(contains(reNotCoach, views));
	}
	
	/**
	 * Check if entries with the coach published status are visible to coaches.
	 */
	@Test
	public void searchViews_coachPublished_asCoaches() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-8-");
		RepositoryEntry reNotCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reNotCoach, RepositoryEntryStatusEnum.coachpublished, false, false);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reCoach, RepositoryEntryStatusEnum.coachpublished, false, false);
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
		repositoryManager.setAccess(reNotCoach, RepositoryEntryStatusEnum.published, false, false);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reCoach, RepositoryEntryStatusEnum.published, false, false);
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
		repositoryManager.setAccess(reNotCoach, RepositoryEntryStatusEnum.closed, false, false);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reCoach, RepositoryEntryStatusEnum.closed, false, false);
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
		repositoryManager.setAccess(reNotCoach, RepositoryEntryStatusEnum.trash, false, false);
		RepositoryEntry reCoach = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reCoach, RepositoryEntryStatusEnum.trash, false, false);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(id, Roles.userRoles());
		
		List<RepositoryEntryMyView> views = repositoryEntryMyCourseViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(reCoach, views));
		Assert.assertFalse(contains(reNotCoach, views));
	}
	
	@Test
	public void searchViews_assessment_values() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mycourses-view-11-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(re, RepositoryEntryStatusEnum.published, false, false);
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
