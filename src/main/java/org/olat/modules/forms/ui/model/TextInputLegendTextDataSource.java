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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.Formatter;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormsModule;
import org.olat.modules.forms.Limit;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.ReportHelper;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TextInputLegendTextDataSource implements LegendTextDataSource {
	
	private static final ResponseFormatter STRINGUIFIED_FORMATTER = new StringuifiedResponseFormatter();

	private final String responseIdentifier;
	private final SessionFilter filter;
	private final ReportHelper reportHelper;
	private final ResponseFormatter responseFormatter;
	
	private EvaluationFormReportDAO reportDAO;
	private EvaluationFormsModule evaluationFormsModule;
	
	public TextInputLegendTextDataSource(String responseIdentifier, SessionFilter filter, ReportHelper reportHelper,
			ResponseFormatter responseFormatter) {
		this.responseIdentifier = responseIdentifier;
		this.filter = filter;
		this.reportHelper = reportHelper;
		this.responseFormatter = responseFormatter;
		this.reportDAO = CoreSpringFactory.getImpl(EvaluationFormReportDAO.class);
		this.evaluationFormsModule = CoreSpringFactory.getImpl(EvaluationFormsModule.class);
	}

	@Override
	public List<SessionText> getResponses() {
		return getResponses(getLimitMax());
	}

	public List<SessionText> getResponses(Limit limit) {
		List<SessionText> sessionTexts = new ArrayList<>();
		List<EvaluationFormResponse> responses = reportDAO.getResponses(responseIdentifier, filter, limit);
		responses.sort((r1, r2) -> reportHelper.getComparator().compare(r1.getSession(), r2.getSession()));
		for (EvaluationFormResponse response : responses) {
			String text = responseFormatter.format(response);
			SessionText sessionText = new SessionText(response.getSession(), text);
			sessionTexts.add(sessionText);
		}
		return sessionTexts;
	}
	
	public Long getResponsesCount() {
		return getResponsesCount(getLimitMax());
	}
	
	public Long getResponsesCount(Limit limit) {
		return reportDAO.getResponsesCount(responseIdentifier, filter, limit);
	}

	private Limit getLimitMax() {
		return Limit.max(evaluationFormsModule.getReportMaxSessions());
	}
	
	public interface ResponseFormatter {
		public String format(EvaluationFormResponse response);
	}
	
	public static ResponseFormatter createResponseFormatter(TextInput textInput, Locale locale) {
		return textInput.isDate()
		? new DateResponseFormatter(locale)
		: STRINGUIFIED_FORMATTER;
	}
	
	private static class StringuifiedResponseFormatter implements ResponseFormatter {

		@Override
		public String format(EvaluationFormResponse response) {
			return response.getStringuifiedResponse();
		}
		
	}
	
	private static class DateResponseFormatter implements ResponseFormatter {
		
		private final Formatter formatter;
		private final EvaluationFormManager evaluationFormManager;

		public DateResponseFormatter(Locale locale) {
			formatter = Formatter.getInstance(locale);
			evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		}
		
		@Override
		public String format(EvaluationFormResponse response) {
			Date date = evaluationFormManager.getDate(response);
			return date != null? formatter.formatDate(date): null;
		}
		
	}

}
