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
package org.olat.modules.appointments.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.appointments.TopicToGroup;
import org.olat.modules.appointments.model.TopicToGroupImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 22 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class TopicToGroupDAO {
	
	@Autowired
	private DB dbInstance;
	
	TopicToGroup create(Topic topic, Group group) {
		TopicToGroupImpl topicToGroup = new TopicToGroupImpl();
		topicToGroup.setCreationDate(new Date());
		topicToGroup.setTopic(topic);
		topicToGroup.setGroup(group);
		dbInstance.getCurrentEntityManager().persist(topicToGroup);
		return topicToGroup;
	}
	
	void delete(TopicToGroup topicToGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.and().append(" topictogroup.key = :topicToGroupKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("topicToGroupKey", topicToGroup.getKey())
				.executeUpdate();
	}

	void delete(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.and().append(" topictogroup.topic.key = :topicKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("topicKey", topic.getKey())
				.executeUpdate();
	}

	public void delete(Group group) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.and().append(" topictogroup.group.key = :groupKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("groupKey", group.getKey())
				.executeUpdate();
	}
	
	void delete(RepositoryEntry entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.append(" where exists (select 1");
		sb.append("                 from appointmenttopic topic");
		sb.append("                where topic.key = topictogroup.topic.key");
		sb.append("                  and topic.entry.key =  :entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.append("              and topic.subIdent =  :subIdent");
		}
		sb.append("               )");
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("entryKey", entry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		query.executeUpdate();
	}

	TopicToGroup loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select topictogroup");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.and().append(" topictogroup.key = :topicToGroupKey");
		
		List<TopicToGroup> topicToGroups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TopicToGroup.class)
				.setParameter("topicToGroupKey", key)
				.getResultList();
		return topicToGroups.isEmpty() ? null : topicToGroups.get(0);
	}
	
	List<TopicToGroup> load(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select topictogroup");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.append("         join fetch topictogroup.group as group");
		sb.and().append(" topictogroup.topic.key = :topicToGroupKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TopicToGroup.class)
				.setParameter("topicToGroupKey", topic.getKey())
				.getResultList();
	}
	
	Long loadGroupCount(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count (distinct topictogroup.group.id)");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.and().append(" topictogroup.topic.key = :topicToGroupKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("topicToGroupKey", topic.getKey())
				.getSingleResult();
	}

	List<Group> loadGroups(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select topictogroup.group");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.and().append(" topictogroup.topic.key = :topicToGroupKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Group.class)
				.setParameter("topicToGroupKey", topic.getKey())
				.getResultList();
	}
	
	public List<Topic> loadRestrictedTopics(RepositoryEntryRef entry, String subIdent, IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct topic");
		sb.append("  from appointmenttopictogroup topictogroup");
		sb.append("       join topictogroup.topic topic");
		sb.append("       join bgroupmember membership");
		sb.append("         on membership.group.key = topictogroup.group.key");
		sb.and().append("membership.identity.key = :identityKey");
		sb.and().append("membership.role = '").append(GroupRoles.participant).append("'");
		sb.and().append("topic.entry.key = :entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append("topic.subIdent = :subIdent");
		}
		
		TypedQuery<Topic> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Topic.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("identityKey", identity.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
				query.setParameter("subIdent", subIdent);
		}
		List<Topic> restricted = query.getResultList();
		
		sb = new QueryBuilder();
		sb.append("select topic");
		sb.append("  from appointmenttopic topic");
		sb.and().append("not exists (select 1");
		sb.append("                    from appointmenttopictogroup topictogroup");
		sb.append("                   where topictogroup.topic.key = topic.key");
		sb.append("             )");
		sb.and().append("topic.entry.key = :entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append("topic.subIdent = :subIdent");
		}
		
		query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Topic.class)
				.setParameter("entryKey", entry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
				query.setParameter("subIdent", subIdent);
		}
		List<Topic> unrestricted = query.getResultList();
		
		restricted.addAll(unrestricted);
		return restricted;
	}

}
