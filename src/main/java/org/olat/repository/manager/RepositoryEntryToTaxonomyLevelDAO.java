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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.model.RepositoryEntryToTaxonomyLevelImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryToTaxonomyLevelDAO {

	@Autowired
	private DB dbInstance;
	
	public RepositoryEntryToTaxonomyLevel createRelation(RepositoryEntry entry, TaxonomyLevel taxonomyLevel) {
		RepositoryEntryToTaxonomyLevelImpl rel = new RepositoryEntryToTaxonomyLevelImpl();
		rel.setCreationDate(new Date());
		rel.setEntry(entry);
		rel.setTaxonomyLevel(taxonomyLevel);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<TaxonomyLevel> getTaxonomyLevels(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.taxonomyLevel from repositoryentrytotaxonomylevel rel")
		  .append(" where rel.entry.key=:entryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaxonomyLevel.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public Map<RepositoryEntryRef,List<TaxonomyLevel>> getTaxonomyLevels(List<? extends RepositoryEntryRef> entries, boolean fetchParents) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel from repositoryentrytotaxonomylevel rel")
		  .append(" inner join fetch rel.entry v")
		  .append(" inner join fetch rel.taxonomyLevel as level");
		if (fetchParents) {
			sb.append(" left join fetch level.parent as parent");
		}
		sb.append(" where v.key in (:entryKeys)");

		List<Long> entryKeys = entries.stream()
				.map(RepositoryEntryRef::getKey)
				.collect(Collectors.toList());
		List<RepositoryEntryToTaxonomyLevel> entryToLevels = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryToTaxonomyLevel.class)
				.setParameter("entryKeys", entryKeys)
				.getResultList();

		Map<Long,RepositoryEntryRef> entryMap = entries.stream()
				.collect(Collectors.toMap(RepositoryEntryRef::getKey, Function.identity(), (u, v) -> u));
		Map<RepositoryEntryRef, List<TaxonomyLevel>> levelsMap = new HashMap<>();
		for(RepositoryEntryToTaxonomyLevel entryToLevel:entryToLevels) {
			RepositoryEntryRef entryRef = entryMap.get(entryToLevel.getEntry().getKey());
			List<TaxonomyLevel> levels = levelsMap
					.computeIfAbsent(entryRef, ref -> new ArrayList<>());
			levels.add(entryToLevel.getTaxonomyLevel());
		}
		return levelsMap;
	}
	
	public Map<Long,List<TaxonomyLevel>> getTaxonomyLevelsByEntryKeys(List<Long> entryKeys) {
		if(entryKeys == null || entryKeys.isEmpty()) return new HashMap<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.entry.key, level from repositoryentrytotaxonomylevel rel")
		  .append(" inner join rel.taxonomyLevel as level")
		  .append(" left join fetch level.type as type")
		  .append(" where rel.entry.key in (:entryKeys)");

		List<Object[]> entryToLevels = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("entryKeys", entryKeys)
				.getResultList();

		Map<Long, List<TaxonomyLevel>> levelsMap = new HashMap<>();
		for(Object[] entryToLevel:entryToLevels) {
			Long entryKey = (Long)entryToLevel[0];
			TaxonomyLevel level = (TaxonomyLevel)entryToLevel[1];
			List<TaxonomyLevel> levels = levelsMap
					.computeIfAbsent(entryKey, ref -> new ArrayList<>());
			levels.add(level);
		}
		return levelsMap;
	}
	
	public List<RepositoryEntry> getRepositoryEntries(TaxonomyLevelRef level) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select v from repositoryentrytotaxonomylevel rel")
		  .append(" inner join rel.entry v")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where rel.taxonomyLevel.key=:levelKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("levelKey", level.getKey())
				.getResultList();
	}
	
	public void deleteRelation(RepositoryEntryRef entry, TaxonomyLevelRef taxonomyLevel) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel from repositoryentrytotaxonomylevel rel")
		  .append(" where rel.entry.key=:entryKey and rel.taxonomyLevel.key=:levelKey");
		List<RepositoryEntryToTaxonomyLevel> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryToTaxonomyLevel.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("levelKey", taxonomyLevel.getKey())
				.getResultList();
		for(RepositoryEntryToTaxonomyLevel relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
	
	public void deleteRelation(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel from repositoryentrytotaxonomylevel rel")
		  .append(" where rel.entry.key=:entryKey");
		List<RepositoryEntryToTaxonomyLevel> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryToTaxonomyLevel.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		for(RepositoryEntryToTaxonomyLevel relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
}
