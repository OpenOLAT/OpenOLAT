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
package org.olat.modules.forms.model.jpa;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.Query;

import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.forms.SessionFilter;

/**
 * 
 * Initial date: 10.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveysFilter implements SessionFilter {

	private Collection<? extends EvaluationFormSurveyRef> surveys;
	private EvaluationFormSessionStatus status;
	private boolean fetchExecutor;
	private EvaluationFormSurveyIdentifier surveyIdentitfier;
	
	public SurveysFilter(Collection<? extends EvaluationFormSurveyRef> surveys) {
		this(surveys, null, false);
	}

	public SurveysFilter(Collection<? extends EvaluationFormSurveyRef> surveys, EvaluationFormSessionStatus status, boolean fetchExecutor) {
		this.surveys = surveys;
		this.status = status;
		this.fetchExecutor = fetchExecutor;
	}
	
	public SurveysFilter(EvaluationFormSurveyIdentifier surveyIdentitfier) {
		this(surveyIdentitfier, null);
	}

	public SurveysFilter(EvaluationFormSurveyIdentifier surveyIdentitfier, EvaluationFormSessionStatus status) {
		this.surveyIdentitfier = surveyIdentitfier;
		this.status = status;
	}

	@Override
	public String getSelectKeys() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select sessionFilter.key");
		sb.append("  from evaluationformsession sessionFilter");
		if (status != null) {
			sb.and().append("sessionFilter.status = '").append(EvaluationFormSessionStatus.done).append("'");
		}
		if (surveys != null) {
			sb.and().append("sessionFilter.survey.key in :surveyFilterKeys");
		}
		if (surveyIdentitfier != null) {
			sb.and().append("sessionFilter.survey.resId = :surveyResId");
			sb.and().append("sessionFilter.survey.resName = :surveyResName");
			if (StringHelper.containsNonWhitespace(surveyIdentitfier.getSubident())) {
				sb.and().append("sessionFilter.survey.resSubident = :surveyResSubident");
			}
			if (StringHelper.containsNonWhitespace(surveyIdentitfier.getSubident2())) {
				sb.and().append("sessionFilter.survey.resSubident2 = :surveyResSubident2");
			}
		}
		return sb.toString();
	}

	@Override
	public void addParameters(Query query) {
		if (surveys != null) {
			List<Long> keys = surveys.stream().map(EvaluationFormSurveyRef::getKey).collect(toList());
			query.setParameter("surveyFilterKeys", keys);
		}
		if (surveyIdentitfier != null) {
			query.setParameter("surveyResId", surveyIdentitfier.getOLATResourceable().getResourceableId());
			query.setParameter("surveyResName", surveyIdentitfier.getOLATResourceable().getResourceableTypeName());
			if (StringHelper.containsNonWhitespace(surveyIdentitfier.getSubident())) {
				query.setParameter("surveyResSubident", surveyIdentitfier.getSubident());
			}
			if (StringHelper.containsNonWhitespace(surveyIdentitfier.getSubident2())) {
				query.setParameter("surveyResSubident2", surveyIdentitfier.getSubident2());
			}
		}
	}

	@Override
	public boolean fetchSurveys() {
		return true;
	}

	@Override
	public boolean fetchExecutor() {
		return fetchExecutor;
	}
}
