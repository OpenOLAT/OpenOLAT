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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
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
	private CurriculumElementDAO curriculumElementDao;

	public List<CurriculumElement> searchImplementations(IdentityRef identity) {
		String query = """
			select el,
			(select count(distinct reToGroup.entry.key) from repoentrytogroup reToGroup
			  where reToGroup.group.key=baseGroup.key
			) as numOfCourses,
			(select count(distinct subEl.key) from curriculumelement subEl
			  where subEl.parent.key=el.key
			) as numOfSubElements
			from curriculumelement el
			inner join fetch el.group baseGroup
			inner join baseGroup.members membership
			left join fetch el.type curElementType
			where el.status in (:status)
			 and membership.identity.key=:identityKey and membership.role=:role
			 and (el.parent.key is not null or curElementType.maxRepositoryEntryRelations<>1 and curElementType.singleElement=false)""";
		
		List<String> status = VISIBLE_STATUS.stream()
				.map(CurriculumElementStatus::name)
				.toList();
		List<Object[]> objectsList = dbInstance.getCurrentEntityManager().createQuery(query, Object[].class)
				.setParameter("status", status)
				.setParameter("identityKey", identity.getKey())
				.setParameter("role", GroupRoles.participant.name())
				.getResultList();
		List<CurriculumElement> elements = new ArrayList<>();
		for(Object[] objects:objectsList) {
			CurriculumElement element = (CurriculumElement)objects[0];
			elements.add(element);
		}

		List<CurriculumElement> implementations;
		if(elements.isEmpty()) {
			implementations = List.of();
		} else {
			Set<Long> implementationsKeys = new HashSet<>();
			for(CurriculumElement element:elements) {
				List<Long> segments = element.getMaterializedPathKeysList();
				if(!segments.isEmpty()) {
					implementationsKeys.add(segments.get(0));
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
