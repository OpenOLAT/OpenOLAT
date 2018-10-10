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

import org.olat.core.CoreSpringFactory;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.Paging;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.ui.ReportHelper;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceLegendTextDataSource implements LegendTextDataSource {

	private final SingleChoice singleChoice;
	private final SessionFilter filter;
	private final ReportHelper reportHelper;
	
	private EvaluationFormReportDAO reportDAO;
	
	public SingleChoiceLegendTextDataSource(SingleChoice singleChoice, SessionFilter filter,
			ReportHelper reportHelper) {
		super();
		this.singleChoice = singleChoice;
		this.filter = filter;
		this.reportHelper = reportHelper;
		this.reportDAO = CoreSpringFactory.getImpl(EvaluationFormReportDAO.class);
	}

	@Override
	public List<SessionText> getResponses() {
		List<SessionText> sessionTexts = new ArrayList<>();
		List<EvaluationFormResponse> responses = reportDAO.getResponses(singleChoice.getId(), filter, Paging.all());
		responses.sort((r1, r2) -> reportHelper.getComparator().compare(r1.getSession(), r2.getSession()));
		
		for (EvaluationFormResponse response : responses) {
			String text = getText(response);
			if (text != null) {
				SessionText sessionText = new SessionText(response.getSession(), text);
				sessionTexts.add(sessionText);
			}
		}
		return sessionTexts;
	}

	private String getText(EvaluationFormResponse response) {
		String choiceId = response.getStringuifiedResponse();
		for (Choice choice: singleChoice.getChoices().asList()) {
			if (choice.getId().equals(choiceId)) {
				return choice.getValue();
			}
		}
		return null;
	}

}
