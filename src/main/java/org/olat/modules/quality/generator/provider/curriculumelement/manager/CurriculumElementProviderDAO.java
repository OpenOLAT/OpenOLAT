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
package org.olat.modules.quality.generator.provider.curriculumelement.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementProviderDAO {
	
	@Autowired
	private DB dbInstance;

	public List<CurriculumElement> loadPending(SearchParameters searchParams) {
		if (validateSearchParams(searchParams)) {
			return new ArrayList<>(0);
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select curEle");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		
		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class);
		appendParameter(query, searchParams);
		return query.getResultList();
	}

	public Long loadPendingCount(SearchParameters searchParams) {
		if (validateSearchParams(searchParams)) {
			return 0l;
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(curEle)");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameter(query, searchParams);
		
		List<Long> counts = query.getResultList();
		return !counts.isEmpty()? counts.get(0): 0l;
	}

	private boolean validateSearchParams(SearchParameters searchParams) {
		return searchParams.getGeneratorRef() == null || searchParams.getGeneratorRef().getKey() == null
				|| searchParams.getOrganisationRefs().isEmpty() || searchParams.getCeTypeKey() == null
				|| searchParams.getFrom() == null || searchParams.getTo() == null;
	}

	private void appendFrom(StringBuilder sb) {
		sb.append(" from  curriculumelement as curEle");
		sb.append("       inner join curEle.curriculum cur");
	}

	private void appendWhere(StringBuilder sb, SearchParameters searchParams) {
		sb.append(" where curEle.type.key = :ceTypeKey");
		sb.append("   and curEle.status = '").append(CurriculumElementStatus.active.name()).append("'");
		sb.append("   and cur.organisation.key in :organisationKeys");
		sb.append("   and curEle.key not in (");
		sb.append("       select datacollection.generatorProviderKey");
		sb.append("         from qualitydatacollection as datacollection");
		sb.append("        where datacollection.generator.key = :generatorKey");
		sb.append("     )");
		
		if (searchParams.isStartDate()) {
			sb.append(" and curEle.beginDate > :from and curEle.beginDate <= :to");
		} else {
			sb.append(" and curEle.endDate > :from and curEle.endDate <= :to");
		}
		
		if (!searchParams.getCurriculumElementRefs().isEmpty()) {
			sb.append(" and curEle.key in :curEleKeys");
		}
	}
	
	private void appendParameter(TypedQuery<?> query, SearchParameters searchParams) {
		List<Long> organisationKeys = searchParams.getOrganisationRefs().stream().map(OrganisationRef::getKey).collect(Collectors.toList());
		query.setParameter("ceTypeKey", searchParams.getCeTypeKey())
				.setParameter("organisationKeys", organisationKeys)
				.setParameter("generatorKey", searchParams.getGeneratorRef().getKey())
				.setParameter("from", searchParams.getFrom())
				.setParameter("to", searchParams.getTo());
		
		if (!searchParams.getCurriculumElementRefs().isEmpty()) {
			List<Long> curEleKeys = searchParams.getCurriculumElementRefs().stream().map(CurriculumElementRef::getKey).collect(Collectors.toList());
			query.setParameter("curEleKeys", curEleKeys);
		}
	}

}
