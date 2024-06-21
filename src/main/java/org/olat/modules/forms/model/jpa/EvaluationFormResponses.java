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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;

/**
 * 
 * Initial date: 04.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormResponses {
	
	private final Map<EvaluationFormSession, Map<String, List<EvaluationFormResponse>>> sessionToResponses;

	public EvaluationFormResponses(List<EvaluationFormResponse> responses) {
		sessionToResponses = new HashMap<>();
		for (EvaluationFormResponse response: responses) {
			EvaluationFormSession session = response.getSession();
			String identifier = response.getResponseIdentifier();

			Map<String, List<EvaluationFormResponse>> identifierToResponse = sessionToResponses.get(session);
			if (identifierToResponse == null) {
				identifierToResponse = new HashMap<>();
				sessionToResponses.put(session, identifierToResponse);
			}
			
			List<EvaluationFormResponse> responseList = identifierToResponse.get(identifier);
			if (responseList == null) {
				responseList = new ArrayList<>();
				identifierToResponse.put(identifier, responseList);
			}
			responseList.add(response);
		}
	}

	public EvaluationFormResponse getResponse(EvaluationFormSession session, String responseIdentifier) {
		List<EvaluationFormResponse> responses = getResponses(session, responseIdentifier);
		if (!responses.isEmpty()) {
			return responses.get(0);
		}
		return null;
	}

	public List<EvaluationFormResponse> getResponses(EvaluationFormSession session, String responseIdentitfier) {
		Map<String, List<EvaluationFormResponse>> identifierToResponses = getResponsesBySession(session);
		List<EvaluationFormResponse> responses = identifierToResponses.get(responseIdentitfier);
		if (responses == null) {
			responses = new ArrayList<>(0);
		}
		return responses;
	}

	public Map<String, List<EvaluationFormResponse>> getResponsesBySession(EvaluationFormSession session) {
		Map<String, List<EvaluationFormResponse>> identifierToResponses = sessionToResponses.get(session);
		if (identifierToResponses == null) {
			identifierToResponses = new HashMap<>(0);
		}
		return identifierToResponses;
	}
	
	public Set<EvaluationFormSession> getSessions() {
		return sessionToResponses.keySet();
	}
	
	public List<EvaluationFormResponse> getResponses(String responseIdentitfier) {
		return sessionToResponses.entrySet().stream()
				.map(Entry::getValue)
				.map(m -> m.get(responseIdentitfier))
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.toList();
	}

}
