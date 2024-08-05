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
package org.olat.modules.forms.ui.multireport;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.handler.EvaluationFormReportHandler;
import org.olat.modules.forms.handler.EvaluationFormReportProvider;
import org.olat.modules.forms.handler.MultipleChoiceResponsesTableHandler;
import org.olat.modules.forms.handler.RubricSliderResponsesTableHandler;
import org.olat.modules.forms.handler.SingleChoiceResponsesTableHandler;
import org.olat.modules.forms.handler.TextInputResponsesTableHandler;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.ReportHelper;

/**
 * 
 * Initial date: 30 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultiEvaluationsFormController extends BasicController {

	private final VelocityContainer mainVC;
	
	public MultiEvaluationsFormController(UserRequest ureq, WindowControl wControl,
			Form form, DataStorage storage, SessionFilter filter, LegendNameGenerator legendNameGenerator) {
		super(ureq, wControl, Util.createPackageTranslator(EvaluationFormReportController.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("multiple_evaluations");
		
		ReportHelper reportHelper = ReportHelper.builder(getLocale())
				.withColors()
				.withLegendNameGenrator(legendNameGenerator)
				.withLegendDependentOnExecutor(false)
				.build();
		
		ComparisonProvider provider = new ComparisonProvider();
		Controller reportCtrl = new EvaluationFormReportController(ureq, wControl, form, storage, filter, provider, reportHelper);
		listenTo(reportCtrl);
		mainVC.put("report", reportCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private static final class ComparisonProvider implements EvaluationFormReportProvider {
		final RubricSliderResponsesTableHandler rubricHandler = new RubricSliderResponsesTableHandler();
		final SingleChoiceResponsesTableHandler singleChoiceHandler = new SingleChoiceResponsesTableHandler();
		final MultipleChoiceResponsesTableHandler multipleChoiceHandler = new MultipleChoiceResponsesTableHandler();
		final TextInputResponsesTableHandler textInputHandler  = new TextInputResponsesTableHandler();

		@Override
		public EvaluationFormReportHandler getReportHandler(PageElement element) {
			return switch(element.getType()) {
				case Rubric.TYPE -> rubricHandler;
				case SingleChoice.TYPE -> singleChoiceHandler;
				case MultipleChoice.TYPE -> multipleChoiceHandler;
				case TextInput.TYPE -> textInputHandler;
				default -> null;
			};
		}
	}
}
