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
package org.olat.group.manager;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 14 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberViewQueriesTest extends OlatTestCase {
	
	@Autowired
	private MemberViewQueries memberViewQueries;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void getRepositoryEntryMembers() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("member-1");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(author, re, GroupRoles.owner.name());

		SearchMembersParams params = new SearchMembersParams();
		params.setSearchAsRole(author, GroupRoles.owner);
		params.setRoles(new GroupRoles[] { GroupRoles.owner, GroupRoles.coach, GroupRoles.participant, GroupRoles.waiting});
		params.setOnlyRunningTestSessions(false);
		params.setUserPropertiesSearch(Map.of(UserConstants.FIRSTNAME, author.getUser().getFirstName()));
		
		List<MemberView> views = memberViewQueries.getRepositoryEntryMembers(re, params);
		Assertions.assertThat(views)
			.hasSize(1).map(MemberView::getKey)
			.containsExactly(author.getKey());
	}
	
	@Test
	public void getRepositoryEntryMembersExcludeRoles() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("member-1");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(author, re, GroupRoles.owner.name());

		SearchMembersParams params = new SearchMembersParams();
		params.setSearchAsRole(author, GroupRoles.owner);
		params.setRoles(new GroupRoles[] { GroupRoles.coach});
		params.setOnlyRunningTestSessions(false);
		params.setUserPropertiesSearch(Map.of(UserConstants.FIRSTNAME, author.getUser().getFirstName()));
		
		List<MemberView> views = memberViewQueries.getRepositoryEntryMembers(re, params);
		Assertions.assertThat(views)
			.isEmpty();
	}
}
