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
package org.olat.modules.quality.analysis.manager;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 05.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AnalysisFilterDAO {
	
	@Autowired
	private DB dbInstance;

	public AvailableAttributes getAvailableAttributes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.analysis.AvailableAttributes(");
		sb.append("       count(collection.topicIdentity.key) > 0");
		sb.append("     , count(collection.topicRepositoryEntry.key) > 0");
		sb.append("     , count(collection.topicOrganisation.key) > 0");
		sb.append("     , count(collection.topicCurriculum.key) > 0");
		sb.append("     , count(collection.topicCurriculumElement.key) > 0");
		sb.append("     , count(contextToOrganisation.organisation.key) > 0");
		sb.append("     , count(contextToCurriculum.curriculum.key) > 0");
		sb.append("     , count(contextToCurriculumElement.curriculumElement.key) > 0");
		sb.append("     , count(contextToTaxonomyLevel.taxonomyLevel.key) > 0");
		sb.append("       )");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		
		TypedQuery<AvailableAttributes> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AvailableAttributes.class);
		appendParameters(query, searchParams);
		return query.getResultList().get(0);
	};

	List<String> loadContextOrganisationPathes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct contextOrganisation.materializedPathKeys");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextOrganisation.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<Curriculum> loadContextCurriculums(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct curriculum");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("curriculum.key is not null");
		
		TypedQuery<Curriculum> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Curriculum.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<CurriculumElement> loadContextCurriculumElements(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct curriculumElement");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("curriculumElement.key is not null");
		
		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<String> loadContextCurriculumElementPathes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct curriculumElement.materializedPathKeys");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("curriculumElement.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<Long> loadContextCurriculumElementsCurriculumKey(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct curriculumElement.curriculum.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("curriculumElement.key is not null");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	public List<String> loadContextTaxonomyLevelPathes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct taxonomyLevel.materializedPathKeys");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("taxonomyLevel.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	Long loadDataCollectionCount(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(distinct collection.key)");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList().get(0);
	}
	
	List<String> loadTopicOrganisationPaths(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct topicOrganisation.materializedPathKeys");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("collection.topicOrganisation.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}
	
	List<Long> loadTopicCurriculumKeys(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct collection.topicCurriculum.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("collection.topicCurriculum.key is not null");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}
	
	List<Long> loadTopicCurriculumElementKeys(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct collection.topicCurriculumElement.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("collection.topicCurriculumElement.key is not null");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}
	
	List<Long> loadTopicIdentityKeys(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct collection.topicIdentity.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("collection.topicIdentity.key is not null");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	public List<Long> loadTopicRepositoryKeys(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct collection.topicRepositoryEntry.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("collection.topicRepositoryEntry.key is not null");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<Long> loadSessionKeys(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		appendSelectSessionKeys(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	static void appendSelectSessionKeys(QueryBuilder sb, AnalysisSearchParameter searchParams) {
		// Only done sessions have an entry in context.evaluationFormSession
		sb.append("select distinct context.evaluationFormSession.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("context.evaluationFormSession.key is not null");
	}
	
	List<GroupedStatistic> loadGroupedStatisticByResponseIdentifiers(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, MultiGroupBy multiGroupBy) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty()) return new ArrayList<>();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.analysis.GroupedStatistic(");
		sb.append("       response.responseIdentifier");
		appendGroupBys(sb, multiGroupBy, true);
		sb.append("     , count(response)");
		sb.append("     , avg(response.numericalResponse)");
		sb.append("       )");
		appendFrom(sb, searchParams);
		sb.append("       inner join context.evaluationFormSession session");
		sb.append("       inner join evaluationformresponse response");
		sb.append("               on response.session.key = session.key");
		sb.append("              and (response.noResponse is false or response.noResponse is null)");
		sb.append("              and response.responseIdentifier in (:responseIdentifiers)");
		appendWhere(sb, searchParams);
		sb.append(" group by response.responseIdentifier");
		appendGroupBys(sb, multiGroupBy, false);
		
		TypedQuery<GroupedStatistic> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GroupedStatistic.class)
				.setParameter("responseIdentifiers", responseIdentifiers);
		appendParameters(query, searchParams);
		return query.getResultList();
	}
	
	private void appendGroupBys(QueryBuilder sb, MultiGroupBy multiGroupBy, boolean select) {
		appendGroupBy(sb, multiGroupBy.getGroupBy1(), select);
		appendGroupBy(sb, multiGroupBy.getGroupBy2(), select);
		appendGroupBy(sb, multiGroupBy.getGroupBy3(), select);
	}

	private void appendGroupBy(QueryBuilder sb, GroupBy groupBy, boolean select) {
		if (groupBy == null) {
			if (select) sb.append(", CAST(NULL as long)");
			return;
		}
		
		switch (groupBy) {
		case TOPIC_IDENTITY:
			sb.append(", collection.topicIdentity.key");
			break;
		case TOPIC_ORGANISATION:
			sb.append(", collection.topicOrganisation.key");
			break;
		case TOPIC_CURRICULUM:
			sb.append(", collection.topicCurriculum.key");
			break;
		case TOPIC_CURRICULUM_ELEMENT:
			sb.append(", collection.topicCurriculumElement.key");
			break;
		case TOPIC_REPOSITORY:
			sb.append(", collection.topicRepositoryEntry.key");
			break;
		case CONTEXT_ORGANISATION:
			sb.append(", contextToOrganisation.organisation.key");
			break;
		case CONTEXT_CURRICULUM:
			sb.append(", contextToCurriculum.curriculum.key");
			break;
		case CONTEXT_CURRICULUM_ELEMENT:
			sb.append(", contextToCurriculumElement.curriculumElement.key");
			break;
		case CONTEXT_TAXONOMY_LEVEL:
			sb.append(", contextToTaxonomyLevel.taxonomyLevel.key");
			break;
		default: 
		}
	}

	private static void appendFrom(QueryBuilder sb, AnalysisSearchParameter searchParams) {
		sb.append("  from qualitydatacollection collection");
		sb.append("       inner join evaluationformsurvey survey");
		sb.append("               on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("              and survey.resId = collection.key");
		sb.append("       left join collection.topicOrganisation topicOrganisation");
		sb.append("       left join qualitycontext context");
		sb.append("              on context.dataCollection.key = collection.key");
		sb.append("       left join contexttocurriculum contextToCurriculum");
		sb.append("              on contextToCurriculum.context.key = context.key");
		sb.append("       left join curriculum curriculum");
		sb.append("              on contextToCurriculum.curriculum.key = curriculum.key");
		sb.append("       left join contexttocurriculumelement contextToCurriculumElement");
		sb.append("              on contextToCurriculumElement.context.key = context.key");
		sb.append("       left join curriculumelement curriculumElement");
		sb.append("              on contextToCurriculumElement.curriculumElement.key = curriculumElement.key");
		sb.append("       left join contexttoorganisation contextToOrganisation");
		sb.append("              on contextToOrganisation.context.key = context.key");
		sb.append("       left join organisation contextOrganisation");
		sb.append("              on contextToOrganisation.organisation.key = contextOrganisation.key");
		sb.append("       left join contexttotaxonomylevel contextToTaxonomyLevel");
		sb.append("              on contextToTaxonomyLevel.context.key = context.key");
		sb.append("       left join ctaxonomylevel taxonomyLevel");
		sb.append("              on contextToTaxonomyLevel.taxonomyLevel.key = taxonomyLevel.key");
		if (searchParams.isWithUserInfosOnly()) {
			sb.append("   left join context.evaluationFormSession sessionInfo");
		}
	}
	
	static void appendWhere(QueryBuilder sb, AnalysisSearchParameter searchParams) {
		sb.and().append("collection.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
		if (searchParams.getFormEntryRef() != null) {
			sb.and().append("survey.formEntry.key = :formEntryKey");
		}
		if (searchParams.getDateRangeFrom() != null) {
			sb.and().append("collection.deadline >= :dateRangeFrom");
		}
		if (searchParams.getDateRangeTo() != null) {
			sb.and().append("collection.deadline <= :dateRangeTo");
		}
		if (searchParams.getTopicIdentityRefs() != null && !searchParams.getTopicIdentityRefs().isEmpty()) {
			sb.and().append("collection.topicIdentity.key in :topicIdentityKeys");
		}
		if (searchParams.getTopicOrganisationRefs() != null && !searchParams.getTopicOrganisationRefs().isEmpty()) {
			sb.and().append("topicOrganisation.key in :topicOrganisationKeys");
		}
		if (searchParams.getTopicCurriculumRefs() != null && !searchParams.getTopicCurriculumRefs().isEmpty()) {
			sb.and().append("collection.topicCurriculum.key in :topicCurriculumKeys");
		}
		if (searchParams.getTopicCurriculumElementRefs() != null && !searchParams.getTopicCurriculumElementRefs().isEmpty()) {
			sb.and().append("collection.topicCurriculumElement.key in :topicCurriculumElementKeys");
		}
		if (searchParams.getTopicRepositoryRefs() != null && !searchParams.getTopicRepositoryRefs().isEmpty()) {
			sb.and().append("collection.topicRepositoryEntry.key in :topicRepositoryEntryKeys");
		}
		if (searchParams.getContextOrganisationRefs() != null && !searchParams.getContextOrganisationRefs().isEmpty()) {
			// load the organisations and all children
			sb.and();
			for (int i = 0; i < searchParams.getContextOrganisationRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("contextOrganisation.materializedPathKeys like :orgPath").append(i);
				if (i == searchParams.getContextOrganisationRefs().size() - 1) {
					sb.append(")");
				}
			}
		}
		if (searchParams.getContextCurriculumRefs() != null && !searchParams.getContextCurriculumRefs().isEmpty()) {
			sb.and().append("curriculum.key in :curriculumKeys");
		}
		if (searchParams.getContextCurriculumElementRefs() != null && !searchParams.getContextCurriculumElementRefs().isEmpty()) {
			// load the curriculum elements and all children
			sb.and();
			for (int i = 0; i < searchParams.getContextCurriculumElementRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("curriculumElement.materializedPathKeys like :elePath").append(i);
				if (i == searchParams.getContextCurriculumElementRefs().size() - 1) {
					sb.append(")");
				}
			}
		}
		if (searchParams.getContextTaxonomyLevelRefs() != null && !searchParams.getContextTaxonomyLevelRefs().isEmpty()) {
			// load the taxonomy level and all children
			sb.and();
			for (int i = 0; i < searchParams.getContextTaxonomyLevelRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("taxonomyLevel.materializedPathKeys like :taxonomyLevelPath").append(i);
				if (i == searchParams.getContextTaxonomyLevelRefs().size() - 1) {
					sb.append(")");
				}
			}
		}
		if (searchParams.isWithUserInfosOnly()) {
			sb.and();
			sb.append("(");
			sb.append("    sessionInfo.email is not null");
			sb.append(" or sessionInfo.firstname is not null");
			sb.append(" or sessionInfo.lastname is not null");
			sb.append(" or sessionInfo.age is not null");
			sb.append(" or sessionInfo.gender is not null");
			sb.append(" or sessionInfo.orgUnit is not null");
			sb.append(" or sessionInfo.studySubject is not null");
			sb.append(")");
		}
	}

	static void appendParameters(Query query, AnalysisSearchParameter searchParams) {
		if (searchParams.getFormEntryRef() != null) {
			query.setParameter("formEntryKey", searchParams.getFormEntryRef().getKey());
		}
		if (searchParams.getDateRangeFrom() != null) {
			query.setParameter("dateRangeFrom", searchParams.getDateRangeFrom());
		}
		if (searchParams.getDateRangeTo() != null) {
			query.setParameter("dateRangeTo", searchParams.getDateRangeTo());
		}
		if (searchParams.getTopicIdentityRefs() != null && !searchParams.getTopicIdentityRefs().isEmpty()) {
			List<Long> keys = searchParams.getTopicIdentityRefs().stream().map(IdentityRef::getKey).collect(toList());
			query.setParameter("topicIdentityKeys", keys);
		}
		if (searchParams.getTopicOrganisationRefs() != null && !searchParams.getTopicOrganisationRefs().isEmpty()) {
			List<Long> keys = searchParams.getTopicOrganisationRefs().stream().map(OrganisationRef::getKey).collect(toList());
			query.setParameter("topicOrganisationKeys", keys);
		}
		if (searchParams.getTopicCurriculumRefs() != null && !searchParams.getTopicCurriculumRefs().isEmpty()) {
			List<Long> keys = searchParams.getTopicCurriculumRefs().stream().map(CurriculumRef::getKey).collect(toList());
			query.setParameter("topicCurriculumKeys", keys);
		}
		if (searchParams.getTopicCurriculumElementRefs() != null && !searchParams.getTopicCurriculumElementRefs().isEmpty()) {
			List<Long> keys = searchParams.getTopicCurriculumElementRefs().stream().map(CurriculumElementRef::getKey).collect(toList());
			query.setParameter("topicCurriculumElementKeys", keys);
		}
		if (searchParams.getTopicRepositoryRefs() != null && !searchParams.getTopicRepositoryRefs().isEmpty()) {
			List<Long> keys = searchParams.getTopicRepositoryRefs().stream().map(RepositoryEntryRef::getKey).collect(toList());
			query.setParameter("topicRepositoryEntryKeys", keys);
		}
		if (searchParams.getContextOrganisationRefs() != null && !searchParams.getContextOrganisationRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getContextOrganisationRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("orgPath").append(i).toString();
				Long key = searchParams.getContextOrganisationRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getContextCurriculumRefs() != null && !searchParams.getContextCurriculumRefs().isEmpty()) {
			List<Long> keys = searchParams.getContextCurriculumRefs().stream().map(CurriculumRef::getKey).collect(toList());
			query.setParameter("curriculumKeys", keys);
		}
		if (searchParams.getContextCurriculumElementRefs() != null && !searchParams.getContextCurriculumElementRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getContextCurriculumElementRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("elePath").append(i).toString();
				Long key = searchParams.getContextCurriculumElementRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getContextTaxonomyLevelRefs() != null && !searchParams.getContextTaxonomyLevelRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getContextTaxonomyLevelRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("taxonomyLevelPath").append(i).toString();
				Long key = searchParams.getContextTaxonomyLevelRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
	}

}
