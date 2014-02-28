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

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 26.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryRelationDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;

	public boolean hasRole(IdentityRef identity, RepositoryEntryRef re, String... roles) {
		List<String> roleList = GroupRoles.toList(roles);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(membership) from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where v.key=:repoKey and membership.identity.key=:identityKey");
		if(roleList.size() > 0) {
			sb.append(" and membership.role in (:roles)");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", re.getKey());
		if(roleList.size() > 0) {
			query.setParameter("roles", roleList);
		}
		
		Number count = query.getSingleResult();
		return count == null ? false : count.intValue() > 0;
	}
	
	public void addRole(Identity identity, RepositoryEntry re, String role) {
		Group group = getDefaultGroup(re);
		groupDao.addMembership(group, identity, role);
	}
	
	public int removeRole(IdentityRef identity, RepositoryEntry re, String role) {
		Group group = getDefaultGroup(re);
		return groupDao.removeMembership(group, identity, role);
	}

	public Group getDefaultGroup(RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder();
		sb.append("select baseGroup from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" where v.key=:repoKey");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Group.class)
				.setParameter("repoKey", re.getKey())
				.getSingleResult();
	}
	
	/**
	 * It will count all members, business groups members too
	 * @param re
	 * @param roles
	 * @return
	 */
	public int countMembers(RepositoryEntryRef re, String... roles) {
		List<String> roleList = GroupRoles.toList(roles);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(members) from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as members")
		  .append(" where v.key=:repoKey");
		if(roleList.size() > 0) {
				sb.append(" and members.role in (:roles)");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("repoKey", re.getKey());
		if(roleList.size() > 0) {
				query.setParameter("roles", roleList);
		}
		
		Number count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public List<Identity> getMembers(RepositoryEntryRef re, RepositoryEntryRelationType type, String... roles) {
		List<String> roleList = GroupRoles.toList(roles);
		
		String def;
		switch(type) {
			case defaultGroup: def = " on relGroup.defaultGroup=true"; break;
			case notDefaultGroup: def = " on relGroup.defaultGroup=false"; break;
			default: def = "";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select members.identity from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup").append(def)
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as members")
		  .append(" where v.key=:repoKey");
		if(roleList.size() > 0) {
				sb.append(" and members.role in (:roles)");
		}
			
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repoKey", re.getKey());
		if(roleList.size() > 0) {
				query.setParameter("roles", roleList);
		}
		return query.getResultList();
	}
	
	public boolean removeMembers(RepositoryEntry re, List<Identity> members) {
		Group group = getDefaultGroup(re);
		for(Identity member:members) {
			groupDao.removeMembership(group, member);
		}
		return true;
	}
	
	public RepositoryEntryToGroupRelation createRelation(Group group, RepositoryEntry re) {
		RepositoryEntryToGroupRelation rel = new RepositoryEntryToGroupRelation();
		rel.setCreationDate(new Date());
		rel.setDefaultGroup(false);
		rel.setGroup(group);
		rel.setEntry(re);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public int removeRelation(Group group, RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from repoentrytogroup as rel")
		  .append(" where rel.entry.key=:repoKey and rel.group.key=:groupKey");

		EntityManager em = dbInstance.getCurrentEntityManager();
		List<RepositoryEntryToGroupRelation> rels = em.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("repoKey", re.getKey())
			.setParameter("groupKey", group.getKey())
			.getResultList();
		
		for(RepositoryEntryToGroupRelation rel:rels) {
			em.remove(rel);
		}
		if(re instanceof RepositoryEntry) {
			((RepositoryEntry)re).getGroups().removeAll(rels);
		}
		return rels.size();
	}
	
	public int removeRelations(RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from repoentrytogroup as rel")
		  .append(" where rel.entry.key=:repoKey");

		EntityManager em = dbInstance.getCurrentEntityManager();
		List<RepositoryEntryToGroupRelation> rels = em.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("repoKey", re.getKey())
			.getResultList();
		
		for(RepositoryEntryToGroupRelation rel:rels) {
			em.remove(rel);
		}
		if(re instanceof RepositoryEntry) {
			((RepositoryEntry)re).getGroups().removeAll(rels);
		}
		return rels.size();
	}

	public int removeNotDefaultRelation(Group group) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from repoentrytogroup as rel")
		  .append(" where rel.group.key=:groupKey");
	
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<RepositoryEntryToGroupRelation> rels = em.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("groupKey", group.getKey())
			.getResultList();
		
		int count = 0;
		for(RepositoryEntryToGroupRelation rel:rels) {
			if(!rel.isDefaultGroup()) {
				em.remove(rel);
				count++;
			}
		}
		return count;
	}
	
	public int countRelations(Group group) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(rel) from repoentrytogroup as rel")
		  .append(" where rel.group.key=:groupKey");

		Number count = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Number.class)
			.setParameter("groupKey", group.getKey())
			.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public List<RepositoryEntryToGroupRelation> getRelations(List<Group> groups) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(rel) from repoentrytogroup as rel")
		  .append(" inner join fetch rel.entry as entry")
		  .append(" inner join fetch rel.group as baseGroup")
		  .append(" where entry.group.key=:groupKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("groups", groups)
			.getResultList();
	}
}
