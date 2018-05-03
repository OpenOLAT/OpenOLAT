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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormResponseImpl;
import org.olat.modules.portfolio.PageBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormResponseDAO {
	
	@Autowired
	private DB dbInstance;
	
	public EvaluationFormResponse createResponse(String responseIdentifier, BigDecimal numericalValue, String stringuifiedResponse,
			Path fileResponse, EvaluationFormSession session) {
		EvaluationFormResponseImpl response = createResponse(responseIdentifier, session);
		response.setNoResponse(false);
		response.setFileResponse(fileResponse);
		response.setNumericalResponse(numericalValue);
		response.setStringuifiedResponse(stringuifiedResponse);
		dbInstance.getCurrentEntityManager().persist(response);
		return response;
	}

	public EvaluationFormResponse createNoResponse(String responseIdentifier, EvaluationFormSession session) {
		EvaluationFormResponseImpl response = createResponse(responseIdentifier, session);
		response.setNoResponse(true);
		dbInstance.getCurrentEntityManager().persist(response);
		return response;
	}
	
	private EvaluationFormResponseImpl createResponse(String responseIdentifier, EvaluationFormSession session) {
		EvaluationFormResponseImpl response = new EvaluationFormResponseImpl();
		response.setCreationDate(new Date());
		response.setLastModified(response.getCreationDate());
		response.setSession(session);
		response.setResponseIdentifier(responseIdentifier);
		return response;
	}

	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(IdentityRef identity, PageBody anchor) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response from evaluationformresponse as response")
		  .append(" inner join response.session as session")
		  .append(" where session.identity.key=:identityKey and session.pageBody.key=:bodyKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormResponse.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("bodyKey", anchor.getKey())
				.getResultList();
	}
	
	public List<EvaluationFormResponse> getResponsesFromPortfolioEvaluation(List<? extends IdentityRef> identities, PageBody anchor, EvaluationFormSessionStatus status) {
		if(identities == null || identities.isEmpty()) return Collections.emptyList();
		
		List<Long> identitiyKeys = identities.stream().map(i -> i.getKey()).collect(Collectors.toList());
		StringBuilder sb = new StringBuilder();
		sb.append("select response from evaluationformresponse as response")
		  .append(" inner join response.session as session")
		  .append(" where session.identity.key in (:identityKeys) and session.pageBody.key=:bodyKey");
		if(status != null) {
			sb.append(" and session.status=:status");
		}
		TypedQuery<EvaluationFormResponse> rQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormResponse.class)
				.setParameter("identityKeys", identitiyKeys)
				.setParameter("bodyKey", anchor.getKey());
		if(status != null) {
			rQuery.setParameter("status", status.name());
		}
		return rQuery.getResultList();
	}
	
	public EvaluationFormResponse updateResponse(BigDecimal numericalValue, String stringuifiedResponse,
			Path fileResponse, EvaluationFormResponse response) {
		EvaluationFormResponseImpl evalResponse = (EvaluationFormResponseImpl)response;
		evalResponse.setLastModified(new Date());
		evalResponse.setNoResponse(false);
		evalResponse.setNumericalResponse(numericalValue);
		evalResponse.setStringuifiedResponse(stringuifiedResponse);
		evalResponse.setFileResponse(fileResponse);
		return dbInstance.getCurrentEntityManager().merge(response);
	}

	public EvaluationFormResponse updateNoResponse(EvaluationFormResponse response) {
		EvaluationFormResponseImpl evalResponse = (EvaluationFormResponseImpl)response;
		evalResponse.setLastModified(new Date());
		evalResponse.setNoResponse(true);
		evalResponse.setNumericalResponse(null);
		evalResponse.setStringuifiedResponse(null);
		evalResponse.setFileResponse(null);
		return dbInstance.getCurrentEntityManager().merge(response);
	}

	public void deleteResponses(List<Long> keys) {
		if (keys == null || keys.isEmpty()) return;
		
		String query = "delete from evaluationformresponse response where response.key in (:keys)";

		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("keys", keys)
				.executeUpdate();
	}

	public EvaluationFormResponse loadResponse(String responseIdentifier, EvaluationFormSession session) {
		List<EvaluationFormResponse> resultList = loadResponses(responseIdentifier, session);
		return resultList.isEmpty()? null: resultList.get(0);
	}

	public List<EvaluationFormResponse> loadResponses(String responseIdentifier, EvaluationFormSession session) {
		String query = new StringBuilder()
				.append("select response from evaluationformresponse as response")
				.append(" inner join response.session as session")
				.append(" where session.key=:sessionKey and response.responseIdentifier=:responseIdentifier")
				.toString();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, EvaluationFormResponse.class)
				.setParameter("sessionKey", session.getKey())
				.setParameter("responseIdentifier", responseIdentifier)
				.getResultList();
	}

	List<EvaluationFormResponse> loadResponsesBySurvey(EvaluationFormSurvey survey) {
		if (survey == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select response from evaluationformresponse as response");
		sb.append(" inner join response.session as session");
		sb.append(" inner join session.survey as survey");
		sb.append(" where survey.key=:surveyKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormResponse.class)
				.setParameter("surveyKey", survey.getKey())
				.getResultList();
	}

}
