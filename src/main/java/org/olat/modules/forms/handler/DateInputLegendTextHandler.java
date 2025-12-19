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
package org.olat.modules.forms.handler;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.DateInput;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.TextInputLegendTextController;
import org.olat.modules.forms.ui.model.EvaluationFormControllerReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.TextInputLegendTextDataSource;
import org.olat.modules.forms.ui.model.TextInputLegendTextDataSource.ResponseFormatter;

/**
 * 
 * Initial date: Dec 17, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DateInputLegendTextHandler implements EvaluationFormReportHandler {

	@Override
	public String getType() {
		return "dateinputlegendtext";
	}

	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl, Form rootForm,
			PageElement element, SessionFilter filter, ReportHelper reportHelper) {
		if (element instanceof DateInput dateInput) {
			DateResponseFormatter responseFormatter = createFormatter(ureq, dateInput);
			TextInputLegendTextDataSource dataSource = new TextInputLegendTextDataSource(dateInput.getId(), filter,
					reportHelper, responseFormatter);
			Controller ctrl = new TextInputLegendTextController(ureq, windowControl, dataSource, reportHelper);
			return new EvaluationFormControllerReportElement(ctrl);
		}
		return null;
	}

	public static DateResponseFormatter createFormatter(UserRequest ureq, DateInput dateInput) {
		return new DateResponseFormatter(ureq.getLocale(), dateInput);
	}
	
	private static class DateResponseFormatter implements ResponseFormatter {
		
		private final Formatter formatter;
		private final DateInput dateInput;

		public DateResponseFormatter(Locale locale, DateInput dateInput) {
			this.dateInput = dateInput;
			this.formatter = Formatter.getInstance(locale);
		}
		
		@Override
		public String format(EvaluationFormResponse response) {
			Date date = DateInputHandler.fromResponseValue(response.getStringuifiedResponse());
			if (dateInput.isTime()) {
				return formatter.formatDateAndTime(date);
			}
			return formatter.formatDate(date);
		}
		
	}

}
