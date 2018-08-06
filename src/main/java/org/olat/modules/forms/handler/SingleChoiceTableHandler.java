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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.ui.CountTableController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.CountDataSource;
import org.olat.modules.forms.ui.model.EvaluationFormControllerReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.SingleChoiceDataSource;
import org.olat.modules.portfolio.ui.editor.PageElement;

/**
 * 
 * Initial date: 18.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceTableHandler implements EvaluationFormReportHandler {

	@Override
	public String getType() {
		return "sctablehandler";
	}

	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl, PageElement element,
			List<? extends EvaluationFormSessionRef> sessions, ReportHelper reportHelper) {
		if (element instanceof SingleChoice) {
			SingleChoice singleChoice = (SingleChoice) element;
			CountDataSource dataSource = new SingleChoiceDataSource(singleChoice, sessions);
			Controller ctrl = new CountTableController(ureq, windowControl, dataSource);
			return new EvaluationFormControllerReportElement(ctrl);
		}
		return null;
	}

}
