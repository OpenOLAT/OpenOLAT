/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.forms.handler;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.EvaluationFormControllerReportFormItemElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.multireport.ChoiceResponsesTableController;

/**
 * 
 * Initial date: 5 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceResponsesTableHandler implements EvaluationFormReportHandler {

	@Override
	public String getType() {
		return "multiplechoiceresponsestable";
	}

	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl, Form rootForm,
			PageElement element, SessionFilter filter, ReportHelper reportHelper) {
		if (element instanceof MultipleChoice multiChoice) {
			FormBasicController ctrl = new ChoiceResponsesTableController(ureq, windowControl, multiChoice, filter, reportHelper, rootForm);
			return new EvaluationFormControllerReportFormItemElement(ctrl);
		}
		return null;
	}
}
