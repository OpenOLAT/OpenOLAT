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

import static java.util.stream.Collectors.toList;

import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
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
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select curEle");
		sb.append("  from curriculumelement as curEle");
		sb.append("       inner join curEle.curriculum cur");;
		appendWhere(sb, searchParams);
		
		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class);
		appendParameter(query, searchParams);
		return query.getResultList();
	}

	private void appendWhere(QueryBuilder sb, SearchParameters searchParams) {
		sb.and().append("curEle.status = '").append(CurriculumElementStatus.active.name()).append("'");
		if (searchParams.isStartDate() && searchParams.getFrom() != null) {
			sb.and().append("curEle.beginDate >= :beginFrom");
		}
		if (searchParams.isStartDate() && searchParams.getTo() != null) {
			sb.and().append("curEle.beginDate <= :beginTo");
		}
		if (!searchParams.isStartDate() && searchParams.getFrom() != null) {
			sb.and().append("curEle.endDate >= :endFrom");
		}
		if (!searchParams.isStartDate() && searchParams.getTo() != null) {
			sb.and().append("curEle.endDate <= :endTo");
		}
		if (searchParams.getCeTypeKey() != null) {
			sb.and().append("curEle.type.key = :ceTypeKey");
		}
		if (searchParams.getGeneratorRef() != null) {
			sb.and();
			sb.append("curEle.key not in (");
			sb.append("select datacollection.generatorProviderKey");
			sb.append("  from qualitydatacollection as datacollection");
			sb.append(" where datacollection.generator.key = :generatorKey");
			sb.append(")");
		}
		if (searchParams.getOrganisationRefs() != null && !searchParams.getOrganisationRefs().isEmpty()) {
			sb.and();
			// load the organisations and all children
			for (int i = 0; i < searchParams.getOrganisationRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("cur.organisation.materializedPathKeys like :orgPath").append(i);
				if (i == searchParams.getOrganisationRefs().size() - 1) {
					sb.append(")");
				}
			}
		}
		if (searchParams.getWhiteListRefs() != null && !searchParams.getWhiteListRefs().isEmpty()) {
			sb.and().append("curEle.key in (:whiteListKeys)");
		}
		if (searchParams.getBlackListRefs() != null && !searchParams.getBlackListRefs().isEmpty()) {
			sb.and().append("curEle.key not in (:blackListKeys)");
		}
	}
	
	private void appendParameter(TypedQuery<?> query, SearchParameters searchParams) {
		if (searchParams.isStartDate() && searchParams.getFrom() != null) {
			query.setParameter("beginFrom", searchParams.getFrom());
		}
		if (searchParams.isStartDate() && searchParams.getTo() != null) {
			query.setParameter("beginTo", searchParams.getTo());
		}
		if (!searchParams.isStartDate() && searchParams.getFrom() != null) {
			query.setParameter("endFrom", searchParams.getFrom());
		}
		if (!searchParams.isStartDate() && searchParams.getTo() != null) {
			query.setParameter("endTo", searchParams.getTo());
		}
		if (searchParams.getCeTypeKey() != null) {
			query.setParameter("ceTypeKey", searchParams.getCeTypeKey());
		}
		if (searchParams.getGeneratorRef() != null) {
			query.setParameter("generatorKey", searchParams.getGeneratorRef().getKey());
		}
		if (searchParams.getOrganisationRefs() != null && !searchParams.getOrganisationRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getOrganisationRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("orgPath").append(i).toString();
				Long key = searchParams.getOrganisationRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getWhiteListRefs() != null && !searchParams.getWhiteListRefs().isEmpty()) {
			List<Long> curEleKeys = searchParams.getWhiteListRefs().stream().map(CurriculumElementRef::getKey).collect(toList());
			query.setParameter("whiteListKeys", curEleKeys);
		}
		if (searchParams.getBlackListRefs() != null && !searchParams.getBlackListRefs().isEmpty()) {
			List<Long> curEleKeys = searchParams.getBlackListRefs().stream().map(CurriculumElementRef::getKey).collect(toList());
			query.setParameter("blackListKeys", curEleKeys);
		}

	}

}
