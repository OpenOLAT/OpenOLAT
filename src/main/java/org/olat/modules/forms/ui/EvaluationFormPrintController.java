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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormPrintSelection;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormsModule;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.handler.DefaultReportProvider;
import org.olat.modules.forms.handler.MultipleChoiceTableHandler;
import org.olat.modules.forms.handler.RubricTableHandler;
import org.olat.modules.forms.handler.SingleChoiceTableHandler;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.06.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormPrintController extends BasicController {
	
	private VelocityContainer mainVC;
	
	private final Form form;
	private final DataStorage storage;
	private final SessionFilter filter;
	private final ReportHelper reportHelper;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private EvaluationFormsModule evaluationFormsModule;

	public EvaluationFormPrintController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			SessionFilter filter, Figures figures, ReportHelper reportHelper,
			EvaluationFormPrintSelection printSelection, String title) {
		super(ureq, wControl);
		this.form = form;
		this.storage = storage;
		this.filter = filter;
		this.reportHelper = reportHelper;

		mainVC = createVelocityContainer("report_print");
		mainVC.contextPut("mainTitle", title);
		if (printSelection.isOverview()) {
			Controller overviewCtrl = new EvaluationFormOverviewController(ureq, getWindowControl(), form, storage, filter, figures);
			mainVC.put("overview", overviewCtrl.getInitialComponent());
		}
		
		if (printSelection.isTables()) {
			DefaultReportProvider provider = new DefaultReportProvider(storage);
			provider.put(Rubric.TYPE, new RubricTableHandler());
			provider.put(SingleChoice.TYPE, new SingleChoiceTableHandler());
			provider.put(MultipleChoice.TYPE, new MultipleChoiceTableHandler());
			Controller tableReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, storage, filter,
					provider, reportHelper);
			mainVC.put("tables", tableReportCtrl.getInitialComponent());
		}

		if (printSelection.isDiagrams()) {
			DefaultReportProvider provider = new DefaultReportProvider(storage);
			Controller diagramReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, storage, filter,
					provider, reportHelper);
			mainVC.put("diagrams", diagramReportCtrl.getInitialComponent());
		}
		
		if (printSelection.isSessions()) {
			Long sessionsCount = evaluationFormManager.loadSessionsCount(filter);
			if (sessionsCount <= evaluationFormsModule.getReportMaxSessions()) {
				mainVC.contextPut("sessionWrappers", createSessionWrappers(ureq));
			} else {
				showWarning("report.max.session.exceeded", String.valueOf(evaluationFormsModule.getReportMaxSessions()));
			}
		}

		putInitialPanel(mainVC);
	}

	private List<SessionWrapper> createSessionWrappers(UserRequest ureq) {
		List<SessionWrapper> wrappers = new ArrayList<>();
		List<EvaluationFormSession> reloadedSessions = evaluationFormManager.loadSessionsFiltered(filter, 0, -1);
		EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(filter);
		for (EvaluationFormSession session: reloadedSessions) {
			SessionWrapper wrapper = createSessionWrapper(ureq, session, responses);
			wrappers.add(wrapper);
		}
		return wrappers;
	}
	
	private SessionWrapper createSessionWrapper(UserRequest ureq, EvaluationFormSession session,
			EvaluationFormResponses responses) {
		String componentName = "se_" + CodeHelper.getRAMUniqueID();
		String legendName = reportHelper.getLegend(session).getName();
		Controller controller = new EvaluationFormExecutionController(ureq, getWindowControl(), session,
				responses, form, storage, null);
		mainVC.put(componentName, controller.getInitialComponent());
		return new SessionWrapper(legendName, componentName);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public static final class SessionWrapper {

		private final String legendName;
		private final String componentName;
		
		protected SessionWrapper(String legendName, String componentName) {
			super();
			this.legendName = legendName;
			this.componentName = componentName;
		}

		public String getLegendName() {
			return legendName;
		}

		public String getComponentName() {
			return componentName;
		}
		
	}

}
