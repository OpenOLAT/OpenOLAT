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

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.SearchMembersParams;

/**
 * 
 * Initial date: 14 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberViewQueriesTest {
	
	@Test
	public void filterByRolesOneRoleOnly() {
		MemberViewQueries viewQueries = new MemberViewQueries();

		MemberView owner1 = new MemberView(null, List.of(), null);
		owner1.getMemberShip().setRepositoryEntryOwner(true);
		MemberView owner2 = new MemberView(null, List.of(), null);
		owner2.getMemberShip().setRepositoryEntryOwner(true);
		MemberView pending = new MemberView(null, List.of(), null);
		pending.getMemberShip().setPending(true);
		
		List<MemberView> memberList = new ArrayList<>();
		memberList.add(owner1);
		memberList.add(owner2);
		memberList.add(pending);
		
		SearchMembersParams params = new SearchMembersParams();
		params.setPending(false);
		params.setRole(GroupRoles.owner);
		
		viewQueries.filterByRoles(memberList, params);
		
		assertThat(memberList)
			.containsExactlyInAnyOrder(owner1, owner2)
			.doesNotContain(pending);
	}
	
	@Test
	public void filterByRolesOneRoleAndPending() {
		MemberViewQueries viewQueries = new MemberViewQueries();

		MemberView owner = new MemberView(null, List.of(), null);
		owner.getMemberShip().setRepositoryEntryOwner(true);
		MemberView coach = new MemberView(null, List.of(), null);
		coach.getMemberShip().setBusinessGroupCoach(true);
		MemberView pending = new MemberView(null, List.of(), null);
		pending.getMemberShip().setPending(true);
		
		List<MemberView> memberList = new ArrayList<>();
		memberList.add(owner);
		memberList.add(coach);
		memberList.add(pending);
		
		SearchMembersParams params = new SearchMembersParams();
		params.setPending(true);
		params.setRole(GroupRoles.coach);
		
		viewQueries.filterByRoles(memberList, params);
		
		assertThat(memberList)
			.containsExactlyInAnyOrder(coach, pending)
			.doesNotContain(owner);
	}

}
