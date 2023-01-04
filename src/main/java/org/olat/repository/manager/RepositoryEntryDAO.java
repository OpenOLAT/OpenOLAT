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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 26.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryDAO {

	@Autowired
	private DB dbInstance;

	public RepositoryEntry loadByKey(Long key) {
		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntryByKey", RepositoryEntry.class)
				.setParameter("repoKey", key)
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	public List<RepositoryEntry> loadByKeys(Collection<Long> keys) {
		if(keys == null || keys.isEmpty()) return new ArrayList<>(1);
		
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntriesByKeys", RepositoryEntry.class)
				.setParameter("repoKeys", keys)
				.getResultList();
	}

	public List<RepositoryEntry> loadForMetaData(String status) {
		if(status.isEmpty()) return new ArrayList<>(1);

		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntriesForMetaData", RepositoryEntry.class)
				.setParameter("repoStatus", status)
				.getResultList();
	}

	public RepositoryEntry loadForUpdate(RepositoryEntry re) {
		//first remove it from caches
		dbInstance.getCurrentEntityManager().detach(re);

		StringBuilder query = new StringBuilder();
		query.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		     .append(" where v.key=:repoKey");

		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager().createQuery(query.toString(), RepositoryEntry.class)
				.setParameter("repoKey", re.getKey())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		return entries == null || entries.isEmpty() ? null : entries.get(0);
	}

	public RepositoryEntry loadByResourceKey(Long resourceKey) {
		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntryByResourceKey", RepositoryEntry.class)
				.setParameter("resourceKey", resourceKey)
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	public RepositoryEntry loadByResource(OLATResource resource) {
		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntryByResourceKey", RepositoryEntry.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}

	public List<RepositoryEntry> loadByResources(Collection<OLATResource> resources) {
		List<Long> resourceKeys = resources.stream().map(OLATResource::getKey).collect(Collectors.toList());
		return loadByResourceKeys(resourceKeys);
	}

	public List<RepositoryEntry> loadByResourceKeys(Collection<Long> resourceKeys) {
		if(resourceKeys == null || resourceKeys.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where ores.key in (:resourceKeys)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("resourceKeys", resourceKeys)
				.getResultList();
	}

	public List<RepositoryEntry> loadRepositoryEntriesByExternalId(String externalId) {
		if (externalId == null) return Collections.emptyList();
		String query = "select v from repositoryentry as v where v.externalId=:externalId";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setParameter("externalId", externalId)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}

	public List<RepositoryEntry> loadRepositoryEntriesByExternalRef(String externalRef) {
		if (externalRef == null) return Collections.emptyList();
		String query = "select v from repositoryentry as v where v.externalRef=:externalRef";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setParameter("externalRef", externalRef)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}
	
	public List<RepositoryEntry> loadRepositoryEntriesLikeExternalRef(String externalRef) {
		if (externalRef == null) return Collections.emptyList();
		
		String query = "select v from repositoryentry as v where v.externalRef like (:externalRef)";
		
		String externalRefParamater = new StringBuilder("%").append(externalRef).append("%").toString();
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setParameter("externalRef", externalRefParamater)
				.getResultList();
	}
	
	public List<RepositoryEntry> loadRepositoryEntries(int firstResult, int maxResult) {
		String query = "select v from repositoryentry as v order by v.key asc";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResult)
				.getResultList();
	}
	
	public RepositoryEntry loadByResourceId(String resourceName, Long resourceId) {
		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntryByResourceId", RepositoryEntry.class)
				.setParameter("resId", resourceId)
				.setParameter("resName", resourceName)
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	public List<RepositoryEntry> loadByResourceIds(String resourceName, Collection<Long> resourceIds) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRepositoryEntryByResourceIds", RepositoryEntry.class)
				.setParameter("resIds", resourceIds)
				.setParameter("resName", resourceName)
				.getResultList();
	}
	
	public List<RepositoryEntry> searchByIdAndRefs(String idAndRefs) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle");

		Long id = null;
		if(StringHelper.isLong(idAndRefs)) {
			try {
				id = Long.parseLong(idAndRefs);
			} catch (NumberFormatException e) {
				//
			}
		}
		sb.append(" where v.externalId=:ref or v.externalRef=:ref or v.softkey=:ref");
		if(id != null) {
			sb.append(" or v.key=:vKey or res.resId=:vKey");
		}
		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("ref", idAndRefs);
		if(id != null) {
			query.setParameter("vKey", id);
		}
		return query.getResultList();
	}

	public List<RepositoryEntry> getAllRepositoryEntries(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}

	public OLATResource loadRepositoryEntryResource(Long key) {
		if (key == null) return null;
		String query = "select v.olatResource from repositoryentry as v  where v.key=:repoKey";

		List<OLATResource> entries = dbInstance.getCurrentEntityManager()
				.createQuery(query, OLATResource.class)
				.setParameter("repoKey", key)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}

	public OLATResource loadRepositoryEntryResourceBySoftKey(String softkey) {
		if(softkey == null || "sf.notconfigured".equals(softkey)) {
			return null;
		}
		String query = "select v.olatResource from repositoryentry as v where v.softkey=:softkey";
		List<OLATResource> entries = dbInstance.getCurrentEntityManager()
				.createQuery(query, OLATResource.class)
				.setParameter("softkey", softkey)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	public List<RepositoryEntry> loadByResourceGroup(Group group) {
		if(group == null) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where relGroup.group.key=:groupKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("groupKey", group.getKey())
				.getResultList();
	}

	/**
	 * Return the last used repository entries with the status review up to published.
	 * 
	 * @param resourceTypeName The resource type
	 * @param firstResult The first result
	 * @param maxResults The maximum number of returned entries
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getLastUsedRepositoryEntries(String resourceTypeName, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where ores.resName=:resourceTypeName and v.status").in(RepositoryEntryStatusEnum.preparationToPublished())
		  .append(" order by statistics.lastUsage desc");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.setParameter("resourceTypeName", resourceTypeName)
				.getResultList();
	}

	public List<RepositoryEntry> getRepositoryEntriesAfterTheEnd(Date date) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" inner join fetch v.lifecycle as lifecycle")
		  .append(" where lifecycle.validTo<:now");

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		CalendarUtils.getEndOfDay(cal);
		Date endOfDay = cal.getTime();

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("now", endOfDay)
				.getResultList();
	}

	public void removeEducationalType(RepositoryEntryEducationalType educationalType) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update repositoryentry re");
		sb.append("   set re.educationalType = null");
		sb.append("     , re.lastModified = :now");
		sb.and().append("re.educationalType.key = :educationalTypeKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("now", new Date())
				.setParameter("educationalTypeKey", educationalType.getKey())
				.executeUpdate();
	}
}