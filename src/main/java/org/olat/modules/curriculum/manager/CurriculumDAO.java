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
package org.olat.modules.curriculum.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	public Curriculum createAndPersist(String identifier, String displayName, String description, Organisation organisation) {
		CurriculumImpl curriculum = new CurriculumImpl();
		curriculum.setCreationDate(new Date());
		curriculum.setLastModified(curriculum.getCreationDate());
		curriculum.setGroup(groupDao.createGroup());
		curriculum.setStatus(CurriculumStatus.active.name());
		curriculum.setDisplayName(displayName);
		curriculum.setIdentifier(identifier);
		curriculum.setDescription(description);
		curriculum.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(curriculum);
		return curriculum;
	}
	
	public List<Curriculum> loadAllCurriculums() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select cur from curriculum cur")
		  .append(" left join fetch cur.organisation org")
		  .append(" inner join fetch cur.group baseGroup");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Curriculum.class)
			.getResultList();
	}
	
	public Curriculum loadByKey(Long key) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select cur from curriculum cur")
		  .append(" left join fetch cur.organisation org")
		  .append(" inner join fetch cur.group baseGroup")
		  .append(" where cur.key=:key");
		
		List<Curriculum> curriculums = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Curriculum.class)
			.setParameter("key", key)
			.getResultList();
		return curriculums == null || curriculums.isEmpty() ? null : curriculums.get(0);
	}

	public List<Curriculum> loadByKeys(Collection<? extends CurriculumRef> refs) {
		if (refs == null || refs.isEmpty()) return new ArrayList<>(0);
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("select cur from curriculum cur")
		  .append(" left join fetch cur.organisation org")
		  .append(" inner join fetch cur.group baseGroup")
		  .append(" where cur.key in :keys");
		
		List<Long> keys = refs.stream().map(CurriculumRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Curriculum.class)
			.setParameter("keys", keys)
			.getResultList();
	}
	
	public List<Curriculum> getMyCurriculums(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select cur from curriculum cur")
		  .append(" left join fetch cur.organisation organis")
		  .append(" inner join fetch cur.group baseGroup")
		  .append(" where exists (select curElement from curriculumelement curElement")
		  .append("  inner join curElement.group as bGroup")
		  .append("  inner join bGroup.members membership")
		  .append("  where curElement.curriculum.key=cur.key and membership.identity.key=:memberKey and membership.role ").in(CurriculumRoles.participant, CurriculumRoles.coach, CurriculumRoles.owner)
		  .append(" )")
		  .append(" and (cur.status is null or cur.status ").in(CurriculumStatus.active.name()).append(")");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Curriculum.class)
				.setParameter("memberKey", identity.getKey())
				.getResultList();
	}
	
	public boolean hasMyCurriculums(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select curElement.key from curriculumelement curElement")
		  .append(" inner join curElement.curriculum as cur")
		  .append(" inner join curElement.group as bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" where membership.identity.key=:memberKey and membership.role ").in(CurriculumRoles.participant, CurriculumRoles.coach, CurriculumRoles.owner)
		  .append(" and (cur.status is null or cur.status ").in(CurriculumStatus.active.name()).append(")");

		List<Long> curriculumKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("memberKey", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return curriculumKeys != null && !curriculumKeys.isEmpty() && curriculumKeys.get(0) != null;
	}
	
	public List<Long> getMyActiveCurriculumKeys(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select curElement.curriculum.key from curriculumelement curElement")
		  .append(" inner join curElement.group as bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" where membership.identity.key=:memberKey and membership.role ").in(CurriculumRoles.participant, CurriculumRoles.coach, CurriculumRoles.owner)
		  .append(" and curElement.status='active' and exists (select v from repoentrytogroup as rel")
		  .append("  inner join rel.entry as v")
		  .append("  where curElement.group.key=rel.group.key and v.status ").in(RepositoryEntryStatusEnum.published)
		  .append(" )");
	
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("memberKey", identity.getKey())
				.getResultList();
	}
	
	public List<Curriculum> search(CurriculumSearchParameters params) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select cur from curriculum cur")
		  .append(" ").append(params.getOrganisations().isEmpty() ? "left" : "inner").append(" join fetch cur.organisation organis")
		  .append(" inner join fetch cur.group baseGroup");
		
		
		if(!params.isWithDeleted()) {
			sb.and()
			  .append(" (cur.status is null or cur.status ").in(CurriculumStatus.active.name()).append(")");
		}

		if(!params.getOrganisations().isEmpty()) {
			sb.and()
			  .append(" organis.key in (:organisationKeys)");
		}
		
		Long key = null;
		String ref = null;
		String fuzzyRef = null;
		if(StringHelper.containsNonWhitespace(params.getSearchString())) {
			ref = params.getSearchString();
			fuzzyRef = PersistenceHelper.makeFuzzyQueryString(ref);
			
			sb.and()
			  .append(" (cur.externalId=:ref or ")
			  .likeFuzzy("cur.displayName", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("cur.identifier", "fuzzyRef")
			  .append(" or exists (select curEl.key from curriculumelement as curEl where")
			  .append("  curEl.curriculum.key=cur.key and")
			  .likeFuzzy("curEl.identifier", "fuzzyRef")
			  .append(")");
			if(StringHelper.isLong(ref)) {
				key = Long.valueOf(ref);
				sb.append(" or cur.key=:curriculumKey");
			}
			sb.append(")");	
		}
		
		if(params.getCurriculumAdmin() != null) {
			sb.and()
			  .append("exists (select membership.key from bgroupmember as membership")
			  .append("  where membership.identity.key=:managerKey")
			  .append("  and (membership.group.key=baseGroup.key or organis.group.key=baseGroup.key)")
			  .append("  and role ").in(CurriculumRoles.curriculummanager, CurriculumRoles.curriculumowner)
			  .append(")");
		}

		TypedQuery<Curriculum> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Curriculum.class);
		if(!params.getOrganisations().isEmpty()) {
			List<Long> organisationKeys = params.getOrganisations()
					.stream().map(OrganisationRef::getKey).collect(Collectors.toList());
			query.setParameter("organisationKeys", organisationKeys);
		}
		if(key != null) {
			query.setParameter("curriculumKey", key);
		}
		if(ref != null) {
			query.setParameter("ref", ref);
		}
		if(fuzzyRef != null) {
			query.setParameter("fuzzyRef", fuzzyRef);
		}
		if(params.getCurriculumAdmin() != null) {
			query.setParameter("managerKey", params.getCurriculumAdmin().getKey());
		}
		return query.getResultList();
	}
	
	public List<CurriculumInfos> searchWithInfos(CurriculumSearchParameters params) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select cur,")
		  .append(" (select count(curElement.key) from curriculumelement curElement")
		  .append("  where curElement.curriculum.key=cur.key and curElement.status ").in(CurriculumElementStatus.active, CurriculumElementStatus.inactive)
		  .append(" ) as numOfElements")
		  .append(" from curriculum cur")
		  .append(" inner join fetch cur.group baseGroup")
		  .append(" ").append(params.getOrganisations().isEmpty() ? "left" : "inner").append(" join fetch cur.organisation organis");
		
		if(!params.isWithDeleted()) {
			sb.and()
			  .append(" (cur.status is null or cur.status ").in(CurriculumStatus.active.name()).append(")");
		}
		
		if(!params.getOrganisations().isEmpty()) {
			sb.and().append(" organis.key in (:organisationKeys)");
		}
		
		Long key = null;
		String ref = null;
		String fuzzyRef = null;
		if(StringHelper.containsNonWhitespace(params.getSearchString())) {
			ref = params.getSearchString();
			fuzzyRef = PersistenceHelper.makeFuzzyQueryString(ref);
			
			sb.and()
			  .append(" (cur.externalId=:ref or ")
			  .likeFuzzy("cur.displayName", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("cur.identifier", "fuzzyRef")
			  .append(" or exists (select curEl.key from curriculumelement as curEl where")
			  .append("  curEl.curriculum.key=cur.key and")
			  .likeFuzzy("curEl.identifier", "fuzzyRef")
			  .append(")");
			if(StringHelper.isLong(ref)) {
				key = Long.valueOf(ref);
				sb.append(" or cur.key=:curriculumKey");
			}
			sb.append(")");	
		}
		
		if(params.getElementOwner() != null || params.getCurriculumAdmin() != null || params.getCurriculumPrincipal() != null) {
			sb.and()
			  .append("(");
			
			boolean needOr = false;
		
			if(params.getElementOwner() != null) {
				needOr = true;
				sb.append("exists (select courseCurEl.key from curriculumelement as courseCurEl")
				  .append(" inner join repoentrytogroup as curRelGroup on (courseCurEl.group.key=curRelGroup.group.key)")
				  .append(" inner join repoentrytogroup as courseRelGroup on (courseRelGroup.entry.key=curRelGroup.entry.key)")
				  .append(" inner join courseRelGroup.group as courseBaseGroup")
				  .append(" inner join courseBaseGroup.members as courseMembership")
				  .append(" where courseMembership.identity.key=:ownerKey and courseMembership.role='").append(GroupRoles.owner.name()).append("'")
				  .append(" and courseCurEl.curriculum.key=cur.key")
				  .append(") or exists (select ownedCurEl.key from curriculumelement as ownedCurEl")
				  .append(" inner join ownedCurEl.group as ownedBaseGroup")
				  .append(" inner join ownedBaseGroup.members as ownedMembership")
				  .append(" where ownedMembership.identity.key=:ownerKey and ownedMembership.role ").in(CurriculumRoles.curriculumelementowner, CurriculumRoles.owner)
				  .append(" and ownedCurEl.curriculum.key=cur.key")
				  .append(")");
			}
			
			if(params.getCurriculumAdmin() != null) {
				if(needOr) {
					sb.append(" or ");
				}
				needOr = true;
				sb.append("exists (select membership.key from bgroupmember as membership")
				  .append("  where membership.identity.key=:managerKey")
				  .append("  and (membership.group.key=baseGroup.key or membership.group.key=organis.group.key)")
				  .append("  and role ").in(CurriculumRoles.curriculumowner, CurriculumRoles.curriculummanager, OrganisationRoles.administrator)
				  .append(")");
			}
			
			if(params.getCurriculumPrincipal() != null) {
				if(needOr) {
					sb.append(" or ");
				}
				sb.append("exists (select membership.key from bgroupmember as membership")
				  .append("  where membership.identity.key=:principalKey")
				  .append("  and (membership.group.key=baseGroup.key or membership.group.key=organis.group.key)")
				  .append("  and role ").in(OrganisationRoles.principal)
				  .append(")");
			}
			
			sb.append(")");
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(!params.getOrganisations().isEmpty()) {
			List<Long> organisationKeys = params.getOrganisations()
					.stream().map(OrganisationRef::getKey).collect(Collectors.toList());
			query.setParameter("organisationKeys", organisationKeys);
		}
		if(key != null) {
			query.setParameter("curriculumKey", key);
		}
		if(ref != null) {
			query.setParameter("ref", ref);
		}
		if(fuzzyRef != null) {
			query.setParameter("fuzzyRef", fuzzyRef);
		}
		if(params.getCurriculumAdmin() != null) {
			query.setParameter("managerKey", params.getCurriculumAdmin().getKey());
		}
		if(params.getCurriculumPrincipal() != null) {
			query.setParameter("principalKey", params.getCurriculumPrincipal().getKey());
		}
		if(params.getElementOwner() != null) {
			query.setParameter("ownerKey", params.getElementOwner().getKey());
		}
		
		List<Object[]> rawObjects = query.getResultList();
		List<CurriculumInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			Curriculum curriculum = (Curriculum)rawObject[0];
			Long numOfElements = PersistenceHelper.extractLong(rawObject, 1);
			infos.add(new CurriculumInfos(curriculum, numOfElements));
		}
		return infos;
	}
	
	public Curriculum update(Curriculum curriculum) {
		((CurriculumImpl)curriculum).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(curriculum);
	}
	
	public void delete(CurriculumImpl curriculum) {
		Group group = curriculum.getGroup();
		groupDao.removeMemberships(group);
		dbInstance.getCurrentEntityManager().remove(curriculum);
		groupDao.removeGroup(group);
	}
	
	public Curriculum flagAsDelete(CurriculumImpl curriculum) {
		Group group = curriculum.getGroup();
		groupDao.removeMemberships(group);
		curriculum.setStatus(CurriculumStatus.deleted.name());
		return update(curriculum);
	}
	
	public List<Identity> getMembersIdentity(CurriculumRef curriculum, String role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from curriculum cur")
		  .append(" inner join cur.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where cur.key=:curriculumKey and membership.role=:role");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("curriculumKey", curriculum.getKey())
				.setParameter("role", role)
				.getResultList();
	}
	
	public boolean hasCurriculumRole(IdentityRef identity, String role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select cur.key from curriculum cur")
		  .append(" inner join cur.group baseGroup")
		  .append(" left join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role");
		List<Long> has = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("role", role)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return has != null && !has.isEmpty() && has.get(0) != null;
	}
	
	public boolean hasOwnerRoleInCurriculumElement(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select v.key from repositoryentry v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join v.groups as curriculumRelGroup")
		  .append(" inner join curriculumelement as curEl on (curEl.group.key = curriculumRelGroup.group.key)")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role");
		List<Long> has = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("role", GroupRoles.owner.name())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return has != null && !has.isEmpty() && has.get(0) != null;
	}
	
	public boolean hasRoleExpanded(CurriculumRef curriculum, IdentityRef identity, String... roles) {
		List<String> roleList = GroupRoles.toList(roles);
		StringBuilder sb = new StringBuilder(256);
		sb.append("select cur.key from curriculum cur")
		  .append(" inner join cur.group baseGroup")
		  .append(" left join baseGroup.members membership")
		  .append(" left join cur.organisation organisation")
		  .append(" left join organisation.group orgGroup")
		  .append(" left join orgGroup.members orgMembership")
		  .append(" where cur.key=:curriculumKey and (")
		  .append("  (membership.identity.key=:identityKey and membership.role in (:roles))")
		  .append("  or")
		  .append("  (orgMembership.identity.key=:identityKey and orgMembership.role in (:roles))")
		  .append(")");
		List<Long> has = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("curriculumKey", curriculum.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("roles", roleList)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return has != null && !has.isEmpty() && curriculum.getKey().equals(has.get(0));
	}
	
}
