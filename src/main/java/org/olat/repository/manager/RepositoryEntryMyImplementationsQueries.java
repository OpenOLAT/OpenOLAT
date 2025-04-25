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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class RepositoryEntryMyImplementationsQueries {
	
	public static final List<CurriculumElementStatus> VISIBLE_STATUS = List.of(CurriculumElementStatus.preparation,
				CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed,
				CurriculumElementStatus.active, CurriculumElementStatus.cancelled,
				CurriculumElementStatus.finished);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;

	public List<CurriculumElement> searchImplementations(IdentityRef identity, boolean bookmarksOnly) {
		String query = """
			select el from curriculumelement el
			left join el.type curElementType
			where el.status in (:status)
			and (el.parent.key is not null or curElementType.maxRepositoryEntryRelations<>1 and curElementType.singleElement=false)
			and (
			  exists (select membership.key from bgroupmember as membership
			  where el.group.key=membership.group.key and membership.identity.key=:identityKey
			  and membership.role=:role
			 )
			 or exists (select reservation.key from resourcereservation as reservation
			  where reservation.resource.key=el.resource.key and reservation.identity.key=:identityKey
			))""";
		
		List<String> status = VISIBLE_STATUS.stream()
				.map(CurriculumElementStatus::name)
				.toList();
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager().createQuery(query, CurriculumElement.class)
				.setParameter("status", status)
				.setParameter("identityKey", identity.getKey())
				.setParameter("role", GroupRoles.participant.name())
				.getResultList();

		List<CurriculumElement> implementations;
		if(elements.isEmpty()) {
			implementations = List.of();
		} else {
			Set<Long> implementationsKeys = new HashSet<>();
			Set<Long> marksResourceIds = null;		
			if(bookmarksOnly) {
				marksResourceIds = markManager.getMarkResourceIds(identity, "CurriculumElement", List.of());
			}
			
			for(CurriculumElement element:elements) {
				List<Long> segments = element.getMaterializedPathKeysList();
				if(!segments.isEmpty()) {
					Long implementationKey = segments.get(0);
					if(marksResourceIds == null || marksResourceIds.contains(implementationKey)) {
						implementationsKeys.add(implementationKey);
					}
				}
			}
		
			List<CurriculumElementRefImpl> implementationsRefs = implementationsKeys.stream()
					.map(CurriculumElementRefImpl::new)
					.toList();
			implementations = curriculumElementDao.loadByKeys(implementationsRefs);
		}
		return implementations;
	}

}
