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
package org.olat.modules.forms.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.ui.ReportHelper;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceLegendTextDataSource implements LegendTextDataSource {

	private final MultipleChoice multipleChoice;
	private final List<? extends EvaluationFormSessionRef> sessions;
	private final ReportHelper reportHelper;
	
	private EvaluationFormReportDAO reportDAO;
	
	public MultipleChoiceLegendTextDataSource(MultipleChoice multipleChoice,
			List<? extends EvaluationFormSessionRef> sessions, ReportHelper reportHelper) {
		super();
		this.multipleChoice = multipleChoice;
		this.sessions = sessions;
		this.reportHelper = reportHelper;
		this.reportDAO = CoreSpringFactory.getImpl(EvaluationFormReportDAO.class);
	}

	@Override
	public List<SessionText> getResponses() {
		List<SessionText> sessionTexts = new ArrayList<>();
		List<EvaluationFormResponse> responses = reportDAO.getResponses(multipleChoice.getId(), sessions);
		Map<EvaluationFormSession, List<EvaluationFormResponse>> sessionToResponse = responses.stream()
				.collect(Collectors.groupingBy(EvaluationFormResponse::getSession));
		
		List<EvaluationFormSession> sessions = new ArrayList<>(sessionToResponse.keySet());
		sessions.sort(reportHelper.getComparator());
		for (EvaluationFormSession session: sessions) {
			List<EvaluationFormResponse> sessionResponses = sessionToResponse.get(session);
			String text = concatResponses(sessionResponses);
			if (text != null) {
				SessionText sessionText = new SessionText(session, text);
				sessionTexts.add(sessionText);
			}
		}
		return sessionTexts;
	}

	private String concatResponses(List<EvaluationFormResponse> responses) {
		StringBuilder choices = new StringBuilder();
		for (EvaluationFormResponse response: responses) {
			String choiceKey = response.getStringuifiedResponse();
			String choice = getChoice(choiceKey);
			if (choice == null && multipleChoice.isWithOthers()) {
				choice = choiceKey;
			}
			if (choice != null) {
				choices.append(choice).append("<br/>");
			}
		}
		return choices.toString();
	}
	
	private String getChoice(String choiceKey) {
		for (Choice choice: multipleChoice.getChoices().asList()) {
			if (choiceKey.equals(choice.getId())) {
				return choice.getValue();
			}
		}
		return null;
	}

}
