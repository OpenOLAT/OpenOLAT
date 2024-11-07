/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.basesecurity.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.model.GroupMembershipHistoryImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GroupMembershipHistoryDAO {

	@Autowired
	private DB dbInstance;
	
	
	public GroupMembershipHistory createMembershipHistory(Group group, Identity identity,
			String role, GroupMembershipStatus status,
			OLATResource origin, OLATResource destination, Identity creator) {
		GroupMembershipHistoryImpl history = new GroupMembershipHistoryImpl();
		history.setCreationDate(new Date());
		history.setRole(role);
		history.setStatus(status);
		history.setGroup(group);
		history.setIdentity(identity);
		
		history.setTransferOrigin(origin);
		history.setTransferDestination(destination);
		
		history.setCreator(creator);
		
		dbInstance.getCurrentEntityManager().persist(history);
		return history;
	}
	
	public List<GroupMembershipHistory> loadMembershipHistory(Group group, Identity identity) {
		String query = """
				select history from bgroupmemberhistory history
				where history.group.key=:groupKey and history.identity.key=:identityKey""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, GroupMembershipHistory.class)
				.setParameter("groupKey", group.getKey())
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
	}
	
	public int deleteMembershipHistory(Group group) {
		String query = "delete from bgroupmemberhistory history where history.group.key=:groupKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("groupKey", group.getKey())
				.executeUpdate();
	}
}
