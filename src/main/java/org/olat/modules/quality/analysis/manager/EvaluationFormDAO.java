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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.EvaluationFormViewSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormDAO {
	
	@Autowired
	private DB dbInstance;

	List<EvaluationFormView> load(EvaluationFormViewSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.analysis.model.EvaluationFormViewImpl(");
		sb.append("       form as formEntry");
		sb.append("     , count(distinct collection) as numberDataCollections");
		sb.append("     , min(collection.start) as soonestDataCollectionDate");
		sb.append("     , max(collection.deadline) as latestDataCollectionDate");
		sb.append("     , count(session.key) as numberParticipants");
		sb.append("       )");
		sb.append("  from repositoryentry form");
		sb.append("       inner join evaluationformsurvey survey");
		sb.append("               on survey.formEntry.key = form.key");
		sb.append("       left join evaluationformsession session");
		sb.append("              on session.survey.key = survey.key");
		sb.append("             and session.status ='").append(EvaluationFormSessionStatus.done).append("'");
		sb.append("       inner join qualitydatacollection as collection");
		sb.append("               on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("              and survey.resId = collection.key");
		sb.append("      ");
		appendWhere(sb, searchParams);
		sb.append(" group by form.key");
		sb.append("        , form.displayname");
		
		TypedQuery<EvaluationFormView> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormView.class);
		appendParameters(query, searchParams);
		return query.getResultList();
	}

	private void appendWhere(QueryBuilder sb, EvaluationFormViewSearchParams searchParams) {
		sb.and().append("collection.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
		if (searchParams.getOrganisationRefs() != null) {
			sb.and();
			sb.append("collection.key in (");
			sb.append("   select dataCollection.key");
			sb.append("     from qualitydatacollectiontoorganisation dcToOrg");
			sb.append("    where dcToOrg.organisation.key in :organisationKeys");
			sb.append(")");
		}
	}

	private void appendParameters(TypedQuery<EvaluationFormView> query, EvaluationFormViewSearchParams searchParams) {
		if (searchParams.getOrganisationRefs() != null) {
			List<Long> organisationKeys = searchParams.getOrganisationRefs().stream().map(OrganisationRef::getKey).collect(Collectors.toList());
			organisationKeys = !organisationKeys.isEmpty()? organisationKeys: Collections.singletonList(-1l);
			query.setParameter("organisationKeys", organisationKeys);
		}
	}

}
