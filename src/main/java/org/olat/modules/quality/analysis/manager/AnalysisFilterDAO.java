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

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupedStatistic;
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

	List<Organisation> loadContextOrganisations(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct organisation");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		sb.and().append("organisation.key is not null");
		
		TypedQuery<Organisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}
	
	List<String> loadContextOrganisationPathes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct organisation.materializedPathKeys");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		sb.and().append("organisation.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<Curriculum> loadContextCurriculums(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct curriculum");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		sb.and().append("curriculum.key is not null");
		
		TypedQuery<Curriculum> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Curriculum.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	List<String> loadContextCurriculumElementPathes(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct curriculumElement.materializedPathKeys");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		sb.and().append("curriculumElement.key is not null");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	Long loadDataCollectionCount(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(distinct collection.key)");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList().get(0);
	}
	
	List<Long> loadTopicOrganisationKeys(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct collection.topicOrganisation.key");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		sb.and().append("collection.topicOrganisation.key is not null");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}
	
	List<Long> loadTopicCurriculumKeys(AnalysisSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct collection.topicCurriculum.key");
		appendFrom(sb);
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
		appendFrom(sb);
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
		appendFrom(sb);
		appendWhere(sb, searchParams);
		sb.and().append("collection.topicIdentity.key is not null");
		
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
		sb.append("select distinct context.evaluationFormSession.key");
		appendFrom(sb);
		appendWhere(sb, searchParams);
		sb.and().append("context.evaluationFormSession.key is not null");
	}
	
	List<GroupedStatistic> loadGroupedStatisticByResponseIdentifiers(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, GroupBy groupBy) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty()) return new ArrayList<>();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.analysis.GroupedStatistic(");
		sb.append("       response.responseIdentifier");
		appendGroupBy(sb, groupBy, true);
		sb.append("     , count(response)");
		sb.append("     , avg(response.numericalResponse)");
		sb.append("       )");
		appendFrom(sb);
		sb.append("       inner join context.evaluationFormSession session");
		sb.append("       inner join evaluationformresponse response");
		sb.append("               on response.session.key = session.key");
		sb.append("              and (response.noResponse is false or response.noResponse is null)");
		sb.append("              and response.responseIdentifier in (:responseIdentifiers)");
		appendWhere(sb, searchParams);
		sb.append(" group by response.responseIdentifier");
		appendGroupBy(sb, groupBy, false);
		
		TypedQuery<GroupedStatistic> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GroupedStatistic.class)
				.setParameter("responseIdentifiers", responseIdentifiers);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	private void appendGroupBy(QueryBuilder sb, GroupBy groupBy, boolean select) {
		switch (groupBy) {
		case CONTEXT_ORAGANISATION:
			sb.append(", contextToOrganisation.organisation.key");
			break;
		case CONTEXT_CURRICULUM:
			sb.append(", contextToCurriculum.curriculum.key");
			break;
		case CONETXT_CURRICULUM_ELEMENT:
			sb.append(", contextToCurriculumElement.curriculumElement.key");
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
		case TOPIC_IDENTITY:
			sb.append(", collection.topicIdentity.key");
			break;
		default: 
			if (select) sb.append(", null");
		}
	}

	static void appendFrom(QueryBuilder sb) {
		sb.append("  from qualitydatacollection collection");
		sb.append("       inner join evaluationformsurvey survey");
		sb.append("               on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("              and survey.resId = collection.key");
		sb.append("       left join qualitycontext context");
		sb.append("               on context.dataCollection.key = collection.key");
		sb.append("       left join contexttocurriculum contextToCurriculum");
		sb.append("               on contextToCurriculum.context.key = context.key");
		sb.append("       left join curriculum curriculum");
		sb.append("               on contextToCurriculum.curriculum.key = curriculum.key");
		sb.append("       left join contexttocurriculumelement contextToCurriculumElement");
		sb.append("               on contextToCurriculumElement.context.key = context.key");
		sb.append("       left join curriculumelement curriculumElement");
		sb.append("               on contextToCurriculumElement.curriculumElement.key = curriculumElement.key");
		sb.append("       left join contexttoorganisation contextToOrganisation");
		sb.append("               on contextToOrganisation.context.key = context.key");
		sb.append("       left join organisation organisation");
		sb.append("               on contextToOrganisation.organisation.key = organisation.key");
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
		if (searchParams.getOrganisationRefs() != null) {
			sb.and();
			for (int i = 0; i < searchParams.getOrganisationRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("organisation.materializedPathKeys like :orgPath").append(i);
				if (i == searchParams.getOrganisationRefs().size() - 1) {
					sb.append(")");
				}
			}
		}
		if (searchParams.getCurriculumRefs() != null) {
			sb.and().append("curriculum.key in :curriculumKeys");
		}
		if (searchParams.getCurriculumElementRefs() != null) {
			sb.and();
			for (int i = 0; i < searchParams.getCurriculumElementRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("curriculumElement.materializedPathKeys like :elePath").append(i);
				if (i == searchParams.getCurriculumElementRefs().size() - 1) {
					sb.append(")");
				}
			}
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
		if (searchParams.getOrganisationRefs() != null) {
			for (int i = 0; i < searchParams.getOrganisationRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("orgPath").append(i).toString();
				Long key = searchParams.getOrganisationRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getCurriculumRefs() != null) {
			List<Long> curriculumKeys = searchParams.getCurriculumRefs().stream().map(CurriculumRef::getKey).collect(toList());
			query.setParameter("curriculumKeys", curriculumKeys);
		}
		if (searchParams.getCurriculumElementRefs() != null) {
			for (int i = 0; i < searchParams.getCurriculumElementRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("elePath").append(i).toString();
				Long key = searchParams.getCurriculumElementRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
	}

}
