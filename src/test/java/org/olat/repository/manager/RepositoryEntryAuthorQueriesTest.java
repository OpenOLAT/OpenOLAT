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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryAuthorViewResults;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Check if the query with the sort argument are "playable" but don't
 * check if the order by goes in the right direction.
 * 
 * Initial date: 04.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryAuthorQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private RepositoryEntryAuthorQueries repositoryEntryAuthorViewQueries;
	@Autowired
	private TaxonomyService taxomomyService;
	
	@Test
	public void searchViews() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("view-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchAuthorRepositoryEntryViewParams params
			= new SearchAuthorRepositoryEntryViewParams(id, roles);
		params.setMarked(Boolean.TRUE);
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.getViews());
	}
	
	@Test
	public void searchViews_withoutRoles() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("view-");
		dbInstance.commit();
		
		SearchAuthorRepositoryEntryViewParams params
			= new SearchAuthorRepositoryEntryViewParams(id, null);
		params.setMarked(Boolean.TRUE);
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.getViews());
	}
	
	@Test
	public void searchViews_deleted() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndLearnResourceManager("view-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchAuthorRepositoryEntryViewParams params
			= new SearchAuthorRepositoryEntryViewParams(id, roles);
		params.setStatus(new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.deleted });
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.getViews());
	}
	
	/**
	 * Search by author
	 */
	@Test
	public void searchViews_preparation_byAuthor() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reOwner, RepositoryEntryStatusEnum.preparation);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reNotOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotOwner, RepositoryEntryStatusEnum.preparation);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		String firstName = id.getUser().getFirstName();
		params.setAuthor(firstName.substring(1, 6));

		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, results));
		Assert.assertFalse(contains(reNotOwner, results));
	}
	
	/**
	 * Check the visibility of entries in preparation status.
	 */
	@Test
	public void searchViews_preparation_asAuthor() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reOwner, RepositoryEntryStatusEnum.preparation);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reNotOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotOwner, RepositoryEntryStatusEnum.preparation);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());

		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, results));
		Assert.assertFalse(contains(reNotOwner, results));
	}
	
	/**
	 * Check the visibility of entries in review status with the various
	 * copy / download / reference flags.
	 */
	@Test
	public void searchViews_review_asAuthor() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reOwner, RepositoryEntryStatusEnum.review);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reNotOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotOwner, RepositoryEntryStatusEnum.review);
		RepositoryEntry reNotOwnerButCopy = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButCopy = repositoryManager.setStatus(reNotOwnerButCopy, RepositoryEntryStatusEnum.review);
		repositoryManager.setAccess(reNotOwnerButCopy, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		RepositoryEntry reNotOwnerButReference = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButReference = repositoryManager.setStatus(reNotOwnerButReference, RepositoryEntryStatusEnum.review);
		repositoryManager.setAccess(reNotOwnerButReference, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, true, false, null);
		RepositoryEntry reNotOwnerButDownload = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButDownload = repositoryManager.setStatus(reNotOwnerButReference, RepositoryEntryStatusEnum.review);
		repositoryManager.setAccess(reNotOwnerButDownload, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setCanCopy(true);
		params.setCanDownload(true);
		params.setCanReference(true);

		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, results));
		Assert.assertFalse(contains(reNotOwner, results));
		Assert.assertTrue(contains(reNotOwnerButCopy, results));
		Assert.assertTrue(contains(reNotOwnerButReference, results));
		Assert.assertTrue(contains(reNotOwnerButDownload, results));
	}
	
	/**
	 * Check the visibility of entries in preparation status.
	 */
	@Test
	public void searchViews_idRefs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reOwner, RepositoryEntryStatusEnum.coachpublished);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reNotOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setStatus(reNotOwner, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry reNotOwnerButCopy = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButCopy = repositoryManager.setStatus(reNotOwnerButCopy, RepositoryEntryStatusEnum.coachpublished);
		repositoryManager.setAccess(reNotOwnerButCopy, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setIdAndRefs(reOwner.getKey().toString());

		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, results));
		Assert.assertFalse(contains(reNotOwner, results));
		Assert.assertFalse(contains(reNotOwnerButCopy, results));
	}
	
	/**
	 * Check the visibility of entries with different status as an author
	 * and the entries have the flags copy allowed set.
	 */
	@Test
	public void searchViews_statusAsAuthorCanCopy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		rePreparation = repositoryManager.setAccess(rePreparation, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		reReview = repositoryManager.setAccess(reReview, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		rePublished = repositoryManager.setAccess(rePublished, false,  RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		reClosed = repositoryManager.setAccess(reClosed, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		reTrash = repositoryManager.setAccess(reTrash, false,  RepositoryEntryAllowToLeaveOptions.atAnyTime, true, false, false, null);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		reDeleted = repositoryManager.setAccess(reDeleted,false, RepositoryEntryAllowToLeaveOptions.atAnyTime,  true, false, false, null);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setCanCopy(true);
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, results));
		Assert.assertTrue(contains(reReview, results));
		Assert.assertTrue(contains(reCoachPublished, results));
		Assert.assertTrue(contains(rePublished, results));
		Assert.assertTrue(contains(reClosed, results));
		Assert.assertFalse(contains(reTrash, results));
		Assert.assertFalse(contains(reDeleted, results));
	}
	
	/**
	 * Check the visibility of entries with different status as an author
	 * with no flags copy / download / reference set.
	 */
	@Test
	public void searchViews_statusAsAuthorNoReferenceable() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, results));
		Assert.assertFalse(contains(reReview, results));
		Assert.assertFalse(contains(reCoachPublished, results));
		Assert.assertFalse(contains(rePublished, results));
		Assert.assertFalse(contains(reClosed, results));
		Assert.assertFalse(contains(reTrash, results));
		Assert.assertFalse(contains(reDeleted, results));
	}
	
	@Test
	public void searchViews_statusAsAuthorCanDownload() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-4b-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		rePreparation = repositoryManager.setAccess(rePreparation, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		reReview = repositoryManager.setAccess(reReview, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		rePublished = repositoryManager.setAccess(rePublished, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		reClosed = repositoryManager.setAccess(reClosed, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		reTrash = repositoryManager.setAccess(reTrash, false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		reDeleted = repositoryManager.setAccess(reDeleted,false, RepositoryEntryAllowToLeaveOptions.atAnyTime, false, false, true, null);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setCanDownload(true);
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, results));
		Assert.assertTrue(contains(reReview, results));
		Assert.assertTrue(contains(reCoachPublished, results));
		Assert.assertTrue(contains(rePublished, results));
		Assert.assertTrue(contains(reClosed, results));
		Assert.assertFalse(contains(reTrash, results));
		Assert.assertFalse(contains(reDeleted, results));
	}
	
	/**
	 * Check the visibility of entries with different status as an author
	 * with no flags copy / download / reference set.
	 */
	@Test
	public void searchViews_status_asAuthorDeleted() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		repositoryEntryRelationDao.addRole(id, rePreparation, GroupRoles.owner.name());
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		repositoryEntryRelationDao.addRole(id, reReview, GroupRoles.owner.name());
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		repositoryEntryRelationDao.addRole(id, reCoachPublished, GroupRoles.owner.name());
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(id, rePublished, GroupRoles.owner.name());
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		repositoryEntryRelationDao.addRole(id, reClosed, GroupRoles.owner.name());
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		repositoryEntryRelationDao.addRole(id, reTrash, GroupRoles.owner.name());
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		repositoryEntryRelationDao.addRole(id, reDeleted, GroupRoles.owner.name());
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setStatus(new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.trash });
		params.setOwnedResourcesOnly(true);
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, results));
		Assert.assertFalse(contains(reReview, results));
		Assert.assertFalse(contains(reCoachPublished, results));
		Assert.assertFalse(contains(rePublished, results));
		Assert.assertFalse(contains(reClosed, results));
		Assert.assertTrue(contains(reTrash, results));
		Assert.assertFalse(contains(reDeleted, results));
	}
	
	/**
	 * Check the visibility of entries with different status as an author.
	 */
	@Test
	public void searchViews_status_asLearnResourceManager() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndLearnResourceManager("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(rePreparation, results));
		Assert.assertTrue(contains(reReview, results));
		Assert.assertTrue(contains(reCoachPublished, results));
		Assert.assertTrue(contains(rePublished, results));
		Assert.assertTrue(contains(reClosed, results));
		Assert.assertFalse(contains(reTrash, results));
		Assert.assertFalse(contains(reDeleted, results));
	}
	
	/**
	 * Check the visibility of entries with different status as an author.
	 */
	@Test
	public void searchViews_status_asOwner() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndLearnResourceManager("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry reOwned = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reOwned = repositoryManager.setStatus(reOwned, RepositoryEntryStatusEnum.preparation);
		repositoryEntryRelationDao.addRole(id, reOwned, GroupRoles.owner.name());
		RepositoryEntry reOwned2 = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reOwned2 = repositoryManager.setStatus(reOwned2, RepositoryEntryStatusEnum.preparation);
		repositoryEntryRelationDao.addRole(id, reOwned2, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setOwnedResourcesOnly(true);
		params.addResourceTypes(reOwned.getOlatResource().getResourceableTypeName());
		params.addResourceTypes(reOwned2.getOlatResource().getResourceableTypeName());
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwned, results));
	}
	
	/**
	 * Check the visibility of entries with different status as an administrator.
	 */
	@Test
	public void searchViews_status_asAdministrator() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.administratorRoles());
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(rePreparation, results));
		Assert.assertTrue(contains(reReview, results));
		Assert.assertTrue(contains(reCoachPublished, results));
		Assert.assertTrue(contains(rePublished, results));
		Assert.assertTrue(contains(reClosed, results));
		Assert.assertFalse(contains(reTrash, results));
		Assert.assertFalse(contains(reDeleted, results));
	}
	
	/**
	 * Check the visibility of entries with different status as an administrator.
	 */
	@Test
	public void searchViews_status_asAdministratorSearchDeleted() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setStatus(rePreparation, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setStatus(reReview, RepositoryEntryStatusEnum.review);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setStatus(reCoachPublished, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setStatus(rePublished, RepositoryEntryStatusEnum.published);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setStatus(reClosed, RepositoryEntryStatusEnum.closed);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setStatus(reTrash, RepositoryEntryStatusEnum.trash);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setStatus(reDeleted, RepositoryEntryStatusEnum.deleted);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.administratorRoles());
		params.setStatus(new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.trash });
		
		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, results));
		Assert.assertFalse(contains(reReview, results));
		Assert.assertFalse(contains(reCoachPublished, results));
		Assert.assertFalse(contains(rePublished, results));
		Assert.assertFalse(contains(reClosed, results));
		Assert.assertTrue(contains(reTrash, results));
		Assert.assertFalse(contains(reDeleted, results));
	}
	
	/**
	 * Check the visibility of entries in preparation status.
	 */
	@Test
	public void searchViews_resource() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reOwner = repositoryManager.setStatus(reOwner, RepositoryEntryStatusEnum.closed);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setOwnedResourcesOnly(true);

		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, results));
	}
	
	@Test
	public void searchViews_taxonomyLevels() {
		Identity reOwner = JunitTestHelper.createAndPersistIdentityAsRndAuthor(random());
		Taxonomy taxonomy = taxomomyService.createTaxonomy(random(), random(), random(), null);
		TaxonomyLevel level1 = taxomomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		TaxonomyLevel level2 = taxomomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		TaxonomyLevel levelOther = taxomomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
		
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(reOwner, entry1, GroupRoles.owner.name());
		repositoryService.addTaxonomyLevel(entry1, level1);
		repositoryService.addTaxonomyLevel(entry1, level2);
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(reOwner, entry2, GroupRoles.owner.name());
		repositoryService.addTaxonomyLevel(entry2, level1);
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(reOwner, entryOther, GroupRoles.owner.name());
		repositoryService.addTaxonomyLevel(entryOther, levelOther);
		RepositoryEntry entryNoLevel = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(reOwner, entryNoLevel, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(reOwner, Roles.administratorRoles());
		params.setTaxonomyLevels(Arrays.asList(level1, level2));

		RepositoryEntryAuthorViewResults results = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(entry1, results));
		Assert.assertTrue(contains(entry2, results));
		Assert.assertFalse(contains(entryOther, results));
		Assert.assertFalse(contains(entryNoLevel, results));
	}
	
	@Test
	public void searchViews_orderBy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("view-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchAuthorRepositoryEntryViewParams params
			= new SearchAuthorRepositoryEntryViewParams(id, roles);
		params.setMarked(Boolean.TRUE);
		
		for(OrderBy orderBy:OrderBy.values()) {
			params.setOrderBy(orderBy);
			params.setOrderByAsc(true);
			RepositoryEntryAuthorViewResults resultsAsc = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
			Assert.assertNotNull(resultsAsc);
			Assert.assertNotNull(resultsAsc.getViews());
			params.setOrderByAsc(false);
			RepositoryEntryAuthorViewResults resultsDesc = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
			Assert.assertNotNull(resultsDesc);
			Assert.assertNotNull(resultsDesc.getViews());
		}
	}
	
	private final boolean contains(RepositoryEntry re, RepositoryEntryAuthorViewResults results) {
		if(results == null || results.getViews() == null) return false;
		
		List<RepositoryEntryAuthorView> views = results.getViews();
		for(RepositoryEntryAuthorView view:views) {
			if(re.getKey().equals(view.getKey())) {
				return true;
			}
		}
		return false;
	}
}
