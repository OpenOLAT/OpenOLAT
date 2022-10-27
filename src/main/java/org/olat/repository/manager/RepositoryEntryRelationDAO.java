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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.model.MembershipInfos;
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
	
	/**
	 * Get roles in the repository entry, with business groups and curriculums
	 * too but not the organizations.
	 * 
	 * @param identity The identity
	 * @param re The repository entry
	 * @return The list of roles
	 */
	public List<String> getRoles(IdentityRef identity, RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select membership.role from repoentrytogroup as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" left join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)")
		  .append(" left join curriculumelement as curEl on (curEl.group.key=baseGroup.key)")
		  .append(" where relGroup.entry.key=:repoKey and membership.identity.key=:identityKey and (relGroup.defaultGroup=true or businessGroup.key is not null or curEl is not null)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", re.getKey())
				.getResultList();
	}
	
	/**
	 * Load role and default information
	 * 
	 * @param identity
	 * @param re
	 * @return Return an array with the role and true if the relation is the default one.
	 */
	public List<Object[]> getRoleAndDefaults(IdentityRef identity, RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select membership.role, relGroup.defaultGroup, curEl.key")
		  .append(" from repoentrytogroup as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" left join curriculumelement curEl on (curEl.group.key=baseGroup.key)")
		  .append(" where relGroup.entry.key=:repoKey and membership.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", re.getKey())
				.getResultList();
	}
	
	/**
	 * Has role in the repository entry only without following relations
	 * to business groups and other entities.
	 * 
	 * @param identity The identity
	 * @param re The repository entry
	 * @param roles The roles to search for
	 * @return true if a role is found
	 */
	public boolean hasRole(IdentityRef identity, RepositoryEntryRef re, String... roles) {
		return hasRole(identity, re, false, roles);
	}

	/**
	 * Has role in the repository entry.
	 * 
	 * @param identity The identity
	 * @param re The repository entry
	 * @param follow follow or not follow the relation to business groups, organizations and curriculum elements
	 * @param roles The roles to search for
	 * @return true if a role is found
	 */
	public boolean hasRole(IdentityRef identity, RepositoryEntryRef re, boolean follow, String... roles) {
		if(identity == null || re == null || re.getKey() == null) return false;
		List<String> roleList = GroupRoles.toList(roles);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(membership) from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup");
		if(!follow) {
			sb.append(" on relGroup.defaultGroup=true");
		}
		sb.append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where v.key=:repoKey and membership.identity.key=:identityKey");
		if(!roleList.isEmpty()) {
			sb.append(" and membership.role in (:roles)");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", re.getKey());
		if(!roleList.isEmpty()) {
			query.setParameter("roles", roleList);
		}
		
		Number count = query.getSingleResult();
		return count != null && count.intValue() > 0;
	}
	
	/**
	 * Has the specified roles in the repository
	 * 
	 * @param identity The identity
	 * @param followBusinessGroups If the query must includes the business groups or not.
	 * @param roles The roles to query
	 * @return
	 */
	public boolean hasRoleExpanded(IdentityRef identity, String... roles) {
		if(identity == null) return false;
		List<String> roleList = GroupRoles.toList(roles);

		StringBuilder sb = new StringBuilder();
		sb.append("select membership.key from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup ")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey");
		if(!roleList.isEmpty()) {
			sb.append(" and membership.role in (:roles)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFirstResult(0)
				.setMaxResults(1)
				.setParameter("identityKey", identity.getKey());
		if(!roleList.isEmpty()) {
			query.setParameter("roles", roleList);
		}
		
		List<Long> first = query.getResultList();
		return first != null && !first.isEmpty() && first.get(0) != null && first.get(0).longValue() >= 0l;
	}
	
	public void addRole(Identity identity, RepositoryEntryRef re, String role) {
		Group group = getDefaultGroup(re);
		groupDao.addMembershipOneWay(group, identity, role);
	}
	
	public int removeRole(IdentityRef identity, RepositoryEntryRef re, String role) {
		Group group = getDefaultGroup(re);
		if(group != null) {
			return groupDao.removeMembership(group, identity, role);
		}
		return 0;
	}
	
	public int removeRole(RepositoryEntry re, String role) {
		Group group = getDefaultGroup(re);
		if(group != null) {
			return groupDao.removeMemberships(group, role);
		}
		return 0;
	}

	/**
	 * Retrieve the default group of the repository entry (the one
	 * marked with the flag defaultGroup=true). The query is cached
	 * by hibernate 2nd level cache.
	 * 
	 * @param re The repository entry
	 * @return The group
	 */
	public Group getDefaultGroup(RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder();
		sb.append("select baseGroup from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" where v.key=:repoKey");

		List<Group> groups = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Group.class)
				.setParameter("repoKey", re.getKey())
				.getResultList();
		return groups == null || groups.isEmpty() ? null : groups.get(0);
	}
	
	/**
	 * Membership calculated with business groups too. Role are owner, coach and participant.
	 * 
	 * @param identity
	 * @param entry
	 * @return
	 */
	public boolean isMember(IdentityRef identity, RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v.key, membership.identity.key ")
		  .append(" from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role in ")
		  .append("   ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
		  .append(" where membership.identity.key=:identityKey and v.key=:repositoryEntryKey ");

		List<Object[]> counter = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repositoryEntryKey", entry.getKey())
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		return !counter.isEmpty();
	}
	
	/**
	 * Membership calculated with business groups too
	 * 
	 * @param identity
	 * @param entry
	 * @return
	 */
	public void filterMembership(IdentityRef identity, Collection<Long> entries) {
		if(entries == null || entries.isEmpty()) return;
		
		List<Long> entryKeysList = new ArrayList<>(entries);
		Set<Object> memberships = new HashSet<>();
		
		int count = 0;
		int batch = 5000;
		do {
			int toIndex = Math.min(count + batch, entryKeysList.size());
			List<Long> toLoad = entryKeysList.subList(count, toIndex);
			List<Object[]> membershipList = dbInstance.getCurrentEntityManager()
					.createNamedQuery("filterRepositoryEntryRelationMembership", Object[].class)
					.setParameter("identityKey", identity.getKey())
					.setParameter("repositoryEntryKey", toLoad)
					.setFlushMode(FlushModeType.COMMIT)
					.getResultList();
			for(Object[] membership: membershipList) {
				memberships.add(membership[0]);
			}
			count += batch;
		} while(count < entryKeysList.size());

		for(Iterator<Long> entryIt=entries.iterator(); entryIt.hasNext(); ) {
			if(!memberships.contains(entryIt.next())) {
				entryIt.remove();
			}
		}
	}
	
	/**
	 * It will count all members, business groups members too but not
	 * the organizations.
	 * 
	 * @param re The repository entry
	 * @param Roles The role (mandatory)
	 * @return
	 */
	public int countMembers(RepositoryEntryRef re, String role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(members) from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as members")
		  .append(" where v.key=:repoKey and members.role=:role");

		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("repoKey", re.getKey())
				.setParameter("role", role)
				.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public int countMembers(List<? extends RepositoryEntryRef> res, Identity excludeMe) {
		if(res == null || res.isEmpty()) return 0;
		
		List<Long> repoKeys = new ArrayList<>(res.size());
		for(RepositoryEntryRef re:res) {
			repoKeys.add(re.getKey());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(distinct members.identity.key) from repositoryentry as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as members")
		  .append(" left join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)")
		  .append(" where v.key in (:repoKeys) and (relGroup.defaultGroup=true or businessGroup.key is not null)");
		if(excludeMe != null) {
			sb.append(" and not(members.identity.key=:identityKey)");
		}

		TypedQuery<Number> countQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("repoKeys", repoKeys);
		if(excludeMe != null) {
			countQuery.setParameter("identityKey", excludeMe.getKey());
		}
		
		Number count = countQuery.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public Map<String, Long> getRoleToCountMemebers(RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select members.role, count(distinct members.id) from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as members")
		  .append(" where v.key=:repoKey")
		  .append(" group by members.role");
		
		List<Object[]> resultList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("repoKey", re.getKey())
				.getResultList();
		Map<String, Long> roleToCountMembers = new HashMap<>(resultList.size());
		for(Object[] objects:resultList) {
			String role = (String)objects[0];
			Long count = (Long)objects[1];
			roleToCountMembers.put(role, count);
		}
		
		return roleToCountMembers;
	}
	
	public Date getEnrollmentDate(RepositoryEntryRef re, IdentityRef identity, String... roles) {
		if(re == null || identity == null) return null;
		
		List<String> roleList = null;
		if(roles != null && roles.length > 0 && roles[0] != null) {
			roleList = new ArrayList<>(roles.length);
			for(String role:roles) {
				roleList.add(role);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select min(members.creationDate) from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as members")
		  .append(" left join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)")
		  .append(" where v.key=:repoKey and (relGroup.defaultGroup=true or businessGroup.key is not null) and members.identity.key=:identityKey");
		if(roleList != null && !roleList.isEmpty()) {
			sb.append(" and members.role in (:roles)");
		}

		TypedQuery<Date> datesQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Date.class)
				.setParameter("repoKey", re.getKey())
				.setParameter("identityKey", identity.getKey());
		if(roleList != null && !roleList.isEmpty()) {
			datesQuery.setParameter("roles", roleList);
		}
		
		List<Date> dates = datesQuery.getResultList();
		return dates.isEmpty() ? null : dates.get(0);
	}
	
	public Map<Long,Date> getEnrollmentDates(RepositoryEntryRef re, String... roles) {
		if(re == null) return null;
		
		List<String> roleList = null;
		if(roles != null && roles.length > 0 && roles[0] != null) {
			roleList = new ArrayList<>(roles.length);
			for(String role:roles) {
				roleList.add(role);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select members.identity.key, min(members.creationDate) from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as members")
		  .append(" left join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)")
		  .append(" where v.key=:repoKey and (relGroup.defaultGroup=true or businessGroup.key is not null)");
		if(roleList != null && !roleList.isEmpty()) {
			sb.append(" and members.role in (:roles)");
		}
		sb.append(" group by members.identity.key");

		TypedQuery<Object[]> datesQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("repoKey", re.getKey());
		if(roleList != null && !roleList.isEmpty()) {
			datesQuery.setParameter("roles", roleList);
		}
		
		List<Object[]> dateList = datesQuery.getResultList();
		Map<Long,Date> dateMap = new HashMap<>((dateList.size() * 2) + 1);
		for(Object[] dateArr:dateList) {
			Long key = (Long)dateArr[0];
			Date date = (Date)dateArr[1];
			if(key != null && date != null) {
				dateMap.put(key, date);
			}
		}

		return dateMap;
	}
	
	public List<Identity> getMembers(RepositoryEntryRef re, RepositoryEntryRelationType type, String... roles) {
		return getMembers(Collections.singletonList(re), type, roles);
	}
	
	/**
	 * The query is limited to the default group and the business group (as specified by
	 * the type parameter).
	 * 
	 * @param res The list repository entry references
	 * @param type The type of relation (only default group, only busines groups or both)
	 * @param roles The roles (optional)
	 * @return A list of identity
	 */
	public List<Identity> getMembers(List<? extends RepositoryEntryRef> res, RepositoryEntryRelationType type, String... roles) {
		if(res == null || res.isEmpty()) return Collections.emptyList();
		
		List<String> roleList = GroupRoles.toList(roles);
		List<Long> repoKeys = res.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList());
		return getMembers(repoKeys, roleList, type, Identity.class);
	}
	
	/**
	 * The query is limited to the default group and the business group (as specified by
	 * the type parameter).
	 * 
	 * @param re The repository entry
	 * @param type The type of relation (only default group, only busines groups or both)
	 * @param roles The roles (optional)
	 * @return A list of identity keys
	 */
	public List<Long> getMemberKeys(RepositoryEntryRef re, RepositoryEntryRelationType type, String... roles) {
		List<String> roleList = GroupRoles.toList(roles);
		List<Long> repoKeys = Collections.singletonList(re.getKey());
		return getMembers(repoKeys, roleList, type, Long.class);
	}
	
	private <U> List<U> getMembers(List<Long> repoKeys, List<String> roleList,
			RepositoryEntryRelationType type, Class<U> resultClass) {
		StringBuilder sb = new StringBuilder(512);
		if(resultClass.equals(Identity.class)) {
			sb.append("select ident");
		} else {
			sb.append("select memberships.identity.key");
		}

		sb.append(" from repositoryentry as v")
		  .append(" inner join v.groups as relGroup");
		if(type == RepositoryEntryRelationType.defaultGroup) {
			sb.append(" on relGroup.defaultGroup=true");
		}
		sb.append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as memberships");
		if(resultClass.equals(Identity.class)) {
			sb.append(" inner join memberships.identity as ident")
			  .append(" inner join fetch ident.user as identUser");
		}
		if(type == RepositoryEntryRelationType.businessGroups) {
			sb.append(" inner join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)");
		} else if(type == RepositoryEntryRelationType.curriculums) {
			sb.append(" inner join curriculumelement as curEl on (curEl.group.key=baseGroup.key)");
		} else if(type == RepositoryEntryRelationType.entryAndCurriculums) {
			sb.append(" left join curriculumelement as curEl on (curEl.group.key=baseGroup.key)");
		}
		sb.append(" where v.key in (:repoKeys) and memberships.role in (:roles)");
		if(type == RepositoryEntryRelationType.entryAndCurriculums) {
			sb.append(" and (relGroup.defaultGroup=true or curEl.key is not null)");
		}
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), resultClass)
				.setParameter("repoKeys", repoKeys)
				.setParameter("roles", roleList)
				.getResultList().stream()
				.distinct()
				.collect(Collectors.toList());
	}
	

	public List<Identity> getRelatedMembers(RepositoryEntryRef re, IdentityRef sourceIdentity, GroupRoles sourceRole, GroupRoles targetRole) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select distinct ident");
		sb.append(" from repositoryentry as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as memberships")
		  .append(" inner join memberships.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join baseGroup.members as sourceMember")
		  .append(" where v.key=:repoKey and memberships.role=:targetRole and sourceMember.identity.key=:sourceIdentityKey")
		  .append(" and sourceMember.role=:sourceRole");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repoKey", re.getKey())
				.setParameter("sourceIdentityKey", sourceIdentity.getKey())
				.setParameter("sourceRole", sourceRole.toString())
				.setParameter("targetRole", targetRole.toString())
				.getResultList();
	}
	
	public boolean removeMembers(RepositoryEntry re, List<Identity> members) {
		Group group = getDefaultGroup(re);
		if(group != null) {
			for(Identity member:members) {
				groupDao.removeMembership(group, member);
			}
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
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<RepositoryEntryToGroupRelation> rels = em.createNamedQuery("relationByRepositoryEntryAndGroup", RepositoryEntryToGroupRelation.class)
			.setParameter("repoKey", re.getKey())
			.setParameter("groupKey", group.getKey())
			.getResultList();

		for(RepositoryEntryToGroupRelation rel:rels) {
			em.remove(rel);
		}
		return rels.size();
	}
	
	/**
	 * This will remove all relations from the repository entry,
	 * the default one too.
	 * 
	 * @param re
	 * @return
	 */
	public int removeRelations(RepositoryEntryRef re) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<RepositoryEntryToGroupRelation> rels = em.createNamedQuery("relationByRepositoryEntry", RepositoryEntryToGroupRelation.class)
			.setParameter("repoKey", re.getKey())
			.getResultList();
		for(RepositoryEntryToGroupRelation rel:rels) {
			em.remove(rel);
		}
		return rels.size();
	}

	public int removeRelation(Group group) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<RepositoryEntryToGroupRelation> rels = em.createNamedQuery("relationByGroup", RepositoryEntryToGroupRelation.class)
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
	
	public void removeRelation(RepositoryEntryToGroupRelation rel) {
		dbInstance.getCurrentEntityManager().remove(rel);
	}
	
	/**
	 * Count the number of relation from a group to repository entries
	 * 
	 * @param group
	 * @return The number of relations
	 */
	public int countRelations(Group group) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select count(rel) from repoentrytogroup as rel")
		  .append(" where rel.group.key=:groupKey");

		Number count = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Number.class)
			.setParameter("groupKey", group.getKey())
			.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public List<RepositoryEntryToGroupRelation> getBusinessGroupAndCurriculumRelations(RepositoryEntryRef re) {
		if(re == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(512);
		sb.append("select rel from repoentrytogroup as rel")
		  .append(" inner join fetch rel.entry as entry")
		  .append(" inner join fetch rel.group as baseGroup")
		  .append(" left join businessgroup as bgp on (bgp.baseGroup.key=baseGroup.key)")
		  .append(" left join curriculumelement curEl on (curEl.group.key=baseGroup.key)")
		  .append(" where entry.key=:repoKey and (curEl.key is not null or bgp.key is not null)");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("repoKey", re.getKey())
			.getResultList();
	}
	
	public List<RepositoryEntryToGroupRelation> getCurriculumRelations(CurriculumElementRef curriculumElement) {
		if(curriculumElement == null || curriculumElement.getKey() == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(512);
		sb.append("select rel from repoentrytogroup as rel")
		  .append(" inner join fetch rel.entry as entry")
		  .append(" inner join fetch rel.group as baseGroup")
		  .append(" inner join curriculumelement curEl on (curEl.group.key=baseGroup.key)")
		  .append(" where curEl.key=:elementKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("elementKey", curriculumElement.getKey())
			.getResultList();
	}
	
	public boolean hasRelation(Group group, RepositoryEntryRef re) {
		if(re == null || group == null) return false;
		
		String query = "select rel.key from repoentrytogroup as rel where rel.entry.key=:repoKey and rel.group.key=:groupKey";

		List<Long> relations = dbInstance.getCurrentEntityManager()
			.createQuery(query, Long.class)
			.setParameter("repoKey", re.getKey())
			.setParameter("groupKey", group.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return relations != null && !relations.isEmpty() && relations.get(0) != null;
	}
	
	
	/**
	 * Get the relation from a base group to the repository entries
	 * 
	 * @param groups
	 * @return The list of relations
	 */
	public List<RepositoryEntryToGroupRelation> getRelations(List<Group> groups) {
		if(groups == null || groups.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from repoentrytogroup as rel")
		  .append(" inner join fetch rel.entry as entry")
		  .append(" inner join fetch rel.group as baseGroup")
		  .append(" where baseGroup in (:groups)");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("groups", groups)
			.getResultList();
	}
	
	public RepositoryEntryToGroupRelation loadRelationByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from repoentrytogroup as rel")
		  .append(" inner join fetch rel.entry as entry")
		  .append(" inner join fetch rel.group as baseGroup")
		  .append(" where rel.key=:key");

		List<RepositoryEntryToGroupRelation> relations = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntryToGroupRelation.class)
			.setParameter("key", key)
			.getResultList();
		return relations == null || relations.isEmpty() ? null : relations.get(0);
	}
	
	public List<Long> getBusinessGroupsKeyOwnedAsAuthor(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bg.key from repoentrytogroup as rel")
		  .append(" inner join rel.group as reBaseGroup on (rel.defaultGroup=true)")
		  .append(" inner join reBaseGroup.members as reMember on (reMember.identity.key=:ownerKey and reMember.role='").append(GroupRoles.owner.name()).append("')")
		  .append(" inner join rel.entry as v")
		  .append(" inner join v.groups as relGroup on (relGroup.defaultGroup=false)")
		  .append(" inner join relGroup.group as bgBaseGroup")
		  .append(" inner join businessgroup as bg on (bg.baseGroup.key=bgBaseGroup.key)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("ownerKey", owner.getKey())
				.getResultList();
	}
	
	public List<MembershipInfos> getMembership(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select v.key, v.displayname, reMember.key, reMember.role, reMember.creationDate,")
		  .append(" userinfos.initialLaunch, userinfos.recentLaunch, userinfos.visit")
		  .append(" from repositoryentry as v")
		  .append(" inner join v.groups as rel")
		  .append(" inner join rel.group as bGroup")
		  .append(" inner join bGroup.members as reMember")
		  .append(" left join usercourseinfos as userinfos on (v.olatResource.key=userinfos.resource.key)")
		  .append(" where reMember.identity.key=:identityKey");
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		
		Set<Long> memberKeys = new HashSet<>();
		List<MembershipInfos> memberhips = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			int col = 0;
			Long entryKey = (Long)rawObject[col++];
			String displayName = (String)rawObject[col++];
			Long memberKey = (Long)rawObject[col++];
			if(memberKeys.contains(memberKey)) {
				continue;//duplicate
			}
			memberKeys.add(memberKey);
			
			String role = (String)rawObject[col++];
			Date creationDate = (Date)rawObject[col++];
			Date initialLaunch = (Date)rawObject[col++];
			Date recentLaunch = (Date)rawObject[col++];
			Long visit =  PersistenceHelper.extractLong(rawObject, col);
			memberhips.add(new MembershipInfos(identity.getKey(), entryKey, displayName, role, creationDate, initialLaunch, recentLaunch, visit));
		}
		return memberhips;
	}
	
	public List<Organisation> getOrganisations(Collection<? extends RepositoryEntryRef> entries) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct relOrg from organisation as relOrg")
		  .append(" inner join fetch relOrg.group as bGroup")
		  .append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" where rel.entry.key in :repoKeys");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Organisation.class)
			.setParameter("repoKeys", entries.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList()))
			.getResultList();
	}
	
	public List<RepositoryEntry> getRepositoryEntries(OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select v from organisation as relOrg")
		  .append(" inner join relOrg.group as bGroup")
		  .append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" inner join rel.entry v")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where relOrg.key=:organisationKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntry.class)
			.setParameter("organisationKey", organisation.getKey())
			.getResultList();
	}
	
	public boolean hasRelationsInQualityManagement(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select context.key from qualitycontext as context")
		  .append(" where context.audienceRepositoryEntry.key=:entryKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("entryKey", entry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}
	
	public List<Long> getBrokenGroupDependencies(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select relGroup.key, relGroup.defaultGroup, baseGroup.key, businessGroup.key, curEl.key, relOrg.key")
		  .append(" from repoentrytogroup as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" left join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)")
		  .append(" left join curriculumelement as curEl on (curEl.group.key=baseGroup.key)")
		  .append(" left join organisation as relOrg on (relOrg.group.key=baseGroup.key)")
		  .append(" where relGroup.defaultGroup=false and businessGroup.key is null and curEl.key is null and relOrg.key is null")
		  .append(" order by relGroup.key");

		List<Object[]> dependenciesRaw = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
		List<Long> relationKeys = new ArrayList<>();
		for(Object[] dependencies:dependenciesRaw) {
			Long relationKey = (Long)dependencies[0];
			Boolean defaultGroup = (Boolean)dependencies[1];
			Long businessGroupKey = (Long)dependencies[3];
			Long curriculumElementKey = (Long)dependencies[4];
			Long organisationKey = (Long)dependencies[5];
			if(defaultGroup != null && !defaultGroup.booleanValue()
					&& businessGroupKey == null && curriculumElementKey == null && organisationKey == null) {
				relationKeys.add(relationKey);
			}
			
		}
		return relationKeys;
	}
}
