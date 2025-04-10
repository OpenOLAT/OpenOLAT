/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.repository.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class RepositoryEntryLifecycleDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RepositoryEntryLifecycle create(String label, String softKey, boolean privateCycle, Date from, Date to) {
		RepositoryEntryLifecycle reLifeCycle = new RepositoryEntryLifecycle();
		reLifeCycle.setCreationDate(new Date());
		reLifeCycle.setLastModified(new Date());
		reLifeCycle.setLabel(label);
		reLifeCycle.setSoftKey(softKey);
		reLifeCycle.setPrivateCycle(privateCycle);
		reLifeCycle.setValidFrom(from);
		reLifeCycle.setValidTo(to);
		dbInstance.getCurrentEntityManager().persist(reLifeCycle);	
		return reLifeCycle;
	}
	
	public RepositoryEntryLifecycle loadById(Long key) {
		List<RepositoryEntryLifecycle> reLifeCycleList = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadReLifeCycle", RepositoryEntryLifecycle.class)
				.setParameter("key", key)
				.getResultList();
		if(reLifeCycleList.isEmpty()) {
			return null;
		}
		return reLifeCycleList.get(0);
	}
	
	public RepositoryEntryLifecycle loadByEntry(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v.lifecycle from repositoryentry as v where v.key=:repoKey");
		List<RepositoryEntryLifecycle> reLifeCycleList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryLifecycle.class)
				.setParameter("repoKey", entry.getKey())
				.getResultList();
		if(reLifeCycleList.isEmpty()) {
			return null;
		}
		return reLifeCycleList.get(0);
	}
	
	public List<RepositoryEntryLifecycle> loadPublicLifecycle() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadPublicReLifeCycle", RepositoryEntryLifecycle.class)
				.getResultList();
	}
	
	public RepositoryEntryLifecycle updateLifecycle(RepositoryEntryLifecycle lifecycle) {
		return dbInstance.getCurrentEntityManager().merge(lifecycle);
	}
	
	public void deleteLifecycle(RepositoryEntryLifecycle lifecycle) {
		RepositoryEntryLifecycle reloadedLifecycle = dbInstance.getCurrentEntityManager()
				.getReference(RepositoryEntryLifecycle.class, lifecycle.getKey());
		
		dbInstance.getCurrentEntityManager().remove(reloadedLifecycle);
	}

	public long countRepositoryEntriesWithLifecycle(RepositoryEntryLifecycle lifecycle) {
		String query = "select count(r) from repositoryentry r where r.lifecycle.key = :lifecycleKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("lifecycleKey", lifecycle.getKey())
				.getSingleResult();
	}

	public Map<Long, Long> countRepositoryEntriesForLifecycles(List<RepositoryEntryLifecycle> lifecycles) {
		if (lifecycles == null || lifecycles.isEmpty()) return Map.of();

		List<Long> lifecycleKeys = lifecycles.stream()
				.map(RepositoryEntryLifecycle::getKey)
				.toList();

		String query = """
		select r.lifecycle.key, count(r) 
		from repositoryentry r 
		where r.lifecycle.key in :lifecycleKeys 
		group by r.lifecycle.key
		""";

		List<Object[]> rawResults = dbInstance.getCurrentEntityManager()
				.createQuery(query, Object[].class)
				.setParameter("lifecycleKeys", lifecycleKeys)
				.getResultList();

		Map<Long, Long> result = new HashMap<>();
		for (Object[] row : rawResults) {
			Long lifecycleKey = (Long) row[0];
			Long count = (Long) row[1];
			result.put(lifecycleKey, count);
		}

		// Fill missing lifecycles with count 0
		for (Long lifecycleKey : lifecycleKeys) {
			result.putIfAbsent(lifecycleKey, 0L);
		}

		return result;
	}

}
