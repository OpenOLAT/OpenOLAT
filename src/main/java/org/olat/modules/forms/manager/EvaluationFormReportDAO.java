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

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.Limit;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.jpa.CalculatedDouble;
import org.olat.modules.forms.model.jpa.CalculatedLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormReportDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Long getResponsesCount(String responseIdentifier, SessionFilter filter, Limit limit) {
		return getResponsesCount(singletonList(responseIdentifier) , filter, limit);
	}

	public Long getResponsesCount(List<String> responseIdentifiers, SessionFilter filter, Limit limit) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select count(response.key)");
		getResponsesAppendFrom(sb, filter, false);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		getResponsesAppendParameters(query, responseIdentifiers, filter, limit);
		return query.getResultList().get(0);
	}
	
	public List<EvaluationFormResponse> getResponses(String responseIdentifier, SessionFilter filter, Limit limit) {
		return getResponses(singletonList(responseIdentifier) , filter, limit);
	}
	
	public List<EvaluationFormResponse> getResponses(List<String> responseIdentifiers, SessionFilter filter, Limit limit) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || filter == null)
			return new ArrayList<>();;
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select response");
		getResponsesAppendFrom(sb, filter, true);
		
		TypedQuery<EvaluationFormResponse> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormResponse.class);
		getResponsesAppendParameters(query, responseIdentifiers, filter, limit);
		return query.getResultList();
	}

	private void getResponsesAppendFrom(QueryBuilder sb, SessionFilter filter, boolean fetch) {
		sb.append("  from evaluationformresponse as response");
		sb.append(" inner join").append(" fetch", fetch).append(" response.session");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (");
		sb.append(filter.getSelectKeys());
		sb.append("       )");
		sb.append("   and (response.noResponse = false or response.noResponse is null)");
	}

	private void getResponsesAppendParameters(Query query, List<String> responseIdentifiers, SessionFilter filter,
			Limit limit) {
		query.setParameter("responseIdentifiers", responseIdentifiers);
		if (limit.getMax() > -1) {
			query.setMaxResults(limit.getMax());
		}
		filter.addParameters(query);
	}

	public List<CalculatedLong> getCountByStringuifideResponse(String responseIdentifier, SessionFilter filter) {
		if (filter == null || !StringHelper.containsNonWhitespace(responseIdentifier))
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedLong(");
		sb.append("       cast(response.stringuifiedResponse as string)");
		sb.append("     , count(response))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier=:responseIdentifier");
		sb.append("   and response.session.key in (");
		sb.append(filter.getSelectKeys());
		sb.append("       )");
		sb.append("   and (response.noResponse = false or response.noResponse is null)");
		sb.append(" group by cast(response.stringuifiedResponse as string)");
		
		TypedQuery<CalculatedLong> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), CalculatedLong.class)
					.setParameter("responseIdentifier", responseIdentifier);
		filter.addParameters(query);
		return query.getResultList();
	}
	
	public List<CalculatedLong> getCountByIdentifiersAndNumerical(List<String> responseIdentifiers,
			SessionFilter filter) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || filter == null)
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedLong(response.responseIdentifier, response.numericalResponse, count(response))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (");
		sb.append(filter.getSelectKeys());
		sb.append("       )");
		sb.append("   and (response.noResponse = false or response.noResponse is null)");
		sb.append(" group by response.responseIdentifier");
		sb.append("        , response.numericalResponse");
		
		TypedQuery<CalculatedLong> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalculatedLong.class)
				.setParameter("responseIdentifiers", responseIdentifiers);
		filter.addParameters(query);
		return query.getResultList();
	}

	public List<CalculatedLong> getCountNoResponsesByIdentifiers(List<String> responseIdentifiers,
			SessionFilter filter) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || filter == null)
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedLong(response.responseIdentifier, count(response))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (");
		sb.append(filter.getSelectKeys());
		sb.append("       )");
		sb.append("   and response.noResponse = true");
		sb.append(" group by response.responseIdentifier");
		
		TypedQuery<CalculatedLong> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalculatedLong.class)
				.setParameter("responseIdentifiers", responseIdentifiers);
		filter.addParameters(query);
		return query.getResultList();
	}

	public List<CalculatedDouble> getAvgByResponseIdentifiers(List<String> responseIdentifiers, SessionFilter filter) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || filter == null )
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedDouble(response.responseIdentifier, avg(response.numericalResponse))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (");
		sb.append(filter.getSelectKeys());
		sb.append("       )");
		sb.append("   and (response.noResponse = false or response.noResponse is null)");
		sb.append(" group by response.responseIdentifier");
		
		TypedQuery<CalculatedDouble> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalculatedDouble.class)
				.setParameter("responseIdentifiers", responseIdentifiers);
		filter.addParameters(query);
		return query.getResultList();
	}
	
}
