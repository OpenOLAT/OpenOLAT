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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRepositoryEntryRelation;
import org.olat.modules.curriculum.model.CurriculumRepositoryEntryRelationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
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
	
	public CurriculumRepositoryEntryRelation createRelation(RepositoryEntry entry, CurriculumElement element, boolean master) {
		CurriculumRepositoryEntryRelationImpl relation = new CurriculumRepositoryEntryRelationImpl();
		relation.setCreationDate(new Date());
		relation.setLastModified(relation.getCreationDate());
		relation.setCurriculumElement(element);
		relation.setEntry(entry);
		relation.setMaster(master);
		dbInstance.getCurrentEntityManager().persist(relation);
		return relation;
	}
	
	public List<CurriculumRepositoryEntryRelation> getRelations(RepositoryEntryRef entry, CurriculumElementRef element) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel from repoentrytocurriculumelement as rel")
		  .append(" where rel.entry.key=:repoKey and rel.curriculumElement.key=:elementKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CurriculumRepositoryEntryRelation.class)
			.setParameter("repoKey", entry.getKey())
			.setParameter("elementKey", element.getKey())
			.getResultList();
	}
	
	public void deleteRelation(RepositoryEntryRef entry, CurriculumElementRef element) {
		List<CurriculumRepositoryEntryRelation> relations = getRelations(entry, element);
		for(CurriculumRepositoryEntryRelation relation:relations) {
			dbInstance.getCurrentEntityManager().remove(relation);
		}
	}
	
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el from curriculumelement as el")
		  .append(" inner join fetch el.group as bGroup")
		  .append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" where rel.entry.key=:repoKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CurriculumElement.class)
			.setParameter("repoKey", entry.getKey())
			.getResultList();
	}
	
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct v from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" inner join v.groups as rel")
		  .append(" inner join curriculumelement as el on (el.group.key=rel.group.key)")
		  .append(" where el.key=:elementKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntry.class)
			.setParameter("elementKey", element.getKey())
			.getResultList();
	}
	
	/**
	 * The method return all the elements of the curriculum and if
	 * needed with an empty list of repository entries.
	 * 
	 * @param curriculum The curriculum
	 * @return A map of curriculum element to their repository entries
	 */
	public Map<CurriculumElement, List<Long>> getCurriculumElementsWithRepositoryEntryKeys(CurriculumRef curriculum) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el, rel.entry.key from curriculumelement el")
		  .append(" inner join el.curriculum curriculum")
		  .append(" left join fetch el.parent parentEl")
		  .append(" left join repoentrytogroup as rel on (el.group.key=rel.group.key)")
		  .append(" where curriculum.key=:curriculumKey");
		
		  //.append(" inner join fetch el.group as bGroup")
		  //.append(" left join fetch el.parent as parentEl")
		  //.append(" left join fetch el.type as elementType")
		  //.append(" left join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		 // .append(" where el.curriculum.key=:curriculumKey");

		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("curriculumKey", curriculum.getKey())
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
}
