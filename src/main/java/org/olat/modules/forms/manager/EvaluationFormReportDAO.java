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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSessionRef;
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

	public List<EvaluationFormResponse> getResponses(String responseIdentifier,
			List<? extends EvaluationFormSessionRef> sessions) {
		List<String> responseIdentifiers = Collections.singletonList(responseIdentifier);
		return getResponses(responseIdentifiers , sessions);
	}
	
	public List<EvaluationFormResponse> getResponses(List<String> responseIdentifiers,
			List<? extends EvaluationFormSessionRef> sessions) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || sessions == null || sessions.isEmpty())
			return new ArrayList<>();;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select response from evaluationformresponse as response");
		sb.append(" inner join fetch response.session");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (:sessionKeys)");
		sb.append("   and (response.noResponse is false or response.noResponse is null)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormResponse.class)
				.setParameter("responseIdentifiers", responseIdentifiers)
				.setParameter("sessionKeys", getSessionKeys(sessions))
				.getResultList();
	}


	public List<CalculatedLong> getCountByStringuifideResponse(String responseIdentifier,
			List<? extends EvaluationFormSessionRef> sessions) {
		if (sessions == null || sessions.isEmpty() || !StringHelper.containsNonWhitespace(responseIdentifier))
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedLong(response.stringuifiedResponse, '', count(response))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier=:responseIdentifier");
		sb.append("   and response.session.key in (:sessionKeys)");
		sb.append("   and (response.noResponse is false or response.noResponse is null)");
		sb.append(" group by response.stringuifiedResponse");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalculatedLong.class)
				.setParameter("responseIdentifier", responseIdentifier)
				.setParameter("sessionKeys", getSessionKeys(sessions))
				.getResultList();
	}
	
	public List<CalculatedLong> getCountByIdentifiersAndNumerical(List<String> responseIdentifiers,
			List<? extends EvaluationFormSessionRef> sessions) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || sessions == null || sessions.isEmpty())
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedLong(response.responseIdentifier, response.numericalResponse, count(response))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (:sessionKeys)");
		sb.append("   and (response.noResponse is false or response.noResponse is null)");
		sb.append(" group by response.responseIdentifier");
		sb.append("        , response.numericalResponse");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalculatedLong.class)
				.setParameter("responseIdentifiers", responseIdentifiers)
				.setParameter("sessionKeys", getSessionKeys(sessions))
				.getResultList();
	}

	public List<CalculatedLong> getCountNoResponsesByIdentifiers(List<String> responseIdentifiers,
			List<? extends EvaluationFormSessionRef> sessions) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || sessions == null || sessions.isEmpty())
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedLong(response.responseIdentifier, '', count(response))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (:sessionKeys)");
		sb.append("   and response.noResponse is true");
		sb.append(" group by response.responseIdentifier");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalculatedLong.class)
				.setParameter("responseIdentifiers", responseIdentifiers)
				.setParameter("sessionKeys", getSessionKeys(sessions))
				.getResultList();
	}

	public List<CalculatedDouble> getAvgByResponseIdentifiers(List<String> responseIdentifiers,
			List<? extends EvaluationFormSessionRef> sessions) {
		if (responseIdentifiers == null || responseIdentifiers.isEmpty() || sessions == null || sessions.isEmpty())
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.forms.model.jpa.CalculatedDouble(response.responseIdentifier, avg(response.numericalResponse))");
		sb.append("  from evaluationformresponse as response");
		sb.append(" where response.responseIdentifier in (:responseIdentifiers)");
		sb.append("   and response.session.key in (:sessionKeys)");
		sb.append("   and (response.noResponse is false or response.noResponse is null)");
		sb.append(" group by response.responseIdentifier");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalculatedDouble.class)
				.setParameter("responseIdentifiers", responseIdentifiers)
				.setParameter("sessionKeys", getSessionKeys(sessions))
				.getResultList();
	}
	
	private List<Long> getSessionKeys(List<? extends EvaluationFormSessionRef> sessions) {
		return sessions.stream().map(EvaluationFormSessionRef::getKey).collect(Collectors.toList());
	}

}
