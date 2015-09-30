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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
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
}