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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.model.CurriculumElementKeyToRepositoryEntryKey;
import org.olat.modules.curriculum.model.CurriculumElementWebDAVInfos;
import org.olat.modules.curriculum.model.RepositoryEntryInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumRepositoryEntryRelationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry) {
		String query = """
				select el from curriculumelement as el
				inner join fetch el.group as bGroup
				inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)
				where rel.entry.key=:repoKey""";

		return dbInstance.getCurrentEntityManager()
			.createQuery(query, CurriculumElement.class)
			.setParameter("repoKey", entry.getKey())
			.getResultList();
	}
	
	public CurriculumElement getDefaultCurriculumElement(RepositoryEntryRef entry) {
		String query = """
				select el from curriculumelement as el
				inner join fetch el.group as bGroup
				inner join repoentrytogroup as rel on (bGroup.key=rel.group.key and rel.defaultElement=true)
				where rel.entry.key=:repoKey""";

		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
			.createQuery(query, CurriculumElement.class)
			.setParameter("repoKey", entry.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return elements != null && !elements.isEmpty() ? elements.get(0) : null;
	}
	
	public long countRepositoryEntries(CurriculumElementRef element) {
		String query = """
				select count(distinct v.key) from repositoryentry as v
				inner join v.groups as rel
				inner join curriculumelement as el on (el.group.key=rel.group.key)
				where el.key=:elementKey""";
		
		List<Number> count = dbInstance.getCurrentEntityManager().createQuery(query, Number.class)
				.setParameter("elementKey", element.getKey())
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null ? count.get(0).longValue() : 0l;
	}
	
	public boolean hasRepositoryEntries(CurriculumElementRef element) {
		String query = """
				select v.key from repositoryentry as v
				inner join v.groups as rel
				inner join curriculumelement as el on (el.group.key=rel.group.key)
				where el.key=:elementKey""";
		
		List<Long> keys = dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.setParameter("elementKey", element.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0l;
	}
	
	public List<RepositoryEntry> getRepositoryTemplates(CurriculumElementRef element) {
		if(element == null) return new ArrayList<>();
		
		String query = """
				select distinct v from repositoryentry as v
				inner join fetch v.olatResource as ores
				inner join fetch v.statistics as statistics
				left join fetch v.lifecycle as lifecycle
				inner join repotemplatetogroup as rel on (rel.entry.key=v.key)
				inner join curriculumelement as el on (el.group.key=rel.group.key)
				where el.key = :elementKey""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, RepositoryEntry.class)
				.setParameter("elementKey", element.getKey())
				.getResultList();
	}
	
	public List<RepositoryEntry> getRepositoryEntries(CurriculumRef curriculum,
			Collection<? extends CurriculumElementRef> elements, RepositoryEntryStatusEnum[] status,
			boolean onlyWithLectures, IdentityRef identity, List<String> roles) {
		if((elements == null || elements.isEmpty()) && curriculum == null) return new ArrayList<>();
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select distinct v from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as rel")
		  .append(" inner join curriculumelement as el on (el.group.key=rel.group.key)")
		  .append(" where v.status ").in(status);
		if(elements != null && !elements.isEmpty()) {
			sb.append(" and el.key in (:elementKeys)");
		}
		if(curriculum != null) {
			sb.append(" and el.curriculum.key=:curriculumKey");
		}
		
		if(onlyWithLectures) {
			sb.append(" and exists (select lectureConfig.key from lectureentryconfig as lectureConfig")
			  .append("  where lectureConfig.entry.key=v.key and lectureConfig.lectureEnabled=true")
			  .append(" )");
		}
		if(identity != null && roles != null && !roles.isEmpty()) {
			sb.append(" and exists (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("  where rel.group.key=membership.group.key and rel.entry.key=v.key and membership.identity.key=:identityKey")
			  .append("  and membership.role in (:roles)")
			  .append(" )");
		}
		
		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntry.class);
		if(elements != null && !elements.isEmpty()) {
			List<Long> elementKeys = elements
					.stream().map(CurriculumElementRef::getKey).collect(Collectors.toList());
			query.setParameter("elementKeys", elementKeys);
		}
		if(curriculum != null) {
			query.setParameter("curriculumKey", curriculum.getKey());
		}
		if(identity != null && roles != null && !roles.isEmpty()) {
			query.setParameter("identityKey", identity.getKey());
			query.setParameter("roles", roles);
		}
		return query.getResultList();
	}
	
	public Map<Long, Set<RepositoryEntry>> getCurriculumElementKeyToRepositoryEntries(Collection<? extends CurriculumElementRef> elements) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select el.key")
		  .append("     , v")
		  .append("  from repositoryentry as v")
		  .append(" inner join v.groups as rel")
		  .append(" inner join curriculumelement as el on (el.group.key=rel.group.key)");
		if(elements != null && !elements.isEmpty()) {
			sb.append(" and el.key in (:elementKeys)");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class);
		if(elements != null && !elements.isEmpty()) {
			query.setParameter("elementKeys", elements.stream().map(CurriculumElementRef::getKey).collect(Collectors.toList()));
		}
		
		List<Object[]> resultList = query.getResultList();
		
		Map<Long, Set<RepositoryEntry>> curriculumElementKeyRepositoryEntries = new HashMap<>();
		for (Object[] result : resultList) {
			Long curriculumElementKey = (Long)result[0];
			RepositoryEntry repositoryEntry = (RepositoryEntry)result[1];
			curriculumElementKeyRepositoryEntries.computeIfAbsent(curriculumElementKey, key -> new HashSet<>()).add(repositoryEntry);
		}
		
		return curriculumElementKeyRepositoryEntries;
	}
	
	public List<CurriculumElementWebDAVInfos> getCurriculumElementInfosForWebDAV(IdentityRef identity, List<String> roles) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.entry.key, el.key, el.displayName, el.identifier,")
		  .append(" parentEl.key, parentEl.displayName, parentEl.identifier")
		  .append(" from curriculumelement as el")
		  .append(" inner join el.group as bGroup")
		  .append(" inner join bGroup.members as memberships")
		  .append(" left join el.parent as parentEl")
		  .append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" where memberships.identity.key=:identityKey and memberships.role in (:roles)")
		  .append(" group by rel.entry.key, el.key, el.displayName, el.identifier, parentEl.key, parentEl.displayName, parentEl.identifier");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("roles", roles)
				.getResultList();
		List<CurriculumElementWebDAVInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			Long repositoryEntryKey = (Long)rawObject[0];
			Long curriculumElementKey = (Long)rawObject[1];
			String curriculumElementDisplayName = (String)rawObject[2];
			String curriculumElementIdentifier = (String)rawObject[3];
			Long parentCurriculumElementKey = (Long)rawObject[4];
			String parentCurriculumElementDisplayName = (String)rawObject[5];
			String parentCurriculumElementIdentifier = (String)rawObject[6];	
			infos.add(new CurriculumElementWebDAVInfos(repositoryEntryKey,
					curriculumElementKey, curriculumElementDisplayName, curriculumElementIdentifier,
					parentCurriculumElementKey, parentCurriculumElementDisplayName, parentCurriculumElementIdentifier));
		}
		return infos;
	}
	
	public List<RepositoryEntryInfos> getRepositoryEntriesWithInfos(CurriculumElementRef curriculumElement) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select distinct v,")
		  .append(" (select count(distinct lblock.key) from lectureblock lblock")
		  .append("  where lblock.entry.key=v.key")
		  .append(" ) as numOfLectures")
		  .append(" from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as rel")
		  .append(" inner join curriculumelement as el on (el.group.key=rel.group.key)")
		  .append(" where el.key=:curriculumElementKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("curriculumElementKey", curriculumElement.getKey())
				.getResultList();
		List<RepositoryEntryInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			RepositoryEntry entry = (RepositoryEntry)rawObject[0];
			long numOfLectureBlocks = PersistenceHelper.extractPrimitiveLong(rawObject, 1);
			infos.add(new RepositoryEntryInfos(entry, numOfLectureBlocks));
		}
		return infos;
	}
	
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry, Identity identity, Collection<CurriculumRoles> roles) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el from curriculumelement as el")
		  .append(" inner join fetch el.group as bGroup")
		  .append(" inner join bGroup.members as memberships")
		  .append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" inner join fetch el.curriculum as curriculum")
		  .append(" left join fetch el.parent as parentEl")
		  .append(" where rel.entry.key=:repoKey")
		  .append("   and memberships.identity.key=:identityKey");
		if (roles != null && !roles.isEmpty()) {
			sb.append(" and memberships.role in (:roles)");
		}
		
		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CurriculumElement.class)
			.setParameter("repoKey", entry.getKey())
			.setParameter("identityKey", identity.getKey());
		if (roles != null && !roles.isEmpty()) {
			query.setParameter("roles", getRoleNames(roles));
		}
		return query.getResultList();
	}
	
	private List<String> getRoleNames(Collection<CurriculumRoles> roles) {
		return roles.stream().map(CurriculumRoles::name).collect(Collectors.toList());
	}
	
	/**
	 * The method return all the elements of the curriculum and if
	 * needed with an empty list of repository entries.
	 * 
	 * @param curriculums A list of curriculum objects
	 * @return A map of curriculum element to their repository entries
	 */
	public Map<CurriculumElement, List<Long>> getCurriculumElementsWithRepositoryEntryKeys(List<? extends CurriculumRef> curriculums,
			CurriculumElementStatus[] status) {
		if(curriculums == null || curriculums.isEmpty()) return Collections.emptyMap();
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select el, rel.entry.key from curriculumelement el")
		  .append(" left join fetch el.type elementType")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" left join fetch el.parent parentEl")
		  .append(" left join fetch parentEl.parent parentParentEl")
		  .append(" left join fetch parentParentEl.parent parentParentParentEl")
		  .append(" left join repoentrytogroup as rel on (el.group.key=rel.group.key)")
		  .append(" where curriculum.key in (:curriculumKeys) and el.status ").in(status);

		List<Long> curriculumKeys = curriculums.stream()
				.map(CurriculumRef::getKey).collect(Collectors.toList());
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("curriculumKeys", curriculumKeys)
			.getResultList();
		
		Map<CurriculumElement, List<Long>> map = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			CurriculumElement element = (CurriculumElement)rawObject[0];
			Long repoKey = (Long)rawObject[1];
			List<Long> entries = map.computeIfAbsent(element, el -> new ArrayList<>());
			if(repoKey != null) {
				entries.add(repoKey);
			}
		}
		return map;
	}
	
	public List<CurriculumElementKeyToRepositoryEntryKey> getRepositoryEntryKeyToCurriculumElementKeys(
			List<? extends CurriculumElementRef> curriculumElements) {
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select distinct el.key");
		sb.append("     , rel.entry.key");
		sb.append("  from curriculumelement as el");
		sb.append(" inner join el.group as bGroup");
		sb.append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)");
		sb.and().append("el.key in :curriculumElementKeys");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("curriculumElementKeys", curriculumElements.stream().map(CurriculumElementRef::getKey).collect(Collectors.toList()))
			.getResultList()
			.stream()
			.map(rawObject -> new CurriculumElementKeyToRepositoryEntryKey((Long)rawObject[0], (Long)rawObject[1]))
			.toList();
	}

	/**
	 * Returns a map that associates a repository entry with a set of curriculum elements that refer to the repository entry.
	 * It uses the group relationship where a repository entry is part of the curriculum element group.
	 *
	 * @param repositoryEntries A collection of repository entries to retrieve the curriculum elements for.
	 * @return A map with repository entries (typically courses) as keys and sets of curriculum elements as values.
	 */
	public Map<RepositoryEntryRef, Set<CurriculumElement>> getCurriculumElementsForRepositoryEntries(Collection<RepositoryEntryRef> repositoryEntries) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct ce, re.key from curriculumelement ce");
		sb.append(" inner join repoentrytogroup r2g on r2g.group.key = ce.group.key");
		sb.append(" inner join repositoryentry re on r2g.entry.key = re.key");
		sb.append(" left join fetch ce.type ceType ");
		sb.append(" where re.key in (:reKeys)");

		List<Long> reKeys = repositoryEntries.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList());
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("reKeys", reKeys).getResultList();

		Map<RepositoryEntryRef, Set<CurriculumElement>> result = new HashMap<>();
		for (Object[] rawObject : rawObjects) {
			CurriculumElement curriculumElement = (CurriculumElement) rawObject[0];
			Long repositoryEntryKey = (Long) rawObject[1];
			RepositoryEntryRef repositoryEntryRef = new RepositoryEntryRefImpl(repositoryEntryKey);
			Set<CurriculumElement> curriculumElements = result.computeIfAbsent(repositoryEntryRef, k -> new HashSet<>());
			curriculumElements.add(curriculumElement);
		}
		return result;
	}
}
