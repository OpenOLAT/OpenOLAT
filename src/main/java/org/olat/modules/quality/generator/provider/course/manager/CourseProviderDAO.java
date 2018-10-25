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
package org.olat.modules.quality.generator.provider.course.manager;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseProviderDAO {

	@Autowired
	private DB dbInstance;

	public List<RepositoryEntry> loadCourses(SearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select entry");
		sb.append("  from repositoryentry entry");
		sb.append("       inner join entry.olatResource ores");
		sb.append("       left join entry.lifecycle lifecycle");
		appendWhere(sb, searchParams);
		
		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class);
		appendParameter(query, searchParams);
		return query.getResultList();
	}

	private void appendWhere(QueryBuilder sb, SearchParameters searchParams) {
		sb.and().append("entry.status = '").append(RepositoryEntryStatusEnum.published).append("'");
		sb.and().append("ores.resName = '").append(CourseModule.ORES_TYPE_COURSE).append("'");
		if (searchParams.getGeneratorRef() != null) {
			sb.and();
			sb.append("entry.key not in (");
			sb.append("select datacollection.generatorProviderKey");
			sb.append("  from qualitydatacollection as datacollection");
			sb.append(" where datacollection.generator.key = :generatorKey");
			if (searchParams.getGeneratorDataCollectionStart() != null) {
				sb.append(" and year(datacollection.start) = year(DATE(:generatorStart))");
				sb.append(" and month(datacollection.start) = month(DATE(:generatorStart))");
				sb.append(" and day(datacollection.start) = day(DATE(:generatorStart))");
			}
			sb.append(")");
		}
		if (searchParams.getOrganisationRefs() != null && !searchParams.getOrganisationRefs().isEmpty()) {
			sb.and();
			sb.append("entry.key in (");
			sb.append("select courseOrg.entry.key");
			sb.append("  from repoentrytoorganisation courseOrg");
			sb.append(" where courseOrg.organisation.key in :organisationKeys");
			sb.append(")");
		}
		if (searchParams.getBeginFrom() != null) {
			sb.and();
			sb.append("lifecycle.validFrom >= :beginFrom");
		}
		if (searchParams.getBeginTo() != null) {
			sb.and();
			sb.append("lifecycle.validFrom <= :beginTo");
		}
		if (searchParams.getEndFrom() != null) {
			sb.and();
			sb.append("lifecycle.validTo >= :endFrom");
		}
		if (searchParams.getEndTo() != null) {
			sb.and();
			sb.append("lifecycle.validTo <= :endTo");
		}
		if (searchParams.getLifecycleValidAt() != null) {
			sb.and();
			sb.append("(lifecycle.validFrom <= :validAt or lifecycle.validFrom is null)");
			sb.and();
			sb.append("(lifecycle.validTo >= :validAt or lifecycle.validTo is null)");
		}
		if (searchParams.getRepositoryEntryRefs() != null && !searchParams.getRepositoryEntryRefs().isEmpty()) {
			sb.and().append("entry.key in :repositoryKeys");
		}
	}

	private void appendParameter(TypedQuery<RepositoryEntry> query, SearchParameters searchParams) {
		if (searchParams.getGeneratorRef() != null) {
			query.setParameter("generatorKey", searchParams.getGeneratorRef().getKey());
			if (searchParams.getGeneratorDataCollectionStart() != null) {
				query.setParameter("generatorStart", searchParams.getGeneratorDataCollectionStart(), TemporalType.DATE);
			}
		}
		if (searchParams.getOrganisationRefs() != null && !searchParams.getOrganisationRefs().isEmpty()) {
			List<Long> organisationKeys = searchParams.getOrganisationRefs().stream().map(OrganisationRef::getKey).collect(toList());
			query.setParameter("organisationKeys", organisationKeys);
		}
		if (searchParams.getBeginFrom() != null) {
			query.setParameter("beginFrom", searchParams.getBeginFrom());
		}
		if (searchParams.getBeginTo() != null) {
			query.setParameter("beginTo", searchParams.getBeginTo());
		}
		if (searchParams.getEndFrom() != null) {
			query.setParameter("endFrom", searchParams.getEndFrom());
		}
		if (searchParams.getEndTo() != null) {
			query.setParameter("endTo", searchParams.getEndTo());
		}
		if (searchParams.getLifecycleValidAt() != null) {
			query.setParameter("validAt", searchParams.getLifecycleValidAt());
		}
		if (searchParams.getRepositoryEntryRefs() != null && !searchParams.getRepositoryEntryRefs().isEmpty()) {
			List<Long> keys = searchParams.getRepositoryEntryRefs().stream().map(RepositoryEntryRef::getKey).collect(toList());
			query.setParameter("repositoryKeys", keys);
		}
	}

}
