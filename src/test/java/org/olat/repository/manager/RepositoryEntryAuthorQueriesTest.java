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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
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
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private RepositoryEntryAuthorQueries repositoryEntryAuthorViewQueries;
	
	@Test
	public void searchViews() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("view-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchAuthorRepositoryEntryViewParams params
			= new SearchAuthorRepositoryEntryViewParams(id, roles);
		params.setMarked(Boolean.TRUE);
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
		Assert.assertNotNull(views);
	}
	
	@Test
	public void searchViews_withoutRoles() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("view-");
		dbInstance.commit();
		
		SearchAuthorRepositoryEntryViewParams params
			= new SearchAuthorRepositoryEntryViewParams(id, null);
		params.setMarked(Boolean.TRUE);
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
		Assert.assertNotNull(views);
	}
	
	@Test
	public void searchViews_deleted() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndLearnResourceManager("view-");
		dbInstance.commit();
		Roles roles = securityManager.getRoles(id);
		
		SearchAuthorRepositoryEntryViewParams params
			= new SearchAuthorRepositoryEntryViewParams(id, roles);
		params.setDeleted(true);
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
		Assert.assertNotNull(views);
	}
	
	/**
	 * Check the visibility of entries in preparation status.
	 */
	@Test
	public void searchViews_preparation_asAuthor() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reOwner, RepositoryEntryStatusEnum.preparation, false, false);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reNotOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reNotOwner, RepositoryEntryStatusEnum.preparation, false, false);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());

		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, views));
		Assert.assertFalse(contains(reNotOwner, views));
	}
	
	/**
	 * Check the visibility of entries in review status with the various
	 * copy / download / reference flags.
	 */
	@Test
	public void searchViews_review_asAuthor() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reOwner, RepositoryEntryStatusEnum.review, false, false);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reNotOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reNotOwner, RepositoryEntryStatusEnum.review, false, false);
		RepositoryEntry reNotOwnerButCopy = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButCopy = repositoryManager.setAccess(reNotOwnerButCopy, RepositoryEntryStatusEnum.review, false, false);
		repositoryManager.setAccess(reNotOwnerButCopy, true, false, false);
		RepositoryEntry reNotOwnerButReference = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButReference = repositoryManager.setAccess(reNotOwnerButReference, RepositoryEntryStatusEnum.review, false, false);
		repositoryManager.setAccess(reNotOwnerButReference, false, true, false);
		RepositoryEntry reNotOwnerButDownload = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButDownload = repositoryManager.setAccess(reNotOwnerButReference, RepositoryEntryStatusEnum.review, false, false);
		repositoryManager.setAccess(reNotOwnerButDownload, false, false, true);
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());

		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, views));
		Assert.assertFalse(contains(reNotOwner, views));
		Assert.assertTrue(contains(reNotOwnerButCopy, views));
		Assert.assertTrue(contains(reNotOwnerButReference, views));
		Assert.assertTrue(contains(reNotOwnerButDownload, views));
	}
	
	/**
	 * Check the visibility of entries in preparation status.
	 */
	@Test
	public void searchViews_idRefs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reOwner, RepositoryEntryStatusEnum.coachpublished, false, false);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reNotOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reNotOwner, RepositoryEntryStatusEnum.coachpublished, false, false);
		RepositoryEntry reNotOwnerButCopy = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reNotOwnerButCopy = repositoryManager.setAccess(reNotOwnerButCopy, RepositoryEntryStatusEnum.coachpublished, false, false);
		repositoryManager.setAccess(reNotOwnerButCopy, true, false, false);
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setIdAndRefs(reOwner.getKey().toString());

		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, views));
		Assert.assertFalse(contains(reNotOwner, views));
		Assert.assertFalse(contains(reNotOwnerButCopy, views));
	}
	
	/**
	 * Check the visibility of entries with different status as an author
	 * and the entries have the flags copy allowed set.
	 */
	@Test
	public void searchViews_status_asAuthor_withCopy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		rePreparation = repositoryManager.setAccess(rePreparation, true, false, false);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		reReview = repositoryManager.setAccess(reReview, true, false, false);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, true, false, false);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		rePublished = repositoryManager.setAccess(rePublished, true, false, false);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		reClosed = repositoryManager.setAccess(reClosed, true, false, false);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		reTrash = repositoryManager.setAccess(reTrash, true, false, false);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		reDeleted = repositoryManager.setAccess(reDeleted, true, false, false);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertTrue(contains(reReview, views));
		Assert.assertTrue(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
	}
	
	/**
	 * Check the visibility of entries with different status as an author
	 * with no flags copy / download / reference set.
	 */
	@Test
	public void searchViews_status_asAuthor() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertFalse(contains(reReview, views));
		Assert.assertFalse(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
	}
	
	/**
	 * Check the visibility of entries with different status as an author
	 * with no flags copy / download / reference set.
	 */
	@Test
	public void searchViews_status_asAuthor_deleted() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		repositoryEntryRelationDao.addRole(id, rePreparation, GroupRoles.owner.name());
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		repositoryEntryRelationDao.addRole(id, reReview, GroupRoles.owner.name());
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		repositoryEntryRelationDao.addRole(id, reCoachPublished, GroupRoles.owner.name());
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		repositoryEntryRelationDao.addRole(id, rePublished, GroupRoles.owner.name());
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		repositoryEntryRelationDao.addRole(id, reClosed, GroupRoles.owner.name());
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		repositoryEntryRelationDao.addRole(id, reTrash, GroupRoles.owner.name());
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		repositoryEntryRelationDao.addRole(id, reDeleted, GroupRoles.owner.name());
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setDeleted(true);
		params.setOwnedResourcesOnly(true);
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertFalse(contains(reReview, views));
		Assert.assertFalse(contains(reCoachPublished, views));
		Assert.assertFalse(contains(rePublished, views));
		Assert.assertFalse(contains(reClosed, views));
		Assert.assertTrue(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
	}
	
	/**
	 * Check the visibility of entries with different status as an author.
	 */
	@Test
	public void searchViews_status_asLearnResourceManager() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndLearnResourceManager("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(rePreparation, views));
		Assert.assertTrue(contains(reReview, views));
		Assert.assertTrue(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
	}
	
	/**
	 * Check the visibility of entries with different status as an administrator.
	 */
	@Test
	public void searchViews_status_asAdministrator() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.administratorRoles());
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(rePreparation, views));
		Assert.assertTrue(contains(reReview, views));
		Assert.assertTrue(contains(reCoachPublished, views));
		Assert.assertTrue(contains(rePublished, views));
		Assert.assertTrue(contains(reClosed, views));
		Assert.assertFalse(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
	}
	
	/**
	 * Check the visibility of entries with different status as an administrator.
	 */
	@Test
	public void searchViews_status_asAdministrator_searchDeleted() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("view-4-");
		
		// a set of entries with every possible status
		RepositoryEntry rePreparation = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePreparation = repositoryManager.setAccess(rePreparation, RepositoryEntryStatusEnum.preparation, true, true);
		RepositoryEntry reReview = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reReview = repositoryManager.setAccess(reReview, RepositoryEntryStatusEnum.review, true, true);
		RepositoryEntry reCoachPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reCoachPublished = repositoryManager.setAccess(reCoachPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		RepositoryEntry rePublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		rePublished = repositoryManager.setAccess(rePublished, RepositoryEntryStatusEnum.published, true, true);
		RepositoryEntry reClosed = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reClosed = repositoryManager.setAccess(reClosed, RepositoryEntryStatusEnum.closed, true, true);
		RepositoryEntry reTrash = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reTrash = repositoryManager.setAccess(reTrash, RepositoryEntryStatusEnum.trash, true, true);
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reDeleted = repositoryManager.setAccess(reDeleted, RepositoryEntryStatusEnum.deleted, true, true);
		
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.administratorRoles());
		params.setDeleted(true);
		
		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertFalse(contains(rePreparation, views));
		Assert.assertFalse(contains(reReview, views));
		Assert.assertFalse(contains(reCoachPublished, views));
		Assert.assertFalse(contains(rePublished, views));
		Assert.assertFalse(contains(reClosed, views));
		Assert.assertTrue(contains(reTrash, views));
		Assert.assertFalse(contains(reDeleted, views));
	}
	
	/**
	 * Check the visibility of entries in preparation status.
	 */
	@Test
	public void searchViews_resource() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAuthor("view-3-");
		RepositoryEntry reOwner = JunitTestHelper.createAndPersistRepositoryEntry(true);
		reOwner = repositoryManager.setAccess(reOwner, RepositoryEntryStatusEnum.closed, false, false);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(id, Roles.authorRoles());
		params.setOwnedResourcesOnly(true);

		List<RepositoryEntryAuthorView> views = repositoryEntryAuthorViewQueries.searchViews(params, 0, -1);
		Assert.assertTrue(contains(reOwner, views));
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
			List<RepositoryEntryAuthorView> viewAsc = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
			Assert.assertNotNull(viewAsc);
			params.setOrderByAsc(false);
			List<RepositoryEntryAuthorView> viewDesc = repositoryEntryAuthorViewQueries.searchViews(params, 0, 10);
			Assert.assertNotNull(viewDesc);
		}
	}
	
	private final boolean contains(RepositoryEntry re, List<RepositoryEntryAuthorView> views) {
		for(RepositoryEntryAuthorView view:views) {
			if(re.getKey().equals(view.getKey())) {
				return true;
			}
		}
		return false;
	}
}
