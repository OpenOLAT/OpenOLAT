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
package org.olat.modules.forms.manager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.forms.model.jpa.EvaluationFormSurveyImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class EvaluationFormSurveyDAO {
	
	@Autowired
	private DB dbInstance;

	EvaluationFormSurvey createSurvey(OLATResourceable ores, String subIdent, String subIdent2,
			RepositoryEntry formEntry, EvaluationFormSurvey previous) {
		EvaluationFormSurveyImpl survey = new EvaluationFormSurveyImpl();
		survey.setCreationDate(new Date());
		survey.setLastModified(survey.getCreationDate());
		survey.setResName(ores.getResourceableTypeName());
		survey.setResId(ores.getResourceableId());
		survey.setResSubident(subIdent);
		survey.setResSubident2(subIdent2);
		survey.setFormEntry(formEntry);
		dbInstance.getCurrentEntityManager().persist(survey);
		Long seriesKey = previous != null? previous.getSeriesKey(): survey.getKey();
		survey.setSeriesKey(seriesKey);
		Integer seriesIndex = previous != null? previous.getSeriesIndex() + 1: 1;
		survey.setSeriesIndex(seriesIndex);
		survey.setSeriesPrevious(previous);
		return dbInstance.getCurrentEntityManager().merge(survey);
	}

	EvaluationFormSurvey updateForm(EvaluationFormSurvey survey, RepositoryEntry formEntry) {
		if (survey instanceof EvaluationFormSurveyImpl) {
			EvaluationFormSurveyImpl surveyImpl = (EvaluationFormSurveyImpl) survey;
			surveyImpl.setFormEntry(formEntry);
			return update(surveyImpl);
		}
		return survey;
	}

	private EvaluationFormSurvey update(EvaluationFormSurveyImpl surveyImpl) {
		surveyImpl.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(surveyImpl);
	}
	
	EvaluationFormSurvey loadByResourceable(OLATResourceable ores, String subIdent, String subIdent2) {
		List<EvaluationFormSurvey> surveys = loadSurveysByResourceable(ores, subIdent, subIdent2);
		return surveys.isEmpty() ? null : surveys.get(0);
	}

	List<EvaluationFormSurvey> loadSurveysByResourceable(OLATResourceable ores, String subIdent, String subIdent2) {
		if (ores == null || ores.getResourceableTypeName() == null || ores.getResourceableId() == null)
			return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select survey from evaluationformsurvey as survey");
		sb.append(" where survey.resName=:resName");
		sb.append("   and survey.resId=:resId");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and survey.resSubident=:resSubident");
		}
		if (StringHelper.containsNonWhitespace(subIdent2)) {
			sb.append(" and survey.resSubident2=:resSubident2");
		}
		
		TypedQuery<EvaluationFormSurvey> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormSurvey.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("resSubident", subIdent);
		}
		if (StringHelper.containsNonWhitespace(subIdent2)) {
			query.setParameter("resSubident2", subIdent2);
		}
		return query.getResultList();
	}

	boolean hasSurvey(RepositoryEntryRef formEntrRef, String oresTypeName) {
		if (formEntrRef == null) return false;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select survey.key");
		sb.append("  from evaluationformsurvey as survey");
		sb.and().append("survey.formEntry.key = :formEntryKey");
		sb.and().append("survey.resName = :oresTypeName");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("formEntryKey", formEntrRef.getKey())
				.setParameter("oresTypeName", oresTypeName)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys == null || keys.isEmpty() || keys.get(0) == null ? false : true;
	}

	void delete(EvaluationFormSurveyRef survey) {
		if (survey == null) return;	

		String query = "delete from evaluationformsurvey as survey where survey.key=:surveyKey";
		
		dbInstance.getCurrentEntityManager().createQuery(query)
			.setParameter("surveyKey", survey.getKey())
			.executeUpdate();
	}

	EvaluationFormSurvey updateSeriesPrevious(EvaluationFormSurvey survey, EvaluationFormSurvey previous) {
		if (survey instanceof EvaluationFormSurveyImpl) {
			EvaluationFormSurveyImpl surveyImpl = (EvaluationFormSurveyImpl) survey;
			surveyImpl.setSeriesPrevious(previous);
			return update(surveyImpl);
		}
		return survey;
	}

	boolean hasSeriesNext(EvaluationFormSurveyRef surveyRef) {
		if (surveyRef == null) return false;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select survey.key");
		sb.append("  from evaluationformsurvey as survey");
		sb.append(" where survey.seriesPrevious.key = :surveyKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("surveyKey", surveyRef.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys == null || keys.isEmpty() || keys.get(0) == null ? false : true;
	}

	EvaluationFormSurvey loadSeriesNext(EvaluationFormSurveyRef surveyRef) {
		if (surveyRef == null) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select survey");
		sb.append("  from evaluationformsurvey as survey");
		sb.append(" where survey.seriesPrevious.key = :surveyKey");
		
		List<EvaluationFormSurvey> next = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormSurvey.class)
				.setParameter("surveyKey", surveyRef.getKey())
				.getResultList();
		return !next.isEmpty() ? next.get(0) : null;
	}

	void reindexSeries(Long seriesKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select survey");
		sb.append("  from evaluationformsurvey as survey");
		sb.append(" where survey.seriesKey = :seriesKey");
		sb.append(" order by survey.seriesIndex");
		
		List<EvaluationFormSurvey> surveys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormSurvey.class)
				.setParameter("seriesKey", seriesKey)
				.getResultList();
		int seriesIndex = 1;
		for (EvaluationFormSurvey survey : surveys) {
			if (survey instanceof EvaluationFormSurveyImpl) {
				EvaluationFormSurveyImpl surveyImpl = (EvaluationFormSurveyImpl) survey;
				surveyImpl.setSeriesIndex(seriesIndex++);
				update(surveyImpl);
			}
		}
	}

}
