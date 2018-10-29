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
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntryAuthorView;
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
}
