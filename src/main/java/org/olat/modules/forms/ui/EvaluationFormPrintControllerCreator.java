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
package org.olat.modules.forms.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormPrintSelection;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;

/**
 * 
 * Initial date: 4 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormPrintControllerCreator implements ControllerCreator {

	private final Form form;
	private final DataStorage storage;
	private final SessionFilter filter;
	private final Figures figures;
	private final ReportHelper reportHelper;
	private final EvaluationFormPrintSelection printSelection;

	public EvaluationFormPrintControllerCreator(Form form, DataStorage storage, SessionFilter filter, Figures figures,
			ReportHelper reportHelper, EvaluationFormPrintSelection printSelection) {
		this.form = form;
		this.storage = storage;
		this.filter = filter;
		this.figures = figures;
		this.reportHelper = reportHelper;
		this.printSelection = printSelection;
	}

	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		return new EvaluationFormPrintController(lureq, lwControl, form, storage,
				filter, figures, reportHelper, printSelection);
	}

}
