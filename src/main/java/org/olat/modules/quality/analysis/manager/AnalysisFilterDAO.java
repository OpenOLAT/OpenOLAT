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
import static org.olat.modules.quality.analysis.TemporalKey.DELIMITER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnlaysisFigures;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalGroupBy;
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
		sb.append("       count(collection.topicIdentity.key)");
		sb.append("     , count(collection.topicRepositoryEntry.key)");
		sb.append("     , count(collection.topicOrganisation.key)");
		sb.append("     , count(collection.topicCurriculum.key)");
		sb.append("     , count(collection.topicCurriculumElement.key)");
		sb.append("     , CASE WHEN max(context.location) is not null THEN length(max(context.location)) ELSE 0 END");
		sb.append("     , count(contextToOrganisation.organisation.key)");
		sb.append("     , count(contextCurriculum.key)");
		sb.append("     , count(contextCurriculumElement.key)");
		sb.append("     , count(contextCurriculumElement.type.key)");
		sb.append("     , count(contextCurriculumOrganisation.key)");
		sb.append("     , count(contextToTaxonomyLevel.taxonomyLevel.key)");
		sb.append("     , CASE WHEN max(survey.seriesIndex) is not null THEN max(survey.seriesIndex) ELSE 0 END");
		sb.append("     , count(collection.key)");
		sb.append("       )");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		
		TypedQuery<AvailableAttributes> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AvailableAttributes.class);
		appendParameters(query, searchParams);
		return query.getResultList().get(0);
	}

	AnlaysisFigures loadAnalyticFigures(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.analysis.model.AnalysisFiguresImpl(");
		sb.append("       count(distinct collection.key)");
		sb.append("     , count(distinct context.evaluationFormParticipation.key) + count(distinct context.evaluationFormSession.key)");
		sb.append("       )");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		
		TypedQuery<AnlaysisFigures> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AnlaysisFigures.class);
		appendParameters(query, searchParams);
		return query.getResultList().get(0);
	}

	List<String> loadContextLocations(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct context.location");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("context.location is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

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
		QueryBuilder sb = dbInstance.isOracle()
				? getContextCurriculumsOraQuery(searchParams)
				: getContextCurriculumsQuery(searchParams);
		
		TypedQuery<Curriculum> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Curriculum.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	private QueryBuilder getContextCurriculumsQuery(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct contextCurriculum");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculum.key is not null");
		return sb;
	}

	private QueryBuilder getContextCurriculumsOraQuery(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select dCurriculum from curriculum as dCurriculum where dCurriculum.key in (");
		sb.append("select distinct contextCurriculum.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculum.key is not null");
		sb.append(")");
		return sb;
	}
	
	List<CurriculumElement> loadContextCurriculumElements(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct contextCurriculumElement");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculumElement.key is not null");
		
		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<String> loadContextCurriculumOrganisationPathes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct contextCurriculumOrganisation.materializedPathKeys");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculumOrganisation.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<String> loadContextCurriculumElementPathes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct contextCurriculumElement.materializedPathKeys");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculumElement.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<Long> loadContextCurriculumElementsCurriculumKey(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct contextCurriculum.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculum.key is not null");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<CurriculumElementType> loadContextCurriculumElementsTypes(AnalysisSearchParameter searchParams) {
		String statement = dbInstance.isOracle()
				? getContextCurriculumElementsTypesOraQuery(searchParams)
				: getContextCurriculumElementsTypesQuery(searchParams);
				
		TypedQuery<CurriculumElementType> query = dbInstance.getCurrentEntityManager()
				.createQuery(statement, CurriculumElementType.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	private String getContextCurriculumElementsTypesQuery(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct contextCurriculumElement.type");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculumElement.type is not null");
		return sb.toString();
	}
	
	private String getContextCurriculumElementsTypesOraQuery(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select dType from curriculumelementtype dType where dType in (");
		sb.append("select distinct contextCurriculumElement.type.key");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("contextCurriculumElement.type is not null");
		sb.append(")");
		return sb.toString();
	}

	List<String> loadContextTaxonomyLevelPathes(AnalysisSearchParameter searchParams) {
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
	
	List<QualityContextRole> loadContextRoles(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct context.role");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		sb.and().append("context.role is not null");
		
		TypedQuery<QualityContextRole> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContextRole.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}
	
	List<QualityDataCollection> loadDataCollection(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct collection");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		
		TypedQuery<QualityDataCollection> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	public Integer loadMaxSeriesIndex(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select max(survey.seriesIndex)");
		appendFrom(sb, searchParams);
		appendWhere(sb, searchParams);
		
		TypedQuery<Integer> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Integer.class);
		appendParameters(query, searchParams);
		List<Integer> resultList = query.getResultList();
		return !resultList.isEmpty()? resultList.get(0): null;
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
	
	List<RawGroupedStatistic> loadGroupedStatistic(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, boolean groupByIdentifier, MultiGroupBy multiGroupBy,
			TemporalGroupBy temporalGroupBy) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty()) return new ArrayList<>();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.analysis.model.RawGroupedStatisticImpl(");
		sb.append(groupByIdentifier? " response.responseIdentifier": " cast(null as string)");
		appendGroupBys(sb, multiGroupBy, true);
		appendTemporalGroupBy(sb, temporalGroupBy, true);
		sb.append("     , count(distinct response.key)");
		sb.append("     , avg(response.numericalResponse)");
		sb.append("       )");
		appendFrom(sb, searchParams);
		sb.append("       inner join context.evaluationFormSession session");
		sb.append("       inner join evaluationformresponse response");
		sb.append("               on response.session.key = session.key");
		sb.append("              and (response.noResponse is false or response.noResponse is null)");
		sb.append("              and response.responseIdentifier in (:responseIdentifiers)");
		appendWhere(sb, searchParams);
		if (groupByIdentifier) {
			sb.groupBy().append(" response.responseIdentifier");
		}
		appendGroupBys(sb, multiGroupBy, false);
		appendTemporalGroupBy(sb, temporalGroupBy, false);
		
		TypedQuery<RawGroupedStatistic> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RawGroupedStatistic.class)
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
			if (select) sb.append(", cast(null as string)");
			return;
		}
		
		if (select) {
			sb.append(",");
		} else {
			sb.groupBy();
		}
		switch (groupBy) {
		case TOPIC_IDENTITY:
			castAsString(sb, "collection.topicIdentity.key", select);
			break;
		case TOPIC_ORGANISATION:
			castAsString(sb, "collection.topicOrganisation.key", select);
			break;
		case TOPIC_CURRICULUM:
			castAsString(sb, "collection.topicCurriculum.key", select);
			break;
		case TOPIC_CURRICULUM_ELEMENT:
			castAsString(sb, "collection.topicCurriculumElement.key", select);
			break;
		case TOPIC_REPOSITORY:
			castAsString(sb, "collection.topicRepositoryEntry.key", select);
			break;
		case CONTEXT_LOCATION:
			sb.append("context.location");
			break;
		case CONTEXT_ORGANISATION:
			castAsString(sb, "contextToOrganisation.organisation.key", select);
			break;
		case CONTEXT_CURRICULUM:
			castAsString(sb, "contextCurriculum.key", select);
			break;
		case CONTEXT_CURRICULUM_ELEMENT:
			castAsString(sb, "contextCurriculumElement.key", select);
			break;
		case CONTEXT_CURRICULUM_ORGANISATION:
			castAsString(sb, "contextCurriculumOrganisation.key", select);
			break;
		case CONTEXT_TAXONOMY_LEVEL:
			castAsString(sb, "contextToTaxonomyLevel.taxonomyLevel.key", select);
			break;
		case DATA_COLLECTION:
			castAsString(sb, "collection.key", select);
			break;
		default: 
		}
	}
	
	private void appendTemporalGroupBy(QueryBuilder sb, TemporalGroupBy temporalGroupBy, boolean select) {
		if (temporalGroupBy == null) {
			if (select) sb.append(", cast(null as string)");
			return;
		}
		
		if (select) {
			sb.append(",");
		} else {
			sb.groupBy();
		}
		switch (temporalGroupBy) {
		case DATA_COLLECTION_DEADLINE_YEAR:
			castAsString(sb, "year(collection.deadline)", true);
			break;
		case DATA_COLLECTION_DEADLINE_HALF_YEAR:
			castAsString(sb, "year(collection.deadline)", true);
			sb.append("||'").append(DELIMITER).append("'||");
			sb.append("CASE");
			sb.append("  WHEN month(collection.deadline) = 1 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 2 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 3 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 4 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 5 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 6 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 7 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 8 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 9 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 10 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 11 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 12 THEN '2'");
			sb.append("END");
			break;
		case DATA_COLLECTION_DEADLINE_QUARTER:
			castAsString(sb, "year(collection.deadline)", true);
			sb.append("||'").append(DELIMITER).append("'||");
			sb.append("CASE");
			sb.append("  WHEN month(collection.deadline) = 1 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 2 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 3 THEN '1'");
			sb.append("  WHEN month(collection.deadline) = 4 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 5 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 6 THEN '2'");
			sb.append("  WHEN month(collection.deadline) = 7 THEN '3'");
			sb.append("  WHEN month(collection.deadline) = 8 THEN '3'");
			sb.append("  WHEN month(collection.deadline) = 9 THEN '3'");
			sb.append("  WHEN month(collection.deadline) = 10 THEN '4'");
			sb.append("  WHEN month(collection.deadline) = 11 THEN '4'");
			sb.append("  WHEN month(collection.deadline) = 12 THEN '4'");
			sb.append("END");
			break;
		case DATA_COLLECTION_DEADLINE_MONTH:
			castAsString(sb, "year(collection.deadline)", true);
			sb.append("||'").append(DELIMITER).append("'||");
			castAsString(sb, "month(collection.deadline)", true);
			break;
		default:
		}
	}

	private void castAsString(QueryBuilder sb, String attribute, boolean select) {
		sb.append("cast(", select).append(attribute).append(" as string)", select);
	}
	
	private static void appendFrom(QueryBuilder sb, AnalysisSearchParameter searchParams) {
		sb.append("  from qualitydatacollection collection");
		sb.append("       inner join evaluationformsurvey survey");
		sb.append("               on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("              and survey.resId = collection.key");
		sb.append("       inner join qualitydatacollectiontoorganisation dc2org");
		sb.append("              on dc2org.dataCollection.key = collection.key");
		sb.append("       inner join organisation dcOrganisation");
		sb.append("              on dc2org.organisation.key = dcOrganisation.key");
		sb.append("       left join collection.topicOrganisation topicOrganisation");
		sb.append("       left join qualitycontext context");
		sb.append("              on context.dataCollection.key = collection.key");
		sb.append("       left join contexttocurriculumelement contextToCurriculumElement");
		sb.append("              on contextToCurriculumElement.context.key = context.key");
		sb.append("       left join curriculumelement contextCurriculumElement");
		sb.append("              on contextToCurriculumElement.curriculumElement.key = contextCurriculumElement.key");
		sb.append("       left join contextCurriculumElement.curriculum contextCurriculum");
		sb.append("       left join contextCurriculum.organisation contextCurriculumOrganisation");
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
		if (searchParams.getDataCollectionOrganisationRefs() != null && !searchParams.getDataCollectionOrganisationRefs().isEmpty()) {
			// load the organisations and all children
			sb.and();
			for (int i = 0; i < searchParams.getDataCollectionOrganisationRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("dcOrganisation.materializedPathKeys like :dcOrgPath").append(i);
				if (i == searchParams.getDataCollectionOrganisationRefs().size() - 1) {
					sb.append(")");
				}
			}
		}
		if (searchParams.getDateRangeFrom() != null) {
			sb.and().append("collection.deadline >= :dateRangeFrom");
		}
		if (searchParams.getDateRangeTo() != null) {
			sb.and().append("collection.deadline <= :dateRangeTo");
		}
		if (searchParams.getDataCollectionRefs() != null && !searchParams.getDataCollectionRefs().isEmpty()) {
			sb.and().append("collection.key in :dataCollectionKeys");
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
		if (searchParams.getContextLocations() != null && !searchParams.getContextLocations().isEmpty()) {
			sb.and().append("context.location in :contextLocations");
		}
		if (searchParams.getContextOrganisationRef() != null) {
			sb.and().append("contextOrganisation.key = :contextOrganisationKey");
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
			sb.and().append("contextCurriculum.key in :contextCurriculumKeys");
		}
		if (searchParams.getContextCurriculumElementRef() != null) {
			sb.and().append("contextCurriculumElement.key = :contextCurriculumElementKey");
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
				sb.append("contextCurriculumElement.materializedPathKeys like :elePath").append(i);
				if (i == searchParams.getContextCurriculumElementRefs().size() - 1) {
					sb.append(")");
				}
			}
		}
		if (searchParams.getContextCurriculumElementTypeRefs() != null && !searchParams.getContextCurriculumElementTypeRefs().isEmpty()) {
			sb.and().append("contextCurriculumElement.type.key in :contextCurriculumElementTypeKeys");
		}
		if (searchParams.getContextCurriculumOrganisationRef() != null) {
			sb.and().append("contextCurriculumOrganisation.key = :contextCurriculumOrganisationKey");
		}
		if (searchParams.getContextCurriculumOrganisationRefs() != null && !searchParams.getContextCurriculumOrganisationRefs().isEmpty()) {
			// load the organisations and all children
			sb.and();
			for (int i = 0; i < searchParams.getContextCurriculumOrganisationRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("contextCurriculumOrganisation.materializedPathKeys like :contextCurriculumOrganisationPath").append(i);
				if (i == searchParams.getContextCurriculumOrganisationRefs().size() - 1) {
					sb.append(")");
				}
			}
		}

		if (searchParams.getContextTaxonomyLevelRef() != null) {
			sb.and().append("taxonomyLevel.key = :contextTaxonomyLevelKey");
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
		if (searchParams.getSeriesIndexes() != null && !searchParams.getSeriesIndexes().isEmpty()) {
			sb.and().append("survey.seriesIndex in :seriesIndexes");
		}
		if (searchParams.getContextRoles() != null && !searchParams.getContextRoles().isEmpty()) {
			sb.and().append("context.role in :contextRoles");
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
		if (searchParams.isTopicIdentityNull()) {
			sb.and().append("collection.topicIdentity is null");
		}
		if (searchParams.isTopicOrganisationNull()) {
			sb.and().append("collection.topicOrganisation is null");
		}
		if (searchParams.isTopicCurriculumNull()) {
			sb.and().append("collection.topicCurriculum is null");
		}
		if (searchParams.isTopicCurriculumElementNull()) {
			sb.and().append("collection.topicCurriculumElement is null");
		}
		if (searchParams.isTopicRepositoryNull()) {
			sb.and().append("collection.topicRepositoryEntry is null");
		}
		if (searchParams.isContextOrganisationNull()) {
			sb.and().append("contextOrganisation is null");
		}
		if (searchParams.isContextCurriculumNull()) {
			sb.and().append("contextCurriculum is null");
		}
		if (searchParams.isContextCurriculumElementNull()) {
			sb.and().append("contextCurriculumElement is null");
		}
		if (searchParams.isContextCurriculumOrganisationNull()) {
			sb.and().append("contextCurriculumOrganisation is null");
		}
		if (searchParams.isContextTaxonomyLevelNull()) {
			sb.and().append("contextToTaxonomyLevel.taxonomyLevel is null");
		}
		if (searchParams.isContextLocationNull()) {
			sb.and().append("context.location is null");
		}
	}

	static void appendParameters(Query query, AnalysisSearchParameter searchParams) {
		if (searchParams.getFormEntryRef() != null) {
			query.setParameter("formEntryKey", searchParams.getFormEntryRef().getKey());
		}
		if (searchParams.getDataCollectionOrganisationRefs() != null && !searchParams.getDataCollectionOrganisationRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getDataCollectionOrganisationRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("dcOrgPath").append(i).toString();
				Long key = searchParams.getDataCollectionOrganisationRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getDateRangeFrom() != null) {
			query.setParameter("dateRangeFrom", searchParams.getDateRangeFrom());
		}
		if (searchParams.getDateRangeTo() != null) {
			query.setParameter("dateRangeTo", searchParams.getDateRangeTo());
		}
		if (searchParams.getDataCollectionRefs() != null && !searchParams.getDataCollectionRefs().isEmpty()) {
			List<Long> keys = searchParams.getDataCollectionRefs().stream().map(QualityDataCollectionRef::getKey).collect(toList());
			query.setParameter("dataCollectionKeys", keys);
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
		if (searchParams.getContextLocations() != null && !searchParams.getContextLocations().isEmpty()) {
			query.setParameter("contextLocations", searchParams.getContextLocations());
		}
		if (searchParams.getContextOrganisationRef() != null) {
			query.setParameter("contextOrganisationKey", searchParams.getContextOrganisationRef().getKey());
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
			query.setParameter("contextCurriculumKeys", keys);
		}
		if (searchParams.getContextCurriculumElementRef() != null) {
			query.setParameter("contextCurriculumElementKey", searchParams.getContextCurriculumElementRef().getKey());
		}
		if (searchParams.getContextCurriculumElementRefs() != null && !searchParams.getContextCurriculumElementRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getContextCurriculumElementRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("elePath").append(i).toString();
				Long key = searchParams.getContextCurriculumElementRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getContextCurriculumElementTypeRefs() != null && !searchParams.getContextCurriculumElementTypeRefs().isEmpty()) {
			List<Long> keys = searchParams.getContextCurriculumElementTypeRefs().stream().map(CurriculumElementTypeRef::getKey).collect(toList());
			query.setParameter("contextCurriculumElementTypeKeys", keys);
		}
		if (searchParams.getContextCurriculumOrganisationRef() != null) {
			query.setParameter("contextCurriculumOrganisationKey", searchParams.getContextCurriculumOrganisationRef().getKey());
		}
		if (searchParams.getContextCurriculumOrganisationRefs() != null && !searchParams.getContextCurriculumOrganisationRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getContextCurriculumOrganisationRefs().size(); i++) {
				String parameter = new StringBuilder(40).append("contextCurriculumOrganisationPath").append(i).toString();
				Long key = searchParams.getContextCurriculumOrganisationRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getContextTaxonomyLevelRef() != null) {
			query.setParameter("contextTaxonomyLevelKey", searchParams.getContextTaxonomyLevelRef().getKey());
		}
		if (searchParams.getContextTaxonomyLevelRefs() != null && !searchParams.getContextTaxonomyLevelRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getContextTaxonomyLevelRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("taxonomyLevelPath").append(i).toString();
				Long key = searchParams.getContextTaxonomyLevelRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getSeriesIndexes() != null && !searchParams.getSeriesIndexes().isEmpty()) {
			query.setParameter("seriesIndexes", searchParams.getSeriesIndexes());
		}
		if (searchParams.getContextRoles() != null && !searchParams.getContextRoles().isEmpty()) {
			query.setParameter("contextRoles", searchParams.getContextRoles());
		}
	}

}
