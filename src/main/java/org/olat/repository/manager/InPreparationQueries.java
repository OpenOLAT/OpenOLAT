/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.model.CurriculumElementInPreparation;
import org.olat.repository.model.RepositoryEntryInPreparation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InPreparationQueries {
	
	private static final List<String> IN_PREPARATION_STATUS = List.of(RepositoryEntryStatusEnum.preparation.name(),
			RepositoryEntryStatusEnum.review.name(), RepositoryEntryStatusEnum.coachpublished.name());
	
	private static final List<String> IN_PREPARATION_CURRICULUM_STATUS = List.of(CurriculumElementStatus.preparation.name(),
			CurriculumElementStatus.provisional.name(), CurriculumElementStatus.confirmed.name(),
			CurriculumElementStatus.active.name());
	
	private static final List<CurriculumElementStatus> IN_PREPARATION_AS_PARTICIPANT_CURRICULUM_STATUS = List.of(CurriculumElementStatus.preparation,
			CurriculumElementStatus.provisional);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	

	public boolean hasInPreparation(IdentityRef identity) {
		List<RepositoryEntry> entries = searchRepositoryEntries(identity, 0, 1);
		if(!entries.isEmpty()) {
			return true;
		}
		if(curriculumModule.isEnabled()) {
			List<CurriculumElement> elements = searchCurriculumElements(identity);
			if(!elements.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private List<RepositoryEntry> searchRepositoryEntries(IdentityRef identity, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select v")
		  .append(" from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" left join fetch v.educationalType as educationalType")
		  .where()
		  .append(" v.status in (:status)");
		
		sb.and().append("(");
		// check participants
		sb.append(" exists (select rel.key from repoentrytogroup as rel, bgroupmember as membership")
		  .append("  where rel.entry.key=v.key and rel.group.key=membership.group.key and membership.identity.key=:identityKey")
		  .append("  and membership.role=:participantRole")
		  .append(" )");
		
		// checks reservation
		sb.append(" or exists (select reservation.key from resourcereservation as reservation")
		  .append("  where reservation.resource.key=res.key and reservation.identity.key=:identityKey")
		  .append(" )");
		
		sb.append(")");
		
		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntry.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("status", IN_PREPARATION_STATUS)
			.setParameter("participantRole", GroupRoles.participant.name());
		
		if(maxResults > 0) {
			query
				.setFirstResult(firstResult)
				.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<RepositoryEntryInPreparation> searchRepositoryEntriesInPreparation(IdentityRef identity) {
		List<RepositoryEntry> entries = searchRepositoryEntries(identity, 0, -1);
		List<Long> entriesKeys = entries.stream()
				.map(RepositoryEntry::getKey).toList();

		Map<Long,List<TaxonomyLevel>> levelsMap;
		if(!entries.isEmpty() && taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty()) {
			levelsMap = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevelsByEntryKeys(entriesKeys);
		} else {
			levelsMap = Map.of();
		}
		
		List<Long> marks = markManager.getMarksResourceId(identity, "RepositoryEntry");
		Set<Long> marksSet = Set.copyOf(marks);
		
		List<RepositoryEntryInPreparation> list = new ArrayList<>(entries.size());
		for(RepositoryEntry entry:entries) {
			List<TaxonomyLevel> levels = levelsMap.get(entry.getKey());
			boolean marked = marksSet.contains(entry.getKey());
			list.add(new RepositoryEntryInPreparation(entry, marked, levels));
		}
		return list;
	}
	
	private List<CurriculumElement> searchCurriculumElements(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select el,")
		  .append(" (select count(distinct reToGroup.entry.key) from repoentrytogroup reToGroup")
		  .append("  where reToGroup.group.key=baseGroup.key")
		  .append(" ) as numOfCourses,")
		  .append(" (select count(distinct reservation.key) from resourcereservation as reservation")
		  .append("  where reservation.resource.key=el.resource.key and reservation.identity.key=:identityKey")
		  .append(" ) as numOfReservations,")
		  .append(" (select count(distinct participants.identity.key) from bgroupmember as participants")
		  .append("  where participants.group.key=baseGroup.key and participants.role=:participantRole")
		  .append("  and participants.identity.key=:identityKey")
		  .append(" ) as numOfParticipants")
		  .append(" from curriculumelement as el")
		  .append(" inner join fetch el.curriculum as curriculum")
		  .append(" inner join fetch el.group as baseGroup")
		  .append(" left join fetch el.resource as rsrc")
		  .append(" left join fetch el.type as type")
		  .where()
		  .append(" el.parent.key is null and el.status in (:status)");

		sb.and().append("(");
		// check participants
		sb.append("exists (select membership.key from bgroupmember as membership")
		  .append(" where baseGroup.key=membership.group.key and membership.identity.key=:identityKey")
		  .append(" and membership.role=:participantRole")
		  .append(")");
		// checks reservation
		sb.append(" or exists (select reservation.key from resourcereservation as reservation")
		  .append("  where reservation.resource.key=rsrc.key and reservation.identity.key=:identityKey")
		  .append(" )");
		
		sb.append(")");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("status", IN_PREPARATION_CURRICULUM_STATUS)
			.setParameter("participantRole", GroupRoles.participant.name());

		List<Object[]> elements = query.getResultList();
		List<CurriculumElement> list = new ArrayList<>();
		for(Object[] objects: elements) {
			CurriculumElement element = (CurriculumElement)objects[0];
			CurriculumElementType type = element.getType();
			long numOfCourse = PersistenceHelper.extractPrimitiveLong(objects, 1);
			long numOfReservation = PersistenceHelper.extractPrimitiveLong(objects, 2);
			long numAsParticipant = PersistenceHelper.extractPrimitiveLong(objects, 3);
			
			if(type.isSingleElement() && type.getMaxRepositoryEntryRelations() == 1) {
				if(numOfCourse == 0) {
					list.add(element);
				}
			} else if(!type.isSingleElement() || type.getMaxRepositoryEntryRelations() == -1) {
				if(numOfReservation > 0l
						|| (numAsParticipant > 0l && IN_PREPARATION_AS_PARTICIPANT_CURRICULUM_STATUS.contains(element.getElementStatus()))) {
					list.add(element);
				}
			}
		}
		
		return list;
	}
	
	public List<CurriculumElementInPreparation> searchCurriculumElementsInPreparation(IdentityRef identity) {
		List<CurriculumElement> curriculumElements = searchCurriculumElements(identity);
		
		List<Long> marks = markManager.getMarksResourceId(identity, "CurriculumElement");
		Set<Long> marksSet = Set.copyOf(marks);
		
		Map<Long,List<TaxonomyLevel>> levelsMap;
		if (!curriculumElements.isEmpty() && taxonomyModule.isEnabled()) {
			levelsMap = curriculumService.getCurriculumElementKeyToTaxonomyLevels(curriculumElements);
		} else {
			levelsMap = Map.of();
		}
		List<CurriculumElementInPreparation> list = new ArrayList<>(curriculumElements.size());
		for(CurriculumElement curriculumElement:curriculumElements) {
			boolean marked = marksSet.contains(curriculumElement.getKey());
			List<TaxonomyLevel> levels = levelsMap.get(curriculumElement.getKey());
			list.add(new CurriculumElementInPreparation(curriculumElement, marked, levels));
		}
		return list;
	}
}