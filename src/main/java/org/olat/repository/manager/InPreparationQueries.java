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

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.logging.Tracing;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class InPreparationQueries {
	
	private static final Logger log = Tracing.createLoggerFor(InPreparationQueries.class);
	
	private static final List<String> IN_PREPARATION_STATUS = List.of(RepositoryEntryStatusEnum.preparation.name(),
			RepositoryEntryStatusEnum.review.name(), RepositoryEntryStatusEnum.coachpublished.name());
	
	private static final List<String> ELEMENT_IN_PREPARATION_STATUS = List.of(CurriculumElementStatus.preparation.name());
	
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

	public boolean hasInPreparation(IdentityRef identity, boolean participantsOnly) {
		List<RepositoryEntry> entries = searchRepositoryEntries(identity, participantsOnly, 0, 1);
		if(!entries.isEmpty()) {
			return true;
		}
		if(curriculumModule.isEnabled()) {
			List<CurriculumElementAndRepositoryEntry> elements = searchCurriculumElements(identity);
			if(!elements.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private List<RepositoryEntry> searchRepositoryEntries(IdentityRef identity, boolean participantsOnly, int firstResult, int maxResults) {
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
		  .append("  and membership.role in (:roles)")
		  .append(" )");
		
		// checks reservation
		sb.append(" or exists (select reservation.key from resourcereservation as reservation")
		  .append("  where reservation.resource.key=res.key and reservation.identity.key=:identityKey")
		  .append(" )");
		
		sb.append(")");
		
		List<String> roles = participantsOnly
				? List.of(GroupRoles.participant.name())
				: List.of(GroupRoles.participant.name(), GroupRoles.coach.name(), GroupRoles.owner.name());
		
		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntry.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("status", IN_PREPARATION_STATUS)
			.setParameter("roles", roles);
		
		if(maxResults > 0) {
			query
				.setFirstResult(firstResult)
				.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<RepositoryEntryInPreparation> searchRepositoryEntriesInPreparation(IdentityRef identity, boolean participantsOnly) {
		List<RepositoryEntry> entries = searchRepositoryEntries(identity, participantsOnly, 0, -1);
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
	
	private List<CurriculumElementAndRepositoryEntry> searchCurriculumElements(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select el, v")
		  .append(" from curriculumelement as el")
		  .append(" inner join fetch el.curriculum as curriculum")
		  .append(" inner join fetch el.group as baseGroup")
		  .append(" inner join fetch el.type as type")
		  .append(" left join fetch el.resource as rsrc")
		  .append(" left join repoentrytogroup as rel on (baseGroup.key =  rel.group.key)")
		  .append(" left join rel.entry as v")
		  .append(" left join fetch v.olatResource as res")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" left join fetch v.educationalType as educationalType")
		  .where()
		  // Implementation single course or with structure
		  .append(" el.parent.key is null and el.status in (:curriculumElementStatus)");

		sb.and().append("(");
		// check participants
		sb.append("exists (select membership.key from bgroupmember as membership")
		  .append(" where baseGroup.key=membership.group.key and membership.identity.key=:identityKey")
		  .append(" and membership.role=:participantRole")
		  .append(" and (v.key is null or v.status in (:repoStatus))")
		  .append(")");
		// checks reservation
		sb.append(" or exists (select reservation.key from resourcereservation as reservation")
		  .append("  where reservation.resource.key=rsrc.key and reservation.identity.key=:identityKey")
		  .append(" )");
		
		sb.append(")");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("repoStatus", IN_PREPARATION_STATUS)
			.setParameter("curriculumElementStatus", ELEMENT_IN_PREPARATION_STATUS)
			.setParameter("participantRole", GroupRoles.participant.name());

		List<Object[]> elements = query.getResultList();
		List<CurriculumElementAndRepositoryEntry> list = new ArrayList<>();
		for(Object[] objects: elements) {
			CurriculumElement element = (CurriculumElement)objects[0];
			CurriculumElementType type = element.getType();
			RepositoryEntry entry = (RepositoryEntry)objects[1];

			log.debug("{} with type {} (single: {}, relations: {}) and entry: {}", element.getDisplayName(), (type == null ? "-" : type.getDisplayName()),
					(type == null ? "-" : type.isSingleElement()), (type == null ? "-" : type.getMaxRepositoryEntryRelations()), (entry == null ? "-" : entry.getDisplayname()));

			CurriculumElementStatus elementStatus = element.getElementStatus();
			if(entry == null && (elementStatus == CurriculumElementStatus.cancelled || elementStatus == CurriculumElementStatus.finished)) {
				// Ignore them
			} else {
				list.add(new CurriculumElementAndRepositoryEntry(element, entry));
			}
		}
		
		return list;
	}
	
	public List<CurriculumElementInPreparation> searchCurriculumElementsInPreparation(IdentityRef identity) {
		List<CurriculumElementAndRepositoryEntry> curriculumElements = searchCurriculumElements(identity);
		
		List<Long> marks = markManager.getMarksResourceId(identity, "CurriculumElement");
		Set<Long> marksSet = Set.copyOf(marks);
		
		Map<Long,List<TaxonomyLevel>> levelsMap;
		if (!curriculumElements.isEmpty() && taxonomyModule.isEnabled()) {
			List<CurriculumElement> elements = curriculumElements.stream()
					.map(CurriculumElementAndRepositoryEntry::element)
					.toList();
			levelsMap = curriculumService.getCurriculumElementKeyToTaxonomyLevels(elements);
		} else {
			levelsMap = Map.of();
		}
		List<CurriculumElementInPreparation> list = new ArrayList<>(curriculumElements.size());
		for(CurriculumElementAndRepositoryEntry curriculumElement:curriculumElements) {
			boolean marked = marksSet.contains(curriculumElement.element().getKey());
			List<TaxonomyLevel> levels = levelsMap.get(curriculumElement.element().getKey());
			list.add(new CurriculumElementInPreparation(curriculumElement.element(), curriculumElement.entry(), marked, levels));
		}
		return list;
	}
	
	private record CurriculumElementAndRepositoryEntry(CurriculumElement element, RepositoryEntry entry) {
		//
	}
}